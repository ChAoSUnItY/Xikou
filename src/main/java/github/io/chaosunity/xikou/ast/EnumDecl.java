package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public class EnumDecl extends BoundableDecl {
    public final PackageRef packageRef;
    public final int modifiers;
    public final Token enumNameToken;
    public final int fieldCount;
    public final FieldDecl[] fieldDecls;
    public final int variantCount;
    public final EnumVariantDecl[] enumVariantDecls;
    public ImplDecl boundImplDecl;
    public ClassType enumType;

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

    public ClassType getSuperclassType() {
        return ClassType.ENUM_CLASS_TYPE;
    }

    @Override
    public ClassType getType() {
        if (enumType != null) return enumType;

        PackageRef packageRef = getPackageRef();
        Token declNameToken = getNameToken();
        String internalPath;

        if (packageRef.qualifiedPath.isEmpty()) {
            internalPath = declNameToken.literal;
        } else {
            internalPath = packageRef.qualifiedPath.replace('.', '/') + "/" + declNameToken.literal;
        }

        return new ClassType(getSuperclassType(), internalPath);
    }
}
