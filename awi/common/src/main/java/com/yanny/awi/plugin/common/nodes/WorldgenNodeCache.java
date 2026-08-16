package com.yanny.awi.plugin.common.nodes;

import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reuses {@link PlacedFeatureNode}s and {@link GenerationStepNode}s across the whole scan - biomes of one dimension and
 * dimensions alike share the very same {@link PlacedFeature} instances and repeat whole feature lists.
 *
 * <p>A placed feature node's content is a pure function of (placed feature, column context): the configuration is part
 * of the placed feature's identity ({@code PlacedFeature -> ConfiguredFeature -> FeatureConfiguration}), while the
 * column context decides how vertical anchors resolve. Keying by identity rather than by {@code equals} keeps
 * third-party {@code PlacementModifier} implementations out of the lookup path.
 *
 * <p>A generation step node's content is a pure function of (step, child nodes). The column context is not part of that
 * key because the children are already resolved against one. {@link StepKey} compares its children by identity, since
 * {@link PlacedFeatureNode} does not override {@code equals} and the children are themselves deduplicated here.
 *
 * <p>The resulting tree is a DAG - a shared node is encoded once per occurrence (the wire format is unchanged) and
 * {@code optimizeList} is idempotent over it. Not thread safe: node building runs on the server thread.
 */
public class WorldgenNodeCache {
    private final Map<ColumnContext, Map<PlacedFeature, PlacedFeatureNode>> placedFeatureNodes = new HashMap<>();
    private final Map<StepKey, GenerationStepNode> generationStepNodes = new HashMap<>();

    private int placedFeatureHits = 0;
    private int placedFeatureMisses = 0;
    private int generationStepHits = 0;
    private int generationStepMisses = 0;

    @NotNull
    public GenerationStepNode getOrCreate(IServerUtils utils, GenerationStep.Decoration step, HolderSet<PlacedFeature> features,
                                          ColumnContext columnContext) {
        List<PlacedFeatureNode> children = new ArrayList<>();

        for (Holder<PlacedFeature> placedFeatureHolder : features) {
            ResourceLocation featureId = placedFeatureHolder.unwrapKey().map(ResourceKey::location).orElse(null);
            children.add(getOrCreate(utils, placedFeatureHolder.value(), columnContext, featureId));
        }

        StepKey key = new StepKey(step, children);
        GenerationStepNode cached = generationStepNodes.get(key);

        if (cached != null) {
            generationStepHits++;
            return cached;
        }

        GenerationStepNode node = new GenerationStepNode(step, children);

        generationStepMisses++;
        generationStepNodes.put(key, node);
        return node;
    }

    @NotNull
    private PlacedFeatureNode getOrCreate(IServerUtils utils, PlacedFeature placedFeature, ColumnContext columnContext,
                                          @Nullable ResourceLocation featureId) {
        Map<PlacedFeature, PlacedFeatureNode> nodes = placedFeatureNodes.computeIfAbsent(columnContext, (c) -> new IdentityHashMap<>());
        PlacedFeatureNode cached = nodes.get(placedFeature);

        if (cached != null) {
            placedFeatureHits++;
            return cached;
        }

        PlacedFeatureNode node = new PlacedFeatureNode(utils, placedFeature, columnContext, featureId);

        placedFeatureMisses++;
        nodes.put(placedFeature, node);
        return node;
    }

    public int getPlacedFeatureHits() {
        return placedFeatureHits;
    }

    public int getPlacedFeatureMisses() {
        return placedFeatureMisses;
    }

    public int getGenerationStepHits() {
        return generationStepHits;
    }

    public int getGenerationStepMisses() {
        return generationStepMisses;
    }

    private record StepKey(GenerationStep.Decoration step, List<PlacedFeatureNode> children) {}
}
