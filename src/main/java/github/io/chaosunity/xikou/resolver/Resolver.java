package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ArrayTypeRef;
import github.io.chaosunity.xikou.ast.types.ObjectTypeRef;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;
import github.io.chaosunity.xikou.resolver.types.ObjectType;

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
        Parameters parameters = constructorDecl.parameters;

        for (int i = 0; i < parameters.parameterCount; i++) {
            Parameter parameter = parameters.parameters[i];

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

        Parameters parameters = constructorDecl.parameters;

        for (int i = 0; i < parameters.parameterCount; i++) {
            Parameter parameter = parameters.parameters[i];

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

            if (memberAccessExpr.ownerExpr instanceof VarExpr) {
                VarExpr ownerVarExpr = (VarExpr) memberAccessExpr.ownerExpr;
                // Special check for type ref
                AbstractType ownerType = table.getType(ownerVarExpr.varIdentifier.literal);

                if (ownerType != null) {
                    FieldRef fieldRef = table.getField(ownerType,
                                                       memberAccessExpr.targetMember.literal);

                    if (fieldRef != null) {
                        ownerVarExpr.resolvedType = ownerType;
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

            // TODO: Handle functions later
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
        } else if (expr instanceof ArrayInitExpr) {
            ArrayInitExpr arrayInitExpr = (ArrayInitExpr) expr;

            resolveTypeRef(arrayInitExpr.componentTypeRef);
            resolveExpr(arrayInitExpr.sizeExpr, scope);

            for (int i = 0; i < arrayInitExpr.initExprCount; i++)
                resolveExpr(arrayInitExpr.initExprs[i], scope);

            if (arrayInitExpr.sizeExpr != null && arrayInitExpr.initExprCount != 0) {
                throw new IllegalStateException("Array initialization with explicit length and initialization is illegal");
            }
        } else if (expr instanceof VarExpr) {
            VarExpr varExpr = (VarExpr) expr;
            LocalVarRef localVarRef = scope.findLocalVar(varExpr.varIdentifier.literal);

            if (localVarRef != null) {
                varExpr.resolvedType = localVarRef.type;
                varExpr.localVarRef = localVarRef;
            } else {
                throw new IllegalStateException("Unknown reference to local variable");
            }
        } else if (expr instanceof IntegerLiteral) {
            IntegerLiteral integerLiteral = (IntegerLiteral) expr;
        }
    }

    private void resolveTypeRef(AbstractTypeRef typeRef) {
        // Primitive type is already resolved in parser phase

        if (typeRef instanceof ObjectTypeRef) {
            ObjectTypeRef objectTypeRef = (ObjectTypeRef) typeRef;
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < objectTypeRef.selectorCount; i++) {
                builder.append(objectTypeRef.selectors[i].literal);

                if (i != objectTypeRef.selectorCount - 1) {
                    builder.append("/");
                }
            }

            objectTypeRef.resolvedType = new ObjectType(builder.toString());
        } else if (typeRef instanceof ArrayTypeRef) {
            ArrayTypeRef arrayTypeRef = (ArrayTypeRef) typeRef;
            resolveTypeRef(arrayTypeRef.componentTypeRef);

            arrayTypeRef.resolvedType = new ArrayType(arrayTypeRef.componentTypeRef.getType());
        }
    }
}
