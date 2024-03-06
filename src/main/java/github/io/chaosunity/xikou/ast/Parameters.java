package github.io.chaosunity.xikou.ast;

public class Parameters {
    public final int parameterCount;
    public final Parameter[] parameters;
    
    public Parameters(int parameterCount, Parameter[] parameters) {
        this.parameterCount = parameterCount;
        this.parameters = parameters;
    }
}
