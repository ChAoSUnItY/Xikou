package github.io.chaosunity.xikou.model;

import github.io.chaosunity.xikou.lexer.Token;

public class FnDecl {
    public final int fnModifiers;
    public final Token name;

    public FnDecl(int fnModifiers, Token name) {
        this.fnModifiers = fnModifiers;
        this.name = name;
    }
}
