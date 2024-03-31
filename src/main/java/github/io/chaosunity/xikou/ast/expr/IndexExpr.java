package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;

public final class IndexExpr implements Expr {

  public final Expr targetExpr;
  public final Expr indexExpr;

  public IndexExpr(Expr targetExpr, Expr indexExpr) {
    this.targetExpr = targetExpr;
    this.indexExpr = indexExpr;
  }

  @Override
  public AbstractType getType() {
    return ((ArrayType) targetExpr.getType()).getComponentType();
  }

  @Override
  public boolean isAssignable() {
    // Array elements are assignable
    return true;
  }
}
