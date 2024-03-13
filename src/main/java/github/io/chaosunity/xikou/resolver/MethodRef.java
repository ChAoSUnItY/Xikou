package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class MethodRef {

  public final AbstractType ownerClassType;
  public final String name;
  public final int parameterCount;
  public final AbstractType[] parameterType;
  public final AbstractType returnType;
  public final boolean isConstructor;

  public MethodRef(AbstractType ownerClassType, String name, int parameterCount,
      AbstractType[] parameterType, AbstractType returnType, boolean isConstructor) {
    this.ownerClassType = ownerClassType;
    this.name = name;
    this.parameterCount = parameterCount;
    this.parameterType = parameterType;
    this.returnType = returnType;
    this.isConstructor = isConstructor;
  }
}
