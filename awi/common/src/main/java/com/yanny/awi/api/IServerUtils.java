package com.yanny.awi.api;

import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IServerUtils extends ICoreServerUtils<IServerUtils> {
    @NotNull
    <T extends FeatureConfiguration> List<Block> collectBlocks(IServerUtils utils, T entry);

    @NotNull
    <T extends BlockStateProvider> List<Block> collectBlocks(IServerUtils utils, T entry);

    @NotNull
    <T extends FeatureConfiguration> TooltipBuilder getFeatureTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends PlacementModifier> TooltipBuilder getPlacementModifierTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends IntProvider> TooltipBuilder getIntProviderTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends RuleTest> TooltipBuilder getRuleTestTooltip(IServerUtils utils, T entry);
}
