package com.yanny.awi.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.function.BiFunction;

public interface IServerRegistry {
    <T extends PlacedFeature> void registerFeatureCollector(IServerUtils utils, BiFunction<IServerUtils, T, FeatureHolder> getter);

    <T extends FeatureConfiguration> void registerItemCollector(IServerUtils utils, BiFunction<IServerUtils, T, List<Item>> getter);

    <T extends FeatureConfiguration> void registerFeatureTooltip(IServerUtils utils, BiFunction<IServerUtils, T, ITooltipNode> getter);
}
