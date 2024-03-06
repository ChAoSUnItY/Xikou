package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.LocalVarRef;
import github.io.chaosunity.xikou.resolver.types.Type;

public class VarExpr extends Expr {
    public final Token varIdentifier;
    public Type resolvedType;
    public LocalVarRef localVarRef;

    public VarExpr(Token varIdentifier) {
        this.varIdentifier = varIdentifier;
    }

    @Override
    public Type getType() {
        return resolvedType;
    }

    @Override
    public boolean isAssignable() {
        return true;
    }
}
