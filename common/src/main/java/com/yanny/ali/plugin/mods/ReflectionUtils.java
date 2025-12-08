package com.yanny.ali.plugin.mods;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReflectionUtils {
    public static <T extends BaseAccessor<?>> T copyClassData(Class<T> myClass, Object targetObject) {
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
                        Optional<Field> optional = getFieldsUpTo(targetClass, Object.class).stream().filter((f) -> f.getName().equals(myField.getName())).findFirst();

                        if (optional.isPresent()) {
                            Field targetField = optional.get();

                            targetField.setAccessible(true);
                            myField.setAccessible(true);

                            // Copy the value from the inaccessible field to your field
                            Object value = targetField.get(targetObject);

                            if (fieldAnnotation.clazz() == Object.class) {
                                myField.set(myObject, value);
                            } else {
                                //noinspection unchecked
                                myField.set(myObject, copyClassData((Class<T>) fieldAnnotation.clazz(), value));
                            }
                        } else {
                            throw new NoSuchFieldException(myField.getName());
                        }
                    }
                }

                return myObject;
            }

            throw new IllegalStateException("Class is not annotated with @ClassAccessor");
        } catch (Throwable e) {
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

    @NotNull
    private static List<Field> getFieldsUpTo(Class<?> startClass, @Nullable Class<?> exclusiveParent) {
        List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields = getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }
}
