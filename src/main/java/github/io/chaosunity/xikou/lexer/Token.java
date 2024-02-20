package github.io.chaosunity.xikou.lexer;

public class Token {
    public final TokenType type;
    public final String literal;
    
    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }
    
    public int length() {
        return literal.length();
    }
}
