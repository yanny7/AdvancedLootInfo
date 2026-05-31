package com.yanny.awi.api;

import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;
import java.util.function.BiFunction;

public interface IServerRegistry extends ICoreServerRegistry<IServerUtils> {
    <T extends FeatureConfiguration> void registerFeatureBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter);

    <T extends BlockStateProvider> void registerStateProviderBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter);

    <T extends FeatureConfiguration> void registerFeatureTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends PlacementModifier> void registerPlacementModifierTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends IntProvider> void registerIntProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends RuleTest> void registerRuleTestTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);
}
