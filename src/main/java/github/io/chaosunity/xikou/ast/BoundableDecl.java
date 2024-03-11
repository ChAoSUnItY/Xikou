package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;

public abstract class BoundableDecl {
    public abstract Token getNameToken();

    public String getName() {
        return getNameToken().literal;
    }

    public abstract PackageRef getPackageRef();

    public abstract ImplDecl getImplDecl();

    public PrimaryConstructorDecl getPrimaryConstructorDecl() {
        ImplDecl implDecl = getImplDecl();
        return implDecl != null ? implDecl.primaryConstructorDecl : null;
    }

    public abstract void bindImplbidirectionally(ImplDecl implDecl);

    public AbstractType getType() {
        PackageRef packageRef = getPackageRef();
        Token declNameToken = getNameToken();
        String internalPath;

        if (packageRef.qualifiedPath.isEmpty()) {
            internalPath = declNameToken.literal;
        } else {
            internalPath = packageRef.qualifiedPath.replace('.', '/') + "/" + declNameToken.literal;
        }

        return new ClassType(internalPath);
    }
}
