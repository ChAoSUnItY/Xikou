package github.io.chaosunity.xikou.resolver.types;

import java.util.Objects;

public class ArrayType implements AbstractType {

  private final AbstractType componentType;

  public ArrayType(AbstractType componentType) {
    this.componentType = componentType;
  }

  public AbstractType getComponentType() {
    return componentType;
  }

  public int getDimensions() {
    int dimensions = 1;
    AbstractType componentType = getComponentType();

    while (componentType instanceof ArrayType) {
      dimensions++;
      componentType = ((ArrayType) componentType).getComponentType();
    }

    return dimensions;
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArrayType arrayType = (ArrayType) o;
    return Objects.equals(componentType, arrayType.componentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(componentType);
  }
}
