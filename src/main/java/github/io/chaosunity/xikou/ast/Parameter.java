package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;

public class Parameter {
    public final Token name;
    public final AbstractTypeRef typeRef;
    
    public Parameter(Token name, AbstractTypeRef typeRef) {
        this.name = name;
        this.typeRef = typeRef;
    }
}
