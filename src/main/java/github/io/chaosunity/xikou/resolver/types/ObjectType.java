package github.io.chaosunity.xikou.resolver.types;

import java.util.Objects;

public final class ObjectType implements Type {
    public final String internalName;

    public ObjectType(String internalName) {
        this.internalName = internalName;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public String getDescriptor() {
        return String.format("L%s;", internalName);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectType that = (ObjectType) o;
        return Objects.equals(internalName, that.internalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalName);
    }
}
