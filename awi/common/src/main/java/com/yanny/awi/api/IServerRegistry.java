package com.yanny.awi.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IServerRegistry {

    @NotNull
    <T extends PlacedFeature> FeatureHolder registerFeatureCollector(IServerUtils utils, T placedFeature);

    @NotNull
    <T extends FeatureConfiguration> List<Item> registerItemCollector(IServerUtils utils, T configuredFeature);

    @NotNull
    <T extends FeatureConfiguration> ITooltipNode registerFeatureTooltip(IServerUtils utils, T configuredFeature);

}
