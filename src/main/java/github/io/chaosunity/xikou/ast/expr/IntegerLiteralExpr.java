package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public class IntegerLiteralExpr implements Expr {

  public final Token integerToken;

  public IntegerLiteralExpr(Token integerToken) {
    this.integerToken = integerToken;
  }

  public int asConstant() {
    return Integer.parseInt(integerToken.literal);
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.INT;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }
}
