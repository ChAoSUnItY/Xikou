package github.io.chaosunity.xikou.gen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ClassFileGen {
    protected final Path outputFolderPath;
    protected final ExprGen exprGen = new ExprGen();

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

    protected abstract MethodVisitor genDefaultPrimaryConstructor(ClassWriter cw);
}
