package com.yanny.alicompat.accessor;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReflectionUtils {
    private static final List<Class<?>> WIDENING = List.of(byte.class, short.class, int.class, long.class, float.class, double.class);

    public static <T extends BaseAccessor<?>> T copyClassData(Class<T> myClass, Object targetObject) {
        ClassAccessor classAnnotation = myClass.getAnnotation(ClassAccessor.class);

        if (classAnnotation == null) {
            throw new IllegalStateException("Class is not annotated with @ClassAccessor");
        }

        try {
            return copyClassData(myClass, targetObject, Class.forName(classAnnotation.value()));
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T extends BaseAccessor<?>> T copyClassData(Class<T> myClass, Object targetObject, Class<?> targetClass) {
        try {
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
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static void validate(Class<?> myClass, Class<?> targetClass) throws ReflectiveOperationException {
        List<Field> targetFields = getFieldsUpTo(targetClass, Object.class);

        for (Field myField : myClass.getDeclaredFields()) {
            FieldAccessor fieldAnnotation = myField.getAnnotation(FieldAccessor.class);

            if (fieldAnnotation == null) {
                continue;
            }

            Field targetField = targetFields.stream()
                    .filter((f) -> f.getName().equals(myField.getName()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchFieldException(targetClass.getName() + "." + myField.getName()));

            if (fieldAnnotation.clazz() == Object.class) {
                if (!isAssignable(targetField.getType(), myField.getType())) {
                    throw new IllegalStateException(targetClass.getName() + "." + myField.getName() + " is " + targetField.getType().getName() + ", accessor expects " + myField.getType().getName());
                }
            } else {
                ClassAccessor nestedAnnotation = fieldAnnotation.clazz().getAnnotation(ClassAccessor.class);

                if (nestedAnnotation == null) {
                    throw new IllegalStateException("Class " + fieldAnnotation.clazz().getName() + " is not annotated with @ClassAccessor");
                }

                Class<?> nestedTarget = Class.forName(nestedAnnotation.value());

                if (!nestedTarget.isAssignableFrom(targetField.getType())) {
                    throw new IllegalStateException(targetClass.getName() + "." + myField.getName() + " is " + targetField.getType().getName() + ", accessor reads " + nestedTarget.getName());
                }

                validate(fieldAnnotation.clazz(), nestedTarget);
            }
        }
    }

    private static boolean isAssignable(Class<?> from, Class<?> to) {
        if (from.isPrimitive() && to.isPrimitive()) {
            return from == to || (WIDENING.contains(from) && WIDENING.indexOf(from) < WIDENING.indexOf(to));
        }

        return wrap(to).isAssignableFrom(wrap(from));
    }

    private static Class<?> wrap(Class<?> type) {
        return type.isPrimitive() ? MethodType.methodType(type).wrap().returnType() : type;
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
