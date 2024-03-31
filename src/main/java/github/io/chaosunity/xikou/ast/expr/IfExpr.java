package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class IfExpr implements Expr {

  public final Expr condExpr;
  public final BlockExpr trueBranchExpr;
  public final Expr falseBranchExpr;

  public IfExpr(Expr condExpr, BlockExpr trueBranchExpr, Expr falseBranchExpr) {
    this.condExpr = condExpr;
    this.trueBranchExpr = trueBranchExpr;
    this.falseBranchExpr = falseBranchExpr;
  }

  @Override
  public AbstractType getType() {
    if (falseBranchExpr != null) {
      // Get common types of true and false branches
    }

    return trueBranchExpr.getType();
  }

  @Override
  public boolean isAssignable() {
    return false;
  }
}
