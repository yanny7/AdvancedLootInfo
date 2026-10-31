package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;

public final class LootTableAnalysis {
    private static final Map<EntityType<?>, List<Outcome>> DEFAULTS = new ConcurrentHashMap<>();
    private static final CallSites.Sink SINK = new CallSites.Sink(RuntimeNames.LOOT_DATA_GET_LOOT_TABLE, LootDataResolver.class, 0);

    private LootTableAnalysis() {}

    @NotNull
    public static ComputedLootTables getEntityLootTables(RegistryAccess registries, EntityType<?> type, Class<?> entityClass) {
        if (!RuntimeNames.LOOT_AVAILABLE) {
            return new ComputedLootTables(Map.of(), Map.of());
        }

        ResourceLocation typeDefault = type.getDefaultLootTable();
        List<LootSubject.Observation> own = new ArrayList<>();

        for (Outcome outcome : defaults(registries, type, entityClass)) {
            if (outcome instanceof Outcome.Success success && success.result() != null) {
                own.addAll(LootTableModel.observations(success.result(), success.decisions()));
            }
        }

        return new ComputedLootTables(LootAggregation.tables(own, typeDefault, true), LootAggregation.tables(rolled(registries, Profile.LOOT, entityClass,
                () -> new EntitySubject(type, entityClass), self(entityClass)), typeDefault, false));
    }

    @NotNull
    public static Set<ResourceLocation> getRolledLootTables(RegistryAccess registries, Object instance) {
        if (!RuntimeNames.LOOT_AVAILABLE) {
            return Set.of();
        }

        return tables(rolled(registries, Profile.ROLLED, instance.getClass(), () -> new InstanceSubject(instance), new Value.Known(instance)));
    }

    @NotNull
    public static Set<ResourceLocation> getReferencedLootTables(Class<?> anyClassOfJar) {
        ModJarIndex index = RuntimeNames.LOOT_AVAILABLE ? ModJarIndex.of(anyClassOfJar) : null;
        Set<ResourceLocation> tables = new LinkedHashSet<>();

        if (index == null) {
            return tables;
        }

        try {
            for (ModJarIndex.Caller caller : index.callers(SINK)) {
                if (caller.entity()) {
                    continue;
                }
                if (caller.source() instanceof CallSites.Constant constant && constant.value() instanceof ResourceLocation table) {
                    tables.add(table);
                } else if (caller.source() instanceof CallSites.Param param) {
                    for (ModJarIndex.Caller outer : index.callersOf(caller.owner(), caller.method().name, caller.method().desc, param.index())) {
                        if (!outer.entity() && outer.source() instanceof CallSites.Constant constant && constant.value() instanceof ResourceLocation table) {
                            tables.add(table);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        return tables;
    }

    static List<Outcome> defaults(RegistryAccess registries, EntityType<?> type, Class<?> entityClass) {
        return DEFAULTS.computeIfAbsent(type, (t) -> {
            Declaration own = SelfModel.ownDeclaration(entityClass, RuntimeNames.GET_DEFAULT_LOOT_TABLE.name(), RuntimeNames.GET_DEFAULT_LOOT_TABLE.desc(), false);

            if (own == null) {
                return List.of();
            }

            try {
                return Explorer.explore(Profile.LOOT, registries, (interpreter) -> {
                    interpreter.subject = new EntitySubject(type, entityClass);
                    return interpreter.machine.execute(own.owner(), own.method(), List.of(self(entityClass)), 0);
                });
            } catch (Throwable e) {
                return List.of();
            }
        });
    }

    static void clearCaches() {
        DEFAULTS.clear();
    }

    @NotNull
    private static List<LootSubject.Observation> rolled(RegistryAccess registries, Profile profile, Class<?> type, Supplier<LootSubject> subject, Value self) {
        List<LootSubject.Observation> observations = new ArrayList<>();
        Set<CallSites.Site> computed = new LinkedHashSet<>();

        try {
            for (Declaration entry : entries(type)) {
                for (CallSites.Site site : CallSites.find(type, entry.owner(), entry.method(), SINK)) {
                    if (site.source() instanceof CallSites.Constant constant && constant.value() != null) {
                        observations.add(new LootSubject.Observation(constant.value(), List.of(), 1));
                    } else if (site.source() == CallSites.Other.INSTANCE) {
                        computed.add(site);
                    }
                }
            }
        } catch (Throwable ignored) {}

        for (CallSites.Site site : computed) {
            try {
                for (Outcome outcome : Explorer.explore(profile, registries, (interpreter) -> {
                    interpreter.subject = subject.get();
                    return interpreter.machine.execute(site.holder().owner(), site.holder().method(), arguments(site, self), 0);
                })) {
                    if (outcome.subject() instanceof LootSubject run) {
                        observations.addAll(run.observations());
                    }
                }
            } catch (Throwable ignored) {}
        }

        return observations;
    }

    @NotNull
    private static Set<ResourceLocation> tables(List<LootSubject.Observation> observations) {
        Set<ResourceLocation> tables = new LinkedHashSet<>();

        observations.forEach((o) -> {
            if (o.value() instanceof ResourceLocation table) {
                tables.add(table);
            }
        });
        return tables;
    }

    @NotNull
    private static List<Declaration> entries(Class<?> start) {
        List<Declaration> entries = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Class<?> type = start; type != null && !LambdaResolver.isPlatform(Type.getInternalName(type)); type = type.getSuperclass()) {
            ClassNode node = Resolution.classNode(type);

            if (node == null) {
                continue;
            }

            for (MethodNode method : node.methods) {
                if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT)) == 0 && !method.name.startsWith("<") && seen.add(method.name + method.desc)
                        && !(method.name.equals(RuntimeNames.GET_DEFAULT_LOOT_TABLE.name()) && method.desc.equals(RuntimeNames.GET_DEFAULT_LOOT_TABLE.desc()))) {
                    entries.add(new Declaration(type, method));
                }
            }
        }

        return entries;
    }

    @NotNull
    private static List<Value> arguments(CallSites.Site site, Value self) {
        MethodNode method = site.holder().method();
        Type[] params = Type.getArgumentTypes(method.desc);
        List<Value> args = new ArrayList<>();

        if ((method.access & Opcodes.ACC_STATIC) == 0) {
            args.add(site.selfReceiver() ? self : Value.Unknown.of(Type.getType(site.holder().owner()), null));
        }

        for (int i = 0; i < params.length; i++) {
            args.add(site.selfParams().contains(i) ? self : Value.Unknown.of(params[i], null));
        }

        return args;
    }

    @Unmodifiable
    @NotNull
    private static Value self(Class<?> entityClass) {
        return Value.Unknown.of(Type.getType(entityClass), Origin.SELF);
    }

    public record ComputedLootTables(Map<List<Decision>, Map<ResourceLocation, Double>> own, Map<List<Decision>, Map<ResourceLocation, Double>> events) {
    }
}
