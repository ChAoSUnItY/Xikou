package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class InfixExpr implements Expr {

  public final Expr lhs;
  public final Token operator;
  public final Expr rhs;

  public InfixExpr(Expr lhs, Token operator, Expr rhs) {
    this.lhs = lhs;
    this.operator = operator;
    this.rhs = rhs;
  }

  @Override
  public boolean isAssignable() {
    return lhs.isAssignable();
  }

  @Override
  public AbstractType getType() {
    return null; // TODO
  }
}
