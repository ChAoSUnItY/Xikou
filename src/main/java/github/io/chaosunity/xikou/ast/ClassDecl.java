package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.Type;

import java.util.Arrays;

public class ClassDecl {
    public final PackageRef packageRef;
    public final int modifiers;
    public final Token className;
    public final int fieldCount;
    public final FieldDecl[] fieldDecls;
    public ImplDecl boundImplDecl;
    private Type classType;
    
    public ClassDecl(PackageRef packageRef, int modifiers, Token className, int fieldCount, FieldDecl[] fieldDecls) {
        this.packageRef = packageRef;
        this.modifiers = modifiers;
        this.className = className;
        this.fieldCount = fieldCount;
        this.fieldDecls = fieldDecls;
    }
    
    public Type getClassType() {
        if (classType == null) {
            String internalPath;

            if (packageRef.qualifiedPath.isEmpty()) {
                internalPath = className.literal;
            } else {
                internalPath = packageRef.qualifiedPath.replace('.', '/') + "/" + className.literal;
            }

            classType = new ObjectType(internalPath);
        }

        return classType;
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
