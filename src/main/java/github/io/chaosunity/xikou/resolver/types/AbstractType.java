package github.io.chaosunity.xikou.resolver.types;

public interface AbstractType {

  String getInternalName();

  String getDescriptor();

  int getSize();

  default ArrayType asArrayType() {
    return new ArrayType(this);
  }
}
