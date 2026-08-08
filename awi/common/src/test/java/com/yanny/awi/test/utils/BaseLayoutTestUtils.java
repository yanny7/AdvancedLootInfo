package com.yanny.awi.test.utils;

import com.mojang.serialization.Lifecycle;
import com.yanny.awi.plugin.common.nodes.BaseLayoutScanner;
import com.yanny.awi.plugin.common.nodes.NodeUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Runs the base-layout scan against vanilla worldgen data without a running server: {@code VanillaRegistries} supplies
 * the datapack registries and the vanilla {@code WorldPreset} supplies real {@link LevelStem}s, so the scan sees the
 * same dimensions/biome sources/noise settings it sees in game.
 */
public class BaseLayoutTestUtils {
    private static HolderLookup.Provider lookup;
    private static RegistryAccess registryAccess;
    private static Registry<LevelStem> levelStems;

    /** Bootstraps Minecraft and the vanilla worldgen registries once; safe to call from several test classes. */
    public static synchronized void bootstrap() {
        if (levelStems != null) {
            return;
        }

        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();

        lookup = VanillaRegistries.createLookup();

        // The scan only ever asks for BIOME (the ProtoChunk biome palette) and NOISE (RandomState).
        registryAccess = new RegistryAccess.ImmutableRegistryAccess(List.of(copy(Registries.BIOME), copy(Registries.NOISE)));

        WorldPreset preset = lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL).value();
        MappedRegistry<LevelStem> registry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable());

        preset.createWorldDimensions().dimensions().forEach((key, stem) -> Registry.register(registry, key, stem));
        levelStems = registry.freeze();
    }

    /**
     * Scans every (dimension, biome) of the vanilla world preset and flattens the result into
     * {@code dimension -> biome -> sorted block descriptions}, ready to be compared or serialized.
     */
    @NotNull
    public static Map<String, Map<String, List<String>>> scan(long seed, NodeUtils.ScanSettings settings) {
        BaseLayoutScanner scanner = BaseLayoutScanner.scan(registryAccess, seed, levelStems, settings, true);
        Map<String, Map<String, List<String>>> result = new TreeMap<>();

        for (LevelStem levelStem : levelStems) {
            ResourceLocation dimension = levelStems.getKey(levelStem);
            Map<String, List<String>> biomes = new TreeMap<>();

            scanner.getBaseLayouts(dimension).forEach((biome, layers) -> biomes.put(biomeName(biome), describe(layers)));
            result.put(String.valueOf(dimension), biomes);
        }

        return result;
    }

    /** One stable, human-readable line per discovered block: {@code block storage [ranges] water placement}. */
    @NotNull
    private static List<String> describe(NodeUtils.LayerHolder layers) {
        List<String> lines = new ArrayList<>();

        for (NodeUtils.BlockInfo info : layers.getBlockInfos()) {
            StringBuilder ranges = new StringBuilder();

            for (int i = 0; i < info.ranges().size(); i++) {
                ranges.append(i > 0 ? ", " : "").append(info.ranges().get(i).toIntString());
            }

            lines.add("%s %s [%s] %s %s".formatted(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(info.block()),
                    info.storageType(), ranges, info.water(), info.placement()));
        }

        lines.sort(String::compareTo);

        return lines;
    }

    @NotNull
    private static String biomeName(Holder<Biome> biome) {
        return biome.unwrapKey().map((key) -> key.location().toString()).orElse("<unnamed biome>");
    }

    @NotNull
    private static <T> Registry<T> copy(ResourceKey<? extends Registry<T>> key) {
        MappedRegistry<T> registry = new MappedRegistry<>(key, Lifecycle.stable());

        lookup.lookupOrThrow(key).listElements().forEach((ref) -> Registry.register(registry, ref.key(), ref.value()));

        return registry.freeze();
    }
}
