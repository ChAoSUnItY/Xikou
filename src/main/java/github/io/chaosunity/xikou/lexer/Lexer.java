package github.io.chaosunity.xikou.lexer;

public class Lexer {

  private int pos = 0;
  private final String source;
  private Token currentToken;

  public Lexer(String source) {
    this.source = source;
    this.currentToken = nextToken();
  }

  private static boolean isWhitespace(char ch) {
    return Character.isWhitespace(ch) || ch == '\n' || ch == '\r' || ch == '\t';
  }

  private static boolean isNumeric(char ch) {
    return Character.isDigit(ch);
  }

  private static boolean isIdentStart(char ch) {
    return Character.isAlphabetic(ch) || ch == '_';
  }

  private static boolean isIdent(char ch) {
    return isNumeric(ch) || isIdentStart(ch);
  }

  private void readChar(int offset) {
    pos += offset;
  }

  private char peekChar(int offset) {
    if (offset + pos >= source.length()) {
      return '\0';
    }

    return source.charAt(offset + pos);
  }

  private void skipWhitespace() {
    while (isWhitespace(peekChar(0))) {
      readChar(1);
    }
  }

  public Token nextToken() {
    skipWhitespace();
    char currentChar = peekChar(0);

    if (currentChar == '(') {
      readChar(1);
      return new Token(TokenType.OpenParenthesis, "(");
    }

    if (currentChar == ')') {
      readChar(1);
      return new Token(TokenType.CloseParenthesis, ")");
    }

    if (currentChar == '{') {
      readChar(1);
      return new Token(TokenType.OpenBrace, "{");
    }

    if (currentChar == '}') {
      readChar(1);
      return new Token(TokenType.CloseBrace, "}");
    }

    if (currentChar == '[') {
      readChar(1);
      return new Token(TokenType.OpenBracket, "[");
    }

    if (currentChar == ']') {
      readChar(1);
      return new Token(TokenType.CloseBracket, "]");
    }

    if (currentChar == '.') {
      readChar(1);
      return new Token(TokenType.Dot, ".");
    }

    if (currentChar == ',') {
      readChar(1);
      return new Token(TokenType.Comma, ",");
    }

    if (currentChar == ';') {
      readChar(1);
      return new Token(TokenType.SemiColon, ";");
    }

    if (currentChar == ':') {
      if (peekChar(1) == ':') {
        readChar(2);
        return new Token(TokenType.DoubleColon, "::");
      }

      readChar(1);
      return new Token(TokenType.Colon, ":");
    }

    if (currentChar == '=') {
      readChar(1);
      return new Token(TokenType.Equal, "=");
    }

    if (currentChar == '-') {
      if (peekChar(1) == '>') {
        readChar(2);
        return new Token(TokenType.SlimArrow, "->");
      }

      readChar(1);
      return new Token(TokenType.Minus, "-");
    }

    if (currentChar == '\'') {
      currentChar = peekChar(1);

      if (currentChar == '\\') {
        // Escaped character
        switch (currentChar = peekChar(2)) {
          case 't':
            currentChar = '\t';
            break;
          case 'b':
            currentChar = '\b';
            break;
          case 'n':
            currentChar = '\n';
            break;
          case 'r':
            currentChar = '\r';
            break;
          case 'f':
            currentChar = '\f';
            break;
          case '\'':
          case '"':
          case '\\':
            break;
          default:
            throw new IllegalStateException(
                String.format("Character %c is not escapable", currentChar));
        }

        readChar(3);
      } else {
        readChar(2);
      }

      if (peekChar(0) != '\'') {
        throw new IllegalStateException("Unenclosed character");
      }

      readChar(1);

      return new Token(TokenType.CharLiteral, String.valueOf(currentChar));
    }

    if (currentChar == '"') {
      StringBuilder builder = new StringBuilder();
      int length = 0;

      readChar(1);

      while ((currentChar = peekChar(length)) != '"' && currentChar != '\0') {
        if (currentChar == '\\') {
          // Escaped character
          switch (currentChar = peekChar(length + 1)) {
            case 't':
              currentChar = '\t';
              break;
            case 'b':
              currentChar = '\b';
              break;
            case 'n':
              currentChar = '\n';
              break;
            case 'r':
              currentChar = '\r';
              break;
            case 'f':
              currentChar = '\f';
              break;
            case '\'':
            case '"':
            case '\\':
              break;
            default:
              throw new IllegalStateException(
                  String.format("Character %c is not escapable", currentChar));
          }

          builder.append(currentChar);
          length += 2;
        } else {
          builder.append(currentChar);
          length++;
        }
      }

      if (currentChar != '"') {
        throw new IllegalStateException("Unenclosed string");
      }

      readChar(length + 1);

      return new Token(TokenType.StringLiteral, builder.toString());
    }

    if (isIdentStart(currentChar)) {
      int length = 1;

      while (isIdent(peekChar(length))) {
        length++;
      }

      String identifier = source.substring(pos, pos + length);
      readChar(length);

      for (TokenType type : TokenType.KEYWORDS) {
        String keyword = type.toString().toLowerCase();

        if (identifier.equals(keyword)) {
          return new Token(type, keyword);
        }
      }

      return new Token(TokenType.Identifier, identifier);
    }

    if (isNumeric(currentChar)) {
      int length = 1;

      while (isNumeric(peekChar(length))) {
        length++;
      }

      Token token = new Token(TokenType.NumberLiteral, source.substring(pos, pos + length));
      readChar(length);
      return token;
    }

    if (currentChar == '\0') {
      readChar(1);
      return new Token(TokenType.EOF, "\0");
    }

    throw new IllegalStateException("Unexpected character");
  }

  public Token advanceToken() {
    Token token = currentToken;
    currentToken = nextToken();
    return token;
  }

  public Token expectToken(TokenType type) {
    if (currentToken.type != type) {
      throw new IllegalStateException(
          String.format("Unexpected token %s, expects %s", currentToken.type, type));
    }

    Token currentToken = getCurrentToken();
    advanceToken();
    return currentToken;
  }

  public boolean peekToken(TokenType type) {
    return currentToken.type == type;
  }

  public boolean acceptToken(TokenType type) {
    if (currentToken.type == type) {
      advanceToken();
      return true;
    }

    return false;
  }

  public int getPos() {
    return pos;
  }

  public Token getCurrentToken() {
    return currentToken;
  }

  public void rewindPos(int pos, Token backupToken) {
    this.pos = pos;
    currentToken = backupToken;
  }
}
