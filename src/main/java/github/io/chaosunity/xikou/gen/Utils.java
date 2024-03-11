package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class Utils {
    public static String getMethodDescriptor(AbstractType returnType, AbstractType... parameters) {
        StringBuilder builder = new StringBuilder("(");

        for (AbstractType parameter : parameters) {
            builder.append(parameter.getDescriptor());
        }

        builder.append(")");
        builder.append(returnType.getDescriptor());
        return builder.toString();
    }

    public static int[] genLocalRefIndicesFromMethodDesc(AbstractType ownerType, AbstractType... parameters) {
        int length = (ownerType != null ? 1 : 0) + parameters.length;
        AbstractType[] localRefs = parameters;
        int[] indices = new int[length];

        if (ownerType != null) {
            localRefs = new AbstractType[length];
            localRefs[0] = ownerType;
            System.arraycopy(parameters, 0, localRefs, 1, parameters.length);
        }

        for (int i = 1; i < length; i++) {
            indices[i] = indices[i - 1] + localRefs[i - 1].getSize();
        }

        return indices;
    }
}
