package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class ReturnExpr implements Expr {

  public final Expr rhs;

  public ReturnExpr(Expr rhs) {
    this.rhs = rhs;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.VOID;
  }
}
