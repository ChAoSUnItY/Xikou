package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class Scope {

  public final ClassType parentClassType;
  public final boolean isInConstructor;
  public final boolean isInInstance;
  private int localVarCount = 0;
  private LocalVarRef[] localVarRefs = new LocalVarRef[1];

  public Scope(ClassType parentClassType, boolean isInConstructor, boolean isInInstance) {
    this.parentClassType = parentClassType;
    this.isInConstructor = isInConstructor;
    this.isInInstance = isInInstance;
  }

  public Scope extend() {
    LocalVarRef[] extendedLocalVarRefs = new LocalVarRef[localVarRefs.length];
    System.arraycopy(localVarRefs, 0, extendedLocalVarRefs, 0, localVarCount);
    Scope newScope = new Scope(parentClassType, isInConstructor, isInInstance);
    newScope.localVarCount = localVarCount;
    newScope.localVarRefs = extendedLocalVarRefs;

    return newScope;
  }

  public LocalVarRef addLocalVar(String name, boolean mutable, AbstractType type) {
    for (int i = 0; i < localVarCount; i++) {
      if (localVarRefs[i].name.equals(name)) {
        throw new IllegalStateException(
            String.format("Redeclaration of local variable %s", name));
      }
    }

    if (localVarCount >= localVarRefs.length) {
      LocalVarRef[] newArr = new LocalVarRef[localVarRefs.length * 2];
      System.arraycopy(localVarRefs, 0, newArr, 0, localVarRefs.length);
      localVarRefs = newArr;
    }

    LocalVarRef localVarRef = localVarRefs[localVarCount] = new LocalVarRef(name, mutable,
        localVarCount, type);
    localVarCount += type.getSize();

    return localVarRef;
  }

  public LocalVarRef findLocalVar(String name) {
    for (int i = 0; i < localVarCount; i++) {
      LocalVarRef localVarRef = localVarRefs[i];

      if (localVarRef == null) {
        continue;
      }

      if (localVarRef.name.equals(name)) {
        return localVarRef;
      }
    }

    return null;
  }
}
