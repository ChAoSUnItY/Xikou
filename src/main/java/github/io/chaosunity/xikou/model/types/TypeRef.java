package github.io.chaosunity.xikou.model.types;

import github.io.chaosunity.xikou.lexer.Token;

public class TypeRef extends AbstractTypeRef {
    public final int selectorCount;
    public final Token[] selectors;
    
    public TypeRef(int selectorCount, Token[] selectors) {
        this.selectorCount = selectorCount;
        this.selectors = selectors;
    }

    @Override
    public String getDescriptor() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < selectorCount; i++) {
            builder.append(selectors[i].literal);

            if (i != selectorCount - 1) {
                builder.append(".");
            }
        }

        return builder.toString();
    }
}
