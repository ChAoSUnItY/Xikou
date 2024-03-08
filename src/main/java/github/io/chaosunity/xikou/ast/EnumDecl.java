package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.Type;

public class EnumDecl extends BoundableDecl {
    public final PackageRef packageRef;
    public final int modifiers;
    public final Token enumNameToken;
    public final int fieldCount;
    public final FieldDecl[] fieldDecls;
    public final int variantCount;
    public final EnumVariantDecl[] enumVariantDecls;
    public ImplDecl boundImplDecl;
    public Type enumType;

    public EnumDecl(PackageRef packageRef, int modifiers, Token enumNameToken, int fieldCount,
                    FieldDecl[] fieldDecls, int variantCount, EnumVariantDecl[] enumVariantDecls) {
        this.packageRef = packageRef;
        this.modifiers = modifiers;
        this.enumNameToken = enumNameToken;
        this.fieldCount = fieldCount;
        this.fieldDecls = fieldDecls;
        this.variantCount = variantCount;
        this.enumVariantDecls = enumVariantDecls;
    }

    @Override
    public PackageRef getPackageRef() {
        return packageRef;
    }

    @Override
    public Token getNameToken() {
        return enumNameToken;
    }

    @Override
    public ImplDecl getImplDecl() {
        return boundImplDecl;
    }

    @Override
    public void bindImplbidirectionally(ImplDecl implDecl) {
        boundImplDecl = implDecl;
        implDecl.boundDecl = this;
    }

    @Override
    public Type getType() {
        return enumType == null ? (enumType = super.getType()) : enumType;
    }
}
