package com.yanny.awi.api;

import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;
import java.util.function.BiFunction;

public interface IServerRegistry extends ICoreServerRegistry<IServerUtils> {
    <T extends FeatureConfiguration> void registerItemCollector(Class<T> type, BiFunction<IServerUtils, T, List<Item>> getter);

    <T extends FeatureConfiguration> void registerFeatureTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipNode> getter);

    <T extends PlacementModifier> void registerPlacementModifierTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipNode> getter);

    <T extends IntProvider> void registerIntProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends RuleTest> void registerRuleTestTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);
}
