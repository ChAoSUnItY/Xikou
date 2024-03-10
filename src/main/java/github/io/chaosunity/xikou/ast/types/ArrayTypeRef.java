package github.io.chaosunity.xikou.ast.types;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.resolver.types.ArrayType;
import github.io.chaosunity.xikou.resolver.types.Type;

public class ArrayTypeRef extends AbstractTypeRef {
    public final AbstractTypeRef componentTypeRef;
    public final Expr sizeExpr;
    public ArrayType resolvedType;


    public ArrayTypeRef(AbstractTypeRef componentTypeRef, Expr sizeExpr) {
        this.componentTypeRef = componentTypeRef;
        this.sizeExpr = sizeExpr;
    }

    @Override
    public Type getType() {
        return resolvedType;
    }
}
