package github.io.chaosunity.xikou.resolver.types;

public final class PrimitiveType extends Type {
    public static final PrimitiveType VOID = new PrimitiveType("unit", "void", 'V');
    public static final PrimitiveType CHAR = new PrimitiveType("char", "char", 'C');
    public static final PrimitiveType BOOL = new PrimitiveType("bool", "bool", 'Z');
    public static final PrimitiveType INT = new PrimitiveType("i32", "int", 'I');
    public static final PrimitiveType LONG = new PrimitiveType("i64", "long", 'J');
    public static final PrimitiveType FLOAT = new PrimitiveType("f32", "float", 'F');
    public static final PrimitiveType DOUBLE = new PrimitiveType("f64", "double", 'D');

    public static final PrimitiveType[] ENTRIES = new PrimitiveType[] { VOID, CHAR, BOOL, INT, LONG, FLOAT, DOUBLE };

    public final String xkTypeName;
    public final String internalName;
    public final char descriptor;
    
    private PrimitiveType(String xkTypeName, String internalName, char descriptor) {
        this.xkTypeName = xkTypeName;
        this.internalName = internalName;
        this.descriptor = descriptor;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public String getDescriptor() {
        return String.valueOf(descriptor);
    }

    @Override
    public int getSize() {
        return equals(LONG) || equals(DOUBLE) ? 2 : 1;
    }
}
