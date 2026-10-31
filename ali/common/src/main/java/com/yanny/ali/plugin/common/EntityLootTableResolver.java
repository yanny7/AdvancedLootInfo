package com.yanny.ali.plugin.common;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.plugin.server.bytecode.LootTableAnalysis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * Maps a loot table id back to the entity types that can drop it, the opposite direction of asking every entity type
 * what it drops.
 * <p>
 * {@link EntityType#getDefaultLootTable()} is derived from the registry key alone, and {@code Mob#getLootTable} is
 * final and delegates to {@code getDefaultLootTable}, so the only entities that can drop a table other than their
 * type's default are the ones overriding that method. Vanilla has exactly one - {@code Sheep}, whose per-color tables
 * extend the type's own table by one path segment ({@code entities/sheep/white}) - so trimming trailing segments off an
 * unmatched {@code entities/} table finds the owning type without any variant registration.
 * <p>
 * A table that neither matches nor trims down to a known type gets the bytecode of the entities of its namespace
 * analysed ({@link LootTableAnalysis}), then the code of their mod jars, and only what is still unexplained falls back
 * to {@link #scanNamespace}, which builds the entities of that namespace. Tables another loot table references, a
 * global loot modifier rolls or mod code outside an entity rolls are excluded from that fallback - they belong to no
 * entity. An entity that is not found even that way, or that is attributed to the wrong type, can be declared in the
 * config's {@code entityLootTables}, which takes precedence over everything derived here.
 */
public class EntityLootTableResolver {
    private static final String ENTITY_PREFIX = "entities/";
    private static final int WEIGHT_SCALE = 1000;
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final Map<ResourceLocation, List<EntityType<?>>> typesByLootTable = new HashMap<>();
    private final Map<EntityType<?>, List<Entity>> entities = new HashMap<>();
    private final Set<ResourceLocation> excludedLootTables = new HashSet<>();
    private final Set<String> analyzedNamespaces = new HashSet<>();
    private final Set<String> referencedNamespaces = new HashSet<>();
    private final Set<String> scannedNamespaces = new HashSet<>();
    private final Map<String, List<Class<?>>> entityClassesByNamespace = new HashMap<>();
    private final Map<ResourceLocation, RandomPage> randomPages = new LinkedHashMap<>();
    private final EntityClasses entityClasses = new EntityClasses();
    private final ICommonUtils utils;
    private final Level level;

    public EntityLootTableResolver(ICommonUtils utils, Level level, Map<ResourceLocation, List<EntityType<?>>> registeredLootTables,
                                   Collection<ResourceLocation> referencedLootTables, Collection<Object> globalLootModifiers) {
        this.utils = utils;
        this.level = level;

        // a table another table pulls in is already explained by that reference, so it must not cost a namespace scan
        excludedLootTables.addAll(referencedLootTables);
        excludeRolledLootTables(globalLootModifiers);

        // config, then plugins, before anything derived: a claimed table keeps its entity even if it resolves elsewhere
        utils.getConfiguration().entityLootTables.forEach((entityId, lootTables) -> {
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);

            if (type.isPresent()) {
                lootTables.forEach((lootTable) -> addMapping(lootTable, type.get()));
            } else {
                LOGGER.warn("Unknown entity type {} in entityLootTables configuration", entityId);
            }
        });

        registeredLootTables.forEach((lootTable, types) -> types.forEach((type) -> addMapping(lootTable, type)));

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type != EntityType.PLAYER) {
                addMapping(type.getDefaultLootTable(), type);
            }
        }

        // the player gets no entry, but its table still has to count as explained so it never reaches the fallback
        excludedLootTables.add(EntityType.PLAYER.getDefaultLootTable());
    }

    /**
     * Groups the loot tables that belong to an entity by their owning type, ordered by registry order of that type and
     * by table id within it, so the entries a viewer shows do not depend on map iteration order.
     */
    @NotNull
    public List<EntityPage> resolveAll(Collection<ResourceLocation> lootTables) {
        Map<EntityType<?>, Set<ResourceLocation>> tablesByType = new HashMap<>();
        Set<ResourceLocation> absorbed = new HashSet<>();
        List<EntityPage> result = new ArrayList<>();

        lootTables.stream()
                .sorted(Comparator.comparing((ResourceLocation id) -> !id.getPath().startsWith(ENTITY_PREFIX)).thenComparing(ResourceLocation::toString))
                .forEach((lootTable) -> {
                    List<EntityType<?>> types = resolve(lootTable);

                    if (!types.isEmpty()) {
                        tablesByType.computeIfAbsent(types.get(0), (k) -> new TreeSet<>(Comparator.comparing(ResourceLocation::toString))).add(lootTable);
                    }
                });

        randomPages.forEach((lootTable, page) -> {
            tablesByType.computeIfAbsent(page.type(), (k) -> new TreeSet<>(Comparator.comparing(ResourceLocation::toString))).add(lootTable);
            absorbed.addAll(page.absorbed());
        });

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            for (ResourceLocation lootTable : tablesByType.getOrDefault(type, Collections.emptySet())) {
                RandomPage page = randomPages.get(lootTable);

                if (page != null) {
                    result.add(new EntityPage(lootTable, List.of(page.type()), page.lootTable(), page.absorbed()));
                } else if (!absorbed.contains(lootTable)) {
                    result.add(new EntityPage(lootTable, resolve(lootTable), null, Set.of()));
                }
            }
        }

        LootTableAnalysis.releaseJarIndexes();
        return result;
    }

    @NotNull
    public List<EntityType<?>> resolve(ResourceLocation lootTable) {
        List<EntityType<?>> types = explain(lootTable);

        if (types != null) {
            return types;
        }
        if (!lootTable.getPath().startsWith(ENTITY_PREFIX)) {
            return Collections.emptyList();
        }

        String namespace = lootTable.getNamespace();

        if (analyzedNamespaces.add(namespace)) {
            analyzeNamespace(namespace);
            types = explain(lootTable);
        }
        if (types == null && referencedNamespaces.add(namespace)) {
            excludeReferencedLootTables(namespace);
            types = explain(lootTable);
        }

        return types != null ? types : scanNamespace(lootTable);
    }

    /**
     * The sample entities of a type, created once and reused. Only the loot modifier path needs them - a modifier's
     * predicate takes an {@code Entity}, not an {@link EntityType}.
     */
    @NotNull
    public List<Entity> getEntities(EntityType<?> type) {
        return entities.computeIfAbsent(type, (t) -> utils.createEntities(t, level));
    }

    @Nullable
    private List<EntityType<?>> explain(ResourceLocation lootTable) {
        List<EntityType<?>> types = lookup(lootTable);

        if (types != null) {
            return types;
        }

        String path = lootTable.getPath();

        if (!path.startsWith(ENTITY_PREFIX)) {
            return null;
        }

        // a variant table extends its type's default table by extra path segments, so trim them one by one
        for (int index = path.lastIndexOf('/'); index >= ENTITY_PREFIX.length(); index = path.lastIndexOf('/')) {
            path = path.substring(0, index);
            types = lookup(new ResourceLocation(lootTable.getNamespace(), path));

            if (types != null) {
                return types;
            }
        }

        return null;
    }

    private void excludeRolledLootTables(Collection<Object> globalLootModifiers) {
        long startTime = System.currentTimeMillis();
        boolean log = utils.getConfiguration().logMoreStatistics;

        for (Object modifier : globalLootModifiers) {
            for (ResourceLocation lootTable : LootTableAnalysis.getRolledLootTables(level.registryAccess(), modifier)) {
                excludedLootTables.add(lootTable);

                if (log) {
                    LOGGER.info("Loot table {} is rolled by global loot modifier {}", lootTable, modifier.getClass().getName());
                }
            }
        }

        if (log) {
            LOGGER.info("Analysing the bytecode of {} global loot modifiers took {}ms", globalLootModifiers.size(), System.currentTimeMillis() - startTime);
        }
    }

    private void analyzeNamespace(String namespace) {
        long startTime = System.currentTimeMillis();
        boolean log = utils.getConfiguration().logMoreStatistics;
        List<Class<?>> classes = new ArrayList<>();

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Class<?> entityClass = type != EntityType.PLAYER && BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(namespace) ? entityClasses.find(type) : null;

            if (entityClass == null) {
                continue;
            }

            classes.add(entityClass);

            LootTableAnalysis.ComputedLootTables tables;

            try {
                tables = LootTableAnalysis.getEntityLootTables(level.registryAccess(), type, entityClass);
            } catch (Throwable e) {
                LOGGER.warn("Failed to analyse the loot tables of entity {} ({})", BuiltInRegistries.ENTITY_TYPE.getKey(type), entityClass.getName(), e);
                continue;
            }

            tables.events().values().forEach((byTable) -> byTable.keySet().forEach((lootTable) -> addAnalyzedMapping(lootTable, type, log)));

            for (Map<ResourceLocation, Double> byTable : tables.own().values()) {
                if (byTable.values().stream().allMatch(LootTableAnalysis.ComputedLootTables::isCertain) && byTable.keySet().stream().noneMatch((t) -> isForeign(t, type))) {
                    byTable.keySet().forEach((lootTable) -> addAnalyzedMapping(lootTable, type, log));
                } else {
                    addRandomPage(type, byTable, log);
                }
            }
        }

        entityClassesByNamespace.put(namespace, classes);

        if (log) {
            LOGGER.info("Analysing the bytecode of {} entities of {} took {}ms", classes.size(), namespace, System.currentTimeMillis() - startTime);
        }
    }

    private void excludeReferencedLootTables(String namespace) {
        long startTime = System.currentTimeMillis();
        boolean log = utils.getConfiguration().logMoreStatistics;

        for (ResourceLocation lootTable : LootTableAnalysis.getReferencedLootTables(entityClassesByNamespace.getOrDefault(namespace, List.of()))) {
            excludedLootTables.add(lootTable);

            if (log) {
                LOGGER.info("Loot table {} is rolled by mod code of {}", lootTable, namespace);
            }
        }

        if (log) {
            LOGGER.info("Analysing the bytecode of the mod jar of {} took {}ms", namespace, System.currentTimeMillis() - startTime);
        }
    }

    private void addAnalyzedMapping(ResourceLocation lootTable, EntityType<?> type, boolean log) {
        if (log && !typesByLootTable.getOrDefault(lootTable, List.of()).contains(type)) {
            LOGGER.info("Loot table {} belongs to entity {} by its bytecode", lootTable, BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }

        addMapping(lootTable, type);
    }

    private void addRandomPage(EntityType<?> type, Map<ResourceLocation, Double> byTable, boolean log) {
        List<ResourceLocation> own = byTable.keySet().stream().filter((t) -> !isForeign(t, type)).sorted(Comparator.comparing(ResourceLocation::toString)).toList();
        ResourceLocation id = own.isEmpty() || own.contains(type.getDefaultLootTable()) ? type.getDefaultLootTable() : own.get(0);

        if (randomPages.containsKey(id)) {
            return;
        }

        Set<ResourceLocation> absorbed = new HashSet<>();

        for (ResourceLocation lootTable : own) {
            if (!lootTable.equals(id) && !typesByLootTable.containsKey(lootTable)) {
                absorbed.add(lootTable);
                excludedLootTables.add(lootTable);
            }
        }

        addMapping(id, type);
        randomPages.put(id, new RandomPage(type, randomLootTable(byTable), absorbed));

        if (log) {
            LOGGER.info("Entity {} drops one of loot tables {} by its bytecode", BuiltInRegistries.ENTITY_TYPE.getKey(type), byTable);
        }
    }

    private boolean isForeign(ResourceLocation lootTable, EntityType<?> type) {
        return typesByLootTable.getOrDefault(lootTable, List.of()).stream().anyMatch((t) -> t != type && t.getDefaultLootTable().equals(lootTable));
    }

    @NotNull
    private static LootTable randomLootTable(Map<ResourceLocation, Double> byTable) {
        List<ResourceLocation> tables = byTable.keySet().stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList();
        List<Double> probabilities = new ArrayList<>(tables.stream().map(byTable::get).toList());
        double rest = 1 - probabilities.stream().mapToDouble(Double::doubleValue).sum();
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));

        if (!LootTableAnalysis.ComputedLootTables.isCertain(1 - rest)) {
            probabilities.add(rest);
        }

        int scale = weightScale(probabilities);

        for (int i = 0; i < tables.size(); i++) {
            pool.add(LootTableReference.lootTableReference(tables.get(i)).setWeight(weight(probabilities.get(i), scale)));
        }
        if (probabilities.size() > tables.size()) {
            pool.add(EmptyLootItem.emptyItem().setWeight(weight(rest, scale)));
        }

        return LootTable.lootTable().setParamSet(LootContextParamSets.ENTITY).withPool(pool).build();
    }

    private static int weightScale(List<Double> probabilities) {
        for (int scale = 1; scale < WEIGHT_SCALE; scale++) {
            int s = scale;

            if (probabilities.stream().allMatch((p) -> Math.abs(p * s - Math.round(p * s)) < 1e-6 && Math.round(p * s) > 0)) {
                return scale;
            }
        }

        return WEIGHT_SCALE;
    }

    private static int weight(double probability, int scale) {
        return Math.max(1, (int) Math.round(probability * scale));
    }

    @NotNull
    private List<EntityType<?>> scanNamespace(ResourceLocation lootTable) {
        String namespace = lootTable.getNamespace();

        if (scannedNamespaces.add(namespace)) {
            long startTime = System.currentTimeMillis();
            int scannedTypes = 0;

            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (type != EntityType.PLAYER && BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(namespace)) {
                    scannedTypes++;

                    for (Entity entity : getEntities(type)) {
                        if (entity instanceof Mob mob) {
                            ResourceLocation entityLootTable = mob.getLootTable();

                            //noinspection ConstantValue - some modded mobs does return null
                            if (entityLootTable != null) {
                                addMapping(entityLootTable, type);
                            }
                        }
                    }
                }
            }

            LOGGER.warn("Loot table {} belongs to no entity by its id, had to create {} entities of {} to find out ({}ms). Declare it in the entityLootTables configuration to skip this.",
                    lootTable, scannedTypes, namespace, System.currentTimeMillis() - startTime);
        }

        List<EntityType<?>> types = typesByLootTable.get(lootTable);

        if (types == null) {
            if (utils.getConfiguration().logMoreStatistics) {
                LOGGER.info("Loot table {} belongs to no entity, treating it as gameplay loot", lootTable);
            }

            return Collections.emptyList();
        }

        return types;
    }

    @Nullable
    private List<EntityType<?>> lookup(ResourceLocation lootTable) {
        List<EntityType<?>> types = typesByLootTable.get(lootTable);

        if (types == null && excludedLootTables.contains(lootTable)) {
            return Collections.emptyList();
        }

        return types;
    }

    private void addMapping(ResourceLocation lootTable, EntityType<?> type) {
        List<EntityType<?>> types = typesByLootTable.computeIfAbsent(lootTable, (k) -> new ArrayList<>());

        if (!types.contains(type)) {
            types.add(type);
        }
    }

    public record EntityPage(ResourceLocation id, List<EntityType<?>> types, @Nullable LootTable synthesized, Set<ResourceLocation> absorbed) {
    }

    private record RandomPage(EntityType<?> type, LootTable lootTable, Set<ResourceLocation> absorbed) {
    }
}
