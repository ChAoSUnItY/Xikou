package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.BoundableDecl;
import github.io.chaosunity.xikou.ast.ClassDecl;
import github.io.chaosunity.xikou.ast.ConstructorDecl;
import github.io.chaosunity.xikou.ast.EnumDecl;
import github.io.chaosunity.xikou.ast.EnumVariantDecl;
import github.io.chaosunity.xikou.ast.FieldDecl;
import github.io.chaosunity.xikou.ast.FnDecl;
import github.io.chaosunity.xikou.ast.ImplDecl;
import github.io.chaosunity.xikou.ast.Parameter;
import github.io.chaosunity.xikou.ast.XkFile;
import github.io.chaosunity.xikou.ast.expr.ArrayInitExpr;
import github.io.chaosunity.xikou.ast.expr.BlockExpr;
import github.io.chaosunity.xikou.ast.expr.CharLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.ConstructorCallExpr;
import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.expr.IndexExpr;
import github.io.chaosunity.xikou.ast.expr.InfixExpr;
import github.io.chaosunity.xikou.ast.expr.IntegerLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.MemberAccessExpr;
import github.io.chaosunity.xikou.ast.expr.MethodCallExpr;
import github.io.chaosunity.xikou.ast.expr.NameExpr;
import github.io.chaosunity.xikou.ast.expr.NullLiteral;
import github.io.chaosunity.xikou.ast.expr.ReturnExpr;
import github.io.chaosunity.xikou.ast.expr.StringLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.TypeableExpr;
import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.ast.stmt.VarDeclStmt;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ArrayTypeRef;
import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.TypeUtils;

public final class Resolver {

  private final SymbolTable table = new SymbolTable();
  private final XkFile[] files;

  public Resolver(XkFile[] files) {
    this.files = files;
  }

  public XkFile[] resolve() {
    for (XkFile file : files) {
      resolveTypeDecls(file);
    }

    for (XkFile file : files) {
      resolveDeclSupertypes(file);
    }

    for (XkFile file : files) {
      resolveDeclMembers(file);
    }

    for (XkFile file : files) {
      resolveDeclBody(file);
    }

    return files;
  }

  private void resolveTypeDecls(XkFile file) {
    for (int i = 0; i < file.declCount; i++) {
      table.registerDecl(file.decls[i]);
    }
  }

  private void resolveDeclSupertypes(XkFile file) {
    for (int i = 0; i < file.declCount; i++) {
      BoundableDecl decl = file.decls[i];

      if (decl instanceof ClassDecl) {
        ClassDecl classDecl = (ClassDecl) decl;

        for (int j = 0; j < classDecl.inheritedCount; j++) {
          resolveTypeRef(classDecl.inheritedClasses[j], false);
        }

        classDecl.resolveSuperclassAndInterfaces();
      } else if (decl instanceof EnumDecl) {
        EnumDecl enumDecl = (EnumDecl) decl;
        enumDecl.interfaceTypes = new ClassType[enumDecl.interfacCount];

        for (int j = 0; j < enumDecl.interfacCount; j++) {
          resolveTypeRef(enumDecl.interfaces[j], false);
          enumDecl.interfaceTypes[j] = enumDecl.interfaces[j].resolvedType;
        }
      }
    }
  }

  private void resolveDeclMembers(XkFile file) {
    for (int i = 0; i < file.declCount; i++) {
      BoundableDecl decl = file.decls[i];
      ConstructorDecl constructorDecl = decl.getConstructorDecl();

      if (constructorDecl != null) {
        resolvePrimaryConstructorDeclEarly(decl.getType(), constructorDecl);
      }

      ImplDecl implDecl = decl.getImplDecl();

      if (implDecl != null) {
        for (int j = 0; j < implDecl.functionCount; j++) {
          resolveFunctionDeclEarly(decl.getType(), implDecl.functionDecls[j]);
        }
      }

      if (decl instanceof ClassDecl) {
        ClassDecl classDecl = (ClassDecl) decl;

        for (int j = 0; j < classDecl.fieldCount; j++) {
          resolveFieldDecl(classDecl.fieldDecls[j]);
        }
      } else if (decl instanceof EnumDecl) {
        EnumDecl enumDecl = (EnumDecl) decl;

        for (int j = 0; j < enumDecl.fieldCount; j++) {
          resolveFieldDecl(enumDecl.fieldDecls[j]);
        }
      }
    }
  }

  private void resolvePrimaryConstructorDeclEarly(ClassType ownerType, ConstructorDecl constructorDecl) {
    for (int i = 0; i < constructorDecl.parameterCount; i++) {
      Parameter parameter = constructorDecl.parameters[i];

      resolveTypeRef(parameter.typeRef, false);
    }

    constructorDecl.scope = new Scope(ownerType, true);
  }

  private void resolveFunctionDeclEarly(ClassType ownerType, FnDecl fnDecl) {
    for (int i = 0; i < fnDecl.parameterCount; i++) {
      Parameter parameter = fnDecl.parameters[i];

      resolveTypeRef(parameter.typeRef, false);
    }

    if (fnDecl.returnTypeRef != null) {
      resolveTypeRef(fnDecl.returnTypeRef, false);

      fnDecl.returnType = fnDecl.returnTypeRef.getType();
    } else {
      fnDecl.returnType = PrimitiveType.VOID;
    }

    fnDecl.scope = new Scope(ownerType, false);
  }

  private void resolveDeclBody(XkFile file) {
    for (int i = 0; i < file.declCount; i++) {
      BoundableDecl decl = file.decls[i];
      ConstructorDecl constructorDecl = decl.getConstructorDecl();

      if (constructorDecl != null) {
        resolveConstructorDecl(constructorDecl);
      }

      ImplDecl implDecl = decl.getImplDecl();

      if (implDecl != null) {
        for (int j = 0; j < implDecl.functionCount; j++) {
          resolveFunctionDecl(implDecl.functionDecls[j]);
        }
      }

      if (decl instanceof ClassDecl) {
        resolveClassDecl((ClassDecl) decl);
      } else if (decl instanceof EnumDecl) {
        resolveEnumDecl((EnumDecl) decl);
      }
    }
  }

  private void resolveClassDecl(ClassDecl classDecl) {
  }

  private void resolveEnumDecl(EnumDecl enumDecl) {
    ConstructorDecl constructorDecl = enumDecl.getConstructorDecl();

    for (int i = 0; i < enumDecl.variantCount; i++) {
      resolveEnumVariantInitialization(enumDecl, constructorDecl,
          enumDecl.enumVariantDecls[i]);
    }
  }

  private void resolveFieldDecl(FieldDecl fieldDecl) {
    resolveTypeRef(fieldDecl.typeRef, false);
  }

  private void resolveEnumVariantInitialization(EnumDecl enumDecl,
      ConstructorDecl constructorDecl,
      EnumVariantDecl variantDecl) {
    MethodRef constructorRef = constructorDecl != null ? constructorDecl.asMethodRef()
        : Utils.genImplcicitPrimaryConstructorRef(
            enumDecl.getType());
    boolean isApplicable = Utils.isInvocationApplicable(variantDecl.argumentCount,
        variantDecl.arguments, constructorRef);

    if (!isApplicable) {
      throw new IllegalStateException(
          "Incompatible primary constructor invocation on enum variant initialization");
    }
  }

  private void resolveConstructorDecl(ConstructorDecl constructorDecl) {
    constructorDecl.scope.addLocalVar("self", true, constructorDecl.implDecl.boundDecl.getType());

    for (int i = 0; i < constructorDecl.parameterCount; i++) {
      Parameter parameter = constructorDecl.parameters[i];

      constructorDecl.scope.addLocalVar(parameter.name.literal, true, parameter.typeRef.getType());
    }

    for (int i = 0; i < constructorDecl.statementCount; i++) {
      resolveStatment(constructorDecl.statements[i], constructorDecl.scope);
    }
  }

  private void resolveFunctionDecl(FnDecl fnDecl) {
    if (fnDecl.selfToken != null) {
      fnDecl.scope.addLocalVar("self", true, fnDecl.implDecl.boundDecl.getType());
    }

    for (int i = 0; i < fnDecl.parameterCount; i++) {
      Parameter parameter = fnDecl.parameters[i];

      fnDecl.scope.addLocalVar(parameter.name.literal, true, parameter.typeRef.getType());
    }

    for (int i = 0; i < fnDecl.statementCount; i++) {
      resolveStatment(fnDecl.statements[i], fnDecl.scope);
    }
  }

  // =================================================
  // Statement resolve functions
  // =================================================

  public void resolveStatment(Statement statement, Scope scope) {
    if (statement instanceof VarDeclStmt) {
      resolveVarDeclStatement((VarDeclStmt) statement, scope);
    } else if (statement instanceof ExprStmt) {
      resolveExprStatement((ExprStmt) statement, scope);
    }
  }

  private void resolveVarDeclStatement(VarDeclStmt varDeclStmt, Scope scope) {
    if (varDeclStmt.initialValue != null) {
      resolveExpr(varDeclStmt.initialValue, scope);
      varDeclStmt.localVarRef = scope.addLocalVar(varDeclStmt.nameToken.literal,
          varDeclStmt.mutToken != null, varDeclStmt.initialValue.getType());
    }
  }

  private void resolveExprStatement(ExprStmt exprStmt, Scope scope) {
    resolveExpr(exprStmt.expr, scope);
  }

  // =================================================
  // Expression resolve functions
  // =================================================

  public void resolveExpr(Expr expr, Scope scope) {
    if (expr instanceof InfixExpr) {
      resolveInfixExpr((InfixExpr) expr, scope);
    } else if (expr instanceof MethodCallExpr) {
      resolveMethodCallExpr((MethodCallExpr) expr, scope);
    } else if (expr instanceof MemberAccessExpr) {
      resolveMemberAccessExpr((MemberAccessExpr) expr, scope);
    } else if (expr instanceof ConstructorCallExpr) {
      resolveConstructorCallExpr((ConstructorCallExpr) expr, scope);
    } else if (expr instanceof IndexExpr) {
      resolveIndexExpr((IndexExpr) expr, scope);
    } else if (expr instanceof ArrayInitExpr) {
      resolveArrayInitExpr((ArrayInitExpr) expr, scope);
    } else if (expr instanceof ReturnExpr) {
      resolveReturnExpr((ReturnExpr) expr, scope);
    } else if (expr instanceof BlockExpr) {
      resolveBlockExpr((BlockExpr) expr, scope);
    } else if (expr instanceof NameExpr) {
      resolveNameExpr((NameExpr) expr, scope);
    } else if (expr instanceof CharLiteralExpr) {
      CharLiteralExpr charLiteralExpr = (CharLiteralExpr) expr;
    } else if (expr instanceof StringLiteralExpr) {
      StringLiteralExpr stringLiteralExpr = (StringLiteralExpr) expr;
    } else if (expr instanceof IntegerLiteralExpr) {
      IntegerLiteralExpr integerLiteralExpr = (IntegerLiteralExpr) expr;
    } else if (expr instanceof NullLiteral) {
      NullLiteral nullLiteral = (NullLiteral) expr;
    }
  }

  private static void resolveNameExpr(NameExpr expr, Scope scope) {
    LocalVarRef localVarRef = scope.findLocalVar(expr.varIdentifier.literal);

    if (localVarRef != null) {
      expr.resolvedType = localVarRef.type;
      expr.localVarRef = localVarRef;
    } else {
      throw new IllegalStateException("Unknown reference to local variable");
    }
  }

  private void resolveArrayInitExpr(ArrayInitExpr expr, Scope scope) {
    resolveTypeRef(expr.componentTypeRef, false);
    resolveExpr(expr.sizeExpr, scope);

    for (int i = 0; i < expr.initExprCount; i++) {
      resolveExpr(expr.initExprs[i], scope);
    }

    if (expr.sizeExpr != null && expr.initExprCount != 0) {
      throw new IllegalStateException(
          "Array initialization with explicit length and initialization is illegal");
    }
  }

  private void resolveConstructorCallExpr(ConstructorCallExpr expr, Scope scope) {
    ClassType ownerClassType = resolveTypeableExpr(expr.ownerTypeExpr, false);

    for (int i = 0; i < expr.argumentCount; i++) {
      resolveExpr(expr.arguments[i], scope);
    }

    MethodRef[] constructorRefs = table.getConstructors(ownerClassType);
    MethodRef resolvedConstructorRef = null;
    boolean hasApplicableConstructor = false;

    for (MethodRef constructorRef : constructorRefs) {
      if (Utils.isInvocationApplicable(expr.argumentCount,
          expr.arguments, constructorRef)) {
        hasApplicableConstructor = true;
        resolvedConstructorRef = constructorRef;
        break;
      }
    }

    if (!hasApplicableConstructor) {
      throw new IllegalStateException("Unknown constructor call");
    }

    expr.resolvedType = ownerClassType;
    expr.resolvedMethodRef = resolvedConstructorRef;
  }

  private void resolveMethodCallExpr(MethodCallExpr expr, Scope scope) {
    AbstractType[] arguementTypes = new AbstractType[expr.argumentCount];

    for (int i = 0; i < expr.argumentCount; i++) {
      resolveExpr(expr.arguments[i], scope);
      arguementTypes[i] = expr.arguments[i].getType();
    }

    if (expr.ownerExpr instanceof TypeableExpr) {
      // Special check for type ref
      ClassType ownerType = resolveTypeableExpr((TypeableExpr) expr.ownerExpr, true);

      if (ownerType != null) {
        MethodRef methodRef = table.getMethod(ownerType, expr.nameToken.literal, arguementTypes);

        if (methodRef != null) {
          expr.resolvedMethodRef = methodRef;
          return;
        } else {
          throw new IllegalStateException(
              String.format("Type %s does not have method %s",
                  ownerType.getInternalName(),
                  expr.nameToken.literal));
        }
      }
    }

    resolveExpr(expr.ownerExpr, scope);

    MethodRef methodRef = table.getMethod(expr.ownerExpr.getType(), expr.nameToken.literal,
        arguementTypes);

    if (methodRef != null) {
      expr.resolvedMethodRef = methodRef;
    } else {
      throw new IllegalStateException(
          String.format("Type %s does not have method %s",
              expr.ownerExpr.getType(),
              expr.nameToken.literal));
    }
  }

  private void resolveMemberAccessExpr(MemberAccessExpr expr, Scope scope) {
    if (expr.ownerExpr instanceof TypeableExpr) {
      // Special check for type ref
      ClassType ownerType = resolveTypeableExpr((TypeableExpr) expr.ownerExpr, true);

      if (ownerType != null) {
        FieldRef fieldRef = table.getField(ownerType,
            expr.nameToken.literal);

        if (fieldRef != null) {
          expr.resolvedFieldRef = fieldRef;
          return;
        } else {
          throw new IllegalStateException(
              String.format("Type %s does not have field %s",
                  ownerType.getInternalName(),
                  expr.nameToken.literal));
        }
      }
    }

    resolveExpr(expr.ownerExpr, scope);

    FieldRef fieldRef = table.getField(expr.ownerExpr.getType(),
        expr.nameToken.literal);

    if (fieldRef != null) {
      expr.resolvedFieldRef = fieldRef;
    } else {
      throw new IllegalStateException(
          String.format("Type %s does not have field %s",
              expr.ownerExpr.getType().getInternalName(),
              expr.nameToken.literal));
    }
  }

  private void resolveIndexExpr(IndexExpr expr, Scope scope) {
    resolveExpr(expr.targetExpr, scope);
    resolveExpr(expr.indexExpr, scope);

    if (!(expr.targetExpr.getType() instanceof ArrayType)) {
      throw new IllegalStateException("Cannot index a non-array type");
    }

    if (expr.indexExpr.getType() != PrimitiveType.INT) {
      throw new IllegalStateException("Cannot index an array type with i32");
    }
  }

  private void resolveInfixExpr(InfixExpr expr, Scope scope) {
    resolveExpr(expr.lhs, scope);
    resolveExpr(expr.rhs, scope);

    if (expr.operator.type == TokenType.Equal) {
      boolean assignable = scope.isInConstructor && expr.lhs instanceof MemberAccessExpr
          && ((MemberAccessExpr) expr.lhs).resolvedFieldRef.ownerClassType.equals(
          scope.parentClassType);
      assignable |= expr.lhs.isAssignable();

      if (!assignable) {
        throw new IllegalStateException("Illegal assignment");
      }

      if (expr.rhs.getType() == PrimitiveType.VOID) {
        throw new IllegalStateException("void type cannot be used as value");
      }

      if (!TypeUtils.isInstanceOf(expr.rhs.getType(), expr.lhs.getType())) {
        throw new IllegalStateException(
            String.format("Illegal assignment: %s is not compatible with %s",
                expr.rhs.getType().getInternalName(),
                expr.lhs.getType().getInternalName()));
      }
    }
  }

  private void resolveReturnExpr(ReturnExpr expr, Scope scope) {
    resolveExpr(expr.rhs, scope);
  }

  private void resolveBlockExpr(BlockExpr expr, Scope scope) {
    Scope blockScope = scope.extend();

    for (int i = 0; i < expr.statementCount; i++) {
      resolveStatment(expr.statements[i], blockScope);
    }
  }

  private ClassType resolveTypeableExpr(TypeableExpr typeableExpr, boolean recoverable) {
    ClassTypeRef typeRef = typeableExpr.asTypeRef();

    resolveTypeRef(typeRef, recoverable);

    return typeRef.resolvedType;
  }

  // =================================================
  // Type resolve functions
  // =================================================

  public void resolveTypeRef(AbstractTypeRef typeRef, boolean recoverable) {
    // Primitive type is already resolved in parser phase

    if (typeRef instanceof ClassTypeRef) {
      ClassTypeRef classTypeRef = (ClassTypeRef) typeRef;
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < classTypeRef.selectorCount; i++) {
        builder.append(classTypeRef.selectors[i].literal);

        if (i != classTypeRef.selectorCount - 1) {
          builder.append("/");
        }
      }

      AbstractType type = table.getType(builder.toString().replace('/', '.'));

      if (!(type instanceof ClassType)) {
        if (recoverable) {
          return;
        }

        throw new IllegalStateException(String.format("Type %s is not an ClassType",
            builder));
      }

      classTypeRef.resolvedType = (ClassType) type;
    } else if (typeRef instanceof ArrayTypeRef) {
      ArrayTypeRef arrayTypeRef = (ArrayTypeRef) typeRef;
      resolveTypeRef(arrayTypeRef.componentTypeRef, recoverable);

      arrayTypeRef.resolvedType = new ArrayType(arrayTypeRef.componentTypeRef.getType());
    }
  }
}
