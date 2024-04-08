package github.io.chaosunity.xikou.resolver.types;

public final class NullType implements AbstractType {

  public static final NullType INSTANCE = new NullType();

  private NullType() {}

  @Override
  public String getInternalName() {
    return "$NULL";
  }

  @Override
  public String getDescriptor() {
    return "$NULL";
  }

  @Override
  public int getSize() {
    return 1;
  }
}
