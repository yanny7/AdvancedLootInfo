package com.yanny.awi.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public interface IServerRegistry {

    <T extends PlacedFeature> FeatureHolder registerFeatureCollector(IServerUtils utils, T placedFeature);

    <T extends FeatureConfiguration> List<Item> registerItemCollector(IServerUtils utils, T configuredFeature);

    <T extends FeatureConfiguration> ITooltipNode registerFeatureTooltip(IServerUtils utils, T configuredFeature);

}
