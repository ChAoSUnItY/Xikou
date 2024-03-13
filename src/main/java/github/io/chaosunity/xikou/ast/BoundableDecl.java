package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public interface BoundableDecl {

  Token getNameToken();

  default String getName() {
    return getNameToken().literal;
  }

  PackageRef getPackageRef();

  ImplDecl getImplDecl();

  default PrimaryConstructorDecl getPrimaryConstructorDecl() {
    ImplDecl implDecl = getImplDecl();
    return implDecl != null ? implDecl.primaryConstructorDecl : null;
  }

  void bindImplbidirectionally(ImplDecl implDecl);

  boolean isInterface();

  ClassType getSuperclassType();

  ClassType[] getInterfaceTypes();

  default ClassType getType() {
    PackageRef packageRef = getPackageRef();
    Token declNameToken = getNameToken();
    String internalPath;

    if (packageRef.qualifiedPath.isEmpty()) {
      internalPath = declNameToken.literal;
    } else {
      internalPath = packageRef.qualifiedPath.replace('.', '/') + "/" + declNameToken.literal;
    }

    return new ClassType(getSuperclassType(), getInterfaceTypes(), isInterface(), internalPath);
  }
}
