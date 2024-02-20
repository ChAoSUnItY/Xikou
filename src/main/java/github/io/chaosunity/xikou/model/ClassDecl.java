package github.io.chaosunity.xikou.model;

import github.io.chaosunity.xikou.lexer.Token;

import java.util.Arrays;

public class ClassDecl {
    public final PackageRef packageRef;
    public final int modifiers;
    public final Token className;
    public final int fieldCount;
    public final FieldDecl[] fieldDecls;
    
    public ClassDecl(PackageRef packageRef, int modifiers, Token className, int fieldCount, FieldDecl[] fieldDecls) {
        this.packageRef = packageRef;
        this.modifiers = modifiers;
        this.className = className;
        this.fieldCount = fieldCount;
        this.fieldDecls = fieldDecls;
    }
    
    public String getInternalName() {
        if (packageRef.qualifiedPath.isEmpty()) {
            return className.literal;
        } else {
            return packageRef.qualifiedPath.replace('.', '/') + "/" + className.literal;
        }
    }

    @Override
    public String toString() {
        return "ClassDecl{" +
                "packageRef=" + packageRef +
                ", modifiers=" + modifiers +
                ", className='" + className + '\'' +
                ", fieldCount=" + fieldCount +
                ", fieldDecls=" + Arrays.toString(fieldDecls) +
                '}';
    }
}
