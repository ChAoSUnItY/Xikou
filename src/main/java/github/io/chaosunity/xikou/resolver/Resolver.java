package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ArrayTypeRef;
import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class Resolver {
    private final SymbolTable table = new SymbolTable();
    private final XkFile file;

    public Resolver(XkFile file) {
        this.file = file;
    }

    public XkFile resolve() {
        resolveTypeDecls();
        resolveDeclMembers();
        resolveDeclBody();

        return file;
    }

    private void resolveTypeDecls() {
        for (int i = 0; i < file.declCount; i++) {
            table.registerDecl(file.decls[i]);
        }
    }

    private void resolveDeclMembers() {
        for (int i = 0; i < file.declCount; i++) {
            BoundableDecl decl = file.decls[i];
            PrimaryConstructorDecl constructorDecl = decl.getPrimaryConstructorDecl();

            if (constructorDecl != null) resolvePrimaryConstructorDeclEarly(constructorDecl);

            if (decl instanceof ClassDecl) {
                ClassDecl classDecl = (ClassDecl) decl;

                for (int j = 0; j < classDecl.fieldCount; j++) {
                    resolveFieldDecl(classDecl.fieldDecls[j]);
                }
            } else if (decl instanceof EnumDecl) {
                EnumDecl enumDecl = (EnumDecl) decl;

                for (int j = 0; j < enumDecl.variantCount; j++) {

                }

                for (int j = 0; j < enumDecl.fieldCount; j++) {
                    resolveFieldDecl(enumDecl.fieldDecls[j]);
                }
            }
        }
    }

    private void resolvePrimaryConstructorDeclEarly(PrimaryConstructorDecl constructorDecl) {
        for (int i = 0; i < constructorDecl.parameterCount; i++) {
            Parameter parameter = constructorDecl.parameters[i];

            resolveTypeRef(parameter.typeRef);
        }
    }

    private void resolveDeclBody() {
        for (int i = 0; i < file.declCount; i++) {
            BoundableDecl decl = file.decls[i];

            if (decl instanceof ClassDecl) {
                resolveClassDecl((ClassDecl) decl);
            } else if (decl instanceof EnumDecl) {
                resolveEnumDecl((EnumDecl) decl);
            }
        }
    }

    private void resolveClassDecl(ClassDecl classDecl) {
        PrimaryConstructorDecl constructorDecl = classDecl.getPrimaryConstructorDecl();

        if (constructorDecl != null) resolvePrimaryConstructorDecl(constructorDecl);
    }

    private void resolveEnumDecl(EnumDecl enumDecl) {
        PrimaryConstructorDecl constructorDecl = enumDecl.getPrimaryConstructorDecl();

        for (int i = 0; i < enumDecl.variantCount; i++) {
            resolveEnumVariantInitialization(enumDecl, constructorDecl,
                                             enumDecl.enumVariantDecls[i]);
        }

        if (constructorDecl != null) resolvePrimaryConstructorDecl(constructorDecl);
    }

    private void resolveFieldDecl(FieldDecl fieldDecl) {
        resolveTypeRef(fieldDecl.typeRef);
    }

    private void resolveEnumVariantInitialization(EnumDecl enumDecl,
                                                  PrimaryConstructorDecl constructorDecl,
                                                  EnumVariantDecl variantDecl) {
        MethodRef constructorRef = constructorDecl != null ? constructorDecl.asMethodRef() : Utils.genImplcicitPrimaryConstructorRef(
                enumDecl.getType());
        boolean isApplicable = Utils.isInvocationApplicable(variantDecl.argumentCount,
                                                            variantDecl.arguments, constructorRef);

        if (!isApplicable) throw new IllegalStateException(
                "Incompatible primary constructor invocation on enum variant initialization");
    }

    private void resolvePrimaryConstructorDecl(PrimaryConstructorDecl constructorDecl) {
        constructorDecl.scope.addLocalVar("self", constructorDecl.implDecl.boundDecl.getType());

        for (int i = 0; i < constructorDecl.parameterCount; i++) {
            Parameter parameter = constructorDecl.parameters[i];

            constructorDecl.scope.addLocalVar(parameter.name.literal, parameter.typeRef.getType());
        }

        for (int i = 0; i < constructorDecl.exprCount; i++) {
            resolveExpr(constructorDecl.exprs[i], constructorDecl.scope);
        }
    }

    private void resolveExpr(Expr expr, Scope scope) {
        if (expr instanceof InfixExpr) {
            InfixExpr infixExpr = (InfixExpr) expr;

            resolveExpr(infixExpr.lhs, scope);
            resolveExpr(infixExpr.rhs, scope);

            if (infixExpr.operator.type == TokenType.Equal && !infixExpr.lhs.isAssignable()) {
                throw new IllegalStateException("Illegal assignment");
            }
        } else if (expr instanceof MemberAccessExpr) {
            MemberAccessExpr memberAccessExpr = (MemberAccessExpr) expr;

            if (memberAccessExpr.ownerExpr instanceof NameExpr) {
                NameExpr ownerNameExpr = (NameExpr) memberAccessExpr.ownerExpr;
                // Special check for type ref
                AbstractType ownerType = table.getType(ownerNameExpr.varIdentifier.literal);

                if (ownerType != null) {
                    FieldRef fieldRef = table.getField(ownerType,
                                                       memberAccessExpr.targetMember.literal);

                    if (fieldRef != null) {
                        ownerNameExpr.resolvedType = ownerType;
                        memberAccessExpr.fieldRef = fieldRef;
                        return;
                    } else {
                        throw new IllegalStateException(
                                String.format("Type %s does not have field %s",
                                              ownerType.getInternalName(),
                                              memberAccessExpr.targetMember.literal));
                    }
                }
            }

            resolveExpr(memberAccessExpr.ownerExpr, scope);

            FieldRef fieldRef = table.getField(memberAccessExpr.ownerExpr.getType(),
                                               memberAccessExpr.targetMember.literal);

            if (fieldRef != null) {
                memberAccessExpr.fieldRef = fieldRef;
            } else {
                throw new IllegalStateException(
                        String.format("Unknown reference to field %s in type %s",
                                      memberAccessExpr.targetMember.literal,
                                      memberAccessExpr.ownerExpr.getType().getInternalName()));
            }
        } else if (expr instanceof ConstructorCallExpr) {
            ConstructorCallExpr constructorCallExpr = (ConstructorCallExpr) expr;
            ClassType ownerClassType = resolveTypeableExpr(constructorCallExpr.ownerTypeExpr);

            for (int i = 0; i < constructorCallExpr.argumentCount; i++)
                resolveExpr(constructorCallExpr.arguments[i], scope);

            MethodRef[] constructorRefs = table.getConstructors(ownerClassType);
            MethodRef resolvedConstructorRef = null;
            boolean hasApplicableConstructor = false;

            for (MethodRef constructorRef : constructorRefs) {
                if (Utils.isInvocationApplicable(constructorCallExpr.argumentCount,
                                                 constructorCallExpr.arguments, constructorRef)) {
                    hasApplicableConstructor = true;
                    resolvedConstructorRef = constructorRef;
                    break;
                }
            }

            if (!hasApplicableConstructor)
                throw new IllegalStateException("Unknown constructor call");

            constructorCallExpr.resolvedType = ownerClassType;
            constructorCallExpr.resolvedMethodRef = resolvedConstructorRef;
        } else if (expr instanceof ArrayInitExpr) {
            ArrayInitExpr arrayInitExpr = (ArrayInitExpr) expr;

            resolveTypeRef(arrayInitExpr.componentTypeRef);
            resolveExpr(arrayInitExpr.sizeExpr, scope);

            for (int i = 0; i < arrayInitExpr.initExprCount; i++)
                resolveExpr(arrayInitExpr.initExprs[i], scope);

            if (arrayInitExpr.sizeExpr != null && arrayInitExpr.initExprCount != 0) {
                throw new IllegalStateException(
                        "Array initialization with explicit length and initialization is illegal");
            }
        } else if (expr instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) expr;
            LocalVarRef localVarRef = scope.findLocalVar(nameExpr.varIdentifier.literal);

            if (localVarRef != null) {
                nameExpr.resolvedType = localVarRef.type;
                nameExpr.localVarRef = localVarRef;
            } else {
                throw new IllegalStateException("Unknown reference to local variable");
            }
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

    private ClassType resolveTypeableExpr(TypeableExpr typeableExpr) {
        ClassTypeRef typeRef = typeableExpr.asTypeRef();

        resolveTypeRef(typeRef);

        return typeRef.resolvedType;
    }

    private void resolveTypeRef(AbstractTypeRef typeRef) {
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

            classTypeRef.resolvedType = new ClassType(builder.toString());
        } else if (typeRef instanceof ArrayTypeRef) {
            ArrayTypeRef arrayTypeRef = (ArrayTypeRef) typeRef;
            resolveTypeRef(arrayTypeRef.componentTypeRef);

            arrayTypeRef.resolvedType = new ArrayType(arrayTypeRef.componentTypeRef.getType());
        }
    }
}
