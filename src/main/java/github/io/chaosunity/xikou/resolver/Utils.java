package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.ConstructorDecl;
import github.io.chaosunity.xikou.ast.FnDecl;
import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.TypeUtils;

public class Utils {

  public static MethodRef genImplcicitPrimaryConstructorRef(ClassType ownerClassType) {
    return new MethodRef(
        ownerClassType, "<init>", 0, new AbstractType[0], ownerClassType, false, true);
  }

  public static boolean isInvocationApplicable(
      int argumentCount, Expr[] arguments, MethodRef methodRef) {
    // TODO: Support vararg in future
    if (methodRef.parameterCount != argumentCount) {
      return false;
    }

    for (int i = 0; i < argumentCount; i++) {
      Expr argument = arguments[i];
      AbstractType parameterType = methodRef.parameterType[i];

      if (!TypeUtils.isInstanceOf(argument.getType(), parameterType)) {
        return false;
      }
    }

    return true;
  }

  public static MethodRef constructorDeclAsMethodRef(
      ClassType ownerClassType, ConstructorDecl constructorDecl) {
    AbstractType[] parameterTypes = new AbstractType[constructorDecl.parameterCount];

    for (int i = 0; i < constructorDecl.parameterCount; i++) {
      parameterTypes[i] = constructorDecl.parameters[i].typeRef.getType();
    }

    return new MethodRef(
        ownerClassType,
        "<init>",
        constructorDecl.statementCount,
        parameterTypes,
        ownerClassType,
        false,
        true);
  }

  public static MethodRef functionDeclAsMethodRef(ClassType ownerClassType, FnDecl fnDecl) {
    AbstractType[] parameterTypes = new AbstractType[fnDecl.parameterCount];

    for (int i = 0; i < fnDecl.parameterCount; i++) {
      parameterTypes[i] = fnDecl.parameters[i].typeRef.getType();
    }

    return new MethodRef(
        ownerClassType,
        fnDecl.nameToken.literal,
        fnDecl.parameterCount,
        parameterTypes,
        fnDecl.returnType,
        fnDecl.selfToken == null,
        false);
  }
}
