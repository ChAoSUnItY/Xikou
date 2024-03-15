package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.ConstructorDecl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public abstract class ClassFileGen {

  protected final Path outputFolderPath;
  protected final ExprGen exprGen = new ExprGen();
  protected final StmtGen stmtGen = new StmtGen(exprGen);

  protected ClassFileGen(Path outputFolderPath) {
    this.outputFolderPath = outputFolderPath;
  }

  public final void genClassFile() {
    try {
      Files.copy(new ByteArrayInputStream(genClassFileBytes()), getClassFilePath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract byte[] genClassFileBytes();

  protected abstract Path getClassFilePath();

  protected abstract void genPrimaryConstructor(ClassWriter cw);

  protected void genPrimaryConstrcutorBody(ClassWriter cw, MethodVisitor mw,
      ConstructorDecl constructorDecl) {
    if (constructorDecl != null) {
      Label constructorBodyEnd = new Label();

      for (int i = 0; i < constructorDecl.statementCount; i++) {
        stmtGen.genStatement(mw, constructorDecl.statements[i], constructorBodyEnd);
      }

      mw.visitLabel(constructorBodyEnd);
    }
  }

  protected abstract MethodVisitor genDefaultPrimaryConstructor(ClassWriter cw);
}
