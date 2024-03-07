package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.Type;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.file.Path;

public class EnumGen extends ClassFileGen {
    private static final String VALUES_FIELD_NAME = "$VALUES";

    private final EnumDecl enumDecl;

    public EnumGen(Path outputFolderPath, EnumDecl enumDecl) {
        super(outputFolderPath);
        this.enumDecl = enumDecl;
    }

    @Override
    protected byte[] genClassFileBytes() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, enumDecl.modifiers | Opcodes.ACC_ENUM, enumDecl.getType().getInternalName(), String.format("Ljava/lang/Enum<%s>;", enumDecl.getType().getDescriptor()), "java/lang/Enum", null);

        genStaticInit(cw);
        genValueOf(cw);
        genValues(cw);
        genPrimaryConstructor(cw);
        genFields(cw);

        cw.visitEnd();
        return cw.toByteArray();
    }

    @Override
    protected Path getClassFilePath() {
        return outputFolderPath.resolve(enumDecl.getNameToken().literal + ".class");
    }

    @Override
    protected void genPrimaryConstructor(ClassWriter cw) {
        ImplDecl implDecl = enumDecl.getImplDecl();
        PrimaryConstructorDecl constructorDecl = implDecl != null ? implDecl.primaryConstructorDecl : null;
        MethodVisitor mw;

        if (constructorDecl != null) {
            Parameters parameters = constructorDecl.parameters;
            int parameterCount = parameters.parameterCount;
            Type[] parameterTypes = new Type[parameterCount + 2];

            for (int i = 0; i < parameterCount; i++) {
                parameterTypes[i] = parameters.parameters[i].typeRef.getType();
            }

            System.arraycopy(new Type[]{new ObjectType("java/lang/String"), PrimitiveType.INT}, 0, parameterTypes, parameterCount, 2);

            mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Utils.getMethodDescriptor(PrimitiveType.VOID, parameterTypes), null, null);

            mw.visitCode();
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitVarInsn(Opcodes.ALOAD, parameterCount + 1);
            mw.visitVarInsn(Opcodes.ILOAD, parameterCount + 2);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>", Utils.getMethodDescriptor(PrimitiveType.VOID, new Type[]{new ObjectType("java/lang/String"), PrimitiveType.INT}), false);
        } else {
            mw = genDefaultPrimaryConstructor(cw);
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
        String descriptor = Utils.getMethodDescriptor(PrimitiveType.VOID, new Type[]{new ObjectType("java/lang/String"), PrimitiveType.INT});
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);

        mw.visitCode();
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitVarInsn(Opcodes.ALOAD, 1);
        mw.visitVarInsn(Opcodes.ILOAD, 2);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>", descriptor, false);

        return mw;
    }

    private void genFields(ClassWriter cw) {
        Type enumType = enumDecl.getType();

        for (int i = 0; i < enumDecl.variantCount; i++) {
            EnumVariantDecl variantDecl = enumDecl.enumVariantDecls[i];

            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM, variantDecl.name.literal, enumType.getDescriptor(), null, null);
        }

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, VALUES_FIELD_NAME, enumType.asArrayType().getDescriptor(), null, null);

        for (int i = 0; i < enumDecl.fieldCount; i++) {
            FieldDecl fieldDecl = enumDecl.fieldDecls[i];

            cw.visitField(fieldDecl.fieldModifiers, fieldDecl.name.literal, fieldDecl.typeRef.getType().getDescriptor(), null, null);
        }
    }

    private void genStaticInit(ClassWriter cw) {
        Type enumType = enumDecl.getType();
        ImplDecl implDecl = enumDecl.getImplDecl();
        PrimaryConstructorDecl constructorDecl = implDecl != null ? implDecl.primaryConstructorDecl : null;
        Type[] constructorParamameterTypes = new Type[2 + (constructorDecl != null ? constructorDecl.parameters.parameterCount : 0)];

        for (int i = 0; i < constructorParamameterTypes.length - 2; i++) {
            constructorParamameterTypes[i] = constructorDecl.parameters.parameters[i].typeRef.getType();
        }

        System.arraycopy(new Type[]{new ObjectType("java/lang/String"), PrimitiveType.INT}, 0, constructorParamameterTypes, constructorParamameterTypes.length - 2, 2);

        String constructorDescriptor = Utils.getMethodDescriptor(PrimitiveType.VOID, constructorParamameterTypes);
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mw.visitCode();

        for (int i = 0; i < enumDecl.variantCount; i++) {
            EnumVariantDecl variantDecl = enumDecl.enumVariantDecls[i];

            mw.visitTypeInsn(Opcodes.NEW, enumType.getInternalName());
            mw.visitInsn(Opcodes.DUP);

            for (int j = 0; j < variantDecl.argumentCount; j++) {
                exprGen.genExpr(mw, variantDecl.arguments[j]);
            }

            mw.visitLdcInsn(variantDecl.name.literal);
            mw.visitLdcInsn(i);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, enumType.getInternalName(), "<init>", constructorDescriptor, false);
            mw.visitFieldInsn(Opcodes.PUTSTATIC, enumType.getInternalName(), variantDecl.name.literal, enumType.getDescriptor());
        }

        // Generate $VALUES
        mw.visitLdcInsn(enumDecl.variantCount);
        mw.visitTypeInsn(Opcodes.ANEWARRAY, enumType.getInternalName());

        for (int i = 0; i < enumDecl.variantCount; i++) {
            EnumVariantDecl variantDecl = enumDecl.enumVariantDecls[i];

            mw.visitInsn(Opcodes.DUP);
            mw.visitLdcInsn(i);
            mw.visitFieldInsn(Opcodes.GETSTATIC, enumType.getInternalName(), variantDecl.name.literal, enumType.getDescriptor());
            mw.visitInsn(Opcodes.AASTORE);
        }

        mw.visitFieldInsn(Opcodes.PUTSTATIC, enumType.getInternalName(), VALUES_FIELD_NAME, enumType.asArrayType().getDescriptor());

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(-1, -1);
        mw.visitEnd();
    }

    private void genValueOf(ClassWriter cw) {
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "valueOf", Utils.getMethodDescriptor(enumDecl.getType(), new Type[]{ new ObjectType("java/lang/String") }), null, null);
        mw.visitCode();
        mw.visitLdcInsn(org.objectweb.asm.Type.getType(enumDecl.getType().getDescriptor()));
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Enum", "valueOf", Utils.getMethodDescriptor(new ObjectType("java/lang/Enum"), new Type[]{ new ObjectType("java/lang/Object"), new ObjectType("java/lang/String") }), false);
        mw.visitTypeInsn(Opcodes.CHECKCAST, enumDecl.getType().getInternalName());
        mw.visitInsn(Opcodes.ARETURN);
        mw.visitMaxs(-1, -1);
        mw.visitEnd();
    }

    private void genValues(ClassWriter cw) {
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "values", Utils.getMethodDescriptor(enumDecl.getType().asArrayType(), new Type[0]), null, null);
        mw.visitCode();
        mw.visitFieldInsn(Opcodes.GETSTATIC, enumDecl.getType().getInternalName(), VALUES_FIELD_NAME, enumDecl.getType().asArrayType().getDescriptor());
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, enumDecl.getType().asArrayType().getDescriptor(), "clone", Utils.getMethodDescriptor(new ObjectType("java/lang/Object"), new Type[0]), false);
        mw.visitTypeInsn(Opcodes.CHECKCAST, enumDecl.getType().asArrayType().getDescriptor());
        mw.visitInsn(Opcodes.ARETURN);
        mw.visitMaxs(-1, -1);
        mw.visitEnd();
    }
}
