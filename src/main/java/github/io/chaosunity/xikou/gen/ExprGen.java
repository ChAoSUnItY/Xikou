package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.LocalVarRef;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ExprGen {
    public void genExpr(MethodVisitor mw, Expr expr) {
        if (expr instanceof IntegerLiteral) {
            genIntegerLiteral(mw, (IntegerLiteral) expr);
        } else if (expr instanceof InfixExpr) {
            InfixExpr infixExpr = (InfixExpr) expr;
            Expr lhs = infixExpr.lhs, rhs = infixExpr.rhs;
            TokenType operatorType = infixExpr.operator.type;

            switch (operatorType) {
                case Equal:
                    if (lhs instanceof MemberAccessExpr) {
                        MemberAccessExpr memberAccessLhs = (MemberAccessExpr) lhs;
                        FieldRef fieldRef = memberAccessLhs.fieldRef;

                        genExpr(mw, memberAccessLhs.ownerExpr);
                        genExpr(mw, rhs);

                        mw.visitFieldInsn(fieldRef.isStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
                                          memberAccessLhs.ownerExpr.getType().getInternalName(),
                                          memberAccessLhs.targetMember.literal,
                                          memberAccessLhs.getType().getDescriptor());
                    }
                    break;
                default:
                    throw new IllegalStateException(
                            String.format("Token %s is not an valid infix operator", operatorType));
            }
        } else if (expr instanceof MemberAccessExpr) {
            MemberAccessExpr memberAccessExpr = (MemberAccessExpr) expr;
            FieldRef fieldRef = memberAccessExpr.fieldRef;

            if (fieldRef.isStatic) {
                mw.visitFieldInsn(Opcodes.GETSTATIC, fieldRef.ownerClassType.getInternalName(),
                                  fieldRef.name, fieldRef.fieldType.getDescriptor());
                return;
            }

            genExpr(mw, memberAccessExpr.ownerExpr);
            mw.visitFieldInsn(Opcodes.GETFIELD, fieldRef.ownerClassType.getInternalName(),
                              fieldRef.name, fieldRef.fieldType.getDescriptor());
        } else if (expr instanceof VarExpr) {
            VarExpr varExpr = (VarExpr) expr;
            LocalVarRef localVarRef = varExpr.localVarRef;
            Type varType = varExpr.getType();

            if (varType instanceof PrimitiveType) {
                int loadOpcode = getPrimitiveLoadOpcode((PrimitiveType) varType);

                mw.visitVarInsn(loadOpcode, localVarRef.index);
            } else if (varType instanceof ObjectType) {
                ObjectType objectType = (ObjectType) varType;

                mw.visitVarInsn(Opcodes.ALOAD, localVarRef.index);
            }
        }
    }

    private static int getPrimitiveLoadOpcode(PrimitiveType varType) {
        int loadOpcode = 0;

        if (varType.equals(PrimitiveType.CHAR) || varType.equals(
                PrimitiveType.BOOL) || varType.equals(PrimitiveType.INT)) {
            loadOpcode = Opcodes.ILOAD;
        } else if (varType.equals(PrimitiveType.LONG)) {
            loadOpcode = Opcodes.LLOAD;
        } else if (varType.equals(PrimitiveType.DOUBLE)) {
            loadOpcode = Opcodes.DLOAD;
        }
        return loadOpcode;
    }

    private void genIntegerLiteral(MethodVisitor mw, IntegerLiteral integerLiteral) {
        mw.visitLdcInsn(integerLiteral.asConstant());
    }
}
