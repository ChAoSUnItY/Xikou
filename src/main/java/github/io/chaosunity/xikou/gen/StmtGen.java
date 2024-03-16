package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.ast.stmt.VarDeclStmt;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import org.objectweb.asm.MethodVisitor;

public final class StmtGen {

  private final ExprGen exprGen;

  StmtGen(ExprGen exprGen) {
    this.exprGen = exprGen;
  }

  public void genStatement(MethodVisitor mw, Statement statement) {
    if (statement instanceof VarDeclStmt) {
      genVarDeclStatement(mw, (VarDeclStmt) statement);
    } else if (statement instanceof ExprStmt) {
      genExprStatment(mw, (ExprStmt) statement);
    }
  }

  private void genVarDeclStatement(MethodVisitor mw, VarDeclStmt varDeclStmt) {
    Expr initialExpr = varDeclStmt.initialValue;

    if (initialExpr != null) {
      int localVarIndex = varDeclStmt.localVarRef.index;
      AbstractType initialValueType = initialExpr.getType();
      exprGen.genExpr(mw, varDeclStmt.initialValue);
      mw.visitVarInsn(Utils.getStoreOpcode(initialValueType), localVarIndex);
    }
  }

  private void genExprStatment(MethodVisitor mw, ExprStmt exprStmt) {
    exprGen.genExpr(mw, exprStmt.expr);
  }
}
