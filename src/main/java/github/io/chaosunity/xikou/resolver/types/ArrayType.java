package github.io.chaosunity.xikou.resolver.types;

import java.util.Objects;

public class ArrayType implements Type {
    private final Type componentType;

    public ArrayType(Type componentType) {
        this.componentType = componentType;
    }

    public Type getComponentType() {
        return componentType;
    }

    @Override
    public String getInternalName() {
        return "[" + componentType.getInternalName();
    }

    @Override
    public String getDescriptor() {
        return "[" + componentType.getDescriptor();
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(componentType, arrayType.componentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentType);
    }
}