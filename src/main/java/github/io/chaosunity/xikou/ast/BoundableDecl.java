package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.Type;

public abstract class BoundableDecl {
    public abstract Token getNameToken();

    public abstract PackageRef getPackageRef();

    public abstract ImplDecl getImplDecl();

    public abstract void bindImplbidirectionally(ImplDecl implDecl);

    public Type getType() {
        PackageRef packageRef = getPackageRef();
        Token declNameToken = getNameToken();
        String internalPath;

        if (packageRef.qualifiedPath.isEmpty()) {
            internalPath = declNameToken.literal;
        } else {
            internalPath = packageRef.qualifiedPath.replace('.', '/') + "/" + declNameToken.literal;
        }

        return new ObjectType(internalPath);
    }
}
