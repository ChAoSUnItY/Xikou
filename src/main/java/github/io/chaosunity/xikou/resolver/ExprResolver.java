package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.expr.ArrayInitExpr;
import github.io.chaosunity.xikou.ast.expr.CharLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.ConstructorCallExpr;
import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.expr.InfixExpr;
import github.io.chaosunity.xikou.ast.expr.IntegerLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.MemberAccessExpr;
import github.io.chaosunity.xikou.ast.expr.MethodCallExpr;
import github.io.chaosunity.xikou.ast.expr.NameExpr;
import github.io.chaosunity.xikou.ast.expr.NullLiteral;
import github.io.chaosunity.xikou.ast.expr.StringLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.TypeableExpr;
import github.io.chaosunity.xikou.ast.types.ClassTypeRef;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.TypeUtils;

public final class ExprResolver {

  private final SymbolTable table;
  private final TypeResolver typeResolver;

  ExprResolver(SymbolTable table, TypeResolver typeResolver) {
    this.table = table;
    this.typeResolver = typeResolver;
  }

  public void resolveExpr(Expr expr, Scope scope) {
    if (expr instanceof InfixExpr) {
      resolveInfixExpr((InfixExpr) expr, scope);
    } else if (expr instanceof MethodCallExpr) {
      resolveMethodCallExpr((MethodCallExpr) expr, scope);
    } else if (expr instanceof MemberAccessExpr) {
      resolveMemberAccessExpr((MemberAccessExpr) expr, scope);
    } else if (expr instanceof ConstructorCallExpr) {
      resolveConstructorCallExpr((ConstructorCallExpr) expr, scope);
    } else if (expr instanceof ArrayInitExpr) {
      resolveArrayInitExpr((ArrayInitExpr) expr, scope);
    } else if (expr instanceof NameExpr) {
      resolveNameExpr((NameExpr) expr, scope);
    } else if (expr instanceof CharLiteralExpr) {
      CharLiteralExpr charLiteralExpr = (CharLiteralExpr) expr;
    } else if (expr instanceof StringLiteralExpr) {
      StringLiteralExpr stringLiteralExpr = (StringLiteralExpr) expr;
    } else if (expr instanceof IntegerLiteralExpr) {
      IntegerLiteralExpr integerLiteralExpr = (IntegerLiteralExpr) expr;
    } else if (expr instanceof NullLiteral) {
      NullLiteral nullLiteral = (NullLiteral) expr;
    }
  }

  private static void resolveNameExpr(NameExpr expr, Scope scope) {
    LocalVarRef localVarRef = scope.findLocalVar(expr.varIdentifier.literal);

    if (localVarRef != null) {
      expr.resolvedType = localVarRef.type;
      expr.localVarRef = localVarRef;
    } else {
      throw new IllegalStateException("Unknown reference to local variable");
    }
  }

  private void resolveArrayInitExpr(ArrayInitExpr expr, Scope scope) {
    typeResolver.resolveTypeRef(expr.componentTypeRef, false);
    resolveExpr(expr.sizeExpr, scope);

    for (int i = 0; i < expr.initExprCount; i++) {
      resolveExpr(expr.initExprs[i], scope);
    }

    if (expr.sizeExpr != null && expr.initExprCount != 0) {
      throw new IllegalStateException(
          "Array initialization with explicit length and initialization is illegal");
    }
  }

  private void resolveConstructorCallExpr(ConstructorCallExpr expr, Scope scope) {
    ClassType ownerClassType = resolveTypeableExpr(expr.ownerTypeExpr, false);

    for (int i = 0; i < expr.argumentCount; i++) {
      resolveExpr(expr.arguments[i], scope);
    }

    MethodRef[] constructorRefs = table.getConstructors(ownerClassType);
    MethodRef resolvedConstructorRef = null;
    boolean hasApplicableConstructor = false;

    for (MethodRef constructorRef : constructorRefs) {
      if (Utils.isInvocationApplicable(expr.argumentCount,
          expr.arguments, constructorRef)) {
        hasApplicableConstructor = true;
        resolvedConstructorRef = constructorRef;
        break;
      }
    }

    if (!hasApplicableConstructor) {
      throw new IllegalStateException("Unknown constructor call");
    }

    expr.resolvedType = ownerClassType;
    expr.resolvedMethodRef = resolvedConstructorRef;
  }

  private void resolveMethodCallExpr(MethodCallExpr expr, Scope scope) {
    AbstractType[] arguementTypes = new AbstractType[expr.argumentCount];

    for (int i = 0; i < expr.argumentCount; i++) {
      resolveExpr(expr.arguments[i], scope);
      arguementTypes[i] = expr.arguments[i].getType();
    }

    if (expr.ownerExpr instanceof TypeableExpr) {
      // Special check for type ref
      ClassType ownerType = resolveTypeableExpr((TypeableExpr) expr.ownerExpr, true);

      if (ownerType != null) {
        MethodRef methodRef = table.getMethod(ownerType, expr.nameToken.literal, arguementTypes);

        if (methodRef != null) {
          expr.resolvedMethodRef = methodRef;
          return;
        } else {
          throw new IllegalStateException(
              String.format("Type %s does not have method %s",
                  ownerType.getInternalName(),
                  expr.nameToken.literal));
        }
      }
    }

    resolveExpr(expr.ownerExpr, scope);

    MethodRef methodRef = table.getMethod(expr.ownerExpr.getType(), expr.nameToken.literal,
        arguementTypes);

    if (methodRef != null) {
      expr.resolvedMethodRef = methodRef;
    } else {
      throw new IllegalStateException(
          String.format("Type %s does not have method %s",
              expr.ownerExpr.getType(),
              expr.nameToken.literal));
    }
  }

  private void resolveMemberAccessExpr(MemberAccessExpr expr, Scope scope) {
    if (expr.ownerExpr instanceof TypeableExpr) {
      // Special check for type ref
      ClassType ownerType = resolveTypeableExpr((TypeableExpr) expr.ownerExpr, true);

      if (ownerType != null) {
        FieldRef fieldRef = table.getField(ownerType,
            expr.nameToken.literal);

        if (fieldRef != null) {
          expr.resolvedFieldRef = fieldRef;
          return;
        } else {
          throw new IllegalStateException(
              String.format("Type %s does not have field %s",
                  ownerType.getInternalName(),
                  expr.nameToken.literal));
        }
      }
    }

    resolveExpr(expr.ownerExpr, scope);

    FieldRef fieldRef = table.getField(expr.ownerExpr.getType(),
        expr.nameToken.literal);

    if (fieldRef != null) {
      expr.resolvedFieldRef = fieldRef;
    } else {
      throw new IllegalStateException(
          String.format("Type %s does not have field %s",
              expr.ownerExpr.getType().getInternalName(),
              expr.nameToken.literal));
    }
  }

  private void resolveInfixExpr(InfixExpr expr, Scope scope) {
    resolveExpr(expr.lhs, scope);
    resolveExpr(expr.rhs, scope);

    if (expr.operator.type == TokenType.Equal) {
      if (!expr.lhs.isAssignable()) {
        throw new IllegalStateException("Illegal assignment");
      }

      if (!TypeUtils.isInstanceOf(expr.rhs.getType(), expr.lhs.getType())) {
        throw new IllegalStateException(
            String.format("Illegal assignment: %s is not compatible with %s",
                expr.rhs.getType().getInternalName(),
                expr.lhs.getType().getInternalName()));
      }
    }
  }

  private ClassType resolveTypeableExpr(TypeableExpr typeableExpr, boolean recoverable) {
    ClassTypeRef typeRef = typeableExpr.asTypeRef();

    typeResolver.resolveTypeRef(typeRef, recoverable);

    return typeRef.resolvedType;
  }
}
