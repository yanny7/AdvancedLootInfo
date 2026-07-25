package com.yanny.awi.api;

import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
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

    @NotNull
    <T extends HeightProvider> TooltipBuilder getHeightProviderTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends BlockPredicate> TooltipBuilder getBlockPredicateTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends BlockStateProvider> TooltipBuilder getBlockStateProviderTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends TreeDecorator> TooltipBuilder getTreeDecoratorTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends FeatureSize> TooltipBuilder getFeatureSizeTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends RootPlacer> TooltipBuilder getRootPlacerTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends FoliagePlacer> TooltipBuilder getFoliagePlacerTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends TrunkPlacer> TooltipBuilder getTrunkPlacerTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends FloatProvider> TooltipBuilder getFloatProviderTooltip(IServerUtils utils, T entry);

    @NotNull
    <T extends StructureProcessor> TooltipBuilder getStructureProcessorTooltip(IServerUtils utils, T entry);
}
