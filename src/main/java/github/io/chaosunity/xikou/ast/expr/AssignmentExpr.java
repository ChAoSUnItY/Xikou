package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class AssignmentExpr implements Expr {

  public final Expr lhs;
  public final Token assignOpToken;
  public final Expr rhs;

  public AssignmentExpr(Expr lhs, Token assignOpToken, Expr rhs) {
    this.lhs = lhs;
    this.assignOpToken = assignOpToken;
    this.rhs = rhs;
  }

  @Override
  public AbstractType getType() {
    return lhs.getType();
  }

  @Override
  public boolean isAssignable() {
    return lhs.isAssignable();
  }
}
