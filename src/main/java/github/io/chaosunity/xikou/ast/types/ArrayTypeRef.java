package github.io.chaosunity.xikou.ast.types;

import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;

public class ArrayTypeRef extends AbstractTypeRef {
    public final AbstractTypeRef componentTypeRef;
    public ArrayType resolvedType;


    public ArrayTypeRef(AbstractTypeRef componentTypeRef) {
        this.componentTypeRef = componentTypeRef;
    }

    @Override
    public AbstractType getType() {
        return resolvedType;
    }
}
