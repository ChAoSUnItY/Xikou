package github.io.chaosunity.xikou.model.expr;

import github.io.chaosunity.xikou.lexer.Token;

public class IntegerLiteral extends Expr {
    public final Token integerToken;
    
    public IntegerLiteral(Token integerToken) {
        this.integerToken = integerToken;
    }
    
    public int asConstant() {
        return Integer.parseInt(integerToken.literal);
    }
}
