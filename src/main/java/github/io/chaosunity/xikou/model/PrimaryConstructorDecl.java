package github.io.chaosunity.xikou.model;

public class PrimaryConstructorDecl {
    public final int constructorModifiers;
    public final Parameters parameters;
    
    public PrimaryConstructorDecl(int constructorModifiers, Parameters parameters) {
        this.constructorModifiers = constructorModifiers;
        this.parameters = parameters;
    }
}
