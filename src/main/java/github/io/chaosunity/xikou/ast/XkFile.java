package github.io.chaosunity.xikou.ast;

import java.nio.file.Path;
import java.util.Arrays;

public class XkFile {
    public final Path absoluteFilePath;
    public final PackageRef packageRef;
    public final int classCount;
    public final ClassDecl[] classDecls;
    
    public XkFile(Path absoluteFilePath, PackageRef packageRef, int classCount, ClassDecl[] classDecls) {
        this.absoluteFilePath = absoluteFilePath;
        this.packageRef = packageRef;
        this.classCount = classCount;
        this.classDecls = classDecls;
    }

    @Override
    public String toString() {
        return "XkFile{" +
                "absoluteFilePath=" + absoluteFilePath +
                ", packageRef=" + packageRef +
                ", classCount=" + classCount +
                ", classDecls=" + Arrays.toString(classDecls) +
                '}';
    }
}
