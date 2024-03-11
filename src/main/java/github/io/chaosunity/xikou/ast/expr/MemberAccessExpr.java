package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class MemberAccessExpr extends Expr {
    public final Expr ownerExpr;
    public final Token targetMember;
    public FieldRef fieldRef;

    public MemberAccessExpr(Expr ownerExpr, Token targetMember) {
        this.ownerExpr = ownerExpr;
        this.targetMember = targetMember;
    }

    @Override
    public AbstractType getType() {
        return fieldRef.fieldType;
    }

    @Override
    public boolean isAssignable() {
        // FIXME: Member functions are not assignable
        return true;
    }
}
