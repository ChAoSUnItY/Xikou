package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.*;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SymbolTable {
    public int declCount = 0;
    public BoundableDecl[] decls = new BoundableDecl[1];

    public Type getType(String path) {
        for (int i = 0; i < declCount; i++) {
            BoundableDecl decl = decls[i];
            Type type = decl.getType();

            if (type.getInternalName().equals(path)) {
                return type;
            }
        }

        try {
            Class ignored = Class.forName(path);

            return new ObjectType(path);
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

    public FieldRef getField(Type ownerType, String name) {
        for (int i = 0; i < declCount; i++) {
            BoundableDecl decl = decls[i];

            if (!decl.getType().equals(ownerType)) continue;

            if (decl instanceof ClassDecl) {
                ClassDecl classDecl = (ClassDecl) decl;

                for (int j = 0; j < classDecl.fieldCount; j++) {
                    FieldDecl fieldDecl = classDecl.fieldDecls[j];

                    if (fieldDecl.name.literal.equals(name)) {
                        return new FieldRef(ownerType, false, name, fieldDecl.typeRef.getType());
                    }
                }

                return null;
            } else if (decl instanceof EnumDecl) {
                EnumDecl enumDecl = (EnumDecl) decl;

                for (int j = 0; j < enumDecl.variantCount; j++) {
                    EnumVariantDecl variantDecl = enumDecl.enumVariantDecls[j];

                    if (variantDecl.name.literal.equals(name)) {
                        return new FieldRef(ownerType, true, name, enumDecl.enumType);
                    }
                }

                for (int j = 0; j < enumDecl.fieldCount; j++) {
                    FieldDecl fieldDecl = enumDecl.fieldDecls[j];

                    if (fieldDecl.name.literal.equals(name)) {
                        return new FieldRef(ownerType, false, name, fieldDecl.typeRef.getType());
                    }
                }

                return null;
            }
        }

        try {
            String ownerClassInternalName = ownerType.getInternalName();
            Class clazz = Class.forName(ownerClassInternalName.replace('/', '.'));
            Field field = clazz.getField(name);
            Type fieldType = getFieldType(name, clazz);

            return new FieldRef(ownerType, Modifier.isStatic(field.getModifiers()), name, fieldType);
        } catch (ClassNotFoundException | NoSuchFieldException ignored) {
        }

        return null;
    }

    private MethodRef[] getConstructors(Type ownerType) {
        for (int i = 0; i < declCount; i++) {
            BoundableDecl decl = decls[i];

            if (!decl.getType().equals(ownerType)) continue;

            PrimaryConstructorDecl constructorDecl = decl.getImplDecl().primaryConstructorDecl;
            Parameters parameters = constructorDecl.parameters;
            Type[] parameterTypes = new Type[parameters.parameterCount];

            for (int j = 0; j < parameters.parameterCount; j++) {
                parameterTypes[j] = parameters.parameters[j].typeRef.getType();
            }

            return new MethodRef[]{new MethodRef(ownerType, "<init>", constructorDecl.exprCount,
                                                 parameterTypes,
                                                 constructorDecl.implDecl.boundDecl.getType())};
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

    private static MethodRef getMethodRefFromConstrutor(Type ownerType, Constructor constructor) {
        int parameterCount = constructor.getParameterCount();
        Class[] parameterReflectionTypes = constructor.getParameterTypes();
        Type[] parameterTypes = new Type[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            parameterTypes[i] = getTypeFromClass(parameterReflectionTypes[i]);
        }

        return new MethodRef(ownerType, "<init>", parameterCount, parameterTypes, ownerType);
    }

    private static Type getFieldType(String name, Class clazz) throws NoSuchFieldException {
        Field field = clazz.getField(name);

        return getTypeFromClass(field.getType());
    }

    private static Type getTypeFromClass(Class clazz) {
        if (clazz.isPrimitive()) {
            for (PrimitiveType primitiveType : PrimitiveType.values()) {
                if (clazz.getCanonicalName().equals(primitiveType.internalName)) {
                    return primitiveType;
                }
            }

            throw new IllegalStateException("Unreachable");
        } else {
            return new ObjectType(clazz.getCanonicalName().replace(".", "/"));
        }
    }
}
