package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class PlusExpr implements InfixExpr {

  public final Expr lhs;
  public final Expr rhs;

  public PlusExpr(Expr lhs, Expr rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.INT;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }

  @Override
  public Expr getLhs() {
    return lhs;
  }

  @Override
  public Expr getRhs() {
    return rhs;
  }
}
