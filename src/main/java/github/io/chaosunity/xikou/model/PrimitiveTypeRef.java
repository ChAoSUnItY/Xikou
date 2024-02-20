package github.io.chaosunity.xikou.model;

import github.io.chaosunity.xikou.lexer.Token;

public class PrimitiveTypeRef extends AbstractTypeRef {
    public final PrimitiveType type;
    public final Token typeRefToken;
    
    public PrimitiveTypeRef(Token typeRefToken, PrimitiveType type) {
        this.type = type;
        this.typeRefToken = typeRefToken;
    }

    @Override
    public String getDescriptor() {
        return String.valueOf(type.descriptor);
    }
}
