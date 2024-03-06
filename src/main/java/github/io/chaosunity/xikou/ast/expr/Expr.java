package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.Type;

public abstract class Expr {
    public abstract Type getType();

    public abstract boolean isAssignable();
}
