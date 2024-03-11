package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class LocalVarRef {
    public final String name;
    public final int index;
    public final AbstractType type;

    public LocalVarRef(String name, int index, AbstractType type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }
}
