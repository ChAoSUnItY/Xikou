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
import github.io.chaosunity.xikou.ast.expr.ArithmeticExpr;
import github.io.chaosunity.xikou.ast.expr.ArrayInitExpr;
import github.io.chaosunity.xikou.ast.expr.AssignmentExpr;
import github.io.chaosunity.xikou.ast.expr.BlockExpr;
import github.io.chaosunity.xikou.ast.expr.CastExpr;
import github.io.chaosunity.xikou.ast.expr.CompareExpr;
import github.io.chaosunity.xikou.ast.expr.CondExpr;
import github.io.chaosunity.xikou.ast.expr.ConstructorCallExpr;
import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.expr.FieldAccessExpr;
import github.io.chaosunity.xikou.ast.expr.IfExpr;
import github.io.chaosunity.xikou.ast.expr.IndexExpr;
import github.io.chaosunity.xikou.ast.expr.InfixExpr;
import github.io.chaosunity.xikou.ast.expr.MethodCallExpr;
import github.io.chaosunity.xikou.ast.expr.NameExpr;
import github.io.chaosunity.xikou.ast.expr.ReturnExpr;
import github.io.chaosunity.xikou.ast.expr.TypeableExpr;
import github.io.chaosunity.xikou.ast.expr.WhileExpr;
import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.ast.stmt.VarDeclStmt;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ArrayTypeRef;
import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
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

  private void resolvePrimaryConstructorDeclEarly(ClassType ownerType,
      ConstructorDecl constructorDecl) {
    for (int i = 0; i < constructorDecl.parameterCount; i++) {
      Parameter parameter = constructorDecl.parameters[i];

      resolveTypeRef(parameter.typeRef, false);
    }

    constructorDecl.scope = new Scope(ownerType, true, true);
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

    fnDecl.scope = new Scope(ownerType, false, fnDecl.selfToken != null);
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

    resolveParameters(constructorDecl.parameterCount, constructorDecl.parameters,
        constructorDecl.scope);
    resolveStatement(constructorDecl.statementCount, constructorDecl.statements,
        constructorDecl.scope);
  }

  private void resolveFunctionDecl(FnDecl fnDecl) {
    if (fnDecl.selfToken != null) {
      fnDecl.scope.addLocalVar("self", true, fnDecl.implDecl.boundDecl.getType());
    }

    resolveParameters(fnDecl.parameterCount, fnDecl.parameters, fnDecl.scope);
    resolveStatement(fnDecl.statementCount, fnDecl.statements, fnDecl.scope);
  }

  private void resolveParameters(int parameterCount, Parameter[] parameters, Scope scope) {
    for (int i = 0; i < parameterCount; i++) {
      Parameter parameter = parameters[i];

      scope.addLocalVar(parameter.name.literal, true, parameter.typeRef.getType());
    }
  }

  private void resolveStatement(int statementCount, Statement[] statements, Scope scope) {
    for (int i = 0; i < statementCount; i++) {
      resolveStatement(statements[i], scope);
    }
  }

  // =================================================
  // Statement resolve functions
  // =================================================

  private void resolveStatement(Statement statement, Scope scope) {
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
    } else if (expr instanceof CondExpr) {
      resolveCondExpr((CondExpr) expr, scope);
    } else if (expr instanceof AssignmentExpr) {
      resolveAssignmentExpr((AssignmentExpr) expr, scope);
    } else if (expr instanceof CastExpr) {
      resolveCastExpr((CastExpr) expr, scope);
    } else if (expr instanceof MethodCallExpr) {
      resolveMethodCallExpr((MethodCallExpr) expr, scope);
    } else if (expr instanceof FieldAccessExpr) {
      resolveFieldAccessExpr((FieldAccessExpr) expr, scope);
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
    } else if (expr instanceof IfExpr) {
      resolveIfExpr((IfExpr) expr, scope);
    } else if (expr instanceof WhileExpr) {
      resolveWhileExpr((WhileExpr) expr, scope);
    }
  }

  private void resolveInfixExpr(InfixExpr expr, Scope scope) {
    resolveExpr(expr.getLhs(), scope);
    resolveExpr(expr.getRhs(), scope);

    if (expr instanceof CompareExpr) {
      resolveCompareExpr((CompareExpr) expr, scope);
    } else if (expr instanceof ArithmeticExpr) {
      resolveArithmeticExpr((ArithmeticExpr) expr, scope);
    }
  }

  private void resolveCompareExpr(CompareExpr compareExpr, Scope scope) {
    AbstractType lhsType = compareExpr.getLhs().getType(), rhsType = compareExpr.getRhs().getType();

    switch (compareExpr.compareOperatorToken.type) {
      case Equal:
      case NotEqual:
        if (!lhsType.equals(rhsType)) {
          throw new IllegalStateException(
              "Cannot compare on different types, use Object#equals instead");
        }
        break;
      case Greater:
      case GreaterEqual:
      case Lesser:
      case LesserEqual: {
        if (!(lhsType instanceof PrimitiveType) || !(rhsType instanceof PrimitiveType)) {
          throw new IllegalStateException("Cannot compare on non-numerical types");
        }

        PrimitiveType lhsPrimitiveType = (PrimitiveType) lhsType, rhsPrimitiveType = (PrimitiveType) rhsType;

        if (lhsPrimitiveType != rhsPrimitiveType) {
          throw new IllegalStateException(
              "Cannot compare on different numerical types, cast one of the type to another first");
        }
        break;
      }
    }
  }

  private void resolveArithmeticExpr(ArithmeticExpr arithmeticExpr, Scope scope) {
    AbstractType lhsType = arithmeticExpr.getLhs().getType(), rhsType = arithmeticExpr.getRhs()
        .getType();

    if (!lhsType.equals(rhsType)) {
      throw new IllegalStateException("Cannot perform arithmetic operation on different types");
    }
  }

  private void resolveCondExpr(CondExpr condExpr, Scope scope) {
    for (int i = 0; i < condExpr.exprCount; i++) {
      resolveExpr(condExpr.exprs[i], scope);

      if (condExpr.exprs[i].getType() != PrimitiveType.BOOL) {
        throw new IllegalStateException(
            "Cannot perform logical operation on different types");
      }
    }
  }

  private void resolveAssignmentExpr(AssignmentExpr expr, Scope scope) {
    resolveExpr(expr.rhs, scope);
    AbstractType rhsType = expr.rhs.getType();

    if (rhsType == PrimitiveType.VOID) {
      throw new IllegalStateException("void type cannot be used as value");
    }

    for (int i = 0; i < expr.targetCount; i++) {
      Expr target = expr.targets[i];

      resolveExpr(target, scope);

      AbstractType targetType = target.getType();

      boolean assignable = scope.isInConstructor && target instanceof FieldAccessExpr
          && ((FieldAccessExpr) target).resolvedFieldRef.ownerClassType.equals(
          scope.parentClassType);
      assignable |= target.isAssignable();

      if (!assignable) {
        throw new IllegalStateException("Illegal assignment");
      }

      switch (expr.assignOpToken.type) {
        case Equal:
          if (!TypeUtils.isInstanceOf(rhsType, targetType)) {
            throw new IllegalStateException(
                String.format("Illegal assignment: %s is not compatible with %s",
                    rhsType.getInternalName(),
                    targetType.getInternalName()));
          }
          break;
        case PlusEqual:
        case MinusEqual:
          if (!targetType.equals(rhsType)) {
            throw new IllegalStateException(
                "Illegal assignment: cannot perform arithmetic operation on different types");
          }
          break;
      }
    }
  }

  private void resolveCastExpr(CastExpr expr, Scope scope) {
    resolveExpr(expr.targetCastExpr, scope);
    resolveTypeRef(expr.targetTypeRef, false);

    if (!TypeUtils.typesCanCast(expr.targetTypeRef.getType(), expr.targetTypeRef.getType())) {
      throw new IllegalStateException("Cannot explicitly cast type");
    }
  }

  private void resolveNameExpr(NameExpr expr, Scope scope) {
    LocalVarRef localVarRef = scope.findLocalVar(expr.varIdentifier.literal);

    if (localVarRef != null) {
      expr.resolvedType = localVarRef.type;
      expr.localVarRef = localVarRef;
    } else {
      throw new IllegalStateException("Unknown reference to local variable");
    }
  }

  private void resolveIfExpr(IfExpr expr, Scope scope) {
    resolveExpr(expr.condExpr, scope);
    resolveBlockExpr(expr.trueBranchExpr, scope.extend());

    if (expr.falseBranchExpr != null) {
      resolveExpr(expr.falseBranchExpr, scope.extend());
    }

    if (expr.condExpr.getType() != PrimitiveType.BOOL) {
      throw new IllegalStateException("If-else condition must be bool");
    }
  }

  private void resolveWhileExpr(WhileExpr expr, Scope scope) {
    resolveExpr(expr.condExpr, scope);
    resolveExpr(expr.iterExpr, scope.extend());

    if (expr.condExpr.getType() != PrimitiveType.BOOL) {
      throw new IllegalStateException("While condition must be bool");
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

        checkMethodCallAccessibility(expr, scope, methodRef);
        return;
      }
    }

    AbstractType ownerType;

    if (expr.ownerExpr == null) {
      ownerType = scope.parentClassType;
    } else {
      resolveExpr(expr.ownerExpr, scope);
      ownerType = expr.ownerExpr.getType();
    }

    MethodRef methodRef = table.getMethod(ownerType, expr.nameToken.literal,
        arguementTypes);
    checkMethodCallAccessibility(expr, scope, methodRef);
  }

  private void checkMethodCallAccessibility(MethodCallExpr expr, Scope scope, MethodRef methodRef) {
    if (methodRef == null) {
      throw new IllegalStateException(
          String.format("Type %s does not have method %s",
              expr.ownerExpr.getType(),
              expr.nameToken.literal));
    }

    if (!methodRef.isStatic && expr.ownerExpr == null && !scope.isInInstance) {
      throw new IllegalStateException(
          String.format("Instance method %s cannot be called in static scope",
              expr.nameToken.literal));
    }

    expr.resolvedMethodRef = methodRef;
  }

  private void resolveFieldAccessExpr(FieldAccessExpr expr, Scope scope) {
    if (expr.ownerExpr instanceof TypeableExpr) {
      // Special check for type ref
      ClassType ownerType = resolveTypeableExpr((TypeableExpr) expr.ownerExpr, true);

      if (ownerType != null) {
        FieldRef fieldRef = table.getField(ownerType,
            expr.nameToken.literal);

        checkFieldAccessibility(expr, scope, fieldRef);
        return;
      }
    }

    resolveExpr(expr.ownerExpr, scope);

    FieldRef fieldRef = table.getField(expr.ownerExpr.getType(),
        expr.nameToken.literal);
    checkFieldAccessibility(expr, scope, fieldRef);
  }

  private void checkFieldAccessibility(FieldAccessExpr expr, Scope scope, FieldRef fieldRef) {
    if (fieldRef == null) {
      throw new IllegalStateException(
          String.format("Type %s does not have field %s",
              expr.ownerExpr.getType().getInternalName(),
              expr.nameToken.literal));
    }

    if (!fieldRef.isStatic && expr.ownerExpr == null && !scope.isInInstance) {
      throw new IllegalStateException(
          String.format("Instance field %s cannot be called in static scope",
              expr.nameToken.literal));
    }

    expr.resolvedFieldRef = fieldRef;
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

  private void resolveReturnExpr(ReturnExpr expr, Scope scope) {
    resolveExpr(expr.rhs, scope);
  }

  private void resolveBlockExpr(BlockExpr expr, Scope scope) {
    Scope blockScope = scope.extend();

    for (int i = 0; i < expr.statementCount; i++) {
      resolveStatement(expr.statements[i], blockScope);
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
