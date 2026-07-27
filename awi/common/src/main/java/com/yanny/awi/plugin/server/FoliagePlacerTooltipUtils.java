package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.foliageplacers.*;
import org.jetbrains.annotations.NotNull;

public class FoliagePlacerTooltipUtils {
    @NotNull
    public static TooltipBuilder getBlobFoliagePlacerTooltip(IServerUtils utils, BlobFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> addBlobFoliagePlacerTooltip(utils, b, placer), Lang.FoliagePlacer.BLOB);
    }

    @NotNull
    public static TooltipBuilder getSpruceFoliagePlacerTooltip(IServerUtils utils, SpruceFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> {
            addBaseFoliagePlacerTooltip(utils, b, placer);
            b.add(utils.getValueTooltip(utils, placer.trunkHeight).build(Lang.Branch.TRUNK_HEIGHT));
        }, Lang.FoliagePlacer.SPRUCE);
    }

    @NotNull
    public static TooltipBuilder getPineFoliagePlacerTooltip(IServerUtils utils, PineFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> {
            addBaseFoliagePlacerTooltip(utils, b, placer);
            b.add(utils.getValueTooltip(utils, placer.height).build(Lang.Branch.HEIGHT));
        }, Lang.FoliagePlacer.PINE);
    }

    @NotNull
    public static TooltipBuilder getAcaciaFoliagePlacerTooltip(IServerUtils utils, AcaciaFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> addBaseFoliagePlacerTooltip(utils, b, placer), Lang.FoliagePlacer.ACACIA);
    }

    @NotNull
    public static TooltipBuilder getBushFoliagePlacerTooltip(IServerUtils utils, BushFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> addBlobFoliagePlacerTooltip(utils, b, placer), Lang.FoliagePlacer.BUSH);
    }

    @NotNull
    public static TooltipBuilder getFancyFoliagePlacerTooltip(IServerUtils utils, FancyFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> addBlobFoliagePlacerTooltip(utils, b, placer), Lang.FoliagePlacer.FANCY);
    }

    @NotNull
    public static TooltipBuilder getMegaJungleFoliagePlacerTooltip(IServerUtils utils, MegaJungleFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> {
            addBaseFoliagePlacerTooltip(utils, b, placer);
            b.add(utils.getValueTooltip(utils, placer.height).build(Lang.Branch.HEIGHT));
        }, Lang.FoliagePlacer.MEGA_JUNGLE);
    }

    @NotNull
    public static TooltipBuilder getMegaPineFoliagePlacerTooltip(IServerUtils utils, MegaPineFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> {
            addBaseFoliagePlacerTooltip(utils, b, placer);
            b.add(utils.getValueTooltip(utils, placer.crownHeight).build(Lang.Branch.CROWN_HEIGHT));
        }, Lang.FoliagePlacer.MEGA_PINE);
    }

    @NotNull
    public static TooltipBuilder getDarkOakFoliagePlacerTooltip(IServerUtils utils, DarkOakFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> addBaseFoliagePlacerTooltip(utils, b, placer), Lang.FoliagePlacer.DARK_OAK);
    }

    @NotNull
    public static TooltipBuilder getRandomSpreadFoliagePlacerTooltip(IServerUtils utils, RandomSpreadFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> {
            addBaseFoliagePlacerTooltip(utils, b, placer);
            b.add(utils.getValueTooltip(utils, placer.foliageHeight).build(Lang.Branch.FOLIAGE_HEIGHT));
            b.add(utils.getValueTooltip(utils, placer.leafPlacementAttempts).build(Lang.Value.LEAF_PLACEMENT_ATTEMPTS));
        }, Lang.FoliagePlacer.RANDOM_SPREAD);
    }

    @NotNull
    public static TooltipBuilder getCherryFoliagePlacerTooltip(IServerUtils utils, CherryFoliagePlacer placer) {
        return TooltipBuilder.array((b) -> {
            addBaseFoliagePlacerTooltip(utils, b, placer);
            b.add(utils.getValueTooltip(utils, placer.height).build(Lang.Branch.HEIGHT));
            b.add(utils.getValueTooltip(utils, placer.wideBottomLayerHoleChance).build(Lang.Value.WIDE_BOTTOM_LAYER_HOLE_CHANCE));
            b.add(utils.getValueTooltip(utils, placer.cornerHoleChance).build(Lang.Value.CORNER_HOLE_CHANCE));
            b.add(utils.getValueTooltip(utils, placer.hangingLeavesChance).build(Lang.Value.HANGING_LEAVES_CHANCE));
            b.add(utils.getValueTooltip(utils, placer.hangingLeavesExtensionChance).build(Lang.Value.HANGING_LEAVES_EXTENSION_CHANCE));
        }, Lang.FoliagePlacer.CHERRY);
    }

    private static void addBlobFoliagePlacerTooltip(IServerUtils utils, TooltipBuilder builder, BlobFoliagePlacer placer) {
        addBaseFoliagePlacerTooltip(utils, builder, placer);
        builder.add(utils.getValueTooltip(utils, placer.height).build(Lang.Value.HEIGHT));
    }

    private static void addBaseFoliagePlacerTooltip(IServerUtils utils, TooltipBuilder builder, FoliagePlacer placer) {
        builder.add(utils.getValueTooltip(utils, placer.radius).build(Lang.Branch.RADIUS));
        builder.add(utils.getValueTooltip(utils, placer.offset).build(Lang.Branch.OFFSET));
    }
}
