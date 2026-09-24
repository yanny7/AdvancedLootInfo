package com.yanny.awi.plugin.common.nodes;

import com.google.gson.JsonObject;
import com.yanny.aci.api.RangeValue;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.api.ISurfaceRuleHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BandlandsLayout implements ISurfaceRuleHandler {
    public static final ResourceLocation TYPE = ResourceLocation.withDefaultNamespace("bandlands");

    private static final double OFFSET_SCALE = 4.0;
    private static final int OFFSET_SAMPLE_RADIUS = 4096;
    private static final int OFFSET_SAMPLE_STEP = 32;

    private final BlockState[] palette;
    private final NormalNoise offsetNoise;
    private int shift = -1;

    private BandlandsLayout(BlockState[] palette, NormalNoise offsetNoise) {
        this.palette = palette;
        this.offsetNoise = offsetNoise;
    }

    @NotNull
    public static BandlandsLayout create(RandomState randomState) {
        return new BandlandsLayout(randomState.surfaceSystem().clayBands, randomState.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET));
    }

    @NotNull
    @Override
    public List<BlockInfo> expand(JsonObject definition, GhostObservation ghost) {
        int bottom = ghost.absoluteY().first();
        int top = ghost.absoluteY().last();
        Block filler = filler();
        Map<Block, NodeUtils.RangeHolder> levels = new LinkedHashMap<>();

        for (int y = bottom; y <= top; y++) {
            Block block = palette[Math.floorMod(y, palette.length)].getBlock();

            if (block != filler) {
                levels.computeIfAbsent(block, (k) -> new NodeUtils.RangeHolder()).add(y);
            }
        }

        List<BlockInfo> infos = new ArrayList<>();
        int layerShift = shift();

        infos.add(new BlockInfo(filler, BlockInfo.StorageType.ABSOLUTE, List.of(new RangeValue(bottom, top)), 0, ghost.water(),
                ghost.placement(), List.of()));
        levels.forEach((block, holder) -> infos.add(new BlockInfo(block, BlockInfo.StorageType.LAYERED, holder.buildRanges(),
                layerShift, ghost.water(), ghost.placement(), List.of())));

        return infos;
    }

    private Block filler() {
        Map<Block, Integer> counts = new LinkedHashMap<>();

        for (BlockState state : palette) {
            counts.merge(state.getBlock(), 1, Integer::sum);
        }

        return Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private int shift() {
        if (shift < 0) {
            int max = 0;

            for (int x = -OFFSET_SAMPLE_RADIUS; x <= OFFSET_SAMPLE_RADIUS; x += OFFSET_SAMPLE_STEP) {
                for (int z = -OFFSET_SAMPLE_RADIUS; z <= OFFSET_SAMPLE_RADIUS; z += OFFSET_SAMPLE_STEP) {
                    max = Math.max(max, Math.abs((int) Math.round(offsetNoise.getValue(x, 0.0, z) * OFFSET_SCALE)));
                }
            }

            shift = max;
        }

        return shift;
    }
}
