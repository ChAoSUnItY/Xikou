package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ObjectTypeRef;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.resolver.types.ObjectType;

public class Resolver {
    private final SymbolTable table = new SymbolTable();
    private final XkFile file;

    public Resolver(XkFile file) {
        this.file = file;
    }

    public XkFile resolve() {
        resolveEarly();
        resolveLate();

        return file;
    }

    private void resolveEarly() {
        for (int i = 0; i < file.declCount; i++) {
            BoundableDecl decl = file.decls[i];

            ImplDecl implDecl = decl.getImplDecl();

            if (implDecl != null) {
                PrimaryConstructorDecl constructorDecl = implDecl.primaryConstructorDecl;

                if (constructorDecl != null)
                    resolvePrimaryConstructorDeclEarly(constructorDecl);
            }

            if (decl instanceof ClassDecl)
                resolveClassDeclEarly((ClassDecl) decl);
            else if (decl instanceof EnumDecl)
                resolveEnumDeclEarly((EnumDecl) decl);
        }
    }

    private void resolveClassDeclEarly(ClassDecl classDecl) {
        for (int j = 0; j < classDecl.fieldCount; j++) {
            resolveFieldDecl(classDecl.fieldDecls[j]);
        }

        table.registerDecl(classDecl);
    }

    private void resolveEnumDeclEarly(EnumDecl enumDecl) {
        ImplDecl implDecl = enumDecl.getImplDecl();
        PrimaryConstructorDecl constructorDecl = null;

        if (implDecl != null)
            constructorDecl = implDecl.primaryConstructorDecl;

        for (int j = 0; j < enumDecl.fieldCount; j++) {
            resolveFieldDecl(enumDecl.fieldDecls[j]);
        }

        for (int i = 0; i < enumDecl.variantCount; i++) {
            resolveEnumVariantDecl(enumDecl, constructorDecl, enumDecl.enumVariantDecls[i]);
        }

        table.registerDecl(enumDecl);
    }

    private void resolvePrimaryConstructorDeclEarly(PrimaryConstructorDecl constructorDecl) {
        Parameters parameters = constructorDecl.parameters;

        for (int i = 0; i < parameters.parameterCount; i++) {
            Parameter parameter = parameters.parameters[i];

            resolveTypeRef(parameter.typeRef);
        }
    }

    private void resolveLate() {
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
        ImplDecl implDecl = classDecl.boundImplDecl;
        PrimaryConstructorDecl constructorDecl = implDecl != null ? implDecl.primaryConstructorDecl : null;

        if (constructorDecl != null)
            resolvePrimaryConstructorDecl(constructorDecl);
    }

    private void resolveEnumDecl(EnumDecl enumDecl) {
        ImplDecl implDecl = enumDecl.boundImplDecl;
        PrimaryConstructorDecl constructorDecl = implDecl != null ? implDecl.primaryConstructorDecl : null;

        if (constructorDecl != null)
            resolvePrimaryConstructorDecl(constructorDecl);
    }

    private void resolveFieldDecl(FieldDecl fieldDecl) {
        resolveTypeRef(fieldDecl.typeRef);
    }

    private void resolveEnumVariantDecl(EnumDecl enumDecl, PrimaryConstructorDecl constructorDecl, EnumVariantDecl variantDecl) {
        MethodRef constructorRef = constructorDecl != null ? constructorDecl.asMethodRef() : Utils.genImplcicitPrimaryConstructorRef(enumDecl.getType());
        boolean isApplicable = Utils.isInvocationApplicable(variantDecl.argumentCount, variantDecl.arguments, constructorRef);

        if (!isApplicable)
            throw new IllegalStateException("Incompatible primary constructor invocation on enum variant initialization");
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

            resolveExpr(memberAccessExpr.ownerExpr, scope);

            // TODO: Handle functions later
            FieldRef fieldRef = table.getField(memberAccessExpr.ownerExpr.getType(), memberAccessExpr.selectedVarExpr.varIdentifier.literal);

            if (fieldRef != null) {
                memberAccessExpr.selectedVarExpr.resolvedType = fieldRef.fieldType;
            } else {
                throw new IllegalStateException("Unknown reference to field");
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
        }
    }
}
