package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.Scope;
import github.io.chaosunity.xikou.resolver.types.Type;

public class PrimaryConstructorDecl {
    public final int constructorModifiers;
    public final Parameters parameters;
    public final int exprCount;
    public final Expr[] exprs;

    public ImplDecl implDecl;
    public final Scope scope;

    public PrimaryConstructorDecl(int constructorModifiers, Parameters parameters, int exprCount,
                                  Expr[] exprs) {
        this.constructorModifiers = constructorModifiers;
        this.parameters = parameters;
        this.exprCount = exprCount;
        this.exprs = exprs;
        this.scope = new Scope();
    }

    public MethodRef asMethodRef() {
        int parameterCount = parameters.parameterCount;
        Type[] parameterTypes = new Type[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            parameterTypes[i] = parameters.parameters[i].typeRef.getType();
        }

        return new MethodRef(implDecl.boundDecl.getType(), "<init>", exprCount, parameterTypes,
                             implDecl.boundDecl.getType());
    }
}
