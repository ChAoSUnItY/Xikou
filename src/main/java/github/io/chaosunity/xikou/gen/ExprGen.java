package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.expr.ArrayInitExpr;
import github.io.chaosunity.xikou.ast.expr.AssignmentExpr;
import github.io.chaosunity.xikou.ast.expr.BlockExpr;
import github.io.chaosunity.xikou.ast.expr.CastExpr;
import github.io.chaosunity.xikou.ast.expr.CharLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.ConstructorCallExpr;
import github.io.chaosunity.xikou.ast.expr.Expr;
import github.io.chaosunity.xikou.ast.expr.IndexExpr;
import github.io.chaosunity.xikou.ast.expr.InfixExpr;
import github.io.chaosunity.xikou.ast.expr.IntegerLiteralExpr;
import github.io.chaosunity.xikou.ast.expr.MemberAccessExpr;
import github.io.chaosunity.xikou.ast.expr.MethodCallExpr;
import github.io.chaosunity.xikou.ast.expr.NameExpr;
import github.io.chaosunity.xikou.ast.expr.NullLiteral;
import github.io.chaosunity.xikou.ast.expr.ReturnExpr;
import github.io.chaosunity.xikou.ast.expr.StringLiteralExpr;
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
    } else if (expr instanceof AssignmentExpr) {
      genAssignmentExpr(mw, (AssignmentExpr) expr);
    } else if (expr instanceof CastExpr) {
      genCastExpr(mw, (CastExpr) expr);
    } else if (expr instanceof MethodCallExpr) {
      genMethodCallExpr(mw, (MethodCallExpr) expr);
    } else if (expr instanceof MemberAccessExpr) {
      genMemberAccessExpr(mw, (MemberAccessExpr) expr);
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
    Expr lhs = infixExpr.lhs, rhs = infixExpr.rhs;
    TokenType operatorType = infixExpr.operator.type;

    if (operatorType != TokenType.DoubleAmpersand && operatorType != TokenType.DoublePipe) {
      genExpr(mw, lhs);
      genExpr(mw, rhs);
    }

    switch (operatorType) {
      case DoubleEqual:
      case NotEqual:
        genCompareExpr(mw, operatorType);
        break;
      case DoubleAmpersand:
      case DoublePipe:
        genShortCircuitExpr(mw, infixExpr, infixExpr.operator.type);
        break;
      case Plus:
        mw.visitInsn(Utils.getAddOpcode(infixExpr.getType()));
        break;
      case Minus:
        mw.visitInsn(Utils.getSubOpcode(infixExpr.getType()));
        break;
      default:
        throw new IllegalStateException(
            String.format("Token %s is not an valid infix operator", operatorType));
    }
  }
  
  private void genCompareExpr(MethodVisitor mw, TokenType operatorType) {
    Label endLabel = new Label(), falseLabel = new Label();
    
    switch (operatorType) {
      case DoubleEqual:
        mw.visitJumpInsn(Opcodes.IF_ICMPEQ, falseLabel);
        break;
      case NotEqual:
        mw.visitJumpInsn(Opcodes.IF_ICMPNE, falseLabel);
        break;
    }

    mw.visitLdcInsn(1);
    mw.visitJumpInsn(Opcodes.GOTO, endLabel);
    mw.visitLabel(falseLabel);
    mw.visitLdcInsn(0);
    mw.visitLabel(endLabel);
  }

  private void genShortCircuitExpr(MethodVisitor mw, InfixExpr infixExpr, TokenType operatorType) {
    int conditionCount = 0;
    Expr[] conditions = new Expr[1];
    Expr infixHolder = infixExpr;

    // Collect all conditions
    while (infixHolder != null) {
      if (conditionCount >= conditions.length) {
        Expr[] newArr = new Expr[conditions.length * 2];
        System.arraycopy(conditions, 0, newArr, 0, conditions.length);
        conditions = newArr;
      }

      if (!(infixHolder instanceof InfixExpr)
          || ((InfixExpr) infixHolder).operator.type != operatorType) {
        conditions[conditionCount++] = infixHolder;
        break;
      }

      InfixExpr lhsCond = (InfixExpr) infixHolder;

      conditions[conditionCount++] = lhsCond.rhs;
      infixHolder = lhsCond.lhs;
    }

    Label endLabel = new Label(), falseLabel = new Label();
    int jumpOpcode;

    switch (operatorType) {
      case DoubleAmpersand:
        jumpOpcode = Opcodes.IFNE;
        break;
      case DoublePipe:
        jumpOpcode = Opcodes.IFEQ;
        break;
      default:
        throw new IllegalStateException("Incompatible opcode for generating short circuit expression");
    }

    for (int i = conditionCount - 1; i >= 0; i--) {
      genExpr(mw, conditions[i]);
      mw.visitJumpInsn(jumpOpcode, falseLabel);
    }
    
    mw.visitLdcInsn(1);
    mw.visitJumpInsn(Opcodes.GOTO, endLabel);
    mw.visitLabel(falseLabel);
    mw.visitLdcInsn(0);
    mw.visitLabel(endLabel);
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

      if (assignmentTarget instanceof MemberAccessExpr) {
        MemberAccessExpr memberAccessExpr = (MemberAccessExpr) assignmentTarget;

        genExpr(mw, memberAccessExpr.ownerExpr);
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

      if (assignmentTarget instanceof MemberAccessExpr) {
        MemberAccessExpr memberAccessExpr = (MemberAccessExpr) assignmentTarget;
        FieldRef fieldRef = memberAccessExpr.resolvedFieldRef;

        if (!isLastAssignment) {
          mw.visitInsn(Utils.getDupX1Opcode(fieldRef.fieldType));
        }

        mw.visitFieldInsn(fieldRef.isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
            memberAccessExpr.ownerExpr.getType().getInternalName(),
            memberAccessExpr.nameToken.literal,
            memberAccessExpr.getType().getDescriptor());
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
    
    if (castExpr.targetCastExpr.getType() instanceof PrimitiveType && castExpr.targetTypeRef.getType() instanceof PrimitiveType) {
      int[] castOpcodeSeq = Utils.getPrimitiveCastOpcodeSeq((PrimitiveType) castExpr.targetCastExpr.getType(), (PrimitiveType) castExpr.targetTypeRef.getType());
      
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
