package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class CompareExpr implements InfixExpr {

  public final Expr lhs;
  public final Token compareOperatorToken;
  public final Expr rhs;

  public CompareExpr(Expr lhs, Token compareOperatorToken, Expr rhs) {
    this.lhs = lhs;
    this.compareOperatorToken = compareOperatorToken;
    this.rhs = rhs;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.BOOL;
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
