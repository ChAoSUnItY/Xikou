package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.ast.types.ArrayTypeRef;
import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public final class TypeResolver {

  private final SymbolTable table;

  TypeResolver(SymbolTable table) {
    this.table = table;
  }

  public void resolveTypeRef(AbstractTypeRef typeRef, boolean recoverable) {
    // Primitive type is already resolved in parser phase

    if (typeRef instanceof ClassTypeRef) {
      ClassTypeRef classTypeRef = (ClassTypeRef) typeRef;
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < classTypeRef.selectorCount; i++) {
        builder.append(classTypeRef.selectors[i].literal);

        if (i != classTypeRef.selectorCount - 1) {
          builder.append("/");
        }
      }

      AbstractType type = table.getType(builder.toString().replace('/', '.'));

      if (!(type instanceof ClassType)) {
        if (recoverable) {
          return;
        }

        throw new IllegalStateException(String.format("Type %s is not an ClassType",
            builder));
      }

      classTypeRef.resolvedType = (ClassType) type;
    } else if (typeRef instanceof ArrayTypeRef) {
      ArrayTypeRef arrayTypeRef = (ArrayTypeRef) typeRef;
      resolveTypeRef(arrayTypeRef.componentTypeRef, recoverable);

      arrayTypeRef.resolvedType = new ArrayType(arrayTypeRef.componentTypeRef.getType());
    }
  }
}
