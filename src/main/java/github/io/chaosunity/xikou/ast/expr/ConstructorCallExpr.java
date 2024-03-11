package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class ConstructorCallExpr extends Expr {
    public final TypeableExpr ownerTypeExpr;
    public final int argumentCount;
    public final Expr[] arguments;
    public AbstractType resolvedType;

    public ConstructorCallExpr(TypeableExpr ownerTypeExpr, int argumentCount, Expr[] arguments) {
        this.ownerTypeExpr = ownerTypeExpr;
        this.argumentCount = argumentCount;
        this.arguments = arguments;
    }

    @Override
    public AbstractType getType() {
        return resolvedType;
    }

    @Override
    public boolean isAssignable() {
        return false;
    }
}
