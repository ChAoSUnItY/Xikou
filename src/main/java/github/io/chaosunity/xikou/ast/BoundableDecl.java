package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public abstract class BoundableDecl {

  public abstract Token getNameToken();

  public String getName() {
    return getNameToken().literal;
  }

  public abstract PackageRef getPackageRef();

  public abstract ImplDecl getImplDecl();

  public PrimaryConstructorDecl getPrimaryConstructorDecl() {
    ImplDecl implDecl = getImplDecl();
    return implDecl != null ? implDecl.primaryConstructorDecl : null;
  }

  public abstract void bindImplbidirectionally(ImplDecl implDecl);

  public abstract ClassType getType();
}
