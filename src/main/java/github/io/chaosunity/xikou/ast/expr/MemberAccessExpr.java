package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class MemberAccessExpr implements Expr {

  public final Expr ownerExpr;
  public final Token nameToken;
  public FieldRef resolvedFieldRef;

  public MemberAccessExpr(Expr ownerExpr, Token nameToken) {
    this.ownerExpr = ownerExpr;
    this.nameToken = nameToken;
  }

  @Override
  public AbstractType getType() {
    return resolvedFieldRef.fieldType;
  }

  @Override
  public boolean isAssignable() {
    return resolvedFieldRef.isMutable;
  }
}
