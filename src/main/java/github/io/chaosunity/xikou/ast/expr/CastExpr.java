package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class CastExpr implements Expr {

  public final Expr targetCastExpr;
  public final AbstractTypeRef targetTypeRef;

  public CastExpr(Expr targetCastExpr, AbstractTypeRef targetTypeRef) {
    this.targetCastExpr = targetCastExpr;
    this.targetTypeRef = targetTypeRef;
  }

  @Override
  public AbstractType getType() {
    return targetTypeRef.getType();
  }
}
