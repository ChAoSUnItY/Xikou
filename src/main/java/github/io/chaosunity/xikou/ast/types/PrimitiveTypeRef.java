package github.io.chaosunity.xikou.ast.types;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class PrimitiveTypeRef implements AbstractTypeRef {

  public final PrimitiveType type;
  public final Token typeRefToken;

  public PrimitiveTypeRef(Token typeRefToken, PrimitiveType type) {
    this.type = type;
    this.typeRefToken = typeRefToken;
  }

  @Override
  public PrimitiveType getType() {
    return type;
  }
}
