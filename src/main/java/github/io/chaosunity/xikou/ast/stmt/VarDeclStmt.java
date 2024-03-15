package github.io.chaosunity.xikou.ast.stmt;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.LocalVarRef;

public class VarDeclStmt implements Statement {

  public final Token mutToken;
  public final Token nameToken;
  public final Expr initialValue;
  public LocalVarRef localVarRef;

  public VarDeclStmt(Token mutToken, Token nameToken, Expr initialValue) {
    this.mutToken = mutToken;
    this.nameToken = nameToken;
    this.initialValue = initialValue;
  }
}
