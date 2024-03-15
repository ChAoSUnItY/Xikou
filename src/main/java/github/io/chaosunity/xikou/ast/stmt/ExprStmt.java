package github.io.chaosunity.xikou.ast.stmt;

import github.io.chaosunity.xikou.ast.expr.Expr;

public class ExprStmt implements Statement {

  public final Expr expr;

  public ExprStmt(Expr expr) {
    this.expr = expr;
  }
}
