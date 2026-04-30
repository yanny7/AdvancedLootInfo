package com.yanny.awi.api;

import com.yanny.aci.api.ICoreServerRegistry;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;
import java.util.function.BiFunction;

public interface IServerRegistry extends ICoreServerRegistry<IServerUtils, IKeyTooltipNode> {
    <T extends FeatureConfiguration> void registerItemCollector(Class<T> type, BiFunction<IServerUtils, T, List<Item>> getter);

    <T extends FeatureConfiguration> void registerFeatureTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends PlacementModifier> void registerPlacementModifierTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);

    <T extends IntProvider> void registerIntProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, ITooltipNode> getter);
}
