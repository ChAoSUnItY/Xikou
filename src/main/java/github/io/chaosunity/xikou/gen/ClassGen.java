package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.Type;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
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
        cw.visit(Opcodes.V1_8, classDecl.modifiers, classDecl.getType().getInternalName(), null, "java/lang/Object", null);

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
        return outputFolderPath.resolve(classDecl.getNameToken().literal + ".class");
    }

    @Override
    protected void genPrimaryConstructor(ClassWriter cw) {
        ImplDecl implDecl = classDecl.getImplDecl();
        PrimaryConstructorDecl constructorDecl = implDecl != null ? implDecl.primaryConstructorDecl : null;
        MethodVisitor mw;

        if (constructorDecl != null) {
            Parameters parameters = constructorDecl.parameters;
            int parameterCount = parameters.parameterCount;
            Type[] parameterTypes = new Type[parameterCount];

            for (int i = 0; i < parameterCount; i++) {
                parameterTypes[i] = parameters.parameters[i].typeRef.getType();
            }

            mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Utils.getMethodDescriptor(PrimitiveType.VOID, parameterTypes), null, null);

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
                mw.visitFieldInsn(Opcodes.PUTFIELD, classDecl.getType().getInternalName(), fieldDecl.name.literal, fieldDecl.typeRef.getType().getDescriptor());
            }
        }

        if (constructorDecl != null) {
            for (int i = 0; i < constructorDecl.exprCount; i++) {
                exprGen.genExpr(mw, constructorDecl.exprs[i]);
            }
        }

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
        cw.visitField(fieldDecl.fieldModifiers, fieldDecl.name.literal, fieldDecl.typeRef.getType().getDescriptor(), null, null);
    }
}
