package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.ast.stmt.VarDeclStmt;

public final class StmtResolver {

  private final SymbolTable table;
  private final ExprResolver exprResolver;
  private final TypeResolver typeResolver;

  public StmtResolver(SymbolTable table, ExprResolver exprResolver, TypeResolver typeResolver) {
    this.table = table;
    this.exprResolver = exprResolver;
    this.typeResolver = typeResolver;
  }

  public void resolveStatment(Statement statement, Scope scope) {
    if (statement instanceof VarDeclStmt) {
      resolveVarDeclStatement((VarDeclStmt) statement, scope);
    } else if (statement instanceof ExprStmt) {
      resolveExprStatement((ExprStmt) statement, scope);
    }
  }

  private void resolveVarDeclStatement(VarDeclStmt varDeclStmt, Scope scope) {
    if (varDeclStmt.initialValue != null) {
      exprResolver.resolveExpr(varDeclStmt.initialValue, scope);
      varDeclStmt.localVarRef = scope.addLocalVar(varDeclStmt.nameToken.literal,
          varDeclStmt.mutToken != null, varDeclStmt.initialValue.getType());
    }
  }

  private void resolveExprStatement(ExprStmt exprStmt, Scope scope) {
    exprResolver.resolveExpr(exprStmt.expr, scope);
  }
}
