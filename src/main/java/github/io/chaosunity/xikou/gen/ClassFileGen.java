package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.ConstDecl;
import github.io.chaosunity.xikou.ast.ConstructorDecl;
import github.io.chaosunity.xikou.ast.FnDecl;
import github.io.chaosunity.xikou.ast.ImplDecl;
import github.io.chaosunity.xikou.ast.expr.ReturnExpr;
import github.io.chaosunity.xikou.ast.stmt.ExprStmt;
import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public abstract class ClassFileGen {

  protected final Path outputFolderPath;
  protected MethodVisitor clinitMw;
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

  protected void genConstructorBody(
      ClassWriter cw, MethodVisitor mw, ConstructorDecl constructorDecl) {
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

    for (int i = 0; i < implDecl.constCount; i++) {
      genConstDecl(cw, implDecl.constDecls[i]);
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

    MethodVisitor mw =
        cw.visitMethod(
            modifiers,
            fnDecl.nameToken.literal,
            Utils.getMethodDescriptor(fnDecl.asMethodRef()),
            null,
            null);

    mw.visitCode();

    for (int i = 0; i < fnDecl.statementCount; i++) {
      stmtGen.genStatement(mw, fnDecl.statements[i]);
    }

    // Implicit return generation
    if (fnDecl.statementCount > 0) {
      Statement lastStatement = fnDecl.statements[fnDecl.statementCount - 1];

      if (lastStatement instanceof ExprStmt) {
        ExprStmt exprStmt = (ExprStmt) lastStatement;

        if (!(exprStmt.expr instanceof ReturnExpr) && fnDecl.returnType == PrimitiveType.VOID) {
          mw.visitInsn(Opcodes.RETURN);
        }
      } else {
        mw.visitInsn(Opcodes.RETURN);
      }
    } else {
      mw.visitInsn(Opcodes.RETURN);
    }

    mw.visitMaxs(-1, -1);
    mw.visitEnd();
  }

  protected abstract MethodVisitor genDefaultPrimaryConstructor(ClassWriter cw);

  protected MethodVisitor genStaticCtor(ClassWriter cw) {
    if (clinitMw == null) {
      clinitMw =
          cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
      clinitMw.visitCode();
    }

    return clinitMw;
  }

  protected void genConstDecl(ClassWriter cw, ConstDecl constDecl) {
    MethodVisitor mw = genStaticCtor(cw);

    cw.visitField(
        constDecl.modifiers | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
        constDecl.nameToken.literal,
        constDecl.resolvedType.getDescriptor(),
        null,
        null);

    exprGen.genExpr(mw, constDecl.initialExpression);

    mw.visitFieldInsn(
        Opcodes.PUTSTATIC,
        constDecl.implDecl.boundDecl.getType().getInternalName(),
        constDecl.nameToken.literal,
        constDecl.resolvedType.getDescriptor());
  }

  protected void finalizeWriters() {
    if (clinitMw == null) {
      return;
    }

    MethodVisitor mw = clinitMw;

    mw.visitInsn(Opcodes.RETURN);
    mw.visitMaxs(-1, -1);
    mw.visitEnd();
  }
}
