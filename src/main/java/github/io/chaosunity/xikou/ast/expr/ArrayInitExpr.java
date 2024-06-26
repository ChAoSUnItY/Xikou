package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class ArrayInitExpr implements Expr {

  public final AbstractTypeRef componentTypeRef;
  public final Expr sizeExpr;
  public final int initExprCount;
  public final Expr[] initExprs;

  public ArrayInitExpr(
      AbstractTypeRef componentTypeRef, Expr sizeExpr, int initExprCount, Expr[] initExprs) {
    this.componentTypeRef = componentTypeRef;
    this.sizeExpr = sizeExpr;
    this.initExprCount = initExprCount;
    this.initExprs = initExprs;
  }

  @Override
  public AbstractType getType() {
    return getComponentType().asArrayType();
  }

  public AbstractType getComponentType() {
    return componentTypeRef.getType();
  }
}
