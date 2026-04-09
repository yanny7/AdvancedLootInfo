package com.yanny.ali.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClassKeyedMap<T> implements Map<Class<?>, T> {
    private static final Class<?> NOT_FOUND_MARKER = Void.class;

    private final Map<Class<?>, T> exactMatchMap = new HashMap<>();
    private final Set<Class<?>> subTypeSupportedKeys = new HashSet<>();
    private final Map<Class<?>, Class<?>> closestKeyCache = new HashMap<>();

    @Override
    public int size() {
        return exactMatchMap.size();
    }

    @Override
    public boolean isEmpty() {
        return exactMatchMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return exactMatchMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return exactMatchMap.containsValue(value);
    }

    @Override
    public T put(Class<?> key, T value) {
        exactMatchMap.put(key, value);
        subTypeSupportedKeys.add(key);
        return value;
    }

    @Override
    public T remove(Object key) {
        throw new IllegalStateException();
    }

    @Override
    public void putAll(Map<? extends Class<?>, ? extends T> m) {
        throw new IllegalStateException();
    }

    @Override
    public void clear() {
        exactMatchMap.clear();
        subTypeSupportedKeys.clear();
        closestKeyCache.clear();
    }

    @NotNull
    @Override
    public Set<Class<?>> keySet() {
        return exactMatchMap.keySet();
    }

    @NotNull
    @Override
    public Collection<T> values() {
        return exactMatchMap.values();
    }

    @NotNull
    @Override
    public Set<Entry<Class<?>, T>> entrySet() {
        return exactMatchMap.entrySet();
    }

    @Nullable
    @Override
    public T get(Object object) {
        if (!(object instanceof Class<?>)) {
            return null;
        }

        //noinspection unchecked
        Class<T> requestedType = (Class<T>) object;

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