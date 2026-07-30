package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.feature.treedecorators.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class TreeDecoratorTooltipUtils {
    @NotNull
    public static TooltipBuilder getTrunkVineDecoratorTooltip(IServerUtils ignoredUtils, TrunkVineDecorator ignoredDecorator) {
        return array(TooltipBuilder::showEmpty, Lang.TreeDecorator.TRUNK_VINE);
    }

    @NotNull
    public static TooltipBuilder getLeaveVineDecoratorTooltip(IServerUtils utils, LeaveVineDecorator decorator) {
        return array((b) -> b.add(utils.getValueTooltip(utils, decorator.probability).build(Lang.Value.PROBABILITY)), Lang.TreeDecorator.LEAVE_VINE);
    }

    @NotNull
    public static TooltipBuilder getPaleMossDecoratorTooltip(IServerUtils utils, PaleMossDecorator decorator) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, decorator.leavesProbability).build(Lang.Value.LEAVES_PROBABILITY));
            b.add(utils.getValueTooltip(utils, decorator.trunkProbability).build(Lang.Value.TRUNK_PROBABILITY));
            b.add(utils.getValueTooltip(utils, decorator.groundProbability).build(Lang.Value.GROUND_PROBABILITY));
        }, Lang.TreeDecorator.PALE_MOSS);
    }

    @NotNull
    public static TooltipBuilder getCreakingHeartDecoratorTooltip(IServerUtils utils, CreakingHeartDecorator decorator) {
        return array((b) -> b.add(utils.getValueTooltip(utils, decorator.probability).build(Lang.Value.PROBABILITY)), Lang.TreeDecorator.CREAKING_HEART);
    }

    @NotNull
    public static TooltipBuilder getCocoaDecoratorTooltip(IServerUtils utils, CocoaDecorator decorator) {
        return array((b) -> b.add(utils.getValueTooltip(utils, decorator.probability).build(Lang.Value.PROBABILITY)), Lang.TreeDecorator.COCOA);
    }

    @NotNull
    public static TooltipBuilder getBeehiveDecoratorTooltip(IServerUtils utils, BeehiveDecorator decorator) {
        return array((b) -> b.add(utils.getValueTooltip(utils, decorator.probability).build(Lang.Value.PROBABILITY)), Lang.TreeDecorator.BEEHIVE);
    }

    @NotNull
    public static TooltipBuilder getAlterGroundDecoratorTooltip(IServerUtils utils, AlterGroundDecorator decorator) {
        return array((b) -> b.add(utils.getValueTooltip(utils, decorator.provider).build(Lang.Branch.PROVIDER)), Lang.TreeDecorator.ALTER_GROUND);
    }

    @NotNull
    public static TooltipBuilder getAttachedToLeavesDecoratorTooltip(IServerUtils utils, AttachedToLeavesDecorator decorator) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, decorator.probability).build(Lang.Value.PROBABILITY));
            b.add(utils.getValueTooltip(utils, decorator.exclusionRadiusXZ).build(Lang.Value.EXCLUSION_RADIUS_XZ));
            b.add(utils.getValueTooltip(utils, decorator.exclusionRadiusY).build(Lang.Value.EXCLUSION_RADIUS_Y));
            b.add(utils.getValueTooltip(utils, decorator.blockProvider).build(Lang.Branch.BLOCK_PROVIDER));
            b.add(utils.getValueTooltip(utils, decorator.requiredEmptyBlocks).build(Lang.Value.REQUIRED_EMPTY_BLOCKS));
            b.add(utils.getValueTooltip(utils, decorator.directions).build(Lang.Branch.DIRECTIONS));
        }, Lang.TreeDecorator.ATTACHED_TO_LEAVES);
    }

    @NotNull
    public static TooltipBuilder getPlaceOnGroundDecoratorTooltip(IServerUtils utils, PlaceOnGroundDecorator decorator) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, decorator.tries).build(Lang.Value.TRIES));
            b.add(utils.getValueTooltip(utils, decorator.radius).build(Lang.Value.RADIUS));
            b.add(utils.getValueTooltip(utils, decorator.height).build(Lang.Value.HEIGHT));
            b.add(utils.getValueTooltip(utils, decorator.blockStateProvider).build(Lang.Branch.BLOCK_PROVIDER));
        }, Lang.TreeDecorator.PLACE_ON_GROUND);
    }

    @NotNull
    public static TooltipBuilder getAttachedToLogsDecoratorTooltip(IServerUtils utils, AttachedToLogsDecorator decorator) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, decorator.probability).build(Lang.Value.PROBABILITY));
            b.add(utils.getValueTooltip(utils, decorator.blockProvider).build(Lang.Branch.BLOCK_PROVIDER));
            b.add(utils.getValueTooltip(utils, decorator.directions).build(Lang.Branch.DIRECTIONS));
        }, Lang.TreeDecorator.ATTACHED_TO_LOGS);
    }
}
