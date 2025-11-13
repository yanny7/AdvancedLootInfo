package com.yanny.ali.manager;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassKeyedMap<T> {
    private static final Class<?> NOT_FOUND_MARKER = Void.class;

    private final Map<Class<?>, T> exactMatchMap = new HashMap<>();
    private final Set<Class<?>> subTypeSupportedKeys = new HashSet<>();
    private final Map<Class<?>, Class<?>> closestKeyCache = new HashMap<>();

    public void put(Class<?> key, T value) {
        exactMatchMap.put(key, value);
        subTypeSupportedKeys.add(key);
    }

    @Nullable
    public T get(Class<?> requestedType) {
        if (exactMatchMap.containsKey(requestedType)) {
            return exactMatchMap.get(requestedType);
        }

        Class<?> closestKey = closestKeyCache.get(requestedType);

        if (closestKey != null) {
            if (closestKey == NOT_FOUND_MARKER) {
                return null;
            }

            return exactMatchMap.get(closestKey);
        }

        closestKey = findClosestSupertypeKey(requestedType);

        if (closestKey != null) {
            closestKeyCache.put(requestedType, closestKey);
            return exactMatchMap.get(closestKey);
        } else {
            closestKeyCache.put(requestedType, NOT_FOUND_MARKER);
            return null;
        }
    }

    private Class<?> findClosestSupertypeKey(Class<?> requestedType) {
        Class<?> current = requestedType.getSuperclass();

        while (current != null) {
            if (subTypeSupportedKeys.contains(current)) {
                return current;
            }

            current = current.getSuperclass();
        }

        return findClosestInterfaceKey(requestedType, subTypeSupportedKeys);
    }

    @Nullable
    private Class<?> findClosestInterfaceKey(Class<?> clazz, Set<Class<?>> targetKeys) {
        for (Class<?> iface : clazz.getInterfaces()) {
            if (targetKeys.contains(iface)) {
                return iface;
            }

            Class<?> found = findClosestInterfaceKey(iface, targetKeys);

            if (found != null) {
                return found;
            }
        }

        if (!clazz.isInterface() && clazz.getSuperclass() != null) {
            return findClosestInterfaceKey(clazz.getSuperclass(), targetKeys);
        }

        return null;
    }
}