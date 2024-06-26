package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class TypeExpr implements TypeableExpr {

  public final ClassTypeRef typeRef;

  public TypeExpr(ClassTypeRef typeRef) {
    this.typeRef = typeRef;
  }

  @Override
  public AbstractType getType() {
    return typeRef.getType();
  }

  @Override
  public ClassTypeRef asTypeRef() {
    return typeRef;
  }
}
