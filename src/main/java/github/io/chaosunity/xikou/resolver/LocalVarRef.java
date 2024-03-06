package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.Type;

public class LocalVarRef {
    public final String name;
    public final int index;
    public final Type type;

    public LocalVarRef(String name, int index, Type type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }
}
