package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.resolver.LocalVarRef;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.Type;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class JvmGen {
    private final Path outputFolder;
    private final XkFile file;

    public JvmGen(Path outputFolder, XkFile file) {
        this.outputFolder = outputFolder;
        this.file = file;
    }

    private void init() throws IOException {
        if (Files.exists(outputFolder)) {
            for (File file : Objects.requireNonNull(outputFolder.toFile().listFiles())) {
                file.delete();
            }
        } else {
            Files.createDirectory(outputFolder);
        }
    }

    public void gen() throws IOException {
        init();

        for (int i = 0; i < file.declCount; i++) {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS); // Must be non-null
            BoundableDecl decl = file.decls[i];

            if (decl instanceof ClassDecl) {
                genClassDecl(cw, (ClassDecl) decl);
            }

            Path targetFilePath = outputFolder.resolve(decl.getNameToken().literal + ".class");
            Files.copy(new ByteArrayInputStream(cw.toByteArray()), targetFilePath);
        }
    }

    private void genClassDecl(ClassWriter cw, ClassDecl classDecl) {
        cw.visit(Opcodes.V1_8, classDecl.modifiers, classDecl.getType().getInternalName(), null, "java/lang/Object", null);
        MethodVisitor primaryConstructorMw = genPrimaryConstructor(cw, classDecl);

        for (int i = 0; i < classDecl.fieldCount; i++) {
            FieldDecl fieldDecl = classDecl.fieldDecls[i];

            genFieldDecl(cw, fieldDecl);
        }

        primaryConstructorMw.visitInsn(Opcodes.RETURN);
        primaryConstructorMw.visitEnd();

        cw.visitEnd();
    }

    private MethodVisitor genDefaultPrimaryConstructor(ClassWriter cw, ClassDecl classDecl) {
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);

        mw.visitCode();
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        return mw;
    }

    private MethodVisitor genPrimaryConstructor(ClassWriter cw, ClassDecl classDecl) {
        PrimaryConstructorDecl primaryConstructorDecl = classDecl.boundImplDecl.primaryConstructorDecl;
        MethodVisitor mw;

        if (primaryConstructorDecl != null) {
            mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Utils.getMethodDescriptor(PrimitiveType.VOID, primaryConstructorDecl.parameters), null, null);

            mw.visitCode();
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        } else {
            mw = genDefaultPrimaryConstructor(cw, classDecl);
        }

        for (int i = 0; i < classDecl.fieldCount; i++) {
            FieldDecl fieldDecl = classDecl.fieldDecls[i];

            if (fieldDecl.initialExpr != null) {
                mw.visitVarInsn(Opcodes.ALOAD, 0);
                genExpr(mw, fieldDecl.initialExpr);
                mw.visitFieldInsn(Opcodes.PUTFIELD, classDecl.getType().getInternalName(), fieldDecl.name.literal, fieldDecl.typeRef.getType().getDescriptor());
            }
        }

        if (primaryConstructorDecl != null) {
            for (int i = 0; i < primaryConstructorDecl.exprCount; i++) {
                genExpr(mw, primaryConstructorDecl.exprs[i]);
            }
        }

        return mw;
    }

    private void genFieldDecl(ClassWriter cw, FieldDecl fieldDecl) {
        cw.visitField(fieldDecl.fieldModifiers, fieldDecl.name.literal, fieldDecl.typeRef.getType().getDescriptor(), null, null);
    }

    private void genExpr(MethodVisitor mw, Expr expr) {
        if (expr instanceof IntegerLiteral) {
            genIntegerLiteral(mw, (IntegerLiteral) expr);
        } else if (expr instanceof InfixExpr) {
            InfixExpr infixExpr = (InfixExpr) expr;
            Expr lhs = infixExpr.lhs, rhs = infixExpr.rhs;
            TokenType operatorType = infixExpr.operator.type;

            switch (operatorType) {
                case Equal:
                    if (lhs instanceof MemberAccessExpr) {
                        MemberAccessExpr memberAccessLhs = (MemberAccessExpr) lhs;

                        genExpr(mw, memberAccessLhs.ownerExpr);
                        genExpr(mw, rhs);

                        mw.visitFieldInsn(Opcodes.PUTFIELD, memberAccessLhs.ownerExpr.getType().getInternalName(), memberAccessLhs.selectedVarExpr.varIdentifier.literal, memberAccessLhs.selectedVarExpr.getType().getInternalName());
                    }
                    break;
                default:
                    throw new IllegalStateException(String.format("Token %s is not an valid infix operator", operatorType));
            }
        } else if (expr instanceof MemberAccessExpr) {
            MemberAccessExpr memberAccessExpr = (MemberAccessExpr) expr;

            genExpr(mw, memberAccessExpr.ownerExpr);
            genExpr(mw, memberAccessExpr.selectedVarExpr);
        } else if (expr instanceof VarExpr) {
            VarExpr varExpr = (VarExpr) expr;
            LocalVarRef localVarRef = varExpr.localVarRef;
            Type varType = varExpr.getType();

            if (varType instanceof PrimitiveType) {
                int loadOpcode = getPrimitiveLoadOpcode((PrimitiveType) varType);

                mw.visitVarInsn(loadOpcode, localVarRef.index);
            } else if (varType instanceof ObjectType) {
                ObjectType objectType = (ObjectType) varType;

                mw.visitVarInsn(Opcodes.ALOAD, localVarRef.index);
            }
        }
    }

    private static int getPrimitiveLoadOpcode(PrimitiveType varType) {
        int loadOpcode = 0;

        if (varType.equals(PrimitiveType.CHAR) ||
            varType.equals(PrimitiveType.BOOL) ||
            varType.equals(PrimitiveType.INT)) {
            loadOpcode = Opcodes.ILOAD;
        } else if (varType.equals(PrimitiveType.LONG)) {
            loadOpcode = Opcodes.LLOAD;
        } else if (varType.equals(PrimitiveType.DOUBLE)) {
            loadOpcode = Opcodes.DLOAD;
        }
        return loadOpcode;
    }

    private void genIntegerLiteral(MethodVisitor mw, IntegerLiteral integerLiteral) {
        mw.visitLdcInsn(integerLiteral.asConstant());
    }
}
