package github.io.chaosunity.xikou.ast.expr;

public interface InfixExpr extends Expr {

  Expr getLhs();

  Expr getRhs();
}
