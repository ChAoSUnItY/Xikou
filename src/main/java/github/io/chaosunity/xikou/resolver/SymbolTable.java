package github.io.chaosunity.xikou.resolver;

import github.io.chaosunity.xikou.ast.ClassDecl;
import github.io.chaosunity.xikou.ast.FieldDecl;
import github.io.chaosunity.xikou.resolver.types.ObjectType;
import github.io.chaosunity.xikou.resolver.types.PrimitiveType;
import github.io.chaosunity.xikou.resolver.types.Type;

import java.lang.reflect.Field;

public class SymbolTable {
    public int classDeclCount = 0;
    public ClassDecl[] classDecls = new ClassDecl[1];

    public void registerClassDecl(ClassDecl classDecl) {
        if (classDeclCount >= classDecls.length) {
            ClassDecl[] newArr = new ClassDecl[classDecls.length * 2];
            System.arraycopy(classDecls, 0, newArr, 0, classDecls.length);
            classDecls = newArr;
        }

        classDecls[classDeclCount++] = classDecl;
    }

    public FieldRef getField(Type ownerClassType, String name) {
        for (int i = 0; i < classDeclCount; i++) {
            ClassDecl classDecl = classDecls[i];

            if (classDecl.getType().equals(ownerClassType)) {
                for (int j = 0; j < classDecl.fieldCount; j++) {
                    FieldDecl fieldDecl = classDecl.fieldDecls[j];

                    if (fieldDecl.name.literal.equals(name)) {
                        return new FieldRef(ownerClassType, name, fieldDecl.typeRef.getType());
                    }
                }

                return null;
            }
        }

        try {
            String ownerClassInternalName = ownerClassType.getInternalName();
            Class clazz = Class.forName(ownerClassInternalName.replace('/', '.'));
            Type fieldType = getFieldType(name, clazz);

            return new FieldRef(ownerClassType, name, fieldType);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Type getFieldType(String name, Class clazz) throws NoSuchFieldException {
        Field field = clazz.getField(name);
        Class fieldTypeClazz = field.getType();
        Type fieldType = null;

        if (fieldTypeClazz.isPrimitive()) {
            for (PrimitiveType primitiveType : PrimitiveType.values()) {
                if (fieldTypeClazz.getCanonicalName().equals(primitiveType.internalName)) {
                    fieldType = primitiveType;
                    break;
                }
            }

            if (fieldType == null)
                throw new IllegalStateException("Unreachable");
        } else {
            fieldType = new ObjectType(fieldTypeClazz.getCanonicalName().replace(".", "/"));
        }
        return fieldType;
    }
}
