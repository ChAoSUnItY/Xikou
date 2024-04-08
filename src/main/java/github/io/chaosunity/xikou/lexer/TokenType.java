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
  OpenBracket,
  CloseBracket,
  Dot,
  Comma,
  SemiColon,
  Colon,
  DoubleColon,
  Equal,
  PlusEqual,
  MinusEqual,
  DoubleEqual,
  NotEqual,
  Greater,
  GreaterEqual,
  Lesser,
  LesserEqual,
  DoubleAmpersand,
  DoublePipe,
  Plus,
  Minus,
  SlimArrow,
  Pub,
  Priv,
  Mut,
  Pkg,
  Class,
  Enum,
  Fn,
  Const,
  Let,
  Self,
  Impl,
  Null,
  Return,
  As,
  If,
  Else,
  While,
  EOF;

  public static final TokenType[] KEYWORDS =
      new TokenType[] {
        Pub, Priv, Mut, Pkg, Class, Enum, Fn, Const, Let, Self, Impl, Null, Return, As, If, While,
        Else
      };

  public int getInfixPrecedence() {
    switch (this) {
      case As:
        return 6;
      case Plus:
      case Minus:
        return 5;
      case DoubleEqual:
      case NotEqual:
      case Greater:
      case GreaterEqual:
      case Lesser:
      case LesserEqual:
        return 4;
      case DoubleAmpersand:
        return 3;
      case DoublePipe:
        return 2;
      case Equal:
      case PlusEqual:
      case MinusEqual:
        return 1;
      default:
        return 0;
    }
  }
}
