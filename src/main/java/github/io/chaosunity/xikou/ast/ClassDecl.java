package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public final class ClassDecl implements BoundableDecl {

  public final PackageRef packageRef;
  public final int modifiers;
  public final Token classNameToken;
  public final int inheritedCount;
  public final ClassTypeRef[] inheritedClasses;
  public final int fieldCount;
  public final FieldDecl[] fieldDecls;
  public ImplDecl boundImplDecl;
  public ClassType classType;
  private ClassType superclassType;
  private ClassType[] interfaceTypes;

  public ClassDecl(
      PackageRef packageRef,
      int modifiers,
      Token classNameToken,
      int inheritedCount,
      ClassTypeRef[] inheritedClasses,
      int fieldCount,
      FieldDecl[] fieldDecls) {
    this.packageRef = packageRef;
    this.modifiers = modifiers;
    this.classNameToken = classNameToken;
    this.inheritedCount = inheritedCount;
    this.inheritedClasses = inheritedClasses;
    this.fieldCount = fieldCount;
    this.fieldDecls = fieldDecls;
  }

  @Override
  public PackageRef getPackageRef() {
    return packageRef;
  }

  @Override
  public Token getNameToken() {
    return classNameToken;
  }

  @Override
  public ImplDecl getImplDecl() {
    return boundImplDecl;
  }

  @Override
  public void bindImplbidirectionally(ImplDecl implDecl) {
    boundImplDecl = implDecl;
    implDecl.boundDecl = this;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public ClassType getSuperclassType() {
    return inheritedClasses[0] != null
        ? inheritedClasses[0].resolvedType
        : ClassType.OBJECT_CLASS_TYPE;
  }

  @Override
  public ClassType[] getInterfaceTypes() {
    return interfaceTypes;
  }

  @Override
  public ClassType getType() {
    return classType != null ? classType : (classType = BoundableDecl.super.getType());
  }

  public void resolveSuperclassAndInterfaces() {
    if (superclassType != null && interfaceTypes != null) {
      return;
    }

    // Precalculate array size of interfaceTypes
    int interfaceTypesCount = 0;

    for (int i = 0; i < inheritedCount; i++) {
      if (inheritedClasses[i].resolvedType.isInterface) {
        interfaceTypesCount++;
      }
    }

    interfaceTypes = new ClassType[interfaceTypesCount];
    interfaceTypesCount = 0;

    for (int i = 0; i < inheritedCount; i++) {
      ClassType classType = inheritedClasses[i].resolvedType;

      if (classType.isInterface) {
        interfaceTypes[interfaceTypesCount++] = classType;
        continue;
      }

      if (superclassType != null) {
        throw new IllegalStateException("Cannot extend multiple classes");
      }

      superclassType = classType;
    }
  }
}
