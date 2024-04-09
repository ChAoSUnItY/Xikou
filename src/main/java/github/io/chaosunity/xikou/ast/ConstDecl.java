package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class ConstDecl {
  public final int modifiers;
  public final Token nameToken;
  public final AbstractTypeRef explicitTypeRef;
  public final Expr initialExpression;
  public AbstractType resolvedType;
  public FieldRef resolvedFieldRef;

  public ConstDecl(
      int modifiers, Token nameToken, AbstractTypeRef explicitTypeRef, Expr initialExpression) {
    this.modifiers = modifiers;
    this.nameToken = nameToken;
    this.explicitTypeRef = explicitTypeRef;
    this.initialExpression = initialExpression;
  }
}
