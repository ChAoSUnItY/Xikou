package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.Type;

public class FieldRef {
    public final Type ownerClassType;
    public final boolean isStatic;
    public final String name;
    public final Type fieldType;

    public FieldRef(Type ownerClassType, boolean isStatic, String name, Type fieldType) {
        this.ownerClassType = ownerClassType;
        this.isStatic = isStatic;
        this.name = name;
        this.fieldType = fieldType;
    }
}
