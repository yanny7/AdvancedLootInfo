package com.yanny.awi.test;

import com.mojang.serialization.Lifecycle;
import com.yanny.aci.spawn.SpawnInfo;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutStructure;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.awi.test.TooltipTestSuite.LOOKUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpawnInfoTest {
    private static final Function<Structure, Structure.StructureSettings> OWN_SETTINGS =
            (s) -> new Structure.StructureSettings(s.biomes(), s.spawnOverrides(), s.step(), s.terrainAdaptation());

    private static MappedRegistry<Biome> biomes;
    private static MappedRegistry<LevelStem> levelStems;
    private static MappedRegistry<Structure> structures;
    private static SpawnInfo spawnInfo;

    @BeforeAll
    static void setup() {
        biomes = new MappedRegistry<>(Registries.BIOME, Lifecycle.stable());
        levelStems = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable());
        structures = new MappedRegistry<>(Registries.STRUCTURE, Lifecycle.stable());

        Holder<Biome> a = biome("a", new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 95, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 100, 1, 1))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4))
                .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4)));
        Holder<Biome> b = biome("b", new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 95, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 100, 1, 1))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4)));
        Holder<Biome> c = biome("c", new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 95, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 100, 1, 1)));
        Holder<Biome> d = biome("d", new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 19, 4, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 100, 1, 1)));
        Holder<Biome> e = biome("e", new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
                .addMobCharge(EntityType.STRIDER, 0.7, 0.15));

        levelStem("overworld", a, b, c, d);
        levelStem("nether", e);

        Registry.register(structures, key(Registries.STRUCTURE, "fort"), new SwampHutStructure(new Structure.StructureSettings(
                HolderSet.direct(e),
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3)))),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE
        )));

        biomes.freeze();
        levelStems.freeze();
        structures.freeze();
        spawnInfo = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, structures)), OWN_SETTINGS);
    }

    @Test
    public void testEntityTypes() {
        assertEquals(Set.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.COW, EntityType.STRIDER, EntityType.BLAZE),
                spawnInfo.getEntityTypes());
    }

    @Test
    public void testEveryBiomeOfDimension() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityType.SKELETON), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Category: monster",
                "    -> Weight: 100",
                "    -> Group size: 1"
        ));
    }

    @Test
    public void testMajorityListsExceptions() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityType.ZOMBIE), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> - test:d",
                "      -> Category: monster",
                "      -> Weight: 95",
                "      -> Group size: 4",
                "    -> + test:d",
                "      -> Category: monster",
                "      -> Weight: 19",
                "      -> Group size: 4"
        ));
    }

    @Test
    public void testMinorityListsBiomes() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityType.COW), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> + test:a",
                "      -> Category: creature",
                "      -> Weight: 8",
                "      -> Group size: 4"
        ));
    }

    @Test
    public void testHalfOfBiomesListsBiomes() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityType.SPIDER), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> + test:a",
                "    -> + test:b",
                "      -> Category: monster",
                "      -> Weight: 100",
                "      -> Group size: 4"
        ));
    }

    @Test
    public void testSpawnCost() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityType.STRIDER), List.of(
                "Spawns:",
                "  -> Dimension: test:nether",
                "    -> Category: creature",
                "    -> Weight: 60",
                "    -> Group size: 1 to 2",
                "    -> Spawn cost: charge 0.7, budget 0.15"
        ));
    }

    @Test
    public void testStructureSpawn() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityType.BLAZE), List.of(
                "Spawns:",
                "  -> Dimension: test:nether",
                "    -> Structure: test:fort",
                "      -> Category: monster",
                "      -> Weight: 10",
                "      -> Group size: 2 to 3"
        ));
    }

    @Test
    public void testNoSpawn() {
        assertTrue(spawnInfo.getEntityTooltip(EntityType.PIG).isBlank(false));
        assertTrue(spawnInfo.getBiomeSpawns(ResourceLocation.fromNamespaceAndPath("test", "missing")).isEmpty());
    }

    @Test
    public void testBiomeSpawns() {
        Map<EntityType<?>, TooltipNode> spawns = spawnInfo.getBiomeSpawns(ResourceLocation.fromNamespaceAndPath("test", "d"));

        assertEquals(List.of(EntityType.SKELETON, EntityType.ZOMBIE), List.copyOf(spawns.keySet()));
        assertTooltip(spawns.get(EntityType.SKELETON), List.of(
                "Category: monster",
                "Weight: 100",
                "Group size: 1"
        ));
        assertTooltip(spawns.get(EntityType.ZOMBIE), List.of(
                "Category: monster",
                "Weight: 19",
                "Group size: 4"
        ));
    }

    @Test
    public void testBiomeSpawnsWithStructure() {
        Map<EntityType<?>, TooltipNode> spawns = spawnInfo.getBiomeSpawns(ResourceLocation.fromNamespaceAndPath("test", "e"));

        assertEquals(List.of(EntityType.BLAZE, EntityType.STRIDER), List.copyOf(spawns.keySet()));
        assertTooltip(spawns.get(EntityType.BLAZE), List.of(
                "Structure: test:fort",
                "  -> Category: monster",
                "  -> Weight: 10",
                "  -> Group size: 2 to 3"
        ));
        assertTooltip(spawns.get(EntityType.STRIDER), List.of(
                "Category: creature",
                "Weight: 60",
                "Group size: 1 to 2",
                "Spawn cost: charge 0.7, budget 0.15"
        ));
    }

    @Test
    public void testBiomeSpawnsMergeBiomeAndStructure() {
        MappedRegistry<Structure> registry = structures(Map.of(
                "camp", spawnSettings(HolderSet.direct(biomes.getHolderOrThrow(key(Registries.BIOME, "a"))), EntityType.ZOMBIE)
        ));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), OWN_SETTINGS);
        Map<EntityType<?>, TooltipNode> spawns = info.getBiomeSpawns(ResourceLocation.fromNamespaceAndPath("test", "a"));

        assertEquals(List.of(EntityType.SKELETON, EntityType.SPIDER, EntityType.ZOMBIE, EntityType.COW), List.copyOf(spawns.keySet()));
        assertTooltip(spawns.get(EntityType.ZOMBIE), List.of(
                "Category: monster",
                "Weight: 95",
                "Group size: 4",
                "Structure: test:camp",
                "  -> Category: monster",
                "  -> Weight: 1",
                "  -> Group size: 1"
        ));
    }

    @Test
    public void testStructuresWithSameSpawnAreGrouped() {
        HolderSet<Biome> biomeSet = HolderSet.direct(biomes.getHolderOrThrow(key(Registries.BIOME, "a")));
        MappedRegistry<Structure> registry = structures(Map.of(
                "camp", spawnSettings(biomeSet, EntityType.WITCH),
                "hut", spawnSettings(biomeSet, EntityType.WITCH),
                "tower", new Structure.StructureSettings(
                        biomeSet,
                        Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                                WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1)))),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.NONE
                )
        ));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), OWN_SETTINGS);

        assertTooltip(info.getEntityTooltip(EntityType.WITCH), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Structure: test:camp",
                "    -> Structure: test:hut",
                "      -> Category: monster",
                "      -> Weight: 1",
                "      -> Group size: 1",
                "    -> Structure: test:tower",
                "      -> Category: monster",
                "      -> Weight: 5",
                "      -> Group size: 1"
        ));
        assertTooltip(info.getBiomeSpawns(ResourceLocation.fromNamespaceAndPath("test", "a")).get(EntityType.WITCH), List.of(
                "Structure: test:camp",
                "Structure: test:hut",
                "  -> Category: monster",
                "  -> Weight: 1",
                "  -> Group size: 1",
                "Structure: test:tower",
                "  -> Category: monster",
                "  -> Weight: 5",
                "  -> Group size: 1"
        ));
    }

    @Test
    public void testStructureSettingsFromPlatform() {
        Structure.StructureSettings modified = new Structure.StructureSettings(
                HolderSet.direct(biomes.getHolderOrThrow(key(Registries.BIOME, "a"))),
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1)))),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE
        );
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, structures)), (s) -> modified);

        assertTrue(info.getEntityTooltip(EntityType.BLAZE).isBlank(false));
        assertTooltip(info.getEntityTooltip(EntityType.WITCH), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Structure: test:fort",
                "      -> Category: monster",
                "      -> Weight: 1",
                "      -> Group size: 1"
        ));
    }

    @Test
    public void testFailingStructureSettingsSkipOnlyThatStructure() {
        MappedRegistry<Structure> registry = structures(Map.of(
                "fort", spawnSettings(HolderSet.direct(biomes.getHolderOrThrow(key(Registries.BIOME, "e"))), EntityType.BLAZE),
                "hut", spawnSettings(HolderSet.direct(biomes.getHolderOrThrow(key(Registries.BIOME, "a"))), EntityType.WITCH)
        ));
        Structure fort = registry.getOrThrow(key(Registries.STRUCTURE, "fort"));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), (s) -> {
            if (s == fort) {
                throw new IllegalStateException("broken structure");
            }

            return OWN_SETTINGS.apply(s);
        });

        assertTrue(info.getEntityTooltip(EntityType.BLAZE).isBlank(false));
        assertTooltip(info.getEntityTooltip(EntityType.WITCH), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Structure: test:hut",
                "      -> Category: monster",
                "      -> Weight: 1",
                "      -> Group size: 1"
        ));
        assertTooltip(info.getEntityTooltip(EntityType.SKELETON), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Category: monster",
                "    -> Weight: 100",
                "    -> Group size: 1"
        ));
    }

    @Test
    public void testUnboundBiomeTagSpawnsNowhere() {
        HolderSet<Biome> unbound = biomes.getOrCreateTag(TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("test", "unbound")));
        MappedRegistry<Structure> registry = structures(Map.of(
                "fort", spawnSettings(HolderSet.direct(biomes.getHolderOrThrow(key(Registries.BIOME, "e"))), EntityType.BLAZE),
                "tagged", spawnSettings(unbound, EntityType.WITCH)
        ));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), OWN_SETTINGS);

        assertTrue(info.getEntityTypes().contains(EntityType.WITCH));
        assertTrue(info.getEntityTooltip(EntityType.WITCH).isBlank(false));
        assertEquals(List.of(EntityType.BLAZE, EntityType.STRIDER), List.copyOf(info.getBiomeSpawns(ResourceLocation.fromNamespaceAndPath("test", "e")).keySet()));
    }

    @Test
    public void testMissingRegistriesYieldNoSpawns() {
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes)), OWN_SETTINGS);

        assertTrue(info.getEntityTooltip(EntityType.ZOMBIE).isBlank(false));
        assertEquals(Set.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.COW, EntityType.STRIDER), info.getEntityTypes());
    }

    @NotNull
    private static MappedRegistry<Structure> structures(Map<String, Structure.StructureSettings> settings) {
        MappedRegistry<Structure> registry = new MappedRegistry<>(Registries.STRUCTURE, Lifecycle.stable());

        settings.forEach((name, s) -> Registry.register(registry, key(Registries.STRUCTURE, name), new SwampHutStructure(s)));
        registry.freeze();
        return registry;
    }

    @NotNull
    private static Structure.StructureSettings spawnSettings(HolderSet<Biome> biomeSet, EntityType<?> type) {
        return new Structure.StructureSettings(
                biomeSet,
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                        WeightedRandomList.create(new MobSpawnSettings.SpawnerData(type, 1, 1, 1)))),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE
        );
    }

    @NotNull
    private static Holder<Biome> biome(String name, MobSpawnSettings.Builder spawns) {
        Biome biome = new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();

        return Registry.registerForHolder(biomes, key(Registries.BIOME, name), biome);
    }

    @SafeVarargs
    private static void levelStem(String name, Holder<Biome>... possibleBiomes) {
        Holder<DimensionType> type = LOOKUP.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        BiomeSource biomeSource = new CheckerboardColumnBiomeSource(HolderSet.direct(possibleBiomes), 2);

        Registry.register(levelStems, key(Registries.LEVEL_STEM, name), new LevelStem(type, new NoiseBasedChunkGenerator(biomeSource, Holder.direct(NoiseGeneratorSettings.dummy()))));
    }

    @NotNull
    private static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, ResourceLocation.fromNamespaceAndPath("test", name));
    }
}
