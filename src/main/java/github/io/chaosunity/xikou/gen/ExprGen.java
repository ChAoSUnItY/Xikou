package github.io.chaosunity.xikou.gen;

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
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.LocalVarRef;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ExprGen {

  public void genExpr(MethodVisitor mw, Expr expr) {
    if (expr instanceof InfixExpr) {
      genInfixExpr(mw, (InfixExpr) expr);
    } else if (expr instanceof MethodCallExpr) {
      genMethodCallExpr(mw, (MethodCallExpr) expr);
    } else if (expr instanceof MemberAccessExpr) {
      genMemberAccessExpr(mw, (MemberAccessExpr) expr);
    } else if (expr instanceof ConstructorCallExpr) {
      genConstructorCallExpr(mw, (ConstructorCallExpr) expr);
    } else if (expr instanceof ArrayInitExpr) {
      genArrayInitExpr(mw, (ArrayInitExpr) expr);
    } else if (expr instanceof NameExpr) {
      genNameExpr(mw, (NameExpr) expr);
    } else if (expr instanceof CharLiteralExpr) {
      genCharLiteralExpr(mw, (CharLiteralExpr) expr);
    } else if (expr instanceof StringLiteralExpr) {
      genStringLiteralExpr(mw, (StringLiteralExpr) expr);
    } else if (expr instanceof IntegerLiteralExpr) {
      genIntegerLiteral(mw, (IntegerLiteralExpr) expr);
    } else if (expr instanceof NullLiteral) {
      genNullLiteral(mw, (NullLiteral) expr);
    }
  }

  private void genInfixExpr(MethodVisitor mw, InfixExpr infixExpr) {
    Expr lhs = infixExpr.lhs, rhs = infixExpr.rhs;
    TokenType operatorType = infixExpr.operator.type;

    switch (operatorType) {
      case Equal:
        if (lhs instanceof MemberAccessExpr) {
          MemberAccessExpr memberAccessLhs = (MemberAccessExpr) lhs;
          FieldRef fieldRef = memberAccessLhs.resolvedFieldRef;

          genExpr(mw, memberAccessLhs.ownerExpr);
          genExpr(mw, rhs);

          mw.visitFieldInsn(fieldRef.isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
              memberAccessLhs.ownerExpr.getType().getInternalName(),
              memberAccessLhs.nameToken.literal,
              memberAccessLhs.getType().getDescriptor());
        } else if (lhs instanceof NameExpr) {
          NameExpr nameExpr = (NameExpr) lhs;

          genExpr(mw, rhs);
          mw.visitVarInsn(Utils.getStoreOpcode(nameExpr.getType()), nameExpr.localVarRef.index);
        }
        break;
      default:
        throw new IllegalStateException(
            String.format("Token %s is not an valid infix operator", operatorType));
    }
  }

  private void genMethodCallExpr(MethodVisitor mw, MethodCallExpr methodCallExpr) {
    MethodRef methodRef = methodCallExpr.resolvedMethodRef;
    int opcode;

    if (methodRef.isStatic) {
      opcode = Opcodes.INVOKESTATIC;
    } else {
      opcode = Opcodes.INVOKEVIRTUAL;
      genExpr(mw, methodCallExpr.ownerExpr);
    }

    for (int i = 0; i < methodCallExpr.argumentCount; i++) {
      genExpr(mw, methodCallExpr.arguments[i]);
    }

    mw.visitMethodInsn(opcode, methodRef.ownerClassType.getInternalName(), methodRef.name,
        Utils.getMethodDescriptor(methodRef), false);
  }

  private void genMemberAccessExpr(MethodVisitor mw, MemberAccessExpr memberAccessExpr) {
    FieldRef fieldRef = memberAccessExpr.resolvedFieldRef;

    if (fieldRef.isStatic) {
      mw.visitFieldInsn(Opcodes.GETSTATIC, fieldRef.ownerClassType.getInternalName(),
          fieldRef.name, fieldRef.fieldType.getDescriptor());
      return;
    }

    genExpr(mw, memberAccessExpr.ownerExpr);
    mw.visitFieldInsn(Opcodes.GETFIELD, fieldRef.ownerClassType.getInternalName(),
        fieldRef.name, fieldRef.fieldType.getDescriptor());
  }

  private void genConstructorCallExpr(MethodVisitor mw, ConstructorCallExpr constructorCallExpr) {
    AbstractType[] types = new AbstractType[constructorCallExpr.argumentCount];

    mw.visitTypeInsn(Opcodes.NEW, constructorCallExpr.getType().getInternalName());
    mw.visitInsn(Opcodes.DUP);

    for (int i = 0; i < constructorCallExpr.argumentCount; i++) {
      genExpr(mw, constructorCallExpr.arguments[i]);
      types[i] = constructorCallExpr.arguments[i].getType();
    }

    mw.visitMethodInsn(Opcodes.INVOKESPECIAL, constructorCallExpr.getType().getInternalName(),
        "<init>", Utils.getMethodDescriptor(constructorCallExpr.resolvedMethodRef), false);
  }

  private void genArrayInitExpr(MethodVisitor mw, ArrayInitExpr arrayInitExpr) {
    AbstractType componentType = arrayInitExpr.getComponentType();

    if (arrayInitExpr.initExprCount > 0) {
      mw.visitLdcInsn(arrayInitExpr.initExprCount);
    } else {
      genExpr(mw, arrayInitExpr.sizeExpr);
    }

    if (componentType instanceof PrimitiveType) {
      mw.visitIntInsn(Opcodes.NEWARRAY, Utils.getArrayTypeOperand((PrimitiveType) componentType));
    } else if (componentType instanceof ClassType) {
      mw.visitTypeInsn(Opcodes.ANEWARRAY, componentType.getInternalName());
    } else {
      // Must be array type
      // TODO: Implement multi array initialization
      ArrayType componentArrayType = (ArrayType) componentType;
    }

    for (int i = 0; i < arrayInitExpr.initExprCount; i++) {
      Expr initExpr = arrayInitExpr.initExprs[i];

      mw.visitInsn(Opcodes.DUP);
      mw.visitLdcInsn(i);
      genExpr(mw, initExpr);
      mw.visitInsn(Utils.getArrayStoreOpcode(initExpr.getType()));
    }
  }

  private void genNameExpr(MethodVisitor mw, NameExpr nameExpr) {
    LocalVarRef localVarRef = nameExpr.localVarRef;
    AbstractType varType = nameExpr.getType();

    mw.visitVarInsn(Utils.getLoadOpcode(varType), localVarRef.index);
  }

  private void genCharLiteralExpr(MethodVisitor mw, CharLiteralExpr charLiteralExpr) {
    mw.visitLdcInsn(charLiteralExpr.characterToken.literal.charAt(0));
  }

  private void genStringLiteralExpr(MethodVisitor mw, StringLiteralExpr stringLiteralExpr) {
    mw.visitLdcInsn(stringLiteralExpr.stringLiteralToken.literal);
  }

  private void genIntegerLiteral(MethodVisitor mw, IntegerLiteralExpr integerLiteralExpr) {
    mw.visitLdcInsn(integerLiteralExpr.asConstant());
  }

  private void genNullLiteral(MethodVisitor mw, NullLiteral nullLiteral) {
    mw.visitInsn(Opcodes.ACONST_NULL);
  }
}
