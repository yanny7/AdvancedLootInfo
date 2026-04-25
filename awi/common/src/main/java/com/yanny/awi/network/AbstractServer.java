package com.yanny.awi.network;

import com.mojang.logging.LogUtils;
import com.yanny.awi.manager.AwiServerRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import org.slf4j.Logger;

public abstract class AbstractServer {
    private static final int MAX_CHUNK_SIZE = 32 * 1024; // 32 KB
    private static final Logger LOGGER = LogUtils.getLogger();

    public void readWorldgenInfo(ServerLevel level) {
        AwiServerRegistry serverRegistry = PluginManager.getInstance().serverRegistry;
        RegistryAccess registryAccess = level.registryAccess();
        Registry<LevelStem> levelStemRegistry = registryAccess.registryOrThrow(Registries.LEVEL_STEM);
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        Registry<PlacedFeature> placedFeatureRegistry = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
        Registry<ConfiguredFeature<?, ?>> configuredFeatureRegistry = registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE);
        Registry<PlacementModifierType<?>> placementModifierTypeRegistry = registryAccess.registryOrThrow(Registries.PLACEMENT_MODIFIER_TYPE);

        for (LevelStem levelStem : levelStemRegistry) {
            new LevelStemNode(serverRegistry, levelStem);
//            ChunkGenerator generator = levelStem.generator();

            LOGGER.info("Level: {}", levelStemRegistry.getKey(levelStem));

//            if (true) {
//                continue; //FIXME
//            }
//
//            if (generator instanceof NoiseBasedChunkGenerator g) {
//                LOGGER.info("Default block: {}", g.generatorSettings().value().defaultBlock());
//            }
//
//            for (Holder<Biome> possibleBiome : generator.getBiomeSource().possibleBiomes()) {
//                Biome biome = possibleBiome.value();
//                BiomeGenerationSettings generationSettings = biome.getGenerationSettings();
//                List<HolderSet<PlacedFeature>> features = generationSettings.features();
//
//                LOGGER.info("  -> Biome: {}", biomeRegistry.getKey(biome));
//
//                for (int i = 0; i < features.size(); i++) {
//                    HolderSet<PlacedFeature> featureStep = features.get(i);
//
//                    LOGGER.info("    -> Decoration: {} ({})", GenerationStep.Decoration.values()[i], featureStep.size());
//
//                    for (Holder<PlacedFeature> placedFeature : featureStep) {
//                        ConfiguredFeature<?, ?> feature = placedFeature.value().feature().value();
//
//                        LOGGER.info("      -> PlacedFeature: {} (ConfiguredFeature: {})", placedFeatureRegistry.getKey(placedFeature.value()), configuredFeatureRegistry.getKey(feature));
//
//                        for (PlacementModifier placementModifier : placedFeature.value().placement()) {
//                            LOGGER.info("        -> PlacementModifier: {}", placementModifierTypeRegistry.getKey(placementModifier.type()));
//                        }
//                    }
//                }
//            }
        }
    }
}
