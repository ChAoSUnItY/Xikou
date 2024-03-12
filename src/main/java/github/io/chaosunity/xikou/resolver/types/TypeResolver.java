package github.io.chaosunity.xikou.resolver.types;

public class TypeResolver {
    // TODO: Complete it
    public static boolean isInstanceOf(AbstractType fromType, AbstractType targetType) {
        if (fromType instanceof PrimitiveType) {
            if (targetType instanceof PrimitiveType) return fromType.equals(targetType);

            return false;
        }

        // Allows implicit down cast for null (assume it as any type).
        // Reversed type conversion from object to null is impossible.
        if (fromType instanceof NullType && targetType instanceof ClassType)
            return true;

        if (fromType instanceof ClassType && targetType instanceof ClassType) {
            if (fromType.equals(targetType))
                return true;

            ClassType fromClassType = (ClassType) fromType;
            ClassType superclassType = fromClassType.superclass;

            // TODO: Resolve interface branches
            while (superclassType != null) {
                if (superclassType.equals(targetType))
                    return true;

                superclassType = superclassType.superclass;
            }

            return false;
        }

        return false;
    }
}
