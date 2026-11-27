package com.yanny.awi.compatibility;

import com.yanny.awi.api.IBlockNode;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.plugin.common.nodes.BaseTerrainNode;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import com.yanny.awi.plugin.common.nodes.GenerationStepNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import com.yanny.awi.plugin.common.nodes.PlacedFeatureNode;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class BlockGeneration {
    public static final int BASE_TERRAIN_STEP = -1;
    private static final int MAX_GROUP_ROWS = 3;

    public record Page(List<Block> blocks, List<Dimension> dimensions) {}

    public record Dimension(ResourceLocation id, int minY, int height, int biomeCount, List<BiomeGroup> groups) {
        @Unmodifiable
        @NotNull
        public List<Source> sources() {
            return groups.stream().flatMap((g) -> g.sources().stream()).toList();
        }

        @Unmodifiable
        @NotNull
        public List<BiomeGroup> rows() {
            return groups.stream().filter((g) -> !merged(g)).toList();
        }

        @Unmodifiable
        @NotNull
        public List<BiomeGroup> merged() {
            return groups.stream().filter(this::merged).toList();
        }

        private boolean merged(BiomeGroup group) {
            return groups.size() > MAX_GROUP_ROWS && group.sources().size() == 1;
        }
    }

    public record BiomeGroup(List<ResourceLocation> biomes, boolean allBiomes, List<Source> sources) {}

    /** {@code node} is the {@link PlacedFeatureNode}, or for base terrain the block node carrying the layer info. */
    public record Source(int step, IDataNode node, List<IBlockNode> blockNodes) {
        @Nullable
        public PlacedFeatureNode feature() {
            return node instanceof PlacedFeatureNode feature ? feature : null;
        }
    }

    private BlockGeneration() {}

    @NotNull
    public static List<Page> build(Map<ResourceLocation, LevelStemNode> worldgenData) {
        Map<Block, Map<ResourceLocation, Map<SourceKey, Accumulator>>> index = new LinkedHashMap<>();
        Map<ResourceLocation, Set<ResourceLocation>> dimensionBiomes = new LinkedHashMap<>();
        Families families = new Families();
        List<Page> pages = new ArrayList<>();

        worldgenData.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> {
            ResourceLocation dimension = entry.getKey();
            Set<ResourceLocation> biomes = dimensionBiomes.computeIfAbsent(dimension, (d) -> new LinkedHashSet<>());

            for (IDataNode child : entry.getValue().nodes()) {
                if (child instanceof BiomeNode biome) {
                    biomes.add(biome.getBiomeId());
                    indexBiome(index, families, dimension, biome);
                }
            }
        });

        families.members(index.keySet()).forEach((blocks) -> {
            Map<ResourceLocation, Map<SourceKey, Accumulator>> merged = new TreeMap<>();
            List<Dimension> dimensions = new ArrayList<>();

            for (Block block : blocks) {
                index.get(block).forEach((dimension, sources) -> sources.forEach((key, accumulator) ->
                        merged.computeIfAbsent(dimension, (d) -> new LinkedHashMap<>())
                                .computeIfAbsent(key, (k) -> new Accumulator(accumulator.step, accumulator.node))
                                .merge(accumulator)));
            }

            merged.forEach((dimension, sources) -> {
                LevelStemNode level = worldgenData.get(dimension);
                Set<ResourceLocation> biomes = dimensionBiomes.get(dimension);

                dimensions.add(new Dimension(dimension, level.getMinY(), level.getHeight(), biomes.size(), group(sources.values(), biomes)));
            });

            pages.add(new Page(blocks, dimensions));
        });

        pages.sort(Comparator.comparing((p) -> Registry.BLOCK.getKey(p.blocks().get(0))));
        return pages;
    }

    private static void indexBiome(Map<Block, Map<ResourceLocation, Map<SourceKey, Accumulator>>> index, Families families, ResourceLocation dimension,
                                   BiomeNode biome) {
        for (IDataNode stepChild : biome.nodes()) {
            if (stepChild instanceof BaseTerrainNode base) {
                for (IDataNode node : base.nodes()) {
                    if (node instanceof IBlockNode blockNode) {
                        add(index, dimension, biome.getBiomeId(), blockNode, new SourceKey(BASE_TERRAIN_STEP, node.getTooltip()), BASE_TERRAIN_STEP, node);
                    }
                }
            } else if (stepChild instanceof GenerationStepNode step) {
                int stepIndex = step.getGenerationStep();

                for (IDataNode featureChild : step.nodes()) {
                    if (featureChild instanceof PlacedFeatureNode feature) {
                        Object featureKey = feature.getFeatureId() != null ? feature.getFeatureId() : feature.getTooltip();
                        List<Block> targets = new ArrayList<>();

                        for (IDataNode node : feature.nodes()) {
                            if (node instanceof IBlockNode blockNode) {
                                add(index, dimension, biome.getBiomeId(), blockNode, new SourceKey(stepIndex, featureKey), stepIndex, feature);
                                targets.addAll(blockNode.getBlocks());
                            }
                        }

                        if (feature.getStats() != null && feature.getStats().veinSize() > 0) {
                            families.union(targets);
                        }
                    }
                }
            }
        }
    }

    private static void add(Map<Block, Map<ResourceLocation, Map<SourceKey, Accumulator>>> index, ResourceLocation dimension, ResourceLocation biome,
                            IBlockNode blockNode, SourceKey key, int step, IDataNode node) {
        for (Block block : blockNode.getBlocks()) {
            Accumulator accumulator = index.computeIfAbsent(block, (b) -> new LinkedHashMap<>())
                    .computeIfAbsent(dimension, (d) -> new LinkedHashMap<>())
                    .computeIfAbsent(key, (k) -> new Accumulator(step, node));

            accumulator.blockNodes.putIfAbsent(List.copyOf(blockNode.getBlocks()), blockNode);
            accumulator.biomes.add(biome);
        }
    }

    @Unmodifiable
    @NotNull
    private static List<BiomeGroup> group(Iterable<Accumulator> sources, Set<ResourceLocation> dimensionBiomes) {
        Map<List<ResourceLocation>, List<Source>> byBiomes = new LinkedHashMap<>();

        for (Accumulator accumulator : sources) {
            byBiomes.computeIfAbsent(accumulator.biomes.stream().sorted().toList(), (b) -> new ArrayList<>())
                    .add(new Source(accumulator.step, accumulator.node, List.copyOf(accumulator.blockNodes.values())));
        }

        return byBiomes.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<List<ResourceLocation>, List<Source>> e) -> -e.getKey().size())
                        .thenComparing((e) -> e.getKey().get(0)))
                .map((e) -> new BiomeGroup(e.getKey(), e.getKey().size() == dimensionBiomes.size(),
                        e.getValue().stream().sorted(Comparator.comparingInt(Source::step).thenComparing(BlockGeneration::sortKey)).toList()))
                .toList();
    }

    @NotNull
    private static String sortKey(Source source) {
        PlacedFeatureNode feature = source.feature();
        return feature != null && feature.getFeatureId() != null ? feature.getFeatureId().toString() : "";
    }

    private record SourceKey(int step, Object key) {}

    private static final class Accumulator {
        private final int step;
        private final IDataNode node;
        private final Map<List<Block>, IBlockNode> blockNodes = new LinkedHashMap<>();
        private final Set<ResourceLocation> biomes = new LinkedHashSet<>();

        private Accumulator(int step, IDataNode node) {
            this.step = step;
            this.node = node;
        }

        private void merge(Accumulator other) {
            other.blockNodes.forEach(blockNodes::putIfAbsent);
            biomes.addAll(other.biomes);
        }
    }

    private static final class Families {
        private final Map<Block, Block> parents = new LinkedHashMap<>();

        private void union(List<Block> blocks) {
            for (Block block : blocks) {
                parents.put(find(block), find(blocks.get(0)));
            }
        }

        @NotNull
        private Block find(Block block) {
            Block parent = parents.computeIfAbsent(block, (b) -> b);

            return parent == block ? block : find(parent);
        }

        @NotNull
        private Collection<List<Block>> members(Set<Block> blocks) {
            Map<Block, List<Block>> families = new LinkedHashMap<>();

            for (Block block : parents.keySet()) {
                if (blocks.contains(block)) {
                    families.computeIfAbsent(find(block), (b) -> new ArrayList<>()).add(block);
                }
            }

            for (Block block : blocks) {
                if (!parents.containsKey(block)) {
                    families.put(block, List.of(block));
                }
            }

            return families.values();
        }
    }
}
