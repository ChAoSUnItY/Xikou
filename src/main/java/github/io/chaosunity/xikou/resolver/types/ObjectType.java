package github.io.chaosunity.xikou.resolver.types;

public final class ObjectType extends Type {
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
}
