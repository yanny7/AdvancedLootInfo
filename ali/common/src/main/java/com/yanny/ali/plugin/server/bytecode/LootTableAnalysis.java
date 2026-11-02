package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

public final class LootTableAnalysis {
    private static final Map<EntityType<?>, List<Outcome>> DEFAULTS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Sites> SITES = new ConcurrentHashMap<>();
    static final List<CallSites.Sink> SINKS = List.of(
            new CallSites.Sink(RuntimeNames.LOOT_DATA_GET_LOOT_TABLE, ownedBy(RuntimeNames.LOOT_DATA_GET_LOOT_TABLE, ReloadableServerRegistries.Holder.class), 0, 0, CallSites.Sink.ANY),
            new CallSites.Sink(RuntimeNames.HOLDER_GETTER_GET, ownedBy(RuntimeNames.HOLDER_GETTER_GET, HolderGetter.Provider.class), 0, 0, CallSites::isLootTableKey),
            new CallSites.Sink(RuntimeNames.LOOT_TABLE_REGISTRY, CallSites::isRegistryRead, 0, CallSites.RECEIVER, CallSites.LootView.INSTANCE::equals),
            new CallSites.Sink(RuntimeNames.LIVING_DROP_KEY, ownedBy(RuntimeNames.LIVING_DROP_KEY, LivingEntity.class), 3, 3, CallSites.Sink.ANY),
            new CallSites.Sink(RuntimeNames.LIVING_DROP_KEY_CONSUMER, ownedBy(RuntimeNames.LIVING_DROP_KEY_CONSUMER, LivingEntity.class), 3, 3, CallSites.Sink.ANY),
            new CallSites.Sink(RuntimeNames.LIVING_DROP_PARAMS, ownedBy(RuntimeNames.LIVING_DROP_PARAMS, LivingEntity.class), 1, 1, CallSites.Sink.ANY),
            new CallSites.Sink(RuntimeNames.LIVING_DROP_INTERACT, ownedBy(RuntimeNames.LIVING_DROP_INTERACT, LivingEntity.class), 1, 1, CallSites.Sink.ANY),
            new CallSites.Sink(RuntimeNames.LIVING_DROP_GIFT, ownedBy(RuntimeNames.LIVING_DROP_GIFT, LivingEntity.class), 1, 1, CallSites.Sink.ANY),
            new CallSites.Sink(RuntimeNames.LIVING_DROP_SHEARING, ownedBy(RuntimeNames.LIVING_DROP_SHEARING, LivingEntity.class), 1, 1, CallSites.Sink.ANY)
    );

    private LootTableAnalysis() {}

    private static CallSites.Matcher ownedBy(RuntimeNames.Ref method, Class<?> owner) {
        return (call, loader) -> RuntimeNames.is(method, call) && CallSites.isA(owner, call.owner, loader);
    }

    @NotNull
    public static ComputedLootTables getEntityLootTables(RegistryAccess registries, EntityType<?> type, Class<?> entityClass) {
        if (!RuntimeNames.LOOT_AVAILABLE) {
            return new ComputedLootTables(Map.of(), Map.of());
        }

        Identifier typeDefault = type.getDefaultLootTable().map(ResourceKey::identifier).orElse(null);
        List<LootSubject.Observation> own = new ArrayList<>();

        for (Outcome outcome : defaults(registries, type, entityClass)) {
            if (outcome instanceof Outcome.Success success) {
                if (success.result() != null) {
                    own.addAll(LootTableModel.observations(success.result(), success.decisions()));
                }
                if (success.subject() instanceof LootSubject subject) {
                    own.addAll(subject.observations());
                }
            }
        }

        return new ComputedLootTables(LootAggregation.tables(own, typeDefault, true), LootAggregation.tables(rolled(registries, Profile.LOOT, entityClass, true,
                () -> new EntitySubject(type, entityClass, null), self(entityClass)), typeDefault, false));
    }

    public static boolean overridesLootSelector(Class<?> entityClass) {
        return RuntimeNames.LOOT_AVAILABLE && ownLootSelector(entityClass) != null;
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
    public static Set<Identifier> getRolledLootTables(RegistryAccess registries, Object instance) {
        if (!RuntimeNames.LOOT_AVAILABLE) {
            return Set.of();
        }

        return tables(rolled(registries, Profile.ROLLED, instance.getClass(), false, () -> new InstanceSubject(instance), new Value.Known(instance)));
    }

    @NotNull
    public static Set<Identifier> getReferencedLootTables(Collection<Class<?>> classesOfJars) {
        Set<Identifier> tables = new LinkedHashSet<>();

        if (!RuntimeNames.LOOT_AVAILABLE) {
            return tables;
        }

        for (ModJarIndex index : classesOfJars.stream().map(ModJarIndex::of).filter(Objects::nonNull).distinct().toList()) {
            for (CallSites.Sink sink : SINKS) {
                try {
                    for (ModJarIndex.Caller caller : index.callers(sink)) {
                        if (caller.entity()) {
                            continue;
                        }
                        if (caller.source() instanceof CallSites.Constant(Object value) && LootTableModel.location(value) instanceof Identifier table) {
                            tables.add(table);
                        } else if (caller.source() instanceof CallSites.Param(int param)) {
                            for (ModJarIndex.Caller outer : index.callersOf(caller.owner(), caller.method().name, caller.method().desc, param)) {
                                if (!outer.entity() && outer.source() instanceof CallSites.Constant(Object value) && sink.accepts(outer.source())
                                        && LootTableModel.location(value) instanceof Identifier table) {
                                    tables.add(table);
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        }

        return tables;
    }

    static List<Outcome> defaults(RegistryAccess registries, EntityType<?> type, Class<?> entityClass) {
        return DEFAULTS.computeIfAbsent(type, (t) -> {
            Declaration own = ownLootSelector(entityClass);

            if (own == null) {
                return List.of();
            }

            try {
                return Explorer.explore(Profile.LOOT, registries, (interpreter) -> {
                    interpreter.subject = new EntitySubject(type, entityClass, null);
                    return interpreter.machine.execute(own.owner(), own.method(), arguments(own.method(), self(entityClass)), 0);
                });
            } catch (Throwable e) {
                return List.of();
            }
        });
    }

    @Nullable
    static Declaration ownLootSelector(Class<?> entityClass) {
        Declaration drop = SelfModel.ownDeclaration(entityClass, RuntimeNames.LIVING_DROP_SELECTOR.name(), RuntimeNames.LIVING_DROP_SELECTOR.desc(), false);

        if (drop != null) {
            return drop;
        }

        return SelfModel.ownDeclaration(entityClass, RuntimeNames.MOB_GET_LOOT_TABLE.name(), RuntimeNames.MOB_GET_LOOT_TABLE.desc(), false);
    }

    @Nullable
    public static Identifier getInstanceLootTable(RegistryAccess registries, Entity entity) {
        Declaration own = RuntimeNames.LOOT_AVAILABLE ? ownLootSelector(entity.getClass()) : null;

        if (own == null || !isLootSelector(RuntimeNames.LIVING_DROP_SELECTOR, own.method())) {
            return null;
        }

        try {
            Set<Object> tables = new HashSet<>();

            for (Outcome outcome : Explorer.explore(Profile.LOOT, registries, (interpreter) -> {
                interpreter.subject = new EntitySubject(entity.getType(), entity.getClass(), entity);
                return interpreter.machine.execute(own.owner(), own.method(), arguments(own.method(), new Value.Known(entity)), 0);
            })) {
                if (!(outcome instanceof Outcome.Success success) || !(success.subject() instanceof LootSubject subject)) {
                    return null;
                }

                subject.observations().forEach((o) -> tables.add(o.value()));
            }

            return tables.size() == 1 && tables.iterator().next() instanceof Identifier table ? table : null;
        } catch (Throwable e) {
            return null;
        }
    }

    private static boolean isLootSelector(RuntimeNames.Ref ref, MethodNode method) {
        return method.name.equals(ref.name()) && method.desc.equals(ref.desc());
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

            CallSites search = new CallSites(type, SINKS, filterByCallers ? (d) -> mayReachSink(d, jars) : (d) -> true);

            for (Declaration entry : entries(type)) {
                for (CallSites.Site site : search.find(entry.owner(), entry.method())) {
                    if (site.source() instanceof CallSites.Constant(Object value) && value != null) {
                        constants.add(LootTableModel.location(value));
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

        if (index == null) {
            return true;
        }

        for (CallSites.Sink sink : SINKS) {
            Set<String> reaching = index.reaching(sink, CallSites.DEPTH);

            if (reaching == null || reaching.contains(declaration.method().name + declaration.method().desc)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static Set<Identifier> tables(List<LootSubject.Observation> observations) {
        Set<Identifier> tables = new LinkedHashSet<>();

        observations.forEach((o) -> {
            if (o.value() instanceof Identifier table) {
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
                        && !isLootSelector(RuntimeNames.LIVING_DROP_SELECTOR, method) && !isLootSelector(RuntimeNames.MOB_GET_LOOT_TABLE, method)) {
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

    @NotNull
    private static List<Value> arguments(MethodNode method, Value self) {
        List<Value> args = new ArrayList<>();

        args.add(self);

        for (Type param : Type.getArgumentTypes(method.desc)) {
            args.add(Value.Unknown.of(param, null));
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

    public record ComputedLootTables(Map<List<Decision>, Map<Identifier, Double>> own, Map<List<Decision>, Map<Identifier, Double>> events) {
        public static boolean isCertain(double probability) {
            return probability >= LootAggregation.ALWAYS;
        }
    }
}
