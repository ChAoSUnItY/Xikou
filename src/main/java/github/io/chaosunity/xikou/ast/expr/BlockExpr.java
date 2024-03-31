package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class BlockExpr implements Expr {

  public final int statementCount;
  public final Statement[] statements;

  public BlockExpr(int statementCount, Statement[] statements) {
    this.statementCount = statementCount;
    this.statements = statements;
  }

  @Override
  public AbstractType getType() {
    if (statementCount > 0 && statements[statementCount - 1] instanceof ExprStmt) {
      return ((ExprStmt) statements[statementCount - 1]).expr.getType();
    } else {
      return PrimitiveType.VOID;
    }
  }
}
