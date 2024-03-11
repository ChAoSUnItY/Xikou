package github.io.chaosunity.xikou.ast;

/**
 * Intermediate structure for parsing purpose, not directly stored in AST.
 */
public class Parameters {
    public final int parameterCount;
    public final Parameter[] parameters;

    public Parameters(int parameterCount, Parameter[] parameters) {
        this.parameterCount = parameterCount;
        this.parameters = parameters;
    }
}
