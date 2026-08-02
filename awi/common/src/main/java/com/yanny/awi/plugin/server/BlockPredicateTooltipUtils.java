package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.blockpredicates.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class BlockPredicateTooltipUtils {
    @NotNull
    public static TooltipBuilder getMatchingBlocksPredicateTooltip(IServerUtils utils, MatchingBlocksPredicate predicate) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.blocks).build(Lang.Branch.BLOCKS));
            b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET));
        }, Lang.BlockPredicate.MATCHING_BLOCKS);
    }

    @NotNull
    public static TooltipBuilder getMatchingBlockTagPredicateTooltip(IServerUtils utils, MatchingBlockTagPredicate predicate) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.tag).build(Lang.Value.TAG));
            b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET));
        }, Lang.BlockPredicate.MATCHING_BLOCK_TAG);
    }

    @NotNull
    public static TooltipBuilder getMatchingFluidsPredicateTooltip(IServerUtils utils, MatchingFluidsPredicate predicate) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.fluids).build(Lang.Branch.FLUIDS));
            b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET));
        }, Lang.BlockPredicate.MATCHING_FLUIDS);
    }

    @NotNull
    public static TooltipBuilder getHasSturdyFacePredicateTooltip(IServerUtils utils, HasSturdyFacePredicate predicate) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.direction).build(Lang.Value.DIRECTION));
            b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET));
        }, Lang.BlockPredicate.HAS_STURDY_FACE);
    }

    @NotNull
    public static TooltipBuilder getSolidPredicateTooltip(IServerUtils utils, SolidPredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET)).showEmpty(), Lang.BlockPredicate.SOLID);
    }

    @NotNull
    public static TooltipBuilder getReplaceablePredicateTooltip(IServerUtils utils, ReplaceablePredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET)).showEmpty(), Lang.BlockPredicate.REPLACEABLE);
    }

    @NotNull
    public static TooltipBuilder getWouldSurvivePredicateTooltip(IServerUtils utils, WouldSurvivePredicate predicate) {
        return array((b) -> {
            b.add(utils.getValueTooltip(utils, predicate.state).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET));
        }, Lang.BlockPredicate.WOULD_SURVIVE);
    }

    @NotNull
    public static TooltipBuilder getInsideWorldBoundsPredicateTooltip(IServerUtils utils, InsideWorldBoundsPredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.offset).build(Lang.Value.OFFSET)).showEmpty(), Lang.BlockPredicate.INSIDE_WORLD_BOUNDS);
    }

    @NotNull
    public static TooltipBuilder getAnyOfPredicateTooltip(IServerUtils utils, AnyOfPredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.predicates).build(Lang.Branch.PREDICATES)), Lang.BlockPredicate.ANY_OF);
    }

    @NotNull
    public static TooltipBuilder getAllOfPredicateTooltip(IServerUtils utils, AllOfPredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.predicates).build(Lang.Branch.PREDICATES)), Lang.BlockPredicate.ALL_OF);
    }

    @NotNull
    public static TooltipBuilder getNotPredicateTooltip(IServerUtils utils, NotPredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.predicate).build(Lang.Branch.PREDICATE)), Lang.BlockPredicate.NOT);
    }

    @NotNull
    public static TooltipBuilder getTrueBlockPredicateTooltip(IServerUtils ignoredUtils, TrueBlockPredicate ignoredPredicate) {
        return array(TooltipBuilder::showEmpty, Lang.BlockPredicate.TRUE_BLOCK);
    }

    @NotNull
    public static TooltipBuilder getUnobstructedPredicateTooltip(IServerUtils utils, UnobstructedPredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.offset()).build(Lang.Value.OFFSET)), Lang.BlockPredicate.UNOBSTRUCTED);
    }

    @NotNull
    public static TooltipBuilder getMatchingBiomesPredicateTooltip(IServerUtils utils, MatchingBiomesPredicate predicate) {
        return array((b) -> b.add(utils.getValueTooltip(utils, predicate.biomes).build(Lang.Branch.BIOMES)), Lang.BlockPredicate.MATCHING_BIOMES);
    }
}
