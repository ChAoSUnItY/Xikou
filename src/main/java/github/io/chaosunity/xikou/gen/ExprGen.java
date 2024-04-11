package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.LocalVarRef;
import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ArrayType;
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ExprGen {

  public void genExpr(MethodVisitor mw, Expr expr) {
    if (expr instanceof InfixExpr) {
      genInfixExpr(mw, (InfixExpr) expr);
    } else if (expr instanceof CondExpr) {
      genCondExpr(mw, (CondExpr) expr);
    } else if (expr instanceof AssignmentExpr) {
      genAssignmentExpr(mw, (AssignmentExpr) expr);
    } else if (expr instanceof CastExpr) {
      genCastExpr(mw, (CastExpr) expr);
    } else if (expr instanceof MethodCallExpr) {
      genMethodCallExpr(mw, (MethodCallExpr) expr);
    } else if (expr instanceof FieldAccessExpr) {
      genMemberAccessExpr(mw, (FieldAccessExpr) expr);
    } else if (expr instanceof ConstructorCallExpr) {
      genConstructorCallExpr(mw, (ConstructorCallExpr) expr);
    } else if (expr instanceof IndexExpr) {
      genIndexExpr(mw, (IndexExpr) expr);
    } else if (expr instanceof ArrayInitExpr) {
      genArrayInitExpr(mw, (ArrayInitExpr) expr);
    } else if (expr instanceof ReturnExpr) {
      genReturnExpr(mw, (ReturnExpr) expr);
    } else if (expr instanceof BlockExpr) {
      genBlockExpr(mw, (BlockExpr) expr);
    } else if (expr instanceof NameExpr) {
      genNameExpr(mw, (NameExpr) expr);
    } else if (expr instanceof IfExpr) {
      genIfExpr(mw, (IfExpr) expr);
    } else if (expr instanceof WhileExpr) {
      genWhileExpr(mw, (WhileExpr) expr);
    } else if (expr instanceof ForExpr) {
      genForExpr(mw, (ForExpr) expr);
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
    genExpr(mw, infixExpr.getLhs());
    genExpr(mw, infixExpr.getRhs());

    if (infixExpr instanceof CompareExpr) {
      genCompareExpr(mw, (CompareExpr) infixExpr);
    } else if (infixExpr instanceof ArithmeticExpr) {
      genArithmeticExpr(mw, (ArithmeticExpr) infixExpr);
    }
  }

  private void genCompareExpr(MethodVisitor mw, CompareExpr compareExpr) {
    if (compareExpr.getLhs().getType() instanceof ClassType) {
      // Enum variant comparison
      genObjectCompareExpr(mw, compareExpr.compareOperatorToken.type == TokenType.NotEqual);
      return;
    }

    Label endLabel = new Label(), falseLabel = new Label();
    int jmpOpcode = 0;

    switch (compareExpr.compareOperatorToken.type) {
      case DoubleEqual:
        jmpOpcode = Opcodes.IF_ICMPNE;
        break;
      case NotEqual:
        jmpOpcode = Opcodes.IF_ICMPEQ;
        break;
      case Greater:
        jmpOpcode = Opcodes.IF_ICMPLE;
        break;
      case GreaterEqual:
        jmpOpcode = Opcodes.IF_ICMPLT;
        break;
      case Lesser:
        jmpOpcode = Opcodes.IF_ICMPGE;
        break;
      case LesserEqual:
        jmpOpcode = Opcodes.IF_ICMPGT;
        break;
    }

    mw.visitJumpInsn(jmpOpcode, falseLabel);
    mw.visitInsn(Opcodes.ICONST_1);
    mw.visitJumpInsn(Opcodes.GOTO, endLabel);
    mw.visitLabel(falseLabel);
    mw.visitInsn(Opcodes.ICONST_0);
    mw.visitLabel(endLabel);
  }

  private void genObjectCompareExpr(MethodVisitor mw, boolean negate) {
    Label falseBranch = new Label(), endLabel = new Label();

    mw.visitJumpInsn(negate ? Opcodes.IF_ACMPEQ : Opcodes.IF_ACMPNE, falseBranch);
    mw.visitInsn(Opcodes.ICONST_1);
    mw.visitJumpInsn(Opcodes.GOTO, endLabel);
    mw.visitLabel(falseBranch);
    mw.visitInsn(Opcodes.ICONST_0);
    mw.visitLabel(endLabel);
  }

  private void genArithmeticExpr(MethodVisitor mw, ArithmeticExpr arithmeticExpr) {
    switch (arithmeticExpr.arithOperatorToken.type) {
      case Plus:
        mw.visitInsn(Utils.getAddOpcode(arithmeticExpr.getType()));
        break;
      case Minus:
        mw.visitInsn(Utils.getSubOpcode(arithmeticExpr.getType()));
        break;
    }
  }

  private void genCondExpr(MethodVisitor mw, CondExpr condExpr) {
    switch (condExpr.condOperatorToken.type) {
      case DoubleAmpersand:
        {
          Label falseBranch = new Label(), endLabel = new Label();

          for (int i = 0; i < condExpr.exprCount; i++) {
            genExpr(mw, condExpr.exprs[i]);

            mw.visitJumpInsn(Opcodes.IFEQ, falseBranch);
          }

          mw.visitInsn(Opcodes.ICONST_1);
          mw.visitJumpInsn(Opcodes.GOTO, endLabel);
          mw.visitLabel(falseBranch);
          mw.visitInsn(Opcodes.ICONST_0);
          mw.visitLabel(endLabel);

          break;
        }
      case DoublePipe:
        {
          Label trueBranch = new Label(), falseBranch = new Label(), endLabel = new Label();

          genExpr(mw, condExpr.exprs[0]);

          for (int i = 1; i < condExpr.exprCount; i++) {
            mw.visitJumpInsn(Opcodes.IFNE, trueBranch);

            genExpr(mw, condExpr.exprs[i]);
          }

          mw.visitJumpInsn(Opcodes.IFEQ, falseBranch);
          mw.visitLabel(trueBranch);
          mw.visitInsn(Opcodes.ICONST_1);
          mw.visitJumpInsn(Opcodes.GOTO, endLabel);
          mw.visitLabel(falseBranch);
          mw.visitInsn(Opcodes.ICONST_0);
          mw.visitLabel(endLabel);

          break;
        }
    }
  }

  private void genAssignmentExpr(MethodVisitor mw, AssignmentExpr assignmentExpr) {
    boolean requiresLhs = assignmentExpr.assignOpToken.type != TokenType.Equal;

    // Setup targets
    for (int i = 0; i < assignmentExpr.targetCount; i++) {
      Expr assignmentTarget = assignmentExpr.targets[i];

      if (assignmentTarget instanceof FieldAccessExpr) {
        FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) assignmentTarget;

        genExpr(mw, fieldAccessExpr.ownerExpr);
      } else if (assignmentTarget instanceof IndexExpr) {
        IndexExpr indexExpr = (IndexExpr) assignmentTarget;

        genExpr(mw, indexExpr.targetExpr);
        genExpr(mw, indexExpr.indexExpr);
      }
    }

    for (int i = assignmentExpr.targetCount - 1; i >= 0; i--) {
      Expr assignmentTarget = assignmentExpr.targets[i];
      boolean isLastAssignment = i == 0;

      if (requiresLhs) {
        genExpr(mw, assignmentTarget);
      }

      if (i == assignmentExpr.targetCount - 1) {
        genExpr(mw, assignmentExpr.rhs);
      }

      if (requiresLhs) {
        switch (assignmentExpr.assignOpToken.type) {
          case PlusEqual:
            mw.visitInsn(Utils.getAddOpcode(assignmentTarget.getType()));
            break;
          case MinusEqual:
            mw.visitInsn(Utils.getSubOpcode(assignmentTarget.getType()));
            break;
        }
      }

      if (assignmentTarget instanceof FieldAccessExpr) {
        FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) assignmentTarget;
        FieldRef fieldRef = fieldAccessExpr.resolvedFieldRef;

        if (!isLastAssignment) {
          mw.visitInsn(Utils.getDupX1Opcode(fieldRef.fieldType));
        }

        mw.visitFieldInsn(
            fieldRef.isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
            fieldAccessExpr.ownerExpr.getType().getInternalName(),
            fieldAccessExpr.nameToken.literal,
            fieldAccessExpr.getType().getDescriptor());
      } else if (assignmentTarget instanceof NameExpr) {
        NameExpr nameExpr = (NameExpr) assignmentTarget;

        if (!isLastAssignment) {
          mw.visitInsn(Utils.getDupOpcode(nameExpr.resolvedType));
        }

        mw.visitVarInsn(Utils.getStoreOpcode(nameExpr.getType()), nameExpr.localVarRef.index);
      } else if (assignmentTarget instanceof IndexExpr) {
        IndexExpr indexExpr = (IndexExpr) assignmentTarget;

        if (!isLastAssignment) {
          mw.visitInsn(Utils.getDupX2Opcode(indexExpr.getType()));
        }

        mw.visitInsn(Utils.getArrayStoreOpcode(indexExpr.getType()));
      }
    }
  }

  private void genCastExpr(MethodVisitor mw, CastExpr castExpr) {
    genExpr(mw, castExpr.targetCastExpr);

    if (castExpr.targetCastExpr.getType() instanceof PrimitiveType
        && castExpr.targetTypeRef.getType() instanceof PrimitiveType) {
      int[] castOpcodeSeq =
          Utils.getPrimitiveCastOpcodeSeq(
              (PrimitiveType) castExpr.targetCastExpr.getType(),
              (PrimitiveType) castExpr.targetTypeRef.getType());

      for (int castOpcode : castOpcodeSeq) {
        mw.visitInsn(castOpcode);
      }
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

    if (methodCallExpr.argumentCount != methodRef.parameterCount) {
      // Vararg synthetic parameter generation
      ArrayType arrayType =
          (ArrayType) methodRef.parameterType[methodCallExpr.resolvedMethodRef.parameterCount - 1];
      AbstractType componentType = arrayType.getComponentType();

      mw.visitInsn(Opcodes.ICONST_0);

      if (componentType instanceof PrimitiveType) {
        mw.visitIntInsn(Opcodes.NEWARRAY, Utils.getArrayTypeOperand((PrimitiveType) componentType));
      } else if (componentType instanceof ClassType) {
        mw.visitTypeInsn(Opcodes.ANEWARRAY, componentType.getInternalName());
      } else {
        // TODO: Implement multi array initialization
        ArrayType componentArrayType = (ArrayType) componentType;
      }
    }

    mw.visitMethodInsn(
        opcode,
        methodRef.ownerClassType.getInternalName(),
        methodRef.name,
        Utils.getMethodDescriptor(methodRef),
        false);
  }

  private void genMemberAccessExpr(MethodVisitor mw, FieldAccessExpr fieldAccessExpr) {
    if (fieldAccessExpr.isLenAccess) {
      genExpr(mw, fieldAccessExpr.ownerExpr);
      mw.visitInsn(Opcodes.ARRAYLENGTH);
      return;
    }

    FieldRef fieldRef = fieldAccessExpr.resolvedFieldRef;

    if (fieldRef.isStatic) {
      mw.visitFieldInsn(
          Opcodes.GETSTATIC,
          fieldRef.ownerClassType.getInternalName(),
          fieldRef.name,
          fieldRef.fieldType.getDescriptor());
      return;
    }

    genExpr(mw, fieldAccessExpr.ownerExpr);
    mw.visitFieldInsn(
        Opcodes.GETFIELD,
        fieldRef.ownerClassType.getInternalName(),
        fieldRef.name,
        fieldRef.fieldType.getDescriptor());
  }

  private void genConstructorCallExpr(MethodVisitor mw, ConstructorCallExpr constructorCallExpr) {
    mw.visitTypeInsn(Opcodes.NEW, constructorCallExpr.getType().getInternalName());
    mw.visitInsn(Opcodes.DUP);

    for (int i = 0; i < constructorCallExpr.argumentCount; i++) {
      genExpr(mw, constructorCallExpr.arguments[i]);
    }

    mw.visitMethodInsn(
        Opcodes.INVOKESPECIAL,
        constructorCallExpr.getType().getInternalName(),
        "<init>",
        Utils.getMethodDescriptor(constructorCallExpr.resolvedMethodRef),
        false);
  }

  private void genIndexExpr(MethodVisitor mw, IndexExpr expr) {
    genExpr(mw, expr.targetExpr);
    genExpr(mw, expr.indexExpr);
    mw.visitInsn(Utils.getArrayLoadOpcode(expr.getType()));
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

  private void genReturnExpr(MethodVisitor mw, ReturnExpr returnExpr) {
    if (returnExpr.rhs != null) {
      genExpr(mw, returnExpr.rhs);
      mw.visitInsn(Utils.getReturnOpcode(returnExpr.rhs.getType()));
    } else {
      mw.visitInsn(Opcodes.RETURN);
    }
  }

  private void genBlockExpr(MethodVisitor mw, BlockExpr blockExpr) {
    StmtGen stmtGen = new StmtGen(this);

    for (int i = 0; i < blockExpr.statementCount; i++) {
      stmtGen.genStatement(mw, blockExpr.statements[i]);
    }
  }

  private void genNameExpr(MethodVisitor mw, NameExpr nameExpr) {
    LocalVarRef localVarRef = nameExpr.localVarRef;
    AbstractType varType = nameExpr.getType();

    mw.visitVarInsn(Utils.getLoadOpcode(varType), localVarRef.index);
  }

  private void genIfExpr(MethodVisitor mw, IfExpr ifExpr) {
    Label falseBranch = new Label(), endLabel = new Label();

    genExpr(mw, ifExpr.condExpr);

    mw.visitJumpInsn(Opcodes.IFEQ, falseBranch);
    genExpr(mw, ifExpr.trueBranchExpr);
    mw.visitJumpInsn(Opcodes.GOTO, endLabel);
    mw.visitLabel(falseBranch);

    if (ifExpr.falseBranchExpr != null) {
      genExpr(mw, ifExpr.falseBranchExpr);
    }

    mw.visitLabel(endLabel);
  }

  private void genWhileExpr(MethodVisitor mw, WhileExpr whileExpr) {
    Label condLabel = new Label(), endLabel = new Label();

    mw.visitLabel(condLabel);
    genExpr(mw, whileExpr.condExpr);
    mw.visitJumpInsn(Opcodes.IFEQ, endLabel);
    genExpr(mw, whileExpr.iterExpr);
    mw.visitJumpInsn(Opcodes.GOTO, condLabel);
    mw.visitLabel(endLabel);
  }

  private void genForExpr(MethodVisitor mw, ForExpr forExpr) {
    AbstractType iterableType = forExpr.iterableTargetExpr.getType();

    if (iterableType instanceof ArrayType) {
      ArrayType arrayType = (ArrayType) iterableType;
      AbstractType componentType = arrayType.getComponentType();
      LocalVarRef immIterableTargetVarRef = forExpr.immIterableTargetVarRef,
          indexVarRef = forExpr.indexVarRef,
          iterateVarRef = forExpr.iterateVarNameExpr.localVarRef;
      boolean isRhsImmediateValue = immIterableTargetVarRef != null;

      if (isRhsImmediateValue) {
        // Store it before for loop generation to avoid generation on
        genExpr(mw, forExpr.iterableTargetExpr);
        mw.visitVarInsn(Opcodes.ASTORE, immIterableTargetVarRef.index);
      }

      mw.visitInsn(Opcodes.ICONST_0);
      mw.visitVarInsn(Opcodes.ISTORE, indexVarRef.index);

      Label condLabel = new Label(), endLabel = new Label();

      mw.visitLabel(condLabel);
      mw.visitVarInsn(Opcodes.ILOAD, indexVarRef.index);

      if (isRhsImmediateValue) {
        mw.visitVarInsn(Opcodes.ALOAD, immIterableTargetVarRef.index);
      } else {
        genExpr(mw, forExpr.iterableTargetExpr);
      }

      mw.visitInsn(Opcodes.ARRAYLENGTH);
      mw.visitJumpInsn(Opcodes.IF_ICMPGE, endLabel);

      if (isRhsImmediateValue) {
        mw.visitVarInsn(Opcodes.ALOAD, immIterableTargetVarRef.index);
      } else {
        genExpr(mw, forExpr.iterableTargetExpr);
      }
      mw.visitVarInsn(Opcodes.ILOAD, indexVarRef.index);
      mw.visitInsn(Utils.getArrayLoadOpcode(componentType));
      mw.visitVarInsn(Utils.getStoreOpcode(componentType), iterateVarRef.index);

      genBlockExpr(mw, forExpr.iterationBlock);

      mw.visitIincInsn(indexVarRef.index, 1);
      mw.visitJumpInsn(Opcodes.GOTO, condLabel);
      mw.visitLabel(endLabel);
    }

    // TODO: Implement `Iterator` based for loop iteration codegen
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
