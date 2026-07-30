package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.jetbrains.annotations.NotNull;

import static com.yanny.aci.tooltip.TooltipBuilder.array;

public class RuleTestTooltipUtils {
    @NotNull
    public static TooltipBuilder getAlwaysTrueTestTooltip(IServerUtils ignoredUtils, AlwaysTrueTest ignoredTest) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, Lang.RuleTest.ALWAYS_TRUE);
    }

    @NotNull
    public static TooltipBuilder getBlockMatchTestTooltip(IServerUtils utils, BlockMatchTest test) {
        return array((b) -> b.add(utils.getValueTooltip(utils, test.block).build(Lang.Value.BLOCK)), Lang.RuleTest.BLOCK_MATCH);
    }

    @NotNull
    public static TooltipBuilder getBlockStateMatchTestTooltip(IServerUtils utils, BlockStateMatchTest test) {
        return array((b) -> b.add(utils.getValueTooltip(utils, test.blockState).build(Lang.Branch.STATE)), Lang.RuleTest.BLOCK_STATE_MATCH);
    }

    @NotNull
    public static TooltipBuilder getTagMatchTestTooltip(IServerUtils utils, TagMatchTest test) {
        return array((b) -> b.add(utils.getValueTooltip(utils, test.tag).build(Lang.Value.TAG)), Lang.RuleTest.TAG_MATCH);
    }

    @NotNull
    public static TooltipBuilder getRandomBlockMatchTestTooltip(IServerUtils utils, RandomBlockMatchTest value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.block).build(Lang.Value.BLOCK));
            b.add(utils.getValueTooltip(utils, value.probability).build(Lang.Value.PROBABILITY));
        }, Lang.RuleTest.RANDOM_BLOCK_MATCH);
    }

    @NotNull
    public static TooltipBuilder getRandomBlockStateMatchTestTooltip(IServerUtils utils, RandomBlockStateMatchTest value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.blockState).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, value.probability).build(Lang.Value.PROBABILITY));
        }, Lang.RuleTest.RANDOM_BLOCK_STATE_MATCH);
    }
}
