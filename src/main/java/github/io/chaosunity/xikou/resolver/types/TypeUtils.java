package github.io.chaosunity.xikou.resolver.types;

public class TypeUtils {

  // TODO: Complete it
  public static boolean isInstanceOf(AbstractType fromType, AbstractType targetType) {
    if (fromType instanceof PrimitiveType && targetType instanceof PrimitiveType) {
      return fromType.equals(targetType);
    }

    // Allows implicit down cast for null (assume it as any type).
    // Reversed type conversion from object to null is impossible.
    if (fromType instanceof NullType && targetType instanceof ClassType) {
      return true;
    }

    if (fromType instanceof ClassType && targetType instanceof ClassType) {
      return TypeUtils.isObjectInstanceOf((ClassType) fromType, (ClassType) targetType);
    }

    if (fromType instanceof ArrayType && targetType instanceof ArrayType) {
      return isInstanceOf(
          ((ArrayType) fromType).getComponentType(), ((ArrayType) targetType).getComponentType());
    }

    return false;
  }

  private static boolean isObjectInstanceOf(ClassType fromType, ClassType targetType) {
    if (fromType.equals(targetType)) {
      return true;
    }

    // Checks interfaces first
    ClassType[] interfaces = fromType.getInterfaces();

    for (ClassType interfaceType : interfaces) {
      if (TypeUtils.isObjectInstanceOf(interfaceType, targetType)) {
        return true;
      }
    }

    // Checks superclass later
    ClassType superclassType = fromType.getSuperclass();

    while (superclassType != null) {
      if (TypeUtils.isObjectInstanceOf(superclassType, targetType)) {
        return true;
      }

      superclassType = superclassType.getSuperclass();
    }

    return false;
  }

  public static boolean typesCanCast(AbstractType fromType, AbstractType toType) {
    if (fromType instanceof PrimitiveType && toType instanceof PrimitiveType) {
      return toType != PrimitiveType.BOOL
          && fromType != PrimitiveType.VOID
          && toType != PrimitiveType.VOID;
    }

    if (fromType instanceof ArrayType && toType instanceof ClassType) {
      return toType.equals(ClassType.OBJECT_CLASS_TYPE);
    }

    return true;
  }
}
