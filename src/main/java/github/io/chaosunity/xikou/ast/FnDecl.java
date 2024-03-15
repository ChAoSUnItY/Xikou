package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.lexer.Token;

public class FnDecl {

  public final int fnModifiers;
  public final Token name;
  public final int parameterCount;
  public final Parameter[] parameters;
  public final Expr[] exprs;

  public FnDecl(int fnModifiers, Token name, int parameterCount, Parameter[] parameters,
      Expr[] exprs) {
    this.fnModifiers = fnModifiers;
    this.name = name;
    this.parameterCount = parameterCount;
    this.parameters = parameters;
    this.exprs = exprs;
  }
}
