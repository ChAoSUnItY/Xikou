package github.io.chaosunity.xikou.resolver.types;

import github.io.chaosunity.xikou.ast.expr.Expr;

import java.util.Objects;

public class ArrayType implements Type {
    private final Type componentType;
    // indicates the actual size of array if present, only used in array initialization.
    private final Expr arraySizeExpr;

    public ArrayType(Type componentType, Expr arraySizeExpr) {
        this.componentType = componentType;
        this.arraySizeExpr = arraySizeExpr;
    }

    public Type getComponentType() {
        return componentType;
    }

    @Override
    public String getInternalName() {
        return "[" + componentType.getInternalName();
    }

    @Override
    public String getDescriptor() {
        return "[" + componentType.getDescriptor();
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(componentType, arrayType.componentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentType);
    }
}
