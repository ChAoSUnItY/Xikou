package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.LocalVarRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;

public final class ForExpr implements Expr {
  public final NameExpr iterateVarNameExpr;
  public final Expr iterableTargetExpr;
  public final BlockExpr iterationBlock;
  // Used when iterableTargetExpr is an immediate value (usually be array initialization)
  public LocalVarRef immIterableTargetVarRef;
  // Used when iterate on an array type
  public LocalVarRef indexVarRef;

  public ForExpr(NameExpr iterateVarNameExpr, Expr iterableTargetExpr, BlockExpr iterationBlock) {
    this.iterateVarNameExpr = iterateVarNameExpr;
    this.iterableTargetExpr = iterableTargetExpr;
    this.iterationBlock = iterationBlock;
  }

  @Override
  public AbstractType getType() {
    return PrimitiveType.VOID;
  }
}
