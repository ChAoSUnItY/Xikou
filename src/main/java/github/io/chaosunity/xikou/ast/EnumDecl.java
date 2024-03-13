package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class EnumDecl implements BoundableDecl {

  public final PackageRef packageRef;
  public final int modifiers;
  public final Token enumNameToken;
  public final int interfacCount;
  public final ClassTypeRef[] interfaces;
  public final int fieldCount;
  public final FieldDecl[] fieldDecls;
  public final int variantCount;
  public final EnumVariantDecl[] enumVariantDecls;
  public ImplDecl boundImplDecl;
  public ClassType enumType;
  public ClassType[] interfaceTypes;

  public EnumDecl(PackageRef packageRef, int modifiers, Token enumNameToken, int interfacCount,
      ClassTypeRef[] interfaces, int fieldCount,
      FieldDecl[] fieldDecls, int variantCount, EnumVariantDecl[] enumVariantDecls) {
    this.packageRef = packageRef;
    this.modifiers = modifiers;
    this.enumNameToken = enumNameToken;
    this.interfacCount = interfacCount;
    this.interfaces = interfaces;
    this.fieldCount = fieldCount;
    this.fieldDecls = fieldDecls;
    this.variantCount = variantCount;
    this.enumVariantDecls = enumVariantDecls;
  }

  @Override
  public PackageRef getPackageRef() {
    return packageRef;
  }

  @Override
  public Token getNameToken() {
    return enumNameToken;
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
    return ClassType.ENUM_CLASS_TYPE;
  }

  @Override
  public ClassType[] getInterfaceTypes() {
    return interfaceTypes;
  }

  @Override
  public ClassType getType() {
    return enumType != null ? enumType : (enumType = BoundableDecl.super.getType());
  }
}
