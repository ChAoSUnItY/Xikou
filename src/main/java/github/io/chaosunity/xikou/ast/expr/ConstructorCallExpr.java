package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class ConstructorCallExpr implements Expr {

  public final TypeableExpr ownerTypeExpr;
  public final int argumentCount;
  public final Expr[] arguments;
  public AbstractType resolvedType;
  public MethodRef resolvedMethodRef;

  public ConstructorCallExpr(TypeableExpr ownerTypeExpr, int argumentCount, Expr[] arguments) {
    this.ownerTypeExpr = ownerTypeExpr;
    this.argumentCount = argumentCount;
    this.arguments = arguments;
  }

  @Override
  public AbstractType getType() {
    return resolvedType;
  }
}
