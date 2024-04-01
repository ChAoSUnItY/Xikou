package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class AssignmentExpr implements Expr {

  public final int targetCount;
  public final Expr[] targets;
  public final Token assignOpToken;
  public final Expr rhs;

  public AssignmentExpr(int targetCount, Expr[] targets, Token assignOpToken, Expr rhs) {
    this.targetCount = targetCount;
    this.targets = targets;
    this.assignOpToken = assignOpToken;
    this.rhs = rhs;
  }

  @Override
  public AbstractType getType() {
    return targets[0].getType();
  }

  @Override
  public boolean isAssignable() {
    for (int i = 0; i < targetCount; i++) {
      if (!targets[i].isAssignable()) {
        return false;
      }
    }

    return true;
  }
}
