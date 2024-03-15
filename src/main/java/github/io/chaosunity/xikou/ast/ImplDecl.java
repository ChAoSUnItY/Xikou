package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;

public class ImplDecl {

  public final Token targetClass;
  public final ConstructorDecl constructorDecl;
  public BoundableDecl boundDecl;

  public ImplDecl(Token targetClass, ConstructorDecl constructorDecl) {
    this.targetClass = targetClass;
    this.constructorDecl = constructorDecl;

    if (constructorDecl != null) {
      constructorDecl.implDecl = this;
    }
  }
}
