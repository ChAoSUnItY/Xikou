package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class FieldRef {
    public final AbstractType ownerClassType;
    public final boolean isStatic;
    public final String name;
    public final AbstractType fieldType;

    public FieldRef(AbstractType ownerClassType, boolean isStatic, String name, AbstractType fieldType) {
        this.ownerClassType = ownerClassType;
        this.isStatic = isStatic;
        this.name = name;
        this.fieldType = fieldType;
    }
}
