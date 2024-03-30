package github.io.chaosunity.xikou.lexer;

public enum TokenType {
  CharLiteral, StringLiteral, NumberLiteral, Identifier, OpenParenthesis, CloseParenthesis,
  OpenBrace, CloseBrace, OpenBracket, CloseBracket, Dot, Comma, SemiColon, Colon, DoubleColon,
  Equal, DoubleEqual, NotEqual, DoubleAmpersand, DoublePipe, Plus, Minus, SlimArrow, Pub, Priv,
  Mut, Pkg, Class, Enum, Fn, Const, Let, Self, Impl, Null, Return, As, EOF;

  public static final TokenType[] KEYWORDS = new TokenType[]{Pub, Priv, Mut, Pkg, Class, Enum, Fn,
      Const, Let, Self, Impl, Return, As, Null};

  public int getInfixPrecedence() {
    switch (this) {
      case As:
        return 6;
      case Plus:
      case Minus:
        return 5;
      case DoubleEqual:
      case NotEqual:
        return 4;
      case DoubleAmpersand:
        return 3;
      case DoublePipe:
        return 2;
      case Equal:
        return 1;
      default:
        return 0;
    }
  }
}
