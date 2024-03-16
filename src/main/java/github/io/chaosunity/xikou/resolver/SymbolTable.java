package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.BoundableDecl;
import github.io.chaosunity.xikou.ast.ClassDecl;
import github.io.chaosunity.xikou.ast.ConstructorDecl;
import github.io.chaosunity.xikou.ast.EnumDecl;
import github.io.chaosunity.xikou.ast.EnumVariantDecl;
import github.io.chaosunity.xikou.ast.FieldDecl;
import github.io.chaosunity.xikou.resolver.types.AbstractType;
import github.io.chaosunity.xikou.resolver.types.ClassType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.TypeUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SymbolTable {

  public int declCount = 0;
  public BoundableDecl[] decls = new BoundableDecl[1];

  public AbstractType getType(String path) {
    for (int i = 0; i < declCount; i++) {
      BoundableDecl decl = decls[i];
      ClassType type = decl.getType();

      if (type.getInternalName().equals(path)) {
        return type;
      }
    }

    try {
      Class clazz = Class.forName(path);

      return getTypeFromClass(clazz);
    } catch (ClassNotFoundException ignored) {
    }

    return null;
  }

  public void registerDecl(BoundableDecl decl) {
    if (declCount >= decls.length) {
      BoundableDecl[] newArr = new BoundableDecl[decls.length * 2];
      System.arraycopy(decls, 0, newArr, 0, decls.length);
      decls = newArr;
    }

    decls[declCount++] = decl;
  }

  public MethodRef getMethod(AbstractType ownerType, String name, AbstractType[] parameterTypes) {
    for (int i = 0; i < declCount; i++) {
      BoundableDecl decl = decls[i];

      if (!decl.getType().equals(ownerType)) {
        continue;
      }

      if (decl instanceof ClassDecl) {
        // TODO: Support method decl in ClassDecl

      } else if (decl instanceof EnumDecl) {
        // TODO: Support method decl in EnumDecl
      }
    }

    try {
      String ownerClassInternalName = ownerType.getInternalName();
      Class clazz = Class.forName(ownerClassInternalName.replace('/', '.'));
      Method[] methods = clazz.getMethods(); // TODO: Narrow down resolution if current class does not have matching method

      for (Method method : methods) {
        if (!method.getName().equals(name)) {
          continue;
        }

        Class[] parameterClazzes = method.getParameterTypes();
        boolean resolutionFailure = false;

        if (parameterClazzes.length != parameterTypes.length) {
          continue;
        }

        AbstractType[] resolvedParameterTypes = new AbstractType[parameterClazzes.length];

        for (int i = 0; i < parameterClazzes.length; i++) {
          AbstractType resolvedParameterType = getTypeFromClass(parameterClazzes[i]);

          if (!TypeUtils.isInstanceOf(parameterTypes[i], resolvedParameterType)) {
            resolutionFailure = true;
            break;
          }

          resolvedParameterTypes[i] = resolvedParameterType;
        }

        if (resolutionFailure) {
          continue;
        }

        return new MethodRef(getTypeFromClass(method.getDeclaringClass()), name,
            resolvedParameterTypes.length, resolvedParameterTypes,
            getTypeFromClass(method.getReturnType()), Modifier.isStatic(method.getModifiers()),
            false);
      }
    } catch (ClassNotFoundException e) {
    }

    return null;
  }

  public FieldRef getField(AbstractType ownerType, String name) {
    for (int i = 0; i < declCount; i++) {
      BoundableDecl decl = decls[i];

      if (!decl.getType().equals(ownerType)) {
        continue;
      }

      if (decl instanceof ClassDecl) {
        ClassDecl classDecl = (ClassDecl) decl;

        return getFieldRef(classDecl.fieldCount, classDecl.fieldDecls, ownerType, name);
      } else if (decl instanceof EnumDecl) {
        EnumDecl enumDecl = (EnumDecl) decl;

        for (int j = 0; j < enumDecl.variantCount; j++) {
          EnumVariantDecl variantDecl = enumDecl.enumVariantDecls[j];

          if (variantDecl.name.literal.equals(name)) {
            return new FieldRef(ownerType, true, name, enumDecl.enumType);
          }
        }

        return getFieldRef(enumDecl.fieldCount, enumDecl.fieldDecls, ownerType, name);
      }
    }

    try {
      String ownerClassInternalName = ownerType.getInternalName();
      Class clazz = Class.forName(ownerClassInternalName.replace('/', '.'));
      Field field = clazz.getField(name);
      AbstractType fieldType = getFieldType(name, clazz);

      return new FieldRef(ownerType, Modifier.isStatic(field.getModifiers()), name,
          fieldType);
    } catch (ClassNotFoundException | NoSuchFieldException ignored) {
    }

    return null;
  }

  private FieldRef getFieldRef(int fieldCount, FieldDecl[] fieldDecls, AbstractType ownerType,
      String name) {
    for (int j = 0; j < fieldCount; j++) {
      FieldDecl fieldDecl = fieldDecls[j];

      if (fieldDecl.name.literal.equals(name)) {
        return new FieldRef(ownerType, false, name, fieldDecl.typeRef.getType());
      }
    }

    return null;
  }

  public MethodRef[] getConstructors(AbstractType ownerType) {
    for (int i = 0; i < declCount; i++) {
      BoundableDecl decl = decls[i];

      if (!decl.getType().equals(ownerType)) {
        continue;
      }

      ConstructorDecl constructorDecl = decl.getConstructorDecl();

      if (constructorDecl == null) {
        continue;
      }

      AbstractType[] parameterTypes = new AbstractType[constructorDecl.parameterCount];

      for (int j = 0; j < constructorDecl.parameterCount; j++) {
        parameterTypes[j] = constructorDecl.parameters[j].typeRef.getType();
      }

      return new MethodRef[]{new MethodRef(ownerType, "<init>", constructorDecl.statementCount,
          parameterTypes,
          constructorDecl.implDecl.boundDecl.getType(),
          false,
          true)};
    }

    try {
      String ownerClassInternalName = ownerType.getInternalName();
      Class clazz = Class.forName(ownerClassInternalName.replace('/', '.'));
      Constructor[] constructors = clazz.getConstructors();
      MethodRef[] constuctorMethodRefs = new MethodRef[constructors.length];

      for (int i = 0; i < constructors.length; i++) {
        constuctorMethodRefs[i] = getMethodRefFromConstrutor(ownerType, constructors[i]);
      }

      return constuctorMethodRefs;
    } catch (ClassNotFoundException ignored) {
    }

    return null;
  }

  private static MethodRef getMethodRefFromConstrutor(AbstractType ownerType,
      Constructor constructor) {
    int parameterCount = constructor.getParameterCount();
    Class[] parameterReflectionTypes = constructor.getParameterTypes();
    AbstractType[] parameterTypes = new AbstractType[parameterCount];

    for (int i = 0; i < parameterCount; i++) {
      parameterTypes[i] = getTypeFromClass(parameterReflectionTypes[i]);
    }

    return new MethodRef(ownerType, "<init>", parameterCount, parameterTypes, ownerType, false,
        true);
  }

  private static AbstractType getFieldType(String name, Class clazz) throws NoSuchFieldException {
    Field field = clazz.getField(name);

    return getTypeFromClass(field.getType());
  }

  private static AbstractType getTypeFromClass(Class clazz) {
    if (clazz.isPrimitive()) {
      for (PrimitiveType primitiveType : PrimitiveType.values()) {
        if (clazz.getCanonicalName().equals(primitiveType.internalName)) {
          return primitiveType;
        }
      }

      throw new IllegalStateException("Unreachable");
    } else {
      String internalName = clazz.getCanonicalName().replace(".", "/");
      Class superClazz = clazz.getSuperclass();
      Class[] interfaceClazzes = clazz.getInterfaces();
      ClassType[] interfaceClasses = new ClassType[interfaceClazzes.length];

      for (int i = 0; i < interfaceClasses.length; i++) {
        AbstractType superclass = getTypeFromClass(interfaceClazzes[i]);

        if (!(superclass instanceof ClassType)) {
          throw new IllegalStateException("ICE: Interface is not an ClassType");
        }

        interfaceClasses[i] = (ClassType) superclass;
      }

      if (superClazz == null) {
        if (clazz.isInterface()) {
          return new ClassType(null, interfaceClasses, true, internalName);
        } else {
          return ClassType.OBJECT_CLASS_TYPE;
        }
      }

      AbstractType superclass = getTypeFromClass(superClazz);

      if (!(superclass instanceof ClassType)) {
        throw new IllegalStateException("ICE: Superclass is not an ClassType");
      }

      return new ClassType((ClassType) superclass, interfaceClasses, false, internalName);
    }
  }
}
