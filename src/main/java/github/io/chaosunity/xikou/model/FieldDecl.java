package github.io.chaosunity.xikou.model;

import github.io.chaosunity.xikou.lexer.Token;

public class FieldDecl {
    public final int fieldModifiers;
    public final Token name;
    public final AbstractTypeRef typeRef;
    
    public FieldDecl(int fieldModifiers, Token name, AbstractTypeRef typeRef) {
        this.fieldModifiers = fieldModifiers;
        this.name = name;
        this.typeRef = typeRef;
    }

    @Override
    public String toString() {
        return "FieldDecl{" +
                "fieldModifiers=" + fieldModifiers +
                ", name='" + name + '\'' +
                ", typeRef='" + typeRef + '\'' +
                '}';
    }
}
