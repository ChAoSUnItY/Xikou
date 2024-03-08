package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.Type;

public class ClassDecl extends BoundableDecl {
    public final PackageRef packageRef;
    public final int modifiers;
    public final Token classNameToken;
    public final int fieldCount;
    public final FieldDecl[] fieldDecls;
    public ImplDecl boundImplDecl;
    private Type classType;

    public ClassDecl(PackageRef packageRef, int modifiers, Token classNameToken, int fieldCount,
                     FieldDecl[] fieldDecls) {
        this.packageRef = packageRef;
        this.modifiers = modifiers;
        this.classNameToken = classNameToken;
        this.fieldCount = fieldCount;
        this.fieldDecls = fieldDecls;
    }

    @Override
    public PackageRef getPackageRef() {
        return packageRef;
    }

    @Override
    public Token getNameToken() {
        return classNameToken;
    }

    @Override
    public ImplDecl getImplDecl() {
        return boundImplDecl;
    }

    @Override
    public Type getType() {
        return classType == null ? (classType = super.getType()) : classType;
    }

    @Override
    public void bindImplbidirectionally(ImplDecl implDecl) {
        boundImplDecl = implDecl;
        implDecl.boundDecl = this;
    }
}
