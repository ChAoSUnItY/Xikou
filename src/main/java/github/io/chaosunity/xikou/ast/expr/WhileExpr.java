package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class WhileExpr implements Expr {
  
  
  @Override
  public AbstractType getType() {
    return null;
  }

  @Override
  public boolean isAssignable() {
    return false;
  }
}
