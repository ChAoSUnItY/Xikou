package github.io.chaosunity.xikou.ast.expr;

import github.io.chaosunity.xikou.resolver.types.Type;

public class MemberAccessExpr extends Expr {
    public final Expr ownerExpr;
    public final VarExpr selectedVarExpr;

    public MemberAccessExpr(Expr ownerExpr, VarExpr selectedVarExpr) {
        this.ownerExpr = ownerExpr;
        this.selectedVarExpr = selectedVarExpr;
    }

    @Override
    public Type getType() {
        return selectedVarExpr.resolvedType;
    }

    @Override
    public boolean isAssignable() {
        // FIXME: Member functions are not assignable
        return true;
    }
}
