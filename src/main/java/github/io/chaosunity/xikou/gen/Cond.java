package github.io.chaosunity.xikou.gen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class Cond {

  public final Label trueLabel = new Label();
  public final Label falseLabel = new Label();

  void genJumpTargets(MethodVisitor mw) {
    Label endLabel = new Label();

    mw.visitLabel(trueLabel);
    mw.visitLdcInsn(1);
    mw.visitJumpInsn(Opcodes.GOTO, endLabel);
    mw.visitLabel(falseLabel);
    mw.visitLdcInsn(0);
    mw.visitLabel(endLabel);
  }
}
