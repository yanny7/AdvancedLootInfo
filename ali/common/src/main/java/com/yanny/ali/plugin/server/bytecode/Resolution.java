package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class Resolution {
    private static final Map<String, Optional<ClassNode>> CLASS_NODES = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Method>> METHODS = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Field>> FIELDS = new ConcurrentHashMap<>();

    private Resolution() {
    }

    static void clearCaches() {
        CLASS_NODES.clear();
        METHODS.clear();
        FIELDS.clear();
    }

    @NotNull
    static Class<?> load(String internalName, ClassLoader loader) {
        return load(internalName, loader, false);
    }

    @NotNull
    static Class<?> load(String internalName, @Nullable ClassLoader loader, boolean initialize) {
        try {
            return Class.forName(internalName.replace('/', '.'), initialize, loader);
        } catch (Throwable e) {
            throw new Fail("class " + internalName);
        }
    }

    static boolean isJdk(Class<?> type) {
        String name = type.getName();

        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.") || name.startsWith("sun.") || name.startsWith("com.sun.");
    }

    @Nullable
    static ClassNode classNode(Class<?> type) {
        if (type.isHidden() || type.isArray() || type.isPrimitive()) {
            return null;
        }

        String internalName = Type.getInternalName(type);

        return CLASS_NODES.computeIfAbsent(internalName, (name) -> {
            ClassLoader loader = type.getClassLoader() != null ? type.getClassLoader() : ClassLoader.getSystemClassLoader();

            try (InputStream is = loader.getResourceAsStream(name + ".class")) {
                if (is == null) {
                    return Optional.empty();
                }

                ClassNode node = new ClassNode();

                new ClassReader(is).accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                return Optional.of(node);
            } catch (Throwable e) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    @Nullable
    static MethodNode findMethod(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }

        return null;
    }

    @Nullable
    static Declared declaration(Class<?> start, String name, String desc, boolean exact) {
        for (Class<?> type = start; type != null; type = exact ? null : type.getSuperclass()) {
            if (isJdk(type)) {
                if (method(type, name, desc) != null) {
                    return new JdkDeclaration(type);
                }

                break;
            }

            ClassNode node = classNode(type);
            MethodNode method = node != null ? findMethod(node, name, desc) : null;

            if (method != null && (method.access & Opcodes.ACC_ABSTRACT) == 0) {
                return new Declaration(type, method);
            }
        }

        Deque<Class<?>> interfaces = new ArrayDeque<>();

        for (Class<?> type = start; type != null; type = type.getSuperclass()) {
            interfaces.addAll(Arrays.asList(type.getInterfaces()));
        }

        while (!interfaces.isEmpty()) {
            Class<?> type = interfaces.poll();

            if (isJdk(type)) {
                continue;
            }

            ClassNode node = classNode(type);
            MethodNode method = node != null ? findMethod(node, name, desc) : null;

            if (method != null && (method.access & Opcodes.ACC_ABSTRACT) == 0) {
                return new Declaration(type, method);
            }

            interfaces.addAll(Arrays.asList(type.getInterfaces()));
        }

        return null;
    }

    @Nullable
    static Method method(Class<?> owner, String name, String desc) {
        return METHODS.computeIfAbsent(owner.getName() + '.' + name + desc, (key) -> {
            Deque<Class<?>> types = new ArrayDeque<>();

            types.add(owner);

            while (!types.isEmpty()) {
                Class<?> type = types.poll();

                for (Method method : type.getDeclaredMethods()) {
                    if (method.getName().equals(name) && Type.getMethodDescriptor(method).equals(desc)) {
                        return Optional.of(method);
                    }
                }

                if (type.getSuperclass() != null) {
                    types.add(type.getSuperclass());
                }

                types.addAll(Arrays.asList(type.getInterfaces()));
            }

            return Optional.empty();
        }).orElse(null);
    }

    static Constructor<?> constructor(Class<?> owner, String desc) {
        for (Constructor<?> constructor : owner.getDeclaredConstructors()) {
            if (Type.getConstructorDescriptor(constructor).equals(desc)) {
                return constructor;
            }
        }

        throw new Fail("no constructor " + owner.getName());
    }

    @NotNull
    static Field field(Class<?> owner, String name) {
        return FIELDS.computeIfAbsent(owner.getName() + '.' + name, (key) -> {
            Deque<Class<?>> types = new ArrayDeque<>();

            types.add(owner);

            while (!types.isEmpty()) {
                Class<?> type = types.poll();

                for (Field field : type.getDeclaredFields()) {
                    if (field.getName().equals(name)) {
                        try {
                            field.setAccessible(true);
                            return Optional.of(field);
                        } catch (Throwable e) {
                            return Optional.empty();
                        }
                    }
                }

                if (type.getSuperclass() != null) {
                    types.add(type.getSuperclass());
                }

                types.addAll(Arrays.asList(type.getInterfaces()));
            }

            return Optional.empty();
        }).orElseThrow(() -> new Fail("field " + owner.getName() + "." + name));
    }

    sealed interface Declared permits Declaration, JdkDeclaration {
        Class<?> owner();
    }

    record Declaration(Class<?> owner, MethodNode method) implements Declared {
    }

    record JdkDeclaration(Class<?> owner) implements Declared {
    }
}
