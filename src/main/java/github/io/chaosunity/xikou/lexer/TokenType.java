package github.io.chaosunity.xikou.lexer;

public enum TokenType {
    CharLiteral, StringLiteral, NumberLiteral, Identifier, OpenParenthesis, CloseParenthesis, OpenBrace, CloseBrace, Dot, Comma, SemiColon, Colon, DoubleColon, Equal, /*  KEYWORDS */
    Pub, Priv, Mut, Pkg, Class, Enum, Fn, Const, Let, Self, Impl, EOF;

    public static final TokenType[] KEYWORDS = new TokenType[]{Pub, Priv, Mut, Pkg, Class, Enum, Fn, Const, Let, Self, Impl};

    public int getInfixPrecedence() {
        switch (this) {
            case Equal:
                return 1;
            default:
                return 0;
        }
    }
}
