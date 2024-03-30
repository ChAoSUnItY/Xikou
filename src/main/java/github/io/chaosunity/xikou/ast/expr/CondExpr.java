package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class CondExpr implements Expr {

  public final int exprCount;
  public final Expr[] exprs;
  public final Token condOperatorToken;

  public CondExpr(int exprCount, Expr[] exprs, Token condOperatorToken) {
    this.exprCount = exprCount;
    this.exprs = exprs;
    this.condOperatorToken = condOperatorToken;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.BOOL;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }
}
