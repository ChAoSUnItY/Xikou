package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class MethodRef {

  public final ClassType ownerClassType;
  public final String name;
  public final int parameterCount;
  public final AbstractType[] parameterType;
  public final AbstractType returnType;
  public final boolean isStatic;
  public final boolean isConstructor;

  public MethodRef(
      ClassType ownerClassType,
      String name,
      int parameterCount,
      AbstractType[] parameterType,
      AbstractType returnType,
      boolean isStatic,
      boolean isConstructor) {
    this.ownerClassType = ownerClassType;
    this.name = name;
    this.parameterCount = parameterCount;
    this.parameterType = parameterType;
    this.returnType = returnType;
    this.isStatic = isStatic;
    this.isConstructor = isConstructor;
  }
}
