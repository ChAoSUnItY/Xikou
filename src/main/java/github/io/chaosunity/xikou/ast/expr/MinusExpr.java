package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class MinusExpr implements InfixExpr {

  public final Expr lhs;
  public final Expr rhs;

  public MinusExpr(Expr lhs, Expr rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.INT;
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
