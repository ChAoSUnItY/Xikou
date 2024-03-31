package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.lexer.Token;

public final class FieldDecl {

  public final int fieldModifiers;
  public final Token name;
  public final AbstractTypeRef typeRef;
  public final Token equalToken;
  public final Expr initialExpr;

  public FieldDecl(int fieldModifiers, Token name, AbstractTypeRef typeRef, Token equalToken,
      Expr initialExpr) {
    this.fieldModifiers = fieldModifiers;
    this.name = name;
    this.typeRef = typeRef;
    this.equalToken = equalToken;
    this.initialExpr = initialExpr;
  }

  @Override
  public String toString() {
    return "FieldDecl{" + "fieldModifiers=" + fieldModifiers + ", name='" + name + '\''
        + ", typeRef='" + typeRef + '\'' + '}';
  }
}
