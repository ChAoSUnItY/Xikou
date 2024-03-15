package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.LocalVarRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class NameExpr implements TypeableExpr {

  public final Token varIdentifier;
  public AbstractType resolvedType;
  public LocalVarRef localVarRef;

  public NameExpr(Token varIdentifier) {
    this.varIdentifier = varIdentifier;
  }

  @Override
  public AbstractType getType() {
    return resolvedType;
  }

  @Override
  public boolean isAssignable() {
    return true;
  }

  @Override
  public ClassTypeRef asTypeRef() {
    return new ClassTypeRef(1, new Token[]{varIdentifier});
  }
}
