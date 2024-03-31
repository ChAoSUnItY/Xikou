package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class ArithmeticExpr implements InfixExpr {

  public final Expr lhs;
  public final Token arithOperatorToken;
  public final Expr rhs;

  public ArithmeticExpr(Expr lhs, Token arithOperatorToken, Expr rhs) {
    this.lhs = lhs;
    this.arithOperatorToken = arithOperatorToken;
    this.rhs = rhs;
  }

  @Override
  public AbstractType getType() {
    // This assumes lhs and rhs have same type
    AbstractType lhsType = lhs.getType();

    if (lhsType instanceof PrimitiveType) {
      switch ((PrimitiveType) lhsType) {
        case INT:
          return PrimitiveType.INT;
        case LONG:
          return PrimitiveType.LONG;
        case FLOAT:
          return PrimitiveType.FLOAT;
        case DOUBLE:
          return PrimitiveType.DOUBLE;
      }
    }

    return PrimitiveType.VOID;
  }

  @Override
  public Expr getLhs() {
    return lhs;
  }

  @Override
  public Expr getRhs() {
    return rhs;
  }
}
