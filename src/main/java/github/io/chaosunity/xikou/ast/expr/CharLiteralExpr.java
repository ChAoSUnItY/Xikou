package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public class CharLiteralExpr extends Expr {

  public final Token characterToken;

  public CharLiteralExpr(Token characterToken) {
    this.characterToken = characterToken;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.CHAR;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }
}
