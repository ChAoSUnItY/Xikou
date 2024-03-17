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
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class Resolver {

  private final SymbolTable table = new SymbolTable();
  private final TypeResolver typeResolver = new TypeResolver(table);
  private final ExprResolver exprResolver = new ExprResolver(table, typeResolver);
  private final StmtResolver stmtResolver = new StmtResolver(table, exprResolver, typeResolver);
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
          typeResolver.resolveTypeRef(classDecl.inheritedClasses[j], false);
        }

        classDecl.resolveSuperclassAndInterfaces();
      } else if (decl instanceof EnumDecl) {
        EnumDecl enumDecl = (EnumDecl) decl;
        enumDecl.interfaceTypes = new ClassType[enumDecl.interfacCount];

        for (int j = 0; j < enumDecl.interfacCount; j++) {
          typeResolver.resolveTypeRef(enumDecl.interfaces[j], false);
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

      typeResolver.resolveTypeRef(parameter.typeRef, false);
    }

    constructorDecl.scope = new Scope(ownerType, true);
  }

  private void resolveFunctionDeclEarly(ClassType ownerType, FnDecl fnDecl) {
    for (int i = 0; i < fnDecl.parameterCount; i++) {
      Parameter parameter = fnDecl.parameters[i];

      typeResolver.resolveTypeRef(parameter.typeRef, false);
    }

    if (fnDecl.returnTypeRef != null) {
      typeResolver.resolveTypeRef(fnDecl.returnTypeRef, false);

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
    typeResolver.resolveTypeRef(fieldDecl.typeRef, false);
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
      stmtResolver.resolveStatment(constructorDecl.statements[i], constructorDecl.scope);
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
      stmtResolver.resolveStatment(fnDecl.statements[i], fnDecl.scope);
    }
  }
}
