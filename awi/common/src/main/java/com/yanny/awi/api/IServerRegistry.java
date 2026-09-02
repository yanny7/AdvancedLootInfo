package com.yanny.awi.api;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.ICoreServerRegistry;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.awi.plugin.server.summary.CountSpan;
import com.yanny.awi.plugin.server.summary.HeightSpanPropagator;
import com.yanny.awi.plugin.server.summary.PlacementPropagator;
import net.minecraft.tags.TagKey;
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

import java.util.List;
import java.util.function.BiFunction;

public interface IServerRegistry extends ICoreServerRegistry<IServerUtils> {
    <T extends FeatureConfiguration> void registerFeatureBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Either<Block, TagKey<Block>>>> getter);

    <T extends BlockStateProvider> void registerStateProviderBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter);

    <T extends RootPlacer> void registerRootPlacerBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter);

    <T extends TreeDecorator> void registerTreeDecoratorBlockCollector(Class<T> type, BiFunction<IServerUtils, T, List<Block>> getter);

    <T extends FeatureConfiguration> void registerFeatureTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends PlacementModifier> void registerPlacementModifierTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends IntProvider> void registerIntProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends RuleTest> void registerRuleTestTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends HeightProvider> void registerHeightProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends BlockPredicate> void registerBlockPredicateTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends BlockStateProvider> void registerBlockStateProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends TreeDecorator> void registerTreeDecoratorTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends FeatureSize> void registerFeatureSizeTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends RootPlacer> void registerRootPlacerTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends FoliagePlacer> void registerFoliagePlacerTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends TrunkPlacer> void registerTrunkPlacerTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends FloatProvider> void registerFloatProviderTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends StructureProcessor> void registerStructureProcessorTooltip(Class<T> type, BiFunction<IServerUtils, T, TooltipBuilder> getter);

    <T extends IntProvider> void registerIntSpanPropagator(Class<T> type, BiFunction<IServerUtils, T, CountSpan> getter);

    <T extends HeightProvider> void registerHeightSpanPropagator(Class<T> type, HeightSpanPropagator<T> getter);

    <T extends PlacementModifier> void registerPlacementPropagator(Class<T> type, PlacementPropagator<T> getter);

    /**
     * @deprecated use {@link #registerEnumTranslation(Class, String, String)}
     */
    @Deprecated(forRemoval = true, since = "1.1.1")
    void registerEnumTranslation(Class<? extends Enum<?>> type, String owner);

    void registerEnumTranslation(Class<? extends Enum<?>> type, String modId, String owner);
}
