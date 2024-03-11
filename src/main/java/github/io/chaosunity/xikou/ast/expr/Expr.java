package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public abstract class Expr {
    public abstract AbstractType getType();

    public abstract boolean isAssignable();
}
