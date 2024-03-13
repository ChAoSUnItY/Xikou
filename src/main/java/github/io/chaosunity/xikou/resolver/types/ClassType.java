package github.io.chaosunity.xikou.resolver.types;

import java.util.Objects;

public final class ClassType implements AbstractType {

  public static final ClassType OBJECT_CLASS_TYPE = new ClassType(null, new ClassType[0], false,
      "java/lang/Object");
  public static final ClassType ENUM_CLASS_TYPE = new ClassType(OBJECT_CLASS_TYPE,
      new ClassType[0],
      false,
      "java/lang/Enum");
  public static final ClassType STRING_CLASS_TYPE = new ClassType(OBJECT_CLASS_TYPE,
      new ClassType[0],
      false,
      "java/lang/String");

  public final ClassType superclass;
  public final ClassType[] interfaces;
  public final boolean isInterface;
  public final String internalName;

  public ClassType(ClassType superclass, ClassType[] interfaces, boolean isInterface,
      String internalName) {
    this.superclass = superclass;
    this.interfaces = interfaces;
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

  public ClassType getSuperclass() {
    return superclass;
  }

  public ClassType[] getInterfaces() {
    return interfaces;
  }

  public boolean isInterface() {
    return isInterface;
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
