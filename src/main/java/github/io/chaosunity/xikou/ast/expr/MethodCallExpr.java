package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class MethodCallExpr implements Expr {

  public final Expr ownerExpr;
  public final Token nameToken;
  public final int argumentCount;
  public final Expr[] arguments;
  public MethodRef resolvedMethodRef;

  public MethodCallExpr(Expr ownerExpr, Token nameToken, int argumentCount, Expr[] arguments) {
    this.ownerExpr = ownerExpr;
    this.nameToken = nameToken;
    this.argumentCount = argumentCount;
    this.arguments = arguments;
  }

  @Override
  public AbstractType getType() {
    return resolvedMethodRef.returnType;
  }
}
