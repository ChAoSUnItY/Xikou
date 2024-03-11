package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.expr.Expr;

/**
 * Intermediate structure for parsing purpose, not directly stored in AST.
 */
public class Arguments {
    public final int argumentCount;
    public final Expr[] arguments;

    public Arguments(int argumentCount, Expr[] arguments) {
        this.argumentCount = argumentCount;
        this.arguments = arguments;
    }
}
