package com.yanny.ali.plugin.mods;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ReflectionUtils {
    public static <T> T copyClassData(Class<T> myClass, Object targetObject) {
        try {
            ClassAccessor classAnnotation = myClass.getAnnotation(ClassAccessor.class);

            if (classAnnotation != null) {
                // Load the inaccessible class by its name
                Class<?> targetClass = Class.forName(classAnnotation.value());

                T myObject = createObject(myClass, targetObject);

                // Iterate over all fields in your annotated class
                for (Field myField : myClass.getDeclaredFields()) {
                    FieldAccessor fieldAnnotation = myField.getAnnotation(FieldAccessor.class);

                    if (fieldAnnotation != null) {
                        // Find the corresponding field in the inaccessible class
                        Field targetField = targetClass.getDeclaredField(myField.getName());

                        targetField.setAccessible(true);
                        myField.setAccessible(true);

                        // Copy the value from the inaccessible field to your field
                        Object value = targetField.get(targetObject);

                        if (fieldAnnotation.clazz() == Object.class) {
                            myField.set(myObject, value);
                        } else {
                            myField.set(myObject, copyClassData(myField.getType(), value));
                        }
                    }
                }

                return myObject;
            }

            throw new IllegalStateException("Class is not annotated with @ClassAccessor");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static <T> T createObject(Class<T> myClass, Object object) {
        //noinspection unchecked
        return Arrays.stream((Constructor<T>[])myClass.getConstructors())
                .filter(((c) -> c.getParameterCount() == 1 && c.getParameterTypes()[0].isAssignableFrom(object.getClass())))
                .findFirst()
                .map((c) -> {
                    c.setAccessible(true);

                    try {
                        return c.newInstance(object);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }).orElseGet(() -> {
                    Constructor<T> declaredConstructor;

                    try {
                        declaredConstructor = myClass.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                    declaredConstructor.setAccessible(true);

                    try {
                        return declaredConstructor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
