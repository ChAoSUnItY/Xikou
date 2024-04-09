package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;

public final class ImplDecl {

  public final Token targetClass;
  public final int constCount;
  public final ConstDecl[] constDecls;
  public final ConstructorDecl constructorDecl;
  public final int functionCount;
  public final FnDecl[] functionDecls;
  public BoundableDecl boundDecl;

  public ImplDecl(
      Token targetClass,
      int constCount,
      ConstDecl[] constDecls,
      ConstructorDecl constructorDecl,
      int functionCount,
      FnDecl[] functionDecls) {
    this.targetClass = targetClass;
    this.constCount = constCount;
    this.constDecls = constDecls;
    this.constructorDecl = constructorDecl;
    this.functionCount = functionCount;
    this.functionDecls = functionDecls;
  }
}
