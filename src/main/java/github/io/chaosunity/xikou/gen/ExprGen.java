package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.ast.expr.*;
import github.io.chaosunity.xikou.lexer.TokenType;
import github.io.chaosunity.xikou.resolver.FieldRef;
import github.io.chaosunity.xikou.resolver.LocalVarRef;
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
        } else if (expr instanceof IntegerLiteralExpr) {
            genIntegerLiteral(mw, (IntegerLiteralExpr) expr);
        }
    }

    private void genInfixExpr(MethodVisitor mw, InfixExpr infixExpr) {
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
    }

    private void genMemberAccessExpr(MethodVisitor mw, MemberAccessExpr memberAccessExpr) {
        FieldRef fieldRef = memberAccessExpr.fieldRef;

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
                           "<init>", Utils.getMethodDescriptor(PrimitiveType.VOID, types), false);
    }

    private void genArrayInitExpr(MethodVisitor mw, ArrayInitExpr arrayInitExpr) {
        AbstractType componentType = arrayInitExpr.getComponentType();

        if (arrayInitExpr.initExprCount > 0) mw.visitLdcInsn(arrayInitExpr.initExprCount);
        else genExpr(mw, arrayInitExpr.sizeExpr);

        if (componentType instanceof PrimitiveType) {
            mw.visitIntInsn(Opcodes.NEWARRAY, getArrayTypeOperand((PrimitiveType) componentType));
        } else if (componentType instanceof ClassType) {
            mw.visitTypeInsn(Opcodes.ANEWARRAY, componentType.getInternalName());
        } else {
            // Must be array type
            ArrayType componentArrayType = (ArrayType) componentType;
        }

        for (int i = 0; i < arrayInitExpr.initExprCount; i++) {
            Expr initExpr = arrayInitExpr.initExprs[i];

            mw.visitInsn(Opcodes.DUP);
            mw.visitLdcInsn(i);
            genExpr(mw, initExpr);
            mw.visitInsn(getArrayStoreOpcode(initExpr.getType()));
        }
    }

    private void genNameExpr(MethodVisitor mw, NameExpr nameExpr) {
        LocalVarRef localVarRef = nameExpr.localVarRef;
        AbstractType varType = nameExpr.getType();

        mw.visitVarInsn(getLoadOpcode(varType), localVarRef.index);
    }

    private void genIntegerLiteral(MethodVisitor mw, IntegerLiteralExpr integerLiteralExpr) {
        mw.visitLdcInsn(integerLiteralExpr.asConstant());
    }

    private void genCharLiteralExpr(MethodVisitor mw, CharLiteralExpr charLiteralExpr) {
        mw.visitLdcInsn(charLiteralExpr.characterToken.literal.charAt(0));
    }

    private static int getArrayTypeOperand(PrimitiveType type) {
        switch (type) {
            case CHAR:
                return Opcodes.T_CHAR;
            case BOOL:
                return Opcodes.T_BOOLEAN;
            case INT:
                return Opcodes.T_INT;
            case LONG:
                return Opcodes.T_LONG;
            case FLOAT:
                return Opcodes.T_FLOAT;
            case DOUBLE:
                return Opcodes.T_DOUBLE;
            default:
                return 0;
        }
    }

    private static int getArrayStoreOpcode(AbstractType type) {
        if (type instanceof PrimitiveType) {
            switch ((PrimitiveType) type) {
                case CHAR:
                    return Opcodes.CASTORE;
                case BOOL:
                    return Opcodes.BASTORE;
                case INT:
                    return Opcodes.IASTORE;
                case LONG:
                    return Opcodes.LASTORE;
                case FLOAT:
                    return Opcodes.FASTORE;
                case DOUBLE:
                    return Opcodes.DASTORE;
                default:
                    return 0;
            }
        } else {
            return Opcodes.AASTORE;
        }
    }

    private static int getArrayLoadOpcode(AbstractType type) {
        if (type instanceof PrimitiveType) {
            switch ((PrimitiveType) type) {
                case CHAR:
                    return Opcodes.CALOAD;
                case BOOL:
                    return Opcodes.BALOAD;
                case INT:
                    return Opcodes.IALOAD;
                case LONG:
                    return Opcodes.LALOAD;
                case FLOAT:
                    return Opcodes.FALOAD;
                case DOUBLE:
                    return Opcodes.DALOAD;
                default:
                    return 0;
            }
        } else {
            return Opcodes.AALOAD;
        }
    }

    private static int getLoadOpcode(AbstractType type) {
        if (type instanceof PrimitiveType) {
            if (type == PrimitiveType.CHAR || type == PrimitiveType.BOOL || type == PrimitiveType.INT) {
                return Opcodes.ILOAD;
            } else if (type == PrimitiveType.LONG) {
                return Opcodes.LLOAD;
            } else if (type == PrimitiveType.DOUBLE) {
                return Opcodes.DLOAD;
            } else {
                return 0;
            }
        } else {
            return Opcodes.ALOAD;
        }
    }
}
