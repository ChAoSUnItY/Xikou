package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class LocalVarRef {

  public final String name;
  public final boolean mutable;
  public final int index;
  public final AbstractType type;

  public LocalVarRef(String name, boolean mutable, int index, AbstractType type) {
    this.name = name;
    this.mutable = mutable;
    this.index = index;
    this.type = type;
  }
}
