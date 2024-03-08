package github.io.chaosunity.xikou.ast.types;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.Type;

public class ObjectTypeRef extends AbstractTypeRef {
    public final int selectorCount;
    public final Token[] selectors;
    public ObjectType resolvedType;

    public ObjectTypeRef(int selectorCount, Token[] selectors) {
        this.selectorCount = selectorCount;
        this.selectors = selectors;
    }

    @Override
    public Type getType() {
        return resolvedType;
    }
}
