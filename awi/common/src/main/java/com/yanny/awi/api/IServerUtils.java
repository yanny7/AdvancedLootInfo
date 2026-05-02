package com.yanny.awi.api;

import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IServerUtils extends ICoreServerUtils<IKeyTooltipNode, ITooltipNode, IServerUtils> {
    @NotNull
    <T extends FeatureConfiguration> List<Item> getItemCollector(IServerUtils utils, T entry);

    @NotNull
    <T extends FeatureConfiguration> ITooltipNode getFeatureTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends PlacementModifier> ITooltipNode getPlacementModifierTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends IntProvider> IKeyTooltipNode getIntProviderTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends RuleTest> IKeyTooltipNode getRuleTestTooltip(IServerUtils utils, T entry);
}
