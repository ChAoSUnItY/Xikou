package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.resolver.Scope;
import github.io.chaosunity.xikou.ast.expr.Expr;

public class PrimaryConstructorDecl {
    public final int constructorModifiers;
    public final Parameters parameters;
    public final int exprCount;
    public final Expr[] exprs;

    public ImplDecl implDecl;
    public final Scope scope;

    public PrimaryConstructorDecl(int constructorModifiers, Parameters parameters, int exprCount, Expr[] exprs) {
        this.constructorModifiers = constructorModifiers;
        this.parameters = parameters;
        this.exprCount = exprCount;
        this.exprs = exprs;
        this.scope = new Scope();
    }
}
