package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.Parameter;
import github.io.chaosunity.xikou.ast.Parameters;
import github.io.chaosunity.xikou.resolver.types.Type;

public final class Utils {
    public static String getMethodDescriptor(Type returnType, Parameters parameters) {
        StringBuilder builder = new StringBuilder("(");

        for (int i = 0; i < parameters.parameterCount; i++) {
            Parameter parameter = parameters.parameters[i];

            builder.append(parameter.typeRef.getType().getDescriptor());
        }

        builder.append(")");
        builder.append(returnType.getDescriptor());
        return builder.toString();
    }
}
