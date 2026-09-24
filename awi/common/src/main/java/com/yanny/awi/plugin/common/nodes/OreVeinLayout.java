package com.yanny.awi.plugin.common.nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.api.ISurfaceRuleHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OreVeinLayout implements ISurfaceRuleHandler {
    public static final Identifier TYPE = Identifier.withDefaultNamespace("ore_vein");

    private static final int SAMPLE_RADIUS = 2048;
    private static final int SAMPLE_STEP = 64;

    private final RandomState randomState;
    private final DynamicOps<JsonElement> ops;
    private final Map<List<Object>, List<BlockInfo>> expanded = new HashMap<>();

    private OreVeinLayout(RandomState randomState, DynamicOps<JsonElement> ops) {
        this.randomState = randomState;
        this.ops = ops;
    }

    @NotNull
    public static OreVeinLayout create(Context context) {
        return new OreVeinLayout(context.randomState(), context.lookup().createSerializationContext(JsonOps.INSTANCE));
    }

    @Override
    public boolean alwaysPlaces() {
        return false;
    }

    @NotNull
    @Override
    public List<BlockInfo> expand(JsonObject definition, GhostObservation ghost) {
        return expanded.computeIfAbsent(List.of(definition, ghost), (k) -> compute(definition, ghost));
    }

    @NotNull
    private List<BlockInfo> compute(JsonObject definition, GhostObservation ghost) {
        OreVeinRule rule = (OreVeinRule) MaterialRule.CODEC.parse(ops, definition).getOrThrow();
        DensitySampler.Bound density = randomState.samplersWithContext(SamplerContext.EMPTY_UNCACHED).get(rule.density());
        OreVeinRule.VeinType veinType = findVeinType(rule);
        NodeUtils.RangeHolder heights = new NodeUtils.RangeHolder();

        if (veinType != null && hasBounds(density, veinType)) {
            ghost.absoluteY().subSet(veinType.minY, true, veinType.maxY, false).forEach(heights::add);
        } else {
            for (int y = ghost.absoluteY().first(); y <= ghost.absoluteY().last(); y++) {
                if (isVeinAt(density, y)) {
                    heights.add(y);
                }
            }
        }

        if (heights.size() == 0) {
            return List.of();
        }

        List<RangeValue> ranges = heights.buildRanges();
        Set<Block> blocks = new LinkedHashSet<>();

        blocks.add(rule.oreBlock().getBlock());

        if (rule.rawOreChance() > 0) {
            blocks.add(rule.rawOreBlock().getBlock());
        }

        blocks.add(rule.fillerBlock().getBlock());

        return blocks.stream().map((block) -> new BlockInfo(block, BlockInfo.StorageType.ABSOLUTE, ranges, 0, ghost.water(),
                ghost.placement(), List.of())).toList();
    }

    @Nullable
    private static OreVeinRule.VeinType findVeinType(OreVeinRule rule) {
        for (OreVeinRule.VeinType type : OreVeinRule.VeinType.values()) {
            OreVeinRule vanilla = type.create(rule.density(), rule.richness(), rule.fillerGap());

            if (vanilla.oreBlock() == rule.oreBlock() && vanilla.rawOreBlock() == rule.rawOreBlock() && vanilla.fillerBlock() == rule.fillerBlock()) {
                return type;
            }
        }

        return null;
    }

    private static boolean hasBounds(DensitySampler.Bound density, OreVeinRule.VeinType type) {
        // VeinType.maxY is exclusive: vanilla's density data uses it as the range_choice max_exclusive.
        return isVeinAt(density, type.minY) && isVeinAt(density, type.maxY - 1)
                && !isVeinAt(density, type.minY - 1) && !isVeinAt(density, type.maxY);
    }

    private static boolean isVeinAt(DensitySampler.Bound density, int y) {
        for (int x = -SAMPLE_RADIUS; x <= SAMPLE_RADIUS; x += SAMPLE_STEP) {
            for (int z = -SAMPLE_RADIUS; z <= SAMPLE_RADIUS; z += SAMPLE_STEP) {
                if (density.sampleValue(x, y, z) > 0) {
                    return true;
                }
            }
        }

        return false;
    }
}
