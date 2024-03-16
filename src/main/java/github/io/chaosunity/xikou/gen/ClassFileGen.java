package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.ConstructorDecl;
import github.io.chaosunity.xikou.ast.FnDecl;
import github.io.chaosunity.xikou.ast.ImplDecl;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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

  protected abstract void genConstructor(ClassWriter cw);

  protected void genConstructorBody(ClassWriter cw, MethodVisitor mw,
      ConstructorDecl constructorDecl) {
    if (constructorDecl != null) {
      for (int i = 0; i < constructorDecl.statementCount; i++) {
        stmtGen.genStatement(mw, constructorDecl.statements[i]);
      }
    }
  }

  protected void genImplDecl(ClassWriter cw, ImplDecl implDecl) {
    if (implDecl == null) {
      return;
    }

    for (int i = 0; i < implDecl.functionCount; i++) {
      genFunctionDeclAndBody(cw, implDecl.functionDecls[i]);
    }
  }

  protected void genFunctionDeclAndBody(ClassWriter cw, FnDecl fnDecl) {
    int modifiers = fnDecl.fnModifiers;

    if (fnDecl.selfToken == null) {
      modifiers |= Opcodes.ACC_STATIC;
    }

    MethodVisitor mw = cw.visitMethod(modifiers, fnDecl.nameToken.literal, Utils.getMethodDescriptor(fnDecl.asMethodRef()), null, null);

    mw.visitCode();

    for (int i = 0; i < fnDecl.statementCount; i++) {
      stmtGen.genStatement(mw, fnDecl.statements[i]);
    }

    // FIXME: implicit void return hack
    if (fnDecl.returnType == PrimitiveType.VOID) {
      mw.visitInsn(Opcodes.RETURN);
    }

    mw.visitMaxs(-1, -1);
    mw.visitEnd();
  }

  protected abstract MethodVisitor genDefaultPrimaryConstructor(ClassWriter cw);
}
