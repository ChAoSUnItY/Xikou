package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.model.*;
import github.io.chaosunity.xikou.model.expr.Expr;
import github.io.chaosunity.xikou.model.expr.IntegerLiteral;
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
        
        for (int i = 0; i < file.classCount; i++) {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassDecl classDecl = file.classDecls[i];
            
            genClassDecl(cw, classDecl);

            Path targetFilePath = outputFolder.resolve(classDecl.className.literal + ".class");
            Files.copy(new ByteArrayInputStream(cw.toByteArray()), targetFilePath);
        }
    }
    
    private void genClassDecl(ClassWriter cw, ClassDecl classDecl) {
        cw.visit(Opcodes.V1_8, classDecl.modifiers, classDecl.getInternalName(), null, "java/lang/Object", null);
        MethodVisitor primaryConstructorMw = genPrimaryConstructor(cw, null, classDecl);
        
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
    
    private MethodVisitor genPrimaryConstructor(ClassWriter cw, MethodVisitor mw, ClassDecl classDecl) {
        if (mw == null) mw = genDefaultPrimaryConstructor(cw, classDecl);
        
        for (int i = 0; i < classDecl.fieldCount; i++) {
            FieldDecl fieldDecl = classDecl.fieldDecls[i];
            
            if (fieldDecl.initialExpr != null) {
                mw.visitVarInsn(Opcodes.ALOAD, 0);
                genExpr(mw, fieldDecl.initialExpr);
                mw.visitFieldInsn(Opcodes.PUTFIELD, classDecl.getInternalName(), fieldDecl.name.literal, fieldDecl.typeRef.getDescriptor());
            }
        }
        
        return mw;
    }
    
    private void genFieldDecl(ClassWriter cw, FieldDecl fieldDecl) {
        cw.visitField(fieldDecl.fieldModifiers, fieldDecl.name.literal, fieldDecl.typeRef.getDescriptor(), null, null);
    }
    
    private void genExpr(MethodVisitor mw, Expr expr) {
        if (expr instanceof IntegerLiteral) {
            genIntegerLiteral(mw, (IntegerLiteral) expr);
        }
    }
    
    private void genIntegerLiteral(MethodVisitor mw, IntegerLiteral integerLiteral) {
        mw.visitLdcInsn(integerLiteral.asConstant());
    }
}
