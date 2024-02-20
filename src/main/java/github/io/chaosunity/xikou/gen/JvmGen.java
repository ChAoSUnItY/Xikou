package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.model.ClassDecl;
import github.io.chaosunity.xikou.model.FieldDecl;
import github.io.chaosunity.xikou.model.XkFile;
import org.objectweb.asm.ClassWriter;
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
        
        for (int i = 0; i < classDecl.fieldCount; i++) {
            FieldDecl fieldDecl = classDecl.fieldDecls[i];
            
            genFieldDecl(cw, fieldDecl);
        }
        
        cw.visitEnd();
    } 
    
    private void genFieldDecl(ClassWriter cw, FieldDecl fieldDecl) {
        cw.visitField(fieldDecl.fieldModifiers, fieldDecl.name.literal, fieldDecl.typeRef.getDescriptor(), null, null);
    }
}
