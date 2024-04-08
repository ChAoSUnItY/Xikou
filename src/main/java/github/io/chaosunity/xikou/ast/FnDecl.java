package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.stmt.Statement;
import github.io.chaosunity.xikou.ast.types.AbstractTypeRef;
import github.io.chaosunity.xikou.lexer.Token;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.Scope;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public final class FnDecl {

  public final int fnModifiers;
  public final Token nameToken;
  public final Token selfToken;
  public final int parameterCount;
  public final Parameter[] parameters;
  public final AbstractTypeRef returnTypeRef;
  public final int statementCount;
  public final Statement[] statements;
  public ImplDecl implDecl;
  public Scope scope;
  public AbstractType returnType;

  public FnDecl(
      int fnModifiers,
      Token nameToken,
      Token selfToken,
      int parameterCount,
      Parameter[] parameters,
      AbstractTypeRef returnTypeRef,
      int statementCount,
      Statement[] statements) {
    this.fnModifiers = fnModifiers;
    this.nameToken = nameToken;
    this.selfToken = selfToken;
    this.parameterCount = parameterCount;
    this.parameters = parameters;
    this.returnTypeRef = returnTypeRef;
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
        nameToken.literal,
        parameterCount,
        parameterTypes,
        returnType,
        selfToken == null,
        false);
  }
}
