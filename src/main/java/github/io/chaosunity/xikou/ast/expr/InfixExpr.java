package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

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
    return false;
  }

  @Override
  public AbstractType getType() {
    switch (operator.type) {
      case As:
        return rhs.getType();
      case DoubleEqual:
      case NotEqual:
      case DoubleAmpersand:
      case DoublePipe:
        return PrimitiveType.BOOL;
      case Plus:
      case Minus:
        return lhs.getType();
      default:
        return null;
    }
  }
}
