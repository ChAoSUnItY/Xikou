package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class WhileExpr implements Expr {

  public final Expr condExpr;
  public final BlockExpr iterExpr;

  public WhileExpr(Expr condExpr, BlockExpr iterExpr) {
    this.condExpr = condExpr;
    this.iterExpr = iterExpr;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.VOID;
  }
}
