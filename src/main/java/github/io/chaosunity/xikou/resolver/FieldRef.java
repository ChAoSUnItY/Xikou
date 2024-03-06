package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.Type;

public class FieldRef {
    public final Type ownerClassType;
    public final String name;
    public final Type fieldType;

    public FieldRef(Type ownerClassType, String name, Type fieldType) {
        this.ownerClassType = ownerClassType;
        this.name = name;
        this.fieldType = fieldType;
    }
}
