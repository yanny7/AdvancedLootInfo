package com.yanny.awi.plugin.server;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.language.Lang;
import net.minecraft.core.Vec3i;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public class ValueTooltipUtils {
    @NotNull
    public static TooltipBuilder getIntProviderTooltip(IServerUtils utils, IntProvider value) {
        return utils.getIntProviderTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getRuleTestTooltip(IServerUtils utils, RuleTest value) {
        return utils.getRuleTestTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getHeightProviderTooltip(IServerUtils utils, HeightProvider value) {
        return utils.getHeightProviderTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, BlockPredicate value) {
        return utils.getBlockPredicateTooltip(utils, value);
    }

    @NotNull
    public static TooltipBuilder getTargetBlockStateTooltip(IServerUtils utils, OreConfiguration.TargetBlockState value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.state).build(Lang.Branch.STATE));
            b.add(utils.getValueTooltip(utils, value.target).build(Lang.Branch.TARGET));
        });
    }

    @NotNull
    public static TooltipBuilder getBlockStateTooltip(IServerUtils utils, BlockState value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getBlock()).build(Lang.Value.BLOCK));

            TooltipBuilder array = TooltipBuilder.array((c) -> {
                value.getValues().forEach((p, v) -> c.add(TooltipBuilder.keyValue(p.getName(), v.toString())));
            });

            b.add(array.build(Lang.Branch.PROPERTIES));
        });
    }

    @NotNull
    public static TooltipBuilder getVec3iTooltip(IServerUtils utils, Vec3i value) {
        return utils.getValueTooltip(utils, "[" + value.getX() + "," + value.getY() + "," + value.getZ() + "]");
    }

    @NotNull
    public static TooltipBuilder getWeightedEntryWrapperTooltip(IServerUtils utils, WeightedEntry.Wrapper<?> value) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, value.getWeight()).build(Lang.Value.WEIGHT));
            b.add(utils.getValueTooltip(utils, value.getData()).build(Lang.Branch.DATA));
        }).key(Lang.Branch.ENTRY);
    }

    @NotNull
    public static TooltipBuilder getWeightTooltip(IServerUtils utils, Weight value) {
        return utils.getValueTooltip(utils, value.asInt());
    }
}
