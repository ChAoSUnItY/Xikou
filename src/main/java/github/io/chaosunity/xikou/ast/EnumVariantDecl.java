package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.lexer.Token;

public final class EnumVariantDecl {

  public final Token name;
  public final int argumentCount;
  public final Expr[] arguments;


  public EnumVariantDecl(Token name, int argumentCount, Expr[] arguments) {
    this.name = name;
    this.argumentCount = argumentCount;
    this.arguments = arguments;
  }
}
