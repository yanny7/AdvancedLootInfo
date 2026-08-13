package com.yanny.ali.plugin.common;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.ICommonUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Maps a loot table id back to the entity types that can drop it, the opposite direction of asking every entity type
 * what it drops. The reason is cost: reading the loot table off an entity means constructing one sample entity per
 * registered type - goals, attributes and brains included - and throwing away every type that has no loot at all,
 * which on a large modpack is the single most expensive part of the scan.
 * <p>
 * Nothing here needs an instance. {@link EntityType#getDefaultLootTable()} is derived from the registry key alone, and
 * {@code Mob#getLootTable} is final and delegates to {@code getDefaultLootTable}, so the only entities that can drop a
 * table other than their type's default are the ones overriding that method. Vanilla has exactly one - {@code Sheep},
 * whose per-color tables extend the type's own table by one path segment ({@code entities/sheep/white}) - so trimming
 * trailing segments off an unmatched {@code entities/} table finds the owning type without any variant registration.
 * <p>
 * A table that neither matches nor trims down to a known type falls back to {@link #scanNamespace}, which does build
 * entities, but only for the namespace of the table that could not be explained - the cost is bound to the number of
 * unusual mods rather than to the size of the registry, and vanilla data never triggers it. An entity that is not
 * found even that way, or that is attributed to the wrong type, can be declared in the config's
 * {@code entityLootTables}, which takes precedence over everything derived here.
 */
public class EntityLootTableResolver {
    private static final String ENTITY_PREFIX = "entities/";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ResourceLocation, List<EntityType<?>>> typesByLootTable = new HashMap<>();
    private final Map<EntityType<?>, List<Entity>> entities = new HashMap<>();
    private final Set<ResourceLocation> excludedLootTables = new HashSet<>();
    private final Set<String> scannedNamespaces = new HashSet<>();
    private final ICommonUtils utils;
    private final Level level;

    public EntityLootTableResolver(ICommonUtils utils, Level level) {
        this.utils = utils;
        this.level = level;

        // configured first, so a table claimed by the config is attributed to that entity even when it would resolve
        // to a different one on its own
        utils.getConfiguration().entityLootTables.forEach((entityId, lootTables) -> {
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);

            if (type.isPresent()) {
                lootTables.forEach((lootTable) -> addMapping(lootTable, type.get()));
            } else {
                LOGGER.warn("Unknown entity type {} in entityLootTables configuration", entityId);
            }
        });

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type != EntityType.PLAYER) {
                type.getDefaultLootTable().ifPresent((resourceKey) -> addMapping(resourceKey.location(), type));
            }
        }

        // the player gets no entry, but its table still has to count as explained so it never reaches the fallback
        EntityType.PLAYER.getDefaultLootTable().ifPresent((resourceKey) -> excludedLootTables.add(resourceKey.location()));
    }

    /**
     * Groups the loot tables that belong to an entity by their owning type, ordered by registry order of that type and
     * by table id within it, so the entries a viewer shows do not depend on map iteration order.
     */
    @NotNull
    public Map<ResourceLocation, List<EntityType<?>>> resolveAll(Collection<ResourceLocation> lootTables) {
        Map<EntityType<?>, List<ResourceLocation>> tablesByType = new HashMap<>();
        Map<ResourceLocation, List<EntityType<?>>> result = new LinkedHashMap<>();

        lootTables.stream().sorted(Comparator.comparing(ResourceLocation::toString)).forEach((lootTable) -> {
            List<EntityType<?>> types = resolve(lootTable);

            if (!types.isEmpty()) {
                tablesByType.computeIfAbsent(types.get(0), (k) -> new ArrayList<>()).add(lootTable);
            }
        });

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            for (ResourceLocation lootTable : tablesByType.getOrDefault(type, Collections.emptyList())) {
                result.put(lootTable, resolve(lootTable));
            }
        }

        return result;
    }

    @NotNull
    public List<EntityType<?>> resolve(ResourceLocation lootTable) {
        List<EntityType<?>> types = lookup(lootTable);

        if (types != null) {
            return types;
        }

        String path = lootTable.getPath();

        if (!path.startsWith(ENTITY_PREFIX)) {
            return Collections.emptyList();
        }

        // a variant table extends its type's default table by extra path segments, so trim them one by one
        for (int index = path.lastIndexOf('/'); index >= ENTITY_PREFIX.length(); index = path.lastIndexOf('/')) {
            path = path.substring(0, index);
            types = lookup(ResourceLocation.fromNamespaceAndPath(lootTable.getNamespace(), path));

            if (types != null) {
                return types;
            }
        }

        return scanNamespace(lootTable);
    }

    /**
     * The sample entities of a type, created once and reused. Only the loot modifier path needs them - a modifier's
     * predicate takes an {@code Entity}, not an {@link EntityType} - so on an installation without global loot
     * modifiers this is never called.
     */
    @NotNull
    public List<Entity> getEntities(EntityType<?> type) {
        return entities.computeIfAbsent(type, (t) -> utils.createEntities(t, level));
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
                            mob.getLootTable().ifPresent((resourceKey) -> addMapping(resourceKey.location(), type));
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
}
