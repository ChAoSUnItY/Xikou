package github.io.chaosunity.xikou.gen;

import github.io.chaosunity.xikou.resolver.MethodRef;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import org.objectweb.asm.Opcodes;

public final class Utils {

  static String getMethodDescriptor(AbstractType returnType, AbstractType... parameters) {
    StringBuilder builder = new StringBuilder("(");

    for (AbstractType parameter : parameters) {
      builder.append(parameter.getDescriptor());
    }

    builder.append(")");
    builder.append(returnType.getDescriptor());
    return builder.toString();
  }

  static String getMethodDescriptor(MethodRef methodRef) {
    StringBuilder builder = new StringBuilder("(");

    for (int i = 0; i < methodRef.parameterCount; i++) {
      builder.append(methodRef.parameterType[i].getDescriptor());
    }

    builder.append(")");
    builder.append(methodRef.isConstructor ? "V" : methodRef.returnType.getDescriptor());
    return builder.toString();
  }

  static int[] genLocalRefIndicesFromMethodDesc(AbstractType ownerType,
      AbstractType... parameters) {
    int length = (ownerType != null ? 1 : 0) + parameters.length;
    AbstractType[] localRefs = parameters;
    int[] indices = new int[length];

    if (ownerType != null) {
      localRefs = new AbstractType[length];
      localRefs[0] = ownerType;
      System.arraycopy(parameters, 0, localRefs, 1, parameters.length);
    }

    for (int i = 1; i < length; i++) {
      indices[i] = indices[i - 1] + localRefs[i - 1].getSize();
    }

    return indices;
  }

  static int getArrayTypeOperand(PrimitiveType type) {
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

  static int getArrayStoreOpcode(AbstractType type) {
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

  static int getArrayLoadOpcode(AbstractType type) {
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

  static int getLoadOpcode(AbstractType type) {
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

  static int getStoreOpcode(AbstractType type) {
    if (type instanceof PrimitiveType) {
      if (type == PrimitiveType.CHAR || type == PrimitiveType.BOOL || type == PrimitiveType.INT) {
        return Opcodes.ISTORE;
      } else if (type == PrimitiveType.LONG) {
        return Opcodes.LSTORE;
      } else if (type == PrimitiveType.DOUBLE) {
        return Opcodes.DSTORE;
      } else {
        return 0;
      }
    } else {
      return Opcodes.ASTORE;
    }
  }

  static int getReturnOpcode(AbstractType type) {
    if (type instanceof PrimitiveType) {
      if (type == PrimitiveType.CHAR || type == PrimitiveType.BOOL || type == PrimitiveType.INT) {
        return Opcodes.IRETURN;
      } else if (type == PrimitiveType.LONG) {
        return Opcodes.LRETURN;
      } else if (type == PrimitiveType.DOUBLE) {
        return Opcodes.DRETURN;
      } else {
        return Opcodes.RETURN;
      }
    } else {
      return Opcodes.ARETURN;
    }
  }

  static int getDupOpcode(AbstractType type) {
    return type.getSize() == 2 ? Opcodes.DUP2 : Opcodes.DUP;
  }

  static int getDupX1Opcode(AbstractType type) {
    return type.getSize() == 2 ? Opcodes.DUP2_X1 : Opcodes.DUP_X1;
  }

  static int getDupX2Opcode(AbstractType type) {
    return type.getSize() == 2 ? Opcodes.DUP2_X2 : Opcodes.DUP_X2;
  }
}
