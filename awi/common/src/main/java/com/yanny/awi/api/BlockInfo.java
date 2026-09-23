package com.yanny.awi.api;

import com.yanny.aci.api.RangeValue;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Structured result for a single surface block: the block itself, how its positions are stored
 * ({@link StorageType}), the value {@link RangeValue}s in that storage's units, how far layers shift with X/Z
 * ({@code layerShift}, 0 when they do not), its {@link WaterConstraint}, its {@link Placement} (floor vs. ceiling/overhang),
 * and for a depth-reported block the absolute Y it is confined to ({@code heights}, empty when it occurs at any height).
 */
public record BlockInfo(Block block, StorageType storageType, List<RangeValue> ranges, int layerShift, WaterConstraint water,
                        Placement placement, List<RangeValue> heights) {
    /**
     * How a block's vertical positions are stored/reported.
     * <ul>
     *     <li>{@link #RELATIVE} — depth below the surface (grass, dirt, sand); {@link BlockInfo#ranges} are depths.</li>
     *     <li>{@link #ABSOLUTE} — a stable absolute Y band (deepslate, bedrock); {@link BlockInfo#ranges} are absolute Ys.</li>
     *     <li>{@link #LAYERED} — recurring absolute-Y strata (badlands bands); {@link BlockInfo#ranges} are absolute Ys.</li>
     * </ul>
     */
    public enum StorageType { RELATIVE, ABSOLUTE, LAYERED }

    /** Whether a placed block needs water above it, dry land above it, or is indifferent to the water level. */
    public enum WaterConstraint { UNDERWATER, DRY, ANY }

    /** Whether a block is a normal below-surface placement, an overhang/ceiling-only placement, or occurs both ways. */
    public enum Placement { FLOOR, CEILING, ANY }
}
