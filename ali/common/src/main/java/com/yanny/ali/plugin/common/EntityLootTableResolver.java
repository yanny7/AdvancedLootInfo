package com.yanny.ali.plugin.common;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.ICommonUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class EntityLootTableResolver {
    private static final String ENTITY_PREFIX = "entities/";
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final Map<Identifier, List<EntityType<?>>> typesByLootTable = new HashMap<>();
    private final Map<EntityType<?>, List<Entity>> entities = new HashMap<>();
    private final Set<Identifier> excludedLootTables = new HashSet<>();
    private final Set<String> scannedNamespaces = new HashSet<>();
    private final Map<Identifier, PageReason> reasons = new HashMap<>();
    private final ICommonUtils utils;
    private final Level level;

    public EntityLootTableResolver(ICommonUtils utils, Level level, Map<Identifier, List<EntityType<?>>> registeredLootTables,
                                   Collection<Identifier> referencedLootTables) {
        this.utils = utils;
        this.level = level;

        // a table another table pulls in is already explained by that reference, so it must not cost a namespace scan
        excludedLootTables.addAll(referencedLootTables);

        // config, then plugins, before anything derived: a claimed table keeps its entity even if it resolves elsewhere
        utils.getConfiguration().entityLootTables.forEach((entityId, lootTables) -> {
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId);

            if (type.isPresent()) {
                lootTables.forEach((lootTable) -> addMapping(lootTable, type.get(), PageReason.CONFIGURATION));
            } else {
                LOGGER.warn("Unknown entity type {} in entityLootTables configuration", entityId);
            }
        });

        registeredLootTables.forEach((lootTable, types) -> types.forEach((type) -> addMapping(lootTable, type, PageReason.PLUGIN)));

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type != EntityType.PLAYER) {
                type.getDefaultLootTable().ifPresent((resourceKey) -> addMapping(resourceKey.identifier(), type, null));
            }
        }

        // the player gets no entry, but its table still has to count as explained so it never reaches the fallback
        EntityType.PLAYER.getDefaultLootTable().ifPresent((resourceKey) -> excludedLootTables.add(resourceKey.identifier()));
    }

    @NotNull
    public List<EntityPage> resolveAll(Collection<Identifier> lootTables) {
        Map<EntityType<?>, Set<Identifier>> tablesByType = new HashMap<>();
        List<EntityPage> result = new ArrayList<>();

        lootTables.stream()
                .sorted(Comparator.comparing((Identifier id) -> !id.getPath().startsWith(ENTITY_PREFIX)).thenComparing(Identifier::toString))
                .forEach((lootTable) -> {
                    List<EntityType<?>> types = resolve(lootTable);

                    if (!types.isEmpty()) {
                        tablesByType.computeIfAbsent(types.getFirst(), (k) -> new TreeSet<>(Comparator.comparing(Identifier::toString))).add(lootTable);
                    }
                });

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            for (Identifier lootTable : tablesByType.getOrDefault(type, Collections.emptySet())) {
                List<EntityType<?>> types = resolve(lootTable);

                result.add(new EntityPage(lootTable, types, reason(lootTable, types)));
            }
        }

        return result;
    }

    @NotNull
    public List<EntityType<?>> resolve(Identifier lootTable) {
        List<EntityType<?>> types = explain(lootTable);

        if (types != null) {
            return types;
        }
        if (!lootTable.getPath().startsWith(ENTITY_PREFIX)) {
            return Collections.emptyList();
        }

        return scanNamespace(lootTable);
    }

    @NotNull
    public List<Entity> getEntities(EntityType<?> type) {
        return entities.computeIfAbsent(type, (t) -> utils.createEntities(t, level));
    }

    @Nullable
    private List<EntityType<?>> explain(Identifier lootTable) {
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
            types = lookup(Identifier.fromNamespaceAndPath(lootTable.getNamespace(), path));

            if (types != null) {
                return types;
            }
        }

        return null;
    }

    @NotNull
    private List<EntityType<?>> scanNamespace(Identifier lootTable) {
        String namespace = lootTable.getNamespace();

        if (scannedNamespaces.add(namespace)) {
            long startTime = System.currentTimeMillis();
            int scannedTypes = 0;

            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (type != EntityType.PLAYER && BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(namespace)) {
                    scannedTypes++;

                    for (Entity entity : getEntities(type)) {
                        if (entity instanceof Mob mob) {
                            mob.getLootTable().ifPresent((resourceKey) -> addMapping(resourceKey.identifier(), type, null));
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
    private List<EntityType<?>> lookup(Identifier lootTable) {
        List<EntityType<?>> types = typesByLootTable.get(lootTable);

        if (types == null && excludedLootTables.contains(lootTable)) {
            return Collections.emptyList();
        }

        return types;
    }

    private void addMapping(Identifier lootTable, EntityType<?> type, @Nullable PageReason reason) {
        List<EntityType<?>> types = typesByLootTable.computeIfAbsent(lootTable, (k) -> new ArrayList<>());

        if (reason != null) {
            reasons.putIfAbsent(lootTable, reason);
        }

        if (!types.contains(type)) {
            types.add(type);
        }
    }

    @Nullable
    private PageReason reason(Identifier lootTable, List<EntityType<?>> types) {
        PageReason reason = reasons.get(lootTable);

        if (reason != null || types.isEmpty() || types.getFirst().getDefaultLootTable().map(ResourceKey::identifier).filter(lootTable::equals).isPresent()) {
            return reason;
        }

        return PageReason.VARIANT;
    }

    public enum PageReason {
        VARIANT,
        CONFIGURATION,
        PLUGIN,
    }

    public record EntityPage(Identifier id, List<EntityType<?>> types, @Nullable PageReason reason) {
    }
}
