package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.Parameter;
import github.io.chaosunity.xikou.ast.Parameters;
import github.io.chaosunity.xikou.resolver.types.Type;

public final class Utils {
    public static String getMethodDescriptor(Type returnType, Type[] parameters) {
        StringBuilder builder = new StringBuilder("(");

        for (Type parameter : parameters) {
            builder.append(parameter.getDescriptor());
        }

        builder.append(")");
        builder.append(returnType.getDescriptor());
        return builder.toString();
    }
}
