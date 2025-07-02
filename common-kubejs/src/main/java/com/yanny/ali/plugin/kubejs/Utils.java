package com.yanny.ali.plugin.kubejs;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Utils {
    @NotNull
    public static <T> List<T> getCapturedInstances(Predicate<?> predicate, Class<T> requiredType) {
        List<T> instances = new ArrayList<>();

        try {
            Field[] fields = predicate.getClass().getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object entry = field.get(predicate);

                if (requiredType.isInstance(entry)) {
                    instances.add(requiredType.cast(entry));
                }
            }
        } catch (IllegalAccessException e) {
            System.err.println("Error while accessing field: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security error while accessing field: " + e.getMessage());
        }

        return instances;
    }
}
