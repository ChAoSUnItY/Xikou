package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class ClassDecl extends BoundableDecl {

  public final PackageRef packageRef;
  public final int modifiers;
  public final Token classNameToken;
  public final int inheritedCount;
  public final ClassTypeRef[] inheritedClasses;
  public final int fieldCount;
  public final FieldDecl[] fieldDecls;
  public ImplDecl boundImplDecl;
  private ClassType classType;

  public ClassDecl(PackageRef packageRef, int modifiers, Token classNameToken, int inheritedCount,
      ClassTypeRef[] inheritedClasses, int fieldCount, FieldDecl[] fieldDecls) {
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

  public ClassType getSuperclassType() {
    return inheritedClasses[0] != null ? inheritedClasses[0].resolvedType
        : ClassType.OBJECT_CLASS_TYPE;
  }

  @Override
  public ClassType getType() {
      if (classType != null) {
          return classType;
      }

    PackageRef packageRef = getPackageRef();
    Token declNameToken = getNameToken();
    String internalPath;

    if (packageRef.qualifiedPath.isEmpty()) {
      internalPath = declNameToken.literal;
    } else {
      internalPath = packageRef.qualifiedPath.replace('.', '/') + "/" + declNameToken.literal;
    }

    return new ClassType(getSuperclassType(), internalPath);
  }

  @Override
  public void bindImplbidirectionally(ImplDecl implDecl) {
    boundImplDecl = implDecl;
    implDecl.boundDecl = this;
  }
}
