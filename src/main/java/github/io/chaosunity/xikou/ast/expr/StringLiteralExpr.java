package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class StringLiteralExpr implements Expr {

  public final Token stringLiteralToken;

  public StringLiteralExpr(Token stringLiteralToken) {
    this.stringLiteralToken = stringLiteralToken;
  }

  @Override
  public AbstractType getType() {
    return ClassType.STRING_CLASS_TYPE;
  }
}
