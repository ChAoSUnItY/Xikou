package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class ConstructorDecl {

  public final int constructorModifiers;
  public final int parameterCount;
  public final Parameter[] parameters;
  public final int statementCount;
  public final Statement[] statements;
  public AbstractType[] parameterTypes;
  public MethodRef resolvedMethodRef;

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
}
