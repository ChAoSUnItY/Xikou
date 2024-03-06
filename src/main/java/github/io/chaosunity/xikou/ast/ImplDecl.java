package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;

public class ImplDecl {
    public final Token targetClass;
    public final PrimaryConstructorDecl primaryConstructorDecl;
    
    public ImplDecl(Token targetClass, PrimaryConstructorDecl primaryConstructorDecl) {
        this.targetClass = targetClass;
        this.primaryConstructorDecl = primaryConstructorDecl;
    }
}
