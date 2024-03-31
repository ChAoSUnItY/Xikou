package github.io.chaosunity.xikou.ast.types;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public final class ClassTypeRef implements AbstractTypeRef {

  public final int selectorCount;
  public final Token[] selectors;
  public ClassType resolvedType;

  public ClassTypeRef(int selectorCount, Token[] selectors) {
    this.selectorCount = selectorCount;
    this.selectors = selectors;
  }

  @Override
  public AbstractType getType() {
    return resolvedType;
  }
}
