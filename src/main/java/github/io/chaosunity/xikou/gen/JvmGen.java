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

    private void init() {
        if (Files.exists(outputFolder)) {
            for (File file : Objects.requireNonNull(outputFolder.toFile().listFiles())) {
                file.delete();
            }
        } else {
            try {
                Files.createDirectory(outputFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void gen() {
        init();

        for (int i = 0; i < file.declCount; i++) {
            BoundableDecl decl = file.decls[i];

            if (decl instanceof ClassDecl) {
                ClassGen classGen = new ClassGen(outputFolder, (ClassDecl) decl);

                classGen.genClassFile();
            } else if (decl instanceof EnumDecl) {
                EnumGen enumGen = new EnumGen(outputFolder, (EnumDecl) decl);

                enumGen.genClassFile();
            }
        }
    }
}
