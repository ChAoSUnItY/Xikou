package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class IntegerLiteral extends Expr {
    public final Token integerToken;

    public IntegerLiteral(Token integerToken) {
        this.integerToken = integerToken;
    }

    public int asConstant() {
        return Integer.parseInt(integerToken.literal);
    }

    @Override
    public AbstractType getType() {
        return PrimitiveType.INT;
    }

    @Override
    public boolean isAssignable() {
        return false;
    }
}
