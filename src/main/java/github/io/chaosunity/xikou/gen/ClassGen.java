package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.ClassDecl;
import github.io.chaosunity.xikou.ast.FieldDecl;
import github.io.chaosunity.xikou.ast.PrimaryConstructorDecl;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.file.Path;

public class ClassGen extends ClassFileGen {
    private final ClassDecl classDecl;

    public ClassGen(Path outputFolderPath, ClassDecl classDecl) {
        super(outputFolderPath);
        this.classDecl = classDecl;
    }

    @Override
    protected byte[] genClassFileBytes() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, classDecl.modifiers, classDecl.getType().getInternalName(), null,
                 "java/lang/Object", null);

        genPrimaryConstructor(cw);

        for (int i = 0; i < classDecl.fieldCount; i++) {
            FieldDecl fieldDecl = classDecl.fieldDecls[i];

            genFieldDecl(cw, fieldDecl);
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    @Override
    protected Path getClassFilePath() {
        return outputFolderPath.resolve(classDecl.getName() + ".class");
    }

    @Override
    protected void genPrimaryConstructor(ClassWriter cw) {
        PrimaryConstructorDecl constructorDecl = classDecl.getPrimaryConstructorDecl();
        MethodVisitor mw;

        if (constructorDecl != null) {
            int parameterCount = constructorDecl.parameterCount;
            AbstractType[] parameterTypes = new AbstractType[parameterCount];

            for (int i = 0; i < parameterCount; i++)
                parameterTypes[i] = constructorDecl.parameters[i].typeRef.getType();

            mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                                Utils.getMethodDescriptor(PrimitiveType.VOID, parameterTypes), null,
                                null);

            for (int i = 0; i < parameterCount; i++) {
                mw.visitParameter(constructorDecl.parameters[i].name.literal, 0);
            }

            mw.visitCode();
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
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

        genPrimaryConstrcutorBody(cw, mw, constructorDecl);

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
