package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.TypeResolver;

public class Utils {
    public static MethodRef genImplcicitPrimaryConstructorRef(AbstractType ownerClassType) {
        return new MethodRef(ownerClassType, "<init>", 0, new AbstractType[0], ownerClassType, true);
    }

    public static boolean isInvocationApplicable(int argumentCount, Expr[] arguments,
                                                 MethodRef methodRef) {
        // TODO: Support vararg in future
        if (methodRef.parameterCount != argumentCount) return false;

        for (int i = 0; i < argumentCount; i++) {
            Expr argument = arguments[i];
            AbstractType parameterType = methodRef.parameterType[i];

            if (!TypeResolver.isInstanceOf(argument.getType(), parameterType)) return false;
        }

        return true;
    }
}
