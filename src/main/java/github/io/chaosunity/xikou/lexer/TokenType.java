package github.io.chaosunity.xikou.lexer;

public enum TokenType {
    CharLiteral,
    StringLiteral,
    NumberLiteral,
    Identifier,
    OpenParenthesis,
    CloseParenthesis,
    OpenBrace,
    CloseBrace,
    Dot,
    Comma,
    SemiColon,
    Colon,
    DoubleColon,
    Equal,
    /*  KEYWORDS */
    Pub,
    Priv,
    Mut,
    Pkg,
    Class,
    Fn,
    Const,
    Let,
    Self,
    Impl,
    EOF;
    
    public static TokenType[] KEYWORDS = new TokenType[] {
            Pub,
            Priv,
            Mut,
            Pkg,
            Class,
            Fn,
            Const,
            Let,
            Self,
            Impl
    };
}
