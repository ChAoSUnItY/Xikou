package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class Scope {

  private int localVarCount = 0;
  private LocalVarRef[] localVarRefs = new LocalVarRef[1];

  public Scope() {
  }

  public Scope extend() {
    LocalVarRef[] extendedLocalVarRefs = new LocalVarRef[localVarRefs.length];
    System.arraycopy(localVarRefs, 0, extendedLocalVarRefs, 0, localVarCount);
    Scope newScope = new Scope();
    newScope.localVarCount = localVarCount;
    newScope.localVarRefs = extendedLocalVarRefs;

    return newScope;
  }

  public LocalVarRef addLocalVar(String name, AbstractType type) {
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

    LocalVarRef localVarRef = localVarRefs[localVarCount] = new LocalVarRef(name, localVarCount,
        type);
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
