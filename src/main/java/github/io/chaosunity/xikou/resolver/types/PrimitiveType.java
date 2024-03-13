package github.io.chaosunity.xikou.resolver.types;

public enum PrimitiveType implements AbstractType {
  VOID("unit", "void", 'V'), CHAR("char", "char", 'C'), BOOL("bool", "bool", 'Z'),
  INT("i32", "int", 'I'), LONG("i64", "long", 'J'), FLOAT("f32", "float", 'F'),
  DOUBLE("f64", "double", 'D');

  public final String xkTypeName;
  public final String internalName;
  public final char descriptor;

  PrimitiveType(String xkTypeName, String internalName, char descriptor) {
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
