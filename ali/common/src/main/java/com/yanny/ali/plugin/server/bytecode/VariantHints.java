package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.network.syncher.EntityDataAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.*;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;

final class VariantHints {
    private static final String ACCESSOR = Type.getDescriptor(EntityDataAccessor.class);

    private VariantHints() {}

    @Unmodifiable
    @NotNull
    static List<List<VariantHint>> of(List<Outcome> outcomes, Class<?> entityClass) {
        Map<EntityDataAccessor<?>, Set<Object>> data = new LinkedHashMap<>();
        Set<Integer> numbers = new TreeSet<>();
        List<Set<Object>> paths = new ArrayList<>();

        for (Outcome outcome : outcomes) {
            Set<Object> keys = new LinkedHashSet<>();

            for (Decision decision : outcome.decisions()) {
                if (decision.kind() == Decision.Kind.RANDOM) {
                    continue;
                }
                if (decision.value() instanceof Integer number) {
                    numbers.add(number);
                }

                collect(decision.subject(), decision.kind() == Decision.Kind.EQUALS ? decision.value() : null, data, keys);
            }

            if (!keys.isEmpty()) {
                paths.add(keys);
            }
        }

        Map<Origin, List<Method>> setters = new HashMap<>();
        Set<List<VariantHint>> groups = new LinkedHashSet<>();

        for (Set<Object> keys : paths) {
            Set<VariantHint> group = new LinkedHashSet<>();

            for (Object key : keys) {
                if (key instanceof EntityDataAccessor<?> accessor) {
                    group.add(new VariantHint.Data(accessor, List.copyOf(data.get(accessor))));
                } else if (key instanceof Origin getter) {
                    for (Method setter : setters.computeIfAbsent(getter, (g) -> setters(entityClass, g))) {
                        List<Object> domain = domain(setter.getParameterTypes()[0], numbers);

                        if (!domain.isEmpty()) {
                            group.add(new VariantHint.Setter(setter, domain));
                        }
                    }
                }
            }

            if (!group.isEmpty()) {
                groups.add(List.copyOf(group));
            }
        }

        return groups.stream().filter((group) -> groups.stream().noneMatch((other) -> other != group && other.size() > group.size() && other.containsAll(group))).toList();
    }

    private static void collect(@Nullable Object part, @Nullable Object value, Map<EntityDataAccessor<?>, Set<Object>> data, Set<Object> keys) {
        if (!(part instanceof Origin origin)) {
            return;
        }

        EntityDataAccessor<?> accessor = accessor(origin);

        if (accessor != null) {
            Set<Object> values = data.computeIfAbsent(accessor, (a) -> new LinkedHashSet<>());

            if (value != null) {
                values.add(value);
            }

            keys.add(accessor);
            return;
        }
        if (Origin.SELF.equals(origin.receiver()) && origin.args().isEmpty() && origin.member().indexOf('(') > 0) {
            keys.add(origin);
            return;
        }

        collect(origin.receiver(), origin.args().isEmpty() ? value : null, data, keys);
        origin.args().forEach((arg) -> collect(arg, null, data, keys));
    }

    @Nullable
    private static EntityDataAccessor<?> accessor(Origin origin) {
        RuntimeNames.Ref get = RuntimeNames.ENTITY_DATA_GET;

        if (get != null && origin.member().equals(get.name() + get.desc()) && origin.args().size() == 1 && origin.args().getFirst() instanceof EntityDataAccessor<?> accessor) {
            return accessor;
        }

        return null;
    }

    @NotNull
    private static List<Method> setters(Class<?> entityClass, Origin getter) {
        int split = getter.member().indexOf('(');

        if (!(Resolution.declaration(entityClass, getter.member().substring(0, split), getter.member().substring(split), false) instanceof Declaration declaration)) {
            return List.of();
        }

        Set<String> accessors = accessors(declaration.method());
        List<Method> setters = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        if (accessors.isEmpty()) {
            return setters;
        }

        for (Class<?> type = entityClass; type != null; type = type.getSuperclass()) {
            ClassNode node = Resolution.classNode(type);

            if (node == null) {
                continue;
            }

            for (MethodNode method : node.methods) {
                if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT)) != 0 || Type.getArgumentTypes(method.desc).length != 1
                        || Type.getReturnType(method.desc) != Type.VOID_TYPE || !seen.add(method.name + method.desc)
                        || Collections.disjoint(accessors(method), accessors) || !writesData(method)) {
                    continue;
                }

                Method setter = Resolution.method(type, method.name, method.desc);

                if (setter != null) {
                    setters.add(setter);
                }
            }
        }

        return setters;
    }

    @NotNull
    private static Set<String> accessors(MethodNode method) {
        Set<String> accessors = new HashSet<>();

        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC && field.desc.equals(ACCESSOR)) {
                accessors.add(field.owner + "." + field.name);
            }
        }

        return accessors;
    }

    private static boolean writesData(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && RuntimeNames.is(RuntimeNames.ENTITY_DATA_SET, call)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static List<Object> domain(Class<?> type, Set<Integer> numbers) {
        if (type.isEnum()) {
            return List.of(type.getEnumConstants());
        }
        if (type == boolean.class) {
            return List.of(false, true);
        }
        if (type == int.class) {
            Set<Object> values = new LinkedHashSet<>();

            values.add(0);
            values.addAll(numbers);
            return List.copyOf(values);
        }

        return List.of();
    }
}
