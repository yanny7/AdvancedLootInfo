package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.trunkplacers.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class TrunkPlacerTooltipUtils {
    @NotNull
    public static TooltipBuilder getStraightTrunkPlacerTooltip(IServerUtils utils, StraightTrunkPlacer placer) {
        return array((b) -> addBaseTrunkPlacerTooltip(utils, b, placer), Lang.TrunkPlacer.STRAIGHT_TRUNK);
    }

    @NotNull
    public static TooltipBuilder getForkingTrunkPlacerTooltip(IServerUtils utils, ForkingTrunkPlacer placer) {
        return array((b) -> addBaseTrunkPlacerTooltip(utils, b, placer), Lang.TrunkPlacer.FORKING_TRUNK);
    }

    @NotNull
    public static TooltipBuilder getGiantTrunkPlacerTooltip(IServerUtils utils, GiantTrunkPlacer placer) {
        return array((b) -> addBaseTrunkPlacerTooltip(utils, b, placer), Lang.TrunkPlacer.GIANT_TRUNK);
    }

    @NotNull
    public static TooltipBuilder getMegaJungleTrunkPlacerTooltip(IServerUtils utils, GiantTrunkPlacer placer) {
        return array((b) -> addBaseTrunkPlacerTooltip(utils, b, placer), Lang.TrunkPlacer.MEGA_JUNGLE);
    }

    @NotNull
    public static TooltipBuilder getDarkOakTrunkPlacerTooltip(IServerUtils utils, DarkOakTrunkPlacer placer) {
        return array((b) -> addBaseTrunkPlacerTooltip(utils, b, placer), Lang.TrunkPlacer.DARK_OAK);
    }

    @NotNull
    public static TooltipBuilder getFancyTrunkPlacerTooltip(IServerUtils utils, FancyTrunkPlacer placer) {
        return array((b) -> addBaseTrunkPlacerTooltip(utils, b, placer), Lang.TrunkPlacer.FANCY_TRUNK);
    }

    @NotNull
    public static TooltipBuilder getBendingTrunkPlacerTooltip(IServerUtils utils, BendingTrunkPlacer value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.minHeightForLeaves).build(Lang.Value.MIN_HEIGHT_FOR_LEAVES));
            b.add(utils.getValueTooltip(utils, value.bendLength).build(Lang.Branch.BAND_LENGTH));
        }, Lang.TrunkPlacer.BENDING_TRUNK);
    }

    @NotNull
    public static TooltipBuilder getUpwardBranchingTrunkPlacerTooltip(IServerUtils utils, UpwardsBranchingTrunkPlacer value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.extraBranchSteps).build(Lang.Branch.EXTRA_BRANCH_STEPS));
            b.add(utils.getValueTooltip(utils, value.placeBranchPerLogProbability).build(Lang.Value.BRANCH_PER_LOG_CHANCE));
            b.add(utils.getValueTooltip(utils, value.extraBranchLength).build(Lang.Branch.EXTRA_BRANCH_LENGTH));
            b.add(utils.getValueTooltip(utils, value.canGrowThrough).build(Lang.Branch.CAN_GROW_THROUGH));
        }, Lang.TrunkPlacer.UPWARD_BRANCHING_TRUNK);
    }

    @NotNull
    public static TooltipBuilder getCherryTrunkPlacerTooltip(IServerUtils utils, CherryTrunkPlacer value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.branchCount).build(Lang.Branch.BRANCH_COUNT));
            b.add(utils.getValueTooltip(utils, value.branchHorizontalLength).build(Lang.Branch.BRANCH_HORIZONTAL_LENGTH));
            b.add(utils.getValueTooltip(utils, value.branchStartOffsetFromTop).build(Lang.Branch.BRANCH_START_OFFSET_FROM_TOP));
            b.add(utils.getValueTooltip(utils, value.secondBranchStartOffsetFromTop).build(Lang.Branch.SECOND_BRANCH_START_OFFSET_FROM_TOP));
            b.add(utils.getValueTooltip(utils, value.branchEndOffsetFromTop).build(Lang.Branch.BRANCH_END_OFFSET_FROM_TOP));
        }, Lang.TrunkPlacer.CHERRY);
    }

    private static void addBaseTrunkPlacerTooltip(IServerUtils utils, TooltipBuilder builder, TrunkPlacer placer) {
        builder.add(utils.getValueTooltip(utils, placer.baseHeight).build(Lang.Value.BASE_HEIGHT));
        builder.add(utils.getValueTooltip(utils, placer.heightRandA).build(Lang.Value.HEIGHT_RAND_A));
        builder.add(utils.getValueTooltip(utils, placer.heightRandB).build(Lang.Value.HEIGHT_RAND_B));
    }
}
