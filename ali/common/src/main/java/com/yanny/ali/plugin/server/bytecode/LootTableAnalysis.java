package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.Declared;

public final class LootTableAnalysis {
    private static final Map<EntityType<?>, List<Outcome>> DEFAULTS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Sites> SITES = new ConcurrentHashMap<>();
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

        return new ComputedLootTables(LootAggregation.tables(own, typeDefault, true), LootAggregation.tables(rolled(registries, Profile.LOOT, entityClass, true,
                () -> new EntitySubject(type, entityClass), self(entityClass)), typeDefault, false));
    }

    public static boolean overridesDefaultLootTable(Class<?> entityClass) {
        return RuntimeNames.LOOT_AVAILABLE && ownDefaultLootTable(entityClass) != null;
    }

    @Unmodifiable
    @NotNull
    public static List<List<VariantHint>> getVariantHints(RegistryAccess registries, EntityType<?> type, Class<?> entityClass) {
        if (!RuntimeNames.LOOT_AVAILABLE) {
            return List.of();
        }

        try {
            return VariantHints.of(defaults(registries, type, entityClass), entityClass);
        } catch (Throwable e) {
            return List.of();
        }
    }

    @NotNull
    public static Set<ResourceLocation> getRolledLootTables(RegistryAccess registries, Object instance) {
        if (!RuntimeNames.LOOT_AVAILABLE) {
            return Set.of();
        }

        return tables(rolled(registries, Profile.ROLLED, instance.getClass(), false, () -> new InstanceSubject(instance), new Value.Known(instance)));
    }

    @NotNull
    public static Set<ResourceLocation> getReferencedLootTables(Collection<Class<?>> classesOfJars) {
        Set<ResourceLocation> tables = new LinkedHashSet<>();

        if (!RuntimeNames.LOOT_AVAILABLE) {
            return tables;
        }

        for (ModJarIndex index : classesOfJars.stream().map(ModJarIndex::of).filter(Objects::nonNull).distinct().toList()) {
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
        }

        return tables;
    }

    static List<Outcome> defaults(RegistryAccess registries, EntityType<?> type, Class<?> entityClass) {
        return DEFAULTS.computeIfAbsent(type, (t) -> {
            Declaration own = ownDefaultLootTable(entityClass);

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

    @Nullable
    private static Declaration ownDefaultLootTable(Class<?> entityClass) {
        Declared declared = Resolution.declaration(entityClass, RuntimeNames.GET_DEFAULT_LOOT_TABLE.name(), RuntimeNames.GET_DEFAULT_LOOT_TABLE.desc(), false);

        return declared instanceof Declaration declaration && !declaration.owner().isAssignableFrom(Mob.class) ? declaration : null;
    }

    public static void releaseJarIndexes() {
        ModJarIndex.clearCaches();
    }

    static void clearCaches() {
        DEFAULTS.clear();
        SITES.clear();
    }

    @NotNull
    private static List<LootSubject.Observation> rolled(RegistryAccess registries, Profile profile, Class<?> type, boolean filterByCallers, Supplier<LootSubject> subject,
                                                        Value self) {
        Sites sites = SITES.computeIfAbsent(type, (t) -> sites(t, filterByCallers));
        List<LootSubject.Observation> observations = new ArrayList<>();

        sites.constants().forEach((constant) -> observations.add(new LootSubject.Observation(constant, List.of(), 1)));

        for (CallSites.Site site : sites.computed()) {
            try {
                for (Outcome outcome : Explorer.explore(profile, registries, (interpreter) -> {
                    interpreter.subject = subject.get();
                    interpreter.cutoff = site.live() != null ? new Machine.Cutoff(site.holder().method(), site.live()) : null;
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
    private static Sites sites(Class<?> type, boolean filterByCallers) {
        List<Object> constants = new ArrayList<>();
        Set<CallSites.Site> computed = new LinkedHashSet<>();

        try {
            Set<String> jars = new HashSet<>();

            for (Class<?> c = type; c != null && !LambdaResolver.isPlatform(Type.getInternalName(c)); c = c.getSuperclass()) {
                jars.add(String.valueOf(ModJarIndex.location(c)));
            }

            CallSites search = new CallSites(type, SINK, filterByCallers ? (d) -> mayReachSink(d, jars) : (d) -> true);

            for (Declaration entry : entries(type)) {
                for (CallSites.Site site : search.find(entry.owner(), entry.method())) {
                    if (site.source() instanceof CallSites.Constant constant && constant.value() != null) {
                        constants.add(constant.value());
                    } else if (site.source() == CallSites.Other.INSTANCE) {
                        computed.add(site);
                    }
                }
            }
        } catch (Throwable ignored) {}

        return new Sites(constants, computed);
    }

    private static boolean mayReachSink(Declaration declaration, Set<String> jars) {
        if (!jars.contains(String.valueOf(ModJarIndex.location(declaration.owner())))) {
            return true;
        }

        ModJarIndex index = ModJarIndex.of(declaration.owner());
        Set<String> reaching = index != null ? index.reaching(SINK, CallSites.DEPTH) : null;

        return reaching == null || reaching.contains(declaration.method().name + declaration.method().desc);
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

    private record Sites(List<Object> constants, Set<CallSites.Site> computed) {
    }

    public record ComputedLootTables(Map<List<Decision>, Map<ResourceLocation, Double>> own, Map<List<Decision>, Map<ResourceLocation, Double>> events) {
        public static boolean isCertain(double probability) {
            return probability >= LootAggregation.ALWAYS;
        }
    }
}
