package github.io.chaosunity.xikou.model.types;

public enum PrimitiveType {
    Void("unit", 'V'),
    Int("i32", 'I'),
    Char("char", 'C'),
    Bool("bool", 'Z');
    
    public final String xkTypeName;
    public final char descriptor;
    
    PrimitiveType(String xkTypeName, char descriptor) {
        this.xkTypeName = xkTypeName;
        this.descriptor = descriptor;
    }
}
