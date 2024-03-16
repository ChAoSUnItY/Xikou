package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.ClassDecl;
import github.io.chaosunity.xikou.ast.ConstructorDecl;
import github.io.chaosunity.xikou.ast.FieldDecl;
import github.io.chaosunity.xikou.ast.ImplDecl;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import java.nio.file.Path;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassGen extends ClassFileGen {

  private final ClassDecl classDecl;

  public ClassGen(Path outputFolderPath, ClassDecl classDecl) {
    super(outputFolderPath);
    this.classDecl = classDecl;
  }

  @Override
  protected byte[] genClassFileBytes() {
    ClassType[] interfaceTypes = classDecl.getInterfaceTypes();
    String[] interfaceInternalNames = new String[interfaceTypes.length];

    for (int i = 0; i < interfaceTypes.length; i++) {
      interfaceInternalNames[i] = interfaceTypes[i].getInternalName();
    }

    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cw.visit(Opcodes.V1_8, classDecl.modifiers, classDecl.getType().getInternalName(), null,
        classDecl.getSuperclassType().getInternalName(), interfaceInternalNames);

    genConstructor(cw);

    for (int i = 0; i < classDecl.fieldCount; i++) {
      FieldDecl fieldDecl = classDecl.fieldDecls[i];

      genFieldDecl(cw, fieldDecl);
    }

    genImplDecl(cw, classDecl.getImplDecl());

    cw.visitEnd();
    return cw.toByteArray();
  }

  @Override
  protected Path getClassFilePath() {
    return outputFolderPath.resolve(classDecl.getName() + ".class");
  }

  @Override
  protected void genConstructor(ClassWriter cw) {
    ConstructorDecl constructorDecl = classDecl.getConstructorDecl();
    MethodVisitor mw;

    if (constructorDecl != null) {
      int parameterCount = constructorDecl.parameterCount;
      AbstractType[] parameterTypes = new AbstractType[parameterCount];

      for (int i = 0; i < parameterCount; i++) {
        parameterTypes[i] = constructorDecl.parameters[i].typeRef.getType();
      }

      mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
          Utils.getMethodDescriptor(PrimitiveType.VOID, parameterTypes), null,
          null);

      for (int i = 0; i < parameterCount; i++) {
        mw.visitParameter(constructorDecl.parameters[i].name.literal, 0);
      }

      mw.visitCode();
      mw.visitVarInsn(Opcodes.ALOAD, 0);
      mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
          classDecl.isInterface());
    } else {
      mw = genDefaultPrimaryConstructor(cw);
    }

    for (int i = 0; i < classDecl.fieldCount; i++) {
      FieldDecl fieldDecl = classDecl.fieldDecls[i];

      if (fieldDecl.initialExpr != null) {
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        exprGen.genExpr(mw, fieldDecl.initialExpr);
        mw.visitFieldInsn(Opcodes.PUTFIELD, classDecl.getType().getInternalName(),
            fieldDecl.name.literal,
            fieldDecl.typeRef.getType().getDescriptor());
      }
    }

    genConstructorBody(cw, mw, constructorDecl);

    mw.visitInsn(Opcodes.RETURN);
    mw.visitMaxs(-1, -1);
    mw.visitEnd();
  }

  @Override
  protected MethodVisitor genDefaultPrimaryConstructor(ClassWriter cw) {
    MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);

    mw.visitCode();
    mw.visitVarInsn(Opcodes.ALOAD, 0);
    mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

    return mw;
  }

  private void genFieldDecl(ClassWriter cw, FieldDecl fieldDecl) {
    cw.visitField(fieldDecl.fieldModifiers, fieldDecl.name.literal,
        fieldDecl.typeRef.getType().getDescriptor(), null, null);
  }
}
