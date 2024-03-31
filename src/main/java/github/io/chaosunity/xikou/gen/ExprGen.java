package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.expr.ArrayInitExpr;
import github.io.chaosunity.xikou.ast.expr.AssignmentExpr;
import github.io.chaosunity.xikou.ast.expr.BlockExpr;
import github.io.chaosunity.xikou.ast.expr.CastExpr;
import github.io.chaosunity.xikou.ast.expr.CharLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.CondExpr;
import github.io.chaosunity.xikou.ast.expr.ConstructorCallExpr;
import github.io.chaosunity.xikou.ast.expr.EqualExpr;
import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.expr.IndexExpr;
import github.io.chaosunity.xikou.ast.expr.InfixExpr;
import github.io.chaosunity.xikou.ast.expr.IntegerLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.FieldAccessExpr;
import github.io.chaosunity.xikou.ast.expr.MethodCallExpr;
import github.io.chaosunity.xikou.ast.expr.MinusExpr;
import github.io.chaosunity.xikou.ast.expr.NameExpr;
import github.io.chaosunity.xikou.ast.expr.NotEqualExpr;
import github.io.chaosunity.xikou.ast.expr.NullLiteral;
import github.io.chaosunity.xikou.ast.expr.PlusExpr;
import github.io.chaosunity.xikou.ast.expr.ReturnExpr;
import github.io.chaosunity.xikou.ast.expr.StringLiteralExpr;
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

    if (infixExpr instanceof EqualExpr || infixExpr instanceof NotEqualExpr) {
      genCmpResult(mw);
    } else if (infixExpr instanceof PlusExpr || infixExpr instanceof MinusExpr) {
      mw.visitInsn(Utils.getAddOpcode(infixExpr.getType()));
    }
  }

  private void genCmpResult(MethodVisitor mw) {
    Label endLabel = new Label(), falseLabel = new Label();

    mw.visitJumpInsn(Opcodes.IF_ICMPNE, falseLabel);
    mw.visitLdcInsn(1);
    mw.visitJumpInsn(Opcodes.GOTO, endLabel);
    mw.visitLabel(falseLabel);
    mw.visitLdcInsn(0);
    mw.visitLabel(endLabel);
  }

  private void genCondExpr(MethodVisitor mw, CondExpr condExpr) {
    switch (condExpr.condOperatorToken.type) {
      case DoubleAmpersand: {
        Label falseLabel = new Label(), endLabel = new Label();

        genExpr(mw, condExpr.exprs[0]);

        for (int i = 1; i < condExpr.exprCount; i++) {
          mw.visitJumpInsn(Opcodes.IFEQ, falseLabel);

          genExpr(mw, condExpr.exprs[i]);
        }

        mw.visitJumpInsn(Opcodes.IFEQ, endLabel);
        mw.visitLdcInsn(1);
        mw.visitJumpInsn(Opcodes.GOTO, endLabel);
        mw.visitLabel(falseLabel);
        mw.visitLdcInsn(0);
        mw.visitLabel(endLabel);
      }
      case DoublePipe: {
        Label falseLabel = new Label(), endLabel = new Label();

        genExpr(mw, condExpr.exprs[0]);

        for (int i = 1; i < condExpr.exprCount; i++) {
          mw.visitJumpInsn(Opcodes.IFEQ, falseLabel);

          genExpr(mw, condExpr.exprs[i]);
        }

        mw.visitJumpInsn(Opcodes.IFEQ, endLabel);
        mw.visitLdcInsn(1);
        mw.visitJumpInsn(Opcodes.GOTO, endLabel);
        mw.visitLabel(falseLabel);
        mw.visitLdcInsn(0);
        mw.visitLabel(endLabel);
      }
    }
  }

  private void genAssignmentExpr(MethodVisitor mw, AssignmentExpr assignmentExpr) {
    int assignmentTargetCount = 0;
    Expr[] assignmentTargets = new Expr[1];
    Expr lhsHolder = assignmentExpr.lhs, rhs = assignmentExpr.rhs;

    // Collect all assignment targets
    while (lhsHolder != null) {
      if (assignmentTargetCount >= assignmentTargets.length) {
        Expr[] newArr = new Expr[assignmentTargets.length * 2];
        System.arraycopy(assignmentTargets, 0, newArr, 0, assignmentTargets.length);
        assignmentTargets = newArr;
      }

      if (!(lhsHolder instanceof AssignmentExpr)) {
        assignmentTargets[assignmentTargetCount++] = lhsHolder;
        break;
      }

      AssignmentExpr lhsAssignment = (AssignmentExpr) lhsHolder;

      assignmentTargets[assignmentTargetCount++] = lhsAssignment.rhs;
      lhsHolder = lhsAssignment.lhs;
    }

    // Setup targets
    for (int i = 0; i < assignmentTargetCount; i++) {
      Expr assignmentTarget = assignmentTargets[i];

      if (assignmentTarget instanceof FieldAccessExpr) {
        FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) assignmentTarget;

        genExpr(mw, fieldAccessExpr.ownerExpr);
      } else if (assignmentTarget instanceof IndexExpr) {
        IndexExpr indexExpr = (IndexExpr) assignmentTarget;

        genExpr(mw, indexExpr.targetExpr);
        genExpr(mw, indexExpr.indexExpr);
      }
    }

    // Gen assign value then dup if needed
    genExpr(mw, rhs);

    for (int i = 0; i < assignmentTargetCount; i++) {
      Expr assignmentTarget = assignmentTargets[i];
      boolean isLastAssignment = i == assignmentTargetCount - 1;

      if (assignmentTarget instanceof FieldAccessExpr) {
        FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) assignmentTarget;
        FieldRef fieldRef = fieldAccessExpr.resolvedFieldRef;

        if (!isLastAssignment) {
          mw.visitInsn(Utils.getDupX1Opcode(fieldRef.fieldType));
        }

        mw.visitFieldInsn(fieldRef.isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
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
      int[] castOpcodeSeq = Utils.getPrimitiveCastOpcodeSeq(
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

    mw.visitMethodInsn(opcode, methodRef.ownerClassType.getInternalName(), methodRef.name,
        Utils.getMethodDescriptor(methodRef), false);
  }

  private void genMemberAccessExpr(MethodVisitor mw, FieldAccessExpr fieldAccessExpr) {
    FieldRef fieldRef = fieldAccessExpr.resolvedFieldRef;

    if (fieldRef.isStatic) {
      mw.visitFieldInsn(Opcodes.GETSTATIC, fieldRef.ownerClassType.getInternalName(),
          fieldRef.name, fieldRef.fieldType.getDescriptor());
      return;
    }

    genExpr(mw, fieldAccessExpr.ownerExpr);
    mw.visitFieldInsn(Opcodes.GETFIELD, fieldRef.ownerClassType.getInternalName(),
        fieldRef.name, fieldRef.fieldType.getDescriptor());
  }

  private void genConstructorCallExpr(MethodVisitor mw, ConstructorCallExpr constructorCallExpr) {
    mw.visitTypeInsn(Opcodes.NEW, constructorCallExpr.getType().getInternalName());
    mw.visitInsn(Opcodes.DUP);

    for (int i = 0; i < constructorCallExpr.argumentCount; i++) {
      genExpr(mw, constructorCallExpr.arguments[i]);
    }

    mw.visitMethodInsn(Opcodes.INVOKESPECIAL, constructorCallExpr.getType().getInternalName(),
        "<init>", Utils.getMethodDescriptor(constructorCallExpr.resolvedMethodRef), false);
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
