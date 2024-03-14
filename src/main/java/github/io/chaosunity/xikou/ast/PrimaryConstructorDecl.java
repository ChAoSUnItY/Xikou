package github.io.chaosunity.xikou.ast;

import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.Scope;
import github.io.chaosunity.xikou.resolver.types.AbstractType;

public class PrimaryConstructorDecl {

  public final int constructorModifiers;
  public final int parameterCount;
  public final Parameter[] parameters;
  public final int exprCount;
  public final Expr[] exprs;

  public ImplDecl implDecl;
  public final Scope scope;

  public PrimaryConstructorDecl(int constructorModifiers, int parameterCount,
      Parameter[] parameters, int exprCount, Expr[] exprs) {
    this.constructorModifiers = constructorModifiers;
    this.parameterCount = parameterCount;
    this.parameters = parameters;
    this.exprCount = exprCount;
    this.exprs = exprs;
    this.scope = new Scope();
  }

  public MethodRef asMethodRef() {
    AbstractType[] parameterTypes = new AbstractType[parameterCount];

    for (int i = 0; i < parameterCount; i++) {
      parameterTypes[i] = parameters[i].typeRef.getType();
    }

    return new MethodRef(implDecl.boundDecl.getType(), "<init>", exprCount, parameterTypes,
        implDecl.boundDecl.getType(), false, true);
  }
}
