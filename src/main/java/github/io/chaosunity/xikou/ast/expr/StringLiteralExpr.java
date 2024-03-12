package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class StringLiteralExpr extends Expr {
    public final Token stringLiteralToken;

    public StringLiteralExpr(Token stringLiteralToken) {
        this.stringLiteralToken = stringLiteralToken;
    }

    @Override
    public AbstractType getType() {
        return new ClassType("java/lang/String");
    }

    @Override
    public boolean isAssignable() {
        return false;
    }
}