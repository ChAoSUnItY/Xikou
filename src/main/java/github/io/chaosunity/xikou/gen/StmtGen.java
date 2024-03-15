package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.ast.stmt.VarDeclStmt;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public final class StmtGen {

  private final ExprGen exprGen;

  StmtGen(ExprGen exprGen) {
    this.exprGen = exprGen;
  }

  public void genStatement(MethodVisitor mw, Statement statement, Label blockEndLabel) {
    if (statement instanceof VarDeclStmt) {
      genVarDeclStatement(mw, (VarDeclStmt) statement, blockEndLabel);
    } else if (statement instanceof ExprStmt) {
      genExprStatment(mw, (ExprStmt) statement, blockEndLabel);
    }
  }

  private void genVarDeclStatement(MethodVisitor mw, VarDeclStmt varDeclStmt, Label blockEndLabel) {
    Expr initialExpr = varDeclStmt.initialValue;

    if (initialExpr != null) {
      Label varLivenessStartLabel = new Label();
      int localVarIndex = varDeclStmt.localVarRef.index;
      AbstractType initialValueType = initialExpr.getType();
      exprGen.genExpr(mw, varDeclStmt.initialValue);
      mw.visitVarInsn(Utils.getStoreOpcode(initialValueType), localVarIndex);
      mw.visitLabel(varLivenessStartLabel);
      mw.visitLocalVariable(varDeclStmt.nameToken.literal, initialValueType.getDescriptor(), null,
          varLivenessStartLabel, blockEndLabel, localVarIndex);
    }
  }

  private void genExprStatment(MethodVisitor mw, ExprStmt exprStmt, Label blockEndLabel) {
    exprGen.genExpr(mw, exprStmt.expr);
  }
}
