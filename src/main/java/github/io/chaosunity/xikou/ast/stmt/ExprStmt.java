package github.io.chaosunity.xikou.ast.stmt;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.lexer.Token;

public class ExprStmt implements Statement {

  public final Expr expr;
  public final Token semicolonToken;

  public ExprStmt(Expr expr, Token semicolonToken) {
    this.expr = expr;
    this.semicolonToken = semicolonToken;
  }
}
