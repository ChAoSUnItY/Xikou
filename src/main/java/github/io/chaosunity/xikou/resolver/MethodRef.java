package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.Type;

public class MethodRef {
    public final Type ownerClassType;
    public final String name;
    public final int parameterCount;
    public final Type[] parameterType;
    public final Type returnType;

    public MethodRef(Type ownerClassType, String name, int parameterCount, Type[] parameterType,
                     Type returnType) {
        this.ownerClassType = ownerClassType;
        this.name = name;
        this.parameterCount = parameterCount;
        this.parameterType = parameterType;
        this.returnType = returnType;
    }
}
