package github.io.chaosunity.xikou.resolver.types;

public class TypeResolver {
    // TODO: Complete it
    public static boolean isInstanceOf(Type fromType, Type targetType) {
        if (fromType instanceof PrimitiveType) {
            if (targetType instanceof PrimitiveType) return fromType.equals(targetType);

            return false;
        }

        return true;
    }
}
