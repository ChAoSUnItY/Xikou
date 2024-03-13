package github.io.chaosunity.xikou.resolver.types;

import java.util.Objects;

public final class ClassType implements AbstractType {

  public static final ClassType OBJECT_CLASS_TYPE = new ClassType(null, false, "java/lang/Object");
  public static final ClassType ENUM_CLASS_TYPE = new ClassType(OBJECT_CLASS_TYPE,
      false,
      "java/lang/Enum");
  public static final ClassType STRING_CLASS_TYPE = new ClassType(OBJECT_CLASS_TYPE,
      false,
      "java/lang/String");

  public final ClassType superclass;
  public final boolean isInterface;
  public final String internalName;

  public ClassType(ClassType superclass, boolean isInterface, String internalName) {
    this.superclass = superclass;
    this.isInterface = isInterface;
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClassType that = (ClassType) o;
    return Objects.equals(internalName, that.internalName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(internalName);
  }
}
