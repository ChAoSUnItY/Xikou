package github.io.chaosunity.xikou.lexer;

public enum TokenType {
  CharLiteral, StringLiteral, NumberLiteral, Identifier, OpenParenthesis, CloseParenthesis,
  OpenBrace, CloseBrace, OpenBracket, CloseBracket, Dot, Comma, SemiColon, Colon, DoubleColon,
  Equal, Minus, SlimArrow, Pub, Priv, Mut, Pkg, Class, Enum, Fn, Const, Let, Self, Impl, Null, Return, EOF;

  public static final TokenType[] KEYWORDS = new TokenType[]{Pub, Priv, Mut, Pkg, Class, Enum, Fn,
      Const, Let, Self, Impl, Return, Null};

  public int getInfixPrecedence() {
    switch (this) {
      case Equal:
        return 1;
      default:
        return 0;
    }
  }
}
