package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class FieldAccessExpr implements Expr {

  public final Expr ownerExpr;
  public final Token nameToken;
  public boolean isLenAccess = false;
  public FieldRef resolvedFieldRef;

  public FieldAccessExpr(Expr ownerExpr, Token nameToken) {
    this.ownerExpr = ownerExpr;
    this.nameToken = nameToken;
  }

  @Override
  public AbstractType getType() {
    return isLenAccess ? PrimitiveType.INT : resolvedFieldRef.fieldType;
  }

  @Override
  public boolean isAssignable() {
    return resolvedFieldRef.isMutable;
  }
}
