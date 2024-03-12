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

        return true;
    }
}
