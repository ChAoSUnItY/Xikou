package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.Scope;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class ConstructorDecl {

  public final int constructorModifiers;
  public final int parameterCount;
  public final Parameter[] parameters;
  public final int statementCount;
  public final Statement[] statements;

  public ImplDecl implDecl;
  public Scope scope;

  public ConstructorDecl(
      int constructorModifiers,
      int parameterCount,
      Parameter[] parameters,
      int statementCount,
      Statement[] statements) {
    this.constructorModifiers = constructorModifiers;
    this.parameterCount = parameterCount;
    this.parameters = parameters;
    this.statementCount = statementCount;
    this.statements = statements;
  }

  public MethodRef asMethodRef() {
    AbstractType[] parameterTypes = new AbstractType[parameterCount];

    for (int i = 0; i < parameterCount; i++) {
      parameterTypes[i] = parameters[i].typeRef.getType();
    }

    return new MethodRef(
        implDecl.boundDecl.getType(),
        "<init>",
        statementCount,
        parameterTypes,
        implDecl.boundDecl.getType(),
        false,
        true);
  }
}
