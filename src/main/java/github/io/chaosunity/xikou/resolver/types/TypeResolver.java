package github.io.chaosunity.xikou.resolver.types;

public class TypeResolver {

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
      return TypeResolver.isObjectInstanceOf((ClassType) fromType, (ClassType) targetType);
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
      if (TypeResolver.isObjectInstanceOf(interfaceType, targetType)) {
        return true;
      }
    }

    // Checks superclass later
    ClassType superclassType = fromType.getSuperclass();

    while (superclassType != null) {
      if (TypeResolver.isObjectInstanceOf(superclassType, targetType)) {
        return true;
      }

      superclassType = superclassType.getSuperclass();
    }

    return false;
  }
}
