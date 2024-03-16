package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;

public class ImplDecl {

  public final Token targetClass;
  public final ConstructorDecl constructorDecl;
  public final int functionCount;
  public final FnDecl[] functionDecls;
  public BoundableDecl boundDecl;

  public ImplDecl(Token targetClass, ConstructorDecl constructorDecl, int functionCount,
      FnDecl[] functionDecls) {
    this.targetClass = targetClass;
    this.constructorDecl = constructorDecl;
    this.functionCount = functionCount;
    this.functionDecls = functionDecls;

    if (constructorDecl != null) {
      constructorDecl.implDecl = this;
    }

    for (int i = 0; i < functionCount; i++) {
      functionDecls[i].implDecl = this;
    }
  }
}
