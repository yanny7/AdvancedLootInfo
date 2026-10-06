package com.yanny.awi.test;

import com.mojang.serialization.Lifecycle;
import com.yanny.aci.spawn.SpawnInfo;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
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

import java.util.ArrayList;
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
                .addSpawn(EntityTypes.ZOMBIE, MobCategory.MONSTER, 95, ConstantInt.of(4))
                .addSpawn(EntityTypes.SKELETON, MobCategory.MONSTER, 100, ConstantInt.of(1))
                .addSpawn(EntityTypes.SPIDER, MobCategory.MONSTER, 100, ConstantInt.of(4))
                .addSpawn(EntityTypes.COW, MobCategory.CREATURE, 8, ConstantInt.of(4)));
        Holder<Biome> b = biome("b", new MobSpawnSettings.Builder()
                .addSpawn(EntityTypes.ZOMBIE, MobCategory.MONSTER, 95, ConstantInt.of(4))
                .addSpawn(EntityTypes.SKELETON, MobCategory.MONSTER, 100, ConstantInt.of(1))
                .addSpawn(EntityTypes.SPIDER, MobCategory.MONSTER, 100, ConstantInt.of(4)));
        Holder<Biome> c = biome("c", new MobSpawnSettings.Builder()
                .addSpawn(EntityTypes.ZOMBIE, MobCategory.MONSTER, 95, ConstantInt.of(4))
                .addSpawn(EntityTypes.SKELETON, MobCategory.MONSTER, 100, ConstantInt.of(1)));
        Holder<Biome> d = biome("d", new MobSpawnSettings.Builder()
                .addSpawn(EntityTypes.ZOMBIE, MobCategory.MONSTER, 19, ConstantInt.of(4))
                .addSpawn(EntityTypes.SKELETON, MobCategory.MONSTER, 100, ConstantInt.of(1)));
        Holder<Biome> e = biome("e", new MobSpawnSettings.Builder()
                .addSpawn(EntityTypes.STRIDER, MobCategory.CREATURE, 60, UniformInt.of(1, 2))
                .addMobSpawnCost(EntityTypes.STRIDER, 0.7, 0.15));

        List<Holder<Biome>> wide = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();

            if (i < 11) {
                spawns.addSpawn(EntityTypes.CREEPER, MobCategory.MONSTER, 100, ConstantInt.of(4));
            }

            if (i < 10) {
                spawns.addSpawn(EntityTypes.ENDERMAN, MobCategory.MONSTER, 10, UniformInt.of(1, 4));
            }

            wide.add(biome("w%02d".formatted(i), spawns));
        }

        levelStem("overworld", a, b, c, d);
        levelStem("nether", e);
        levelStem("wide", wide.toArray(Holder[]::new));

        Registry.register(structures, key(Registries.STRUCTURE, "fort"), new SwampHutStructure(new Structure.StructureSettings(
                HolderSet.direct(e),
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                        WeightedList.<MobSpawnSettings.SpawnerData>builder().add(new MobSpawnSettings.SpawnerData(EntityTypes.BLAZE, UniformInt.of(2, 3)), 10).build())),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE
        )));

        biomes.freeze();
        levelStems.freeze();
        structures.freeze();
        spawnInfo = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, structures)), OWN_SETTINGS, (t) -> true, false);
    }

    @Test
    public void testEntityTypes() {
        assertEquals(Set.of(EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.SPIDER, EntityTypes.COW, EntityTypes.STRIDER, EntityTypes.BLAZE,
                EntityTypes.CREEPER, EntityTypes.ENDERMAN), spawnInfo.getEntityTypes());
    }

    @Test
    public void testEveryBiomeOfDimension() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.SKELETON), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Category: Monster",
                "    -> Weight: 100",
                "    -> Group size: 1"
        ));
    }

    @Test
    public void testFewBiomesListedEvenWhenMajority() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.ZOMBIE), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> + test:a",
                "    -> + test:b",
                "    -> + test:c",
                "      -> Category: Monster",
                "      -> Weight: 95",
                "      -> Group size: 4",
                "    -> + test:d",
                "      -> Category: Monster",
                "      -> Weight: 19",
                "      -> Group size: 4"
        ));
    }

    @Test
    public void testManyBiomesListExceptions() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.CREEPER), List.of(
                "Spawns:",
                "  -> Dimension: test:wide",
                "    -> - test:w11",
                "      -> Category: Monster",
                "      -> Weight: 100",
                "      -> Group size: 4"
        ));
    }

    @Test
    public void testTenBiomesListedEvenWhenMajority() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.ENDERMAN), List.of(
                "Spawns:",
                "  -> Dimension: test:wide",
                "    -> + test:w00",
                "    -> + test:w01",
                "    -> + test:w02",
                "    -> + test:w03",
                "    -> + test:w04",
                "    -> + test:w05",
                "    -> + test:w06",
                "    -> + test:w07",
                "    -> + test:w08",
                "    -> + test:w09",
                "      -> Category: Monster",
                "      -> Weight: 10",
                "      -> Group size: 1 to 4"
        ));
    }

    @Test
    public void testMinorityListsBiomes() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.COW), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> + test:a",
                "      -> Category: Creature",
                "      -> Weight: 8",
                "      -> Group size: 4"
        ));
    }

    @Test
    public void testHalfOfBiomesListsBiomes() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.SPIDER), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> + test:a",
                "    -> + test:b",
                "      -> Category: Monster",
                "      -> Weight: 100",
                "      -> Group size: 4"
        ));
    }

    @Test
    public void testSpawnCost() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.STRIDER), List.of(
                "Spawns:",
                "  -> Dimension: test:nether",
                "    -> Category: Creature",
                "    -> Weight: 60",
                "    -> Group size: 1 to 2",
                "    -> Spawn cost: charge 0.7, budget 0.15"
        ));
    }

    @Test
    public void testStructureSpawn() {
        assertTooltip(spawnInfo.getEntityTooltip(EntityTypes.BLAZE), List.of(
                "Spawns:",
                "  -> Dimension: test:nether",
                "    -> Structure: test:fort",
                "      -> Category: Monster",
                "      -> Weight: 10",
                "      -> Group size: 2 to 3"
        ));
    }

    @Test
    public void testNoSpawn() {
        assertTrue(spawnInfo.getEntityTooltip(EntityTypes.PIG).isBlank(false));
        assertTrue(spawnInfo.getBiomeSpawns(Identifier.fromNamespaceAndPath("test", "missing")).isEmpty());
    }

    @Test
    public void testBiomeSpawns() {
        Map<EntityType<?>, TooltipNode> spawns = spawnInfo.getBiomeSpawns(Identifier.fromNamespaceAndPath("test", "d"));

        assertEquals(List.of(EntityTypes.SKELETON, EntityTypes.ZOMBIE), List.copyOf(spawns.keySet()));
        assertTooltip(spawns.get(EntityTypes.SKELETON), List.of(
                "Category: Monster",
                "Weight: 100",
                "Group size: 1"
        ));
        assertTooltip(spawns.get(EntityTypes.ZOMBIE), List.of(
                "Category: Monster",
                "Weight: 19",
                "Group size: 4"
        ));
    }

    @Test
    public void testBiomeSpawnsWithStructure() {
        Map<EntityType<?>, TooltipNode> spawns = spawnInfo.getBiomeSpawns(Identifier.fromNamespaceAndPath("test", "e"));

        assertEquals(List.of(EntityTypes.BLAZE, EntityTypes.STRIDER), List.copyOf(spawns.keySet()));
        assertTooltip(spawns.get(EntityTypes.BLAZE), List.of(
                "Structure: test:fort",
                "  -> Category: Monster",
                "  -> Weight: 10",
                "  -> Group size: 2 to 3"
        ));
        assertTooltip(spawns.get(EntityTypes.STRIDER), List.of(
                "Category: Creature",
                "Weight: 60",
                "Group size: 1 to 2",
                "Spawn cost: charge 0.7, budget 0.15"
        ));
    }

    @Test
    public void testBiomeSpawnsMergeBiomeAndStructure() {
        MappedRegistry<Structure> registry = structures(Map.of(
                "camp", spawnSettings(HolderSet.direct(biomes.getOrThrow(key(Registries.BIOME, "a"))), EntityTypes.ZOMBIE)
        ));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), OWN_SETTINGS, (t) -> true, false);
        Map<EntityType<?>, TooltipNode> spawns = info.getBiomeSpawns(Identifier.fromNamespaceAndPath("test", "a"));

        assertEquals(List.of(EntityTypes.SKELETON, EntityTypes.SPIDER, EntityTypes.ZOMBIE, EntityTypes.COW), List.copyOf(spawns.keySet()));
        assertTooltip(spawns.get(EntityTypes.ZOMBIE), List.of(
                "Category: Monster",
                "Weight: 95",
                "Group size: 4",
                "Structure: test:camp",
                "  -> Category: Monster",
                "  -> Weight: 1",
                "  -> Group size: 1"
        ));
    }

    @Test
    public void testStructuresWithSameSpawnAreGrouped() {
        HolderSet<Biome> biomeSet = HolderSet.direct(biomes.getOrThrow(key(Registries.BIOME, "a")));
        MappedRegistry<Structure> registry = structures(Map.of(
                "camp", spawnSettings(biomeSet, EntityTypes.WITCH),
                "hut", spawnSettings(biomeSet, EntityTypes.WITCH),
                "tower", new Structure.StructureSettings(
                        biomeSet,
                        Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                                WeightedList.<MobSpawnSettings.SpawnerData>builder().add(new MobSpawnSettings.SpawnerData(EntityTypes.WITCH, ConstantInt.of(1)), 5).build())),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.NONE
                )
        ));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), OWN_SETTINGS, (t) -> true, false);

        assertTooltip(info.getEntityTooltip(EntityTypes.WITCH), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Structure: test:camp",
                "    -> Structure: test:hut",
                "      -> Category: Monster",
                "      -> Weight: 1",
                "      -> Group size: 1",
                "    -> Structure: test:tower",
                "      -> Category: Monster",
                "      -> Weight: 5",
                "      -> Group size: 1"
        ));
        assertTooltip(info.getBiomeSpawns(Identifier.fromNamespaceAndPath("test", "a")).get(EntityTypes.WITCH), List.of(
                "Structure: test:camp",
                "Structure: test:hut",
                "  -> Category: Monster",
                "  -> Weight: 1",
                "  -> Group size: 1",
                "Structure: test:tower",
                "  -> Category: Monster",
                "  -> Weight: 5",
                "  -> Group size: 1"
        ));
    }

    @Test
    public void testStructureSettingsFromPlatform() {
        Structure.StructureSettings modified = new Structure.StructureSettings(
                HolderSet.direct(biomes.getOrThrow(key(Registries.BIOME, "a"))),
                Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                        WeightedList.<MobSpawnSettings.SpawnerData>builder().add(new MobSpawnSettings.SpawnerData(EntityTypes.WITCH, ConstantInt.of(1)), 1).build())),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                TerrainAdjustment.NONE
        );
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, structures)), (s) -> modified, (t) -> true, false);

        assertTrue(info.getEntityTooltip(EntityTypes.BLAZE).isBlank(false));
        assertTooltip(info.getEntityTooltip(EntityTypes.WITCH), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Structure: test:fort",
                "      -> Category: Monster",
                "      -> Weight: 1",
                "      -> Group size: 1"
        ));
    }

    @Test
    public void testFailingStructureSettingsSkipOnlyThatStructure() {
        MappedRegistry<Structure> registry = structures(Map.of(
                "fort", spawnSettings(HolderSet.direct(biomes.getOrThrow(key(Registries.BIOME, "e"))), EntityTypes.BLAZE),
                "hut", spawnSettings(HolderSet.direct(biomes.getOrThrow(key(Registries.BIOME, "a"))), EntityTypes.WITCH)
        ));
        Structure fort = registry.getValueOrThrow(key(Registries.STRUCTURE, "fort"));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), (s) -> {
            if (s == fort) {
                throw new IllegalStateException("broken structure");
            }

            return OWN_SETTINGS.apply(s);
        }, (t) -> true, false);

        assertTrue(info.getEntityTooltip(EntityTypes.BLAZE).isBlank(false));
        assertTooltip(info.getEntityTooltip(EntityTypes.WITCH), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Structure: test:hut",
                "      -> Category: Monster",
                "      -> Weight: 1",
                "      -> Group size: 1"
        ));
        assertTooltip(info.getEntityTooltip(EntityTypes.SKELETON), List.of(
                "Spawns:",
                "  -> Dimension: test:overworld",
                "    -> Category: Monster",
                "    -> Weight: 100",
                "    -> Group size: 1"
        ));
    }

    @Test
    public void testUnboundBiomeTagSpawnsNowhere() {
        HolderSet<Biome> unbound = HolderSet.emptyNamed(biomes, TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("test", "unbound")));
        MappedRegistry<Structure> registry = structures(Map.of(
                "fort", spawnSettings(HolderSet.direct(biomes.getOrThrow(key(Registries.BIOME, "e"))), EntityTypes.BLAZE),
                "tagged", spawnSettings(unbound, EntityTypes.WITCH)
        ));
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, levelStems, registry)), OWN_SETTINGS, (t) -> true, false);

        assertTrue(info.getEntityTypes().contains(EntityTypes.WITCH));
        assertTrue(info.getEntityTooltip(EntityTypes.WITCH).isBlank(false));
        assertEquals(List.of(EntityTypes.BLAZE, EntityTypes.STRIDER), List.copyOf(info.getBiomeSpawns(Identifier.fromNamespaceAndPath("test", "e")).keySet()));
    }

    @Test
    public void testMissingRegistriesYieldNoSpawns() {
        SpawnInfo info = new SpawnInfo(Utils.MOD_ID, new RegistryAccess.ImmutableRegistryAccess(List.of(biomes)), OWN_SETTINGS, (t) -> true, false);

        assertTrue(info.getEntityTooltip(EntityTypes.ZOMBIE).isBlank(false));
        assertEquals(Set.of(EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.SPIDER, EntityTypes.COW, EntityTypes.STRIDER, EntityTypes.CREEPER,
                EntityTypes.ENDERMAN), info.getEntityTypes());
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
                        WeightedList.<MobSpawnSettings.SpawnerData>builder().add(new MobSpawnSettings.SpawnerData(type, ConstantInt.of(1)), 1).build())),
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
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0).build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();

        return Registry.registerForHolder(biomes, key(Registries.BIOME, name), biome);
    }

    @SafeVarargs
    private static void levelStem(String name, Holder<Biome>... possibleBiomes) {
        Holder<DimensionType> type = LOOKUP.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        BiomeSource biomeSource = new CheckerboardColumnBiomeSource(HolderSet.direct(possibleBiomes), 2);

        Registry.register(levelStems, key(Registries.LEVEL_STEM, name), new LevelStem(type, new NoiseBasedChunkGenerator(biomeSource, LOOKUP.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD))));
    }

    @NotNull
    private static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, Identifier.fromNamespaceAndPath("test", name));
    }
}
