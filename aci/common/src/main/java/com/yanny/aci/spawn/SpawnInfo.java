package com.yanny.aci.spawn;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpawnInfo {
    private static final DecimalFormat COST_FORMAT = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final Comparator<Entry> ENTRY_ORDER = Comparator.<Entry, MobCategory>comparing((e) -> e.spawn().category())
            .thenComparing((e) -> BuiltInRegistries.ENTITY_TYPE.getKey(e.type()));
    private static final Comparator<Map.Entry<Spawn, SortedSet<ResourceLocation>>> GROUP_ORDER =
            Comparator.<Map.Entry<Spawn, SortedSet<ResourceLocation>>>comparingInt((e) -> -e.getValue().size()).thenComparing((e) -> e.getValue().first());

    private final Logger logger;
    private final Map<ResourceLocation, Set<ResourceLocation>> dimensionBiomes = new TreeMap<>();
    private final Map<ResourceLocation, List<Entry>> biomeEntries = new HashMap<>();
    private final Map<ResourceLocation, List<Entry>> structureEntries = new TreeMap<>();
    private final Map<ResourceLocation, Set<ResourceLocation>> structureBiomes = new HashMap<>();
    private final Map<EntityType<?>, Map<ResourceLocation, List<Spawn>>> entityBiomes = new HashMap<>();
    private final Map<EntityType<?>, Map<ResourceLocation, List<Spawn>>> entityStructures = new HashMap<>();

    public SpawnInfo(String modId, RegistryAccess registryAccess, Function<Structure, Structure.StructureSettings> structureSettings) {
        logger = CommonLogUtils.getLogger(modId);

        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : entries(registryAccess, Registries.LEVEL_STEM)) {
            try {
                dimensionBiomes.put(entry.getKey().location(), toIds(entry.getValue().generator().getBiomeSource().possibleBiomes()));
            } catch (Throwable e) {
                logger.warn("Failed to collect biomes of dimension {}", entry.getKey().location(), e);
            }
        }

        for (Map.Entry<ResourceKey<Biome>, Biome> entry : entries(registryAccess, Registries.BIOME)) {
            try {
                addBiome(entry.getKey().location(), entry.getValue().getMobSettings());
            } catch (Throwable e) {
                logger.warn("Failed to collect mob spawns of biome {}", entry.getKey().location(), e);
            }
        }

        for (Map.Entry<ResourceKey<Structure>, Structure> entry : entries(registryAccess, Registries.STRUCTURE)) {
            try {
                addStructure(entry.getKey().location(), structureSettings.apply(entry.getValue()));
            } catch (Throwable e) {
                logger.warn("Failed to collect mob spawns of structure {}", entry.getKey().location(), e);
            }
        }
    }

    @NotNull
    public Set<EntityType<?>> getEntityTypes() {
        Set<EntityType<?>> types = new HashSet<>(entityBiomes.keySet());

        types.addAll(entityStructures.keySet());
        return types;
    }

    @NotNull
    public TooltipNode getEntityTooltip(EntityType<?> type) {
        try {
            return buildEntityTooltip(type);
        } catch (Throwable e) {
            logger.warn("Failed to build spawn tooltip of entity {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
            return TooltipNode.empty();
        }
    }

    @NotNull
    public Map<EntityType<?>, TooltipNode> getBiomeSpawns(ResourceLocation biome) {
        try {
            return buildBiomeSpawns(biome);
        } catch (Throwable e) {
            logger.warn("Failed to build spawn tooltips of biome {}", biome, e);
            return Collections.emptyMap();
        }
    }

    @NotNull
    private TooltipNode buildEntityTooltip(EntityType<?> type) {
        Map<ResourceLocation, List<Spawn>> biomes = entityBiomes.getOrDefault(type, Collections.emptyMap());
        Map<ResourceLocation, List<Spawn>> structures = entityStructures.getOrDefault(type, Collections.emptyMap());

        return TooltipBuilder.branch((root) -> dimensionBiomes.forEach((dimension, biomesInDimension) -> {
            Map<Spawn, SortedSet<ResourceLocation>> groups = new HashMap<>();
            TooltipBuilder dimensionBuilder = TooltipBuilder.value(dimension).key(CoreLang.Spawn.DIMENSION);
            boolean spawnsHere = false;

            for (ResourceLocation biome : biomesInDimension) {
                for (Spawn spawn : biomes.getOrDefault(biome, Collections.emptyList())) {
                    groups.computeIfAbsent(spawn, (k) -> new TreeSet<>()).add(biome);
                }
            }

            for (Map.Entry<Spawn, SortedSet<ResourceLocation>> group : groups.entrySet().stream().sorted(GROUP_ORDER).toList()) {
                addBiomeGroup(dimensionBuilder, group.getKey(), group.getValue(), biomesInDimension);
                spawnsHere = true;
            }

            Map<ResourceLocation, List<Spawn>> structuresInDimension = new HashMap<>();

            structures.forEach((structure, spawns) -> {
                if (!Collections.disjoint(structureBiomes.get(structure), biomesInDimension)) {
                    structuresInDimension.put(structure, spawns);
                }
            });

            if (!structuresInDimension.isEmpty()) {
                addStructureGroups(dimensionBuilder, structuresInDimension);
                spawnsHere = true;
            }

            if (spawnsHere) {
                root.add(dimensionBuilder);
            }
        })).build(CoreLang.Spawn.SPAWNS);
    }

    @NotNull
    private Map<EntityType<?>, TooltipNode> buildBiomeSpawns(ResourceLocation biome) {
        Map<EntityType<?>, List<Spawn>> biomeSpawns = new HashMap<>();
        Map<EntityType<?>, Map<ResourceLocation, List<Spawn>>> structureSpawns = new HashMap<>();
        List<Entry> order = new ArrayList<>(biomeEntries.getOrDefault(biome, Collections.emptyList()));
        Map<EntityType<?>, TooltipNode> result = new LinkedHashMap<>();

        for (Entry entry : order) {
            biomeSpawns.computeIfAbsent(entry.type(), (k) -> new ArrayList<>()).add(entry.spawn());
        }

        structureEntries.forEach((structure, entries) -> {
            if (structureBiomes.get(structure).contains(biome)) {
                for (Entry entry : entries) {
                    structureSpawns.computeIfAbsent(entry.type(), (k) -> new TreeMap<>()).computeIfAbsent(structure, (k) -> new ArrayList<>()).add(entry.spawn());
                    order.add(entry);
                }
            }
        });

        for (Entry entry : sorted(order)) {
            result.computeIfAbsent(entry.type(), (type) -> TooltipBuilder.branch((b) -> {
                List<Spawn> spawns = biomeSpawns.getOrDefault(type, Collections.emptyList());

                for (Spawn spawn : spawns) {
                    b.add(TooltipBuilder.asElement(addSpawn(TooltipBuilder.branch((x) -> {}), spawn), spawns.size()));
                }

                addStructureGroups(b, structureSpawns.getOrDefault(type, Collections.emptyMap()));
            }).build());
        }

        return result;
    }

    private void addBiome(ResourceLocation biome, MobSpawnSettings settings) {
        List<Entry> entries = new ArrayList<>();

        for (MobCategory category : MobCategory.values()) {
            for (MobSpawnSettings.SpawnerData data : settings.getMobs(category).unwrap()) {
                entries.add(new Entry(data.type, new Spawn(category, data.getWeight().asInt(), data.minCount, data.maxCount, settings.getMobSpawnCost(data.type))));
            }
        }

        for (Entry entry : entries) {
            biomeEntries.computeIfAbsent(biome, (k) -> new ArrayList<>()).add(entry);
            entityBiomes.computeIfAbsent(entry.type(), (k) -> new HashMap<>()).computeIfAbsent(biome, (k) -> new ArrayList<>()).add(entry.spawn());
        }
    }

    private void addStructure(ResourceLocation structure, Structure.StructureSettings settings) {
        List<Entry> entries = new ArrayList<>();

        settings.spawnOverrides().forEach((category, override) -> {
            // Mods (Hybrid Aquatic) add overrides under unknown categories, which arrive as a null key.
            if (category == null) {
                return;
            }

            for (MobSpawnSettings.SpawnerData data : override.spawns().unwrap()) {
                entries.add(new Entry(data.type, new Spawn(category, data.getWeight().asInt(), data.minCount, data.maxCount, null)));
            }
        });

        if (entries.isEmpty()) {
            return;
        }

        structureBiomes.put(structure, toIds(settings.biomes().stream().toList()));

        for (Entry entry : entries) {
            structureEntries.computeIfAbsent(structure, (k) -> new ArrayList<>()).add(entry);
            entityStructures.computeIfAbsent(entry.type(), (k) -> new TreeMap<>()).computeIfAbsent(structure, (k) -> new ArrayList<>()).add(entry.spawn());
        }
    }

    private static void addBiomeGroup(TooltipBuilder dimension, Spawn spawn, SortedSet<ResourceLocation> biomes, Set<ResourceLocation> biomesInDimension) {
        if (biomes.size() == biomesInDimension.size()) {
            addSpawn(dimension, spawn);
            return;
        }

        boolean excluded = biomes.size() * 2 > biomesInDimension.size();
        List<ResourceLocation> listed = excluded ? biomesInDimension.stream().filter((b) -> !biomes.contains(b)).toList() : List.copyOf(biomes);

        for (int i = 0; i < listed.size(); i++) {
            TooltipBuilder line = TooltipBuilder.value(listed.get(i)).key(excluded ? CoreLang.Spawn.BIOME_EXCLUDED : CoreLang.Spawn.BIOME_INCLUDED);

            dimension.add(i == listed.size() - 1 ? addSpawn(line, spawn) : line);
        }
    }

    private static void addStructureGroups(TooltipBuilder builder, Map<ResourceLocation, List<Spawn>> structures) {
        Map<Spawn, SortedSet<ResourceLocation>> groups = new HashMap<>();

        structures.forEach((structure, spawns) -> {
            for (Spawn spawn : spawns) {
                groups.computeIfAbsent(spawn, (k) -> new TreeSet<>()).add(structure);
            }
        });

        for (Map.Entry<Spawn, SortedSet<ResourceLocation>> group : groups.entrySet().stream().sorted(GROUP_ORDER).toList()) {
            List<ResourceLocation> listed = List.copyOf(group.getValue());

            for (int i = 0; i < listed.size(); i++) {
                TooltipBuilder line = TooltipBuilder.value(listed.get(i)).key(CoreLang.Spawn.STRUCTURE);

                builder.add(i == listed.size() - 1 ? addSpawn(line, group.getKey()) : line);
            }
        }
    }

    @NotNull
    private static TooltipBuilder addSpawn(TooltipBuilder builder, Spawn spawn) {
        builder.add(TooltipBuilder.value(spawn.category().getName()).build(CoreLang.Spawn.CATEGORY));
        builder.add(TooltipBuilder.value(spawn.weight()).build(CoreLang.Spawn.WEIGHT));
        builder.add(TooltipBuilder.number(NumberExpr.uniformInt(spawn.minCount(), spawn.maxCount())).build(CoreLang.Spawn.GROUP_SIZE));

        if (spawn.cost() != null) {
            builder.add(TooltipBuilder.value(COST_FORMAT.format(spawn.cost().charge()), COST_FORMAT.format(spawn.cost().energyBudget())).build(CoreLang.Spawn.SPAWN_COST));
        }

        return builder;
    }

    @NotNull
    private static <T> Set<Map.Entry<ResourceKey<T>, T>> entries(RegistryAccess registryAccess, ResourceKey<? extends Registry<T>> key) {
        return registryAccess.registry(key).map(Registry::entrySet).orElse(Collections.emptySet());
    }

    @NotNull
    private static List<Entry> sorted(List<Entry> entries) {
        return entries.stream().sorted(ENTRY_ORDER).toList();
    }

    @NotNull
    private static Set<ResourceLocation> toIds(Collection<Holder<Biome>> biomes) {
        return biomes.stream().flatMap((b) -> b.unwrapKey().stream()).map(ResourceKey::location).collect(Collectors.toCollection(TreeSet::new));
    }

    private record Spawn(MobCategory category, int weight, int minCount, int maxCount, @Nullable MobSpawnSettings.MobSpawnCost cost) {}

    private record Entry(EntityType<?> type, Spawn spawn) {}
}
