package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.NullType;

public class NullLiteral implements Expr {

  public final Token nullLiteralToken;

  public NullLiteral(Token nullLiteralToken) {
    this.nullLiteralToken = nullLiteralToken;
  }

  @Override
  public AbstractType getType() {
    return NullType.INSTANCE;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }
}
