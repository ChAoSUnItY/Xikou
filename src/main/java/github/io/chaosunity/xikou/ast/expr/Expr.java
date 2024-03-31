package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public interface Expr {

  AbstractType getType();

  default boolean isAssignable() {
    return false;
  }
}
