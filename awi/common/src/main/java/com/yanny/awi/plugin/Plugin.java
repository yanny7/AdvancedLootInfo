package com.yanny.awi.plugin;

import com.yanny.aci.tooltip.CommonValueTooltip;
import com.yanny.awi.api.*;
import com.yanny.awi.datagen.LanguageHolder;
import com.yanny.awi.plugin.client.widget.*;
import com.yanny.awi.plugin.common.nodes.*;
import com.yanny.awi.plugin.server.*;
import net.minecraft.core.Vec3i;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.*;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.heightproviders.*;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

@AwiEntrypoint
public class Plugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "awi";
    }

    @Override
    public void registerCommon(ICommonRegistry registry) {
        LanguageHolder.TRANSLATION_MAP.keySet().forEach(registry::registerTranslationKey);
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(LevelStemNode.ID, LevelStemWidget::new);
        registry.registerWidget(BiomeNode.ID, BiomeWidget::new);
        registry.registerWidget(GenerationStepNode.ID, GenerationStepWidget::new);
        registry.registerWidget(PlacedFeatureNode.ID, PlacedFeatureWidget::new);
        registry.registerWidget(BlockNode.ID, BlockWidget::new);
        registry.registerWidget(BaseTerrainNode.ID, BaseTerrainWidget::new);

        registry.registerDataNode(LevelStemNode.ID, LevelStemNode::new);
        registry.registerDataNode(BiomeNode.ID, BiomeNode::new);
        registry.registerDataNode(GenerationStepNode.ID, GenerationStepNode::new);
        registry.registerDataNode(PlacedFeatureNode.ID, PlacedFeatureNode::new);
        registry.registerDataNode(BlockNode.ID, BlockNode::new);
        registry.registerDataNode(BaseTerrainNode.ID, BaseTerrainNode::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        new CommonValueTooltip<IServerUtils, IServerRegistry>().registerAll(registry);

        registry.registerValueTooltip(IntProvider.class, ValueTooltipUtils::getIntProviderTooltip);
        registry.registerValueTooltip(RuleTest.class, ValueTooltipUtils::getRuleTestTooltip);
        registry.registerValueTooltip(HeightProvider.class, ValueTooltipUtils::getHeightProviderTooltip);
        registry.registerValueTooltip(BlockPredicate.class, ValueTooltipUtils::getBlockPredicateTooltip);
        registry.registerValueTooltip(OreConfiguration.TargetBlockState.class, ValueTooltipUtils::getTargetBlockStateTooltip);
        registry.registerValueTooltip(BlockState.class, ValueTooltipUtils::getBlockStateTooltip);
        registry.registerValueTooltip(Vec3i.class, ValueTooltipUtils::getVec3iTooltip);
        registry.registerValueTooltip(WeightedEntry.Wrapper.class, ValueTooltipUtils::getWeightedEntryWrapperTooltip);
        registry.registerValueTooltip(Weight.class, ValueTooltipUtils::getWeightTooltip);

        registry.registerValueTooltip(Block.class, RegistriesTooltipUtils::getBlockTooltip);
        registry.registerValueTooltip(Fluid.class, RegistriesTooltipUtils::getFluidTooltip);
        registry.registerValueTooltip(PlacementModifierType.class, RegistriesTooltipUtils::getPlacementModifierTooltip);
        registry.registerValueTooltip(IntProviderType.class, RegistriesTooltipUtils::getIntProviderTooltip);
        registry.registerValueTooltip(RuleTestType.class, RegistriesTooltipUtils::getRuleTestTypeTooltip);
        registry.registerValueTooltip(HeightProviderType.class, RegistriesTooltipUtils::getHeightProviderTooltip);
        registry.registerValueTooltip(BlockPredicateType.class, RegistriesTooltipUtils::getBlockPredicateTooltip);
        registry.registerValueTooltip(Feature.class, RegistriesTooltipUtils::getFeatureTypeTooltip);

        registry.registerFeatureTooltip(CountConfiguration.class, FeatureConfigurationTooltipUtils::getCountConfigurationTooltip);
        registry.registerFeatureTooltip(OreConfiguration.class, FeatureConfigurationTooltipUtils::getOreConfigurationTooltip);

        registry.registerIntProviderTooltip(ConstantInt.class, IntProviderTooltipUtils::getConstantIntTooltip);
        registry.registerIntProviderTooltip(UniformInt.class, IntProviderTooltipUtils::getUniformIntTooltip);
        registry.registerIntProviderTooltip(BiasedToBottomInt.class, IntProviderTooltipUtils::getBiasedToBottomIntTooltip);
        registry.registerIntProviderTooltip(ClampedInt.class, IntProviderTooltipUtils::getClampedIntTooltip);
        registry.registerIntProviderTooltip(WeightedListInt.class, IntProviderTooltipUtils::getWeightedListIntTooltip);
        registry.registerIntProviderTooltip(ClampedNormalInt.class, IntProviderTooltipUtils::getClampedNormalIntTooltip);

        registry.registerHeightProviderTooltip(ConstantHeight.class, HeightProviderTooltipUtils::getConstantHeightTooltip);
        registry.registerHeightProviderTooltip(UniformHeight.class, HeightProviderTooltipUtils::getUniformHeightTooltip);
        registry.registerHeightProviderTooltip(BiasedToBottomHeight.class, HeightProviderTooltipUtils::getBiasedToBottomHeightTooltip);
        registry.registerHeightProviderTooltip(VeryBiasedToBottomHeight.class, HeightProviderTooltipUtils::getVeryBiasedToBottomHeightTooltip);
        registry.registerHeightProviderTooltip(TrapezoidHeight.class, HeightProviderTooltipUtils::getTrapezoidHeightTooltip);
        registry.registerHeightProviderTooltip(WeightedListHeight.class, HeightProviderTooltipUtils::getWeightedListHeightTooltip);

        registry.registerRuleTestTooltip(AlwaysTrueTest.class, RuleTestTooltipUtils::getAlwaysTrueTooltip);

        registry.registerBlockPredicateTooltip(MatchingBlocksPredicate.class, BlockPredicateTooltipUtils::getMatchingBlocksPredicateTooltip);
        registry.registerBlockPredicateTooltip(MatchingBlockTagPredicate.class, BlockPredicateTooltipUtils::getMatchingBlockTagPredicateTooltip);
        registry.registerBlockPredicateTooltip(MatchingFluidsPredicate.class, BlockPredicateTooltipUtils::getMatchingFluidsPredicateTooltip);
        registry.registerBlockPredicateTooltip(HasSturdyFacePredicate.class, BlockPredicateTooltipUtils::getHasSturdyFacePredicateTooltip);
        registry.registerBlockPredicateTooltip(SolidPredicate.class, BlockPredicateTooltipUtils::getSolidPredicateTooltip);
        registry.registerBlockPredicateTooltip(ReplaceablePredicate.class, BlockPredicateTooltipUtils::getReplaceablePredicateTooltip);
        registry.registerBlockPredicateTooltip(WouldSurvivePredicate.class, BlockPredicateTooltipUtils::getWouldSurvivePredicateTooltip);
        registry.registerBlockPredicateTooltip(InsideWorldBoundsPredicate.class, BlockPredicateTooltipUtils::getInsideWorldBoundsPredicateTooltip);
        registry.registerBlockPredicateTooltip(AnyOfPredicate.class, BlockPredicateTooltipUtils::getAnyOfPredicateTooltip);
        registry.registerBlockPredicateTooltip(AllOfPredicate.class, BlockPredicateTooltipUtils::getAllOfPredicateTooltip);
        registry.registerBlockPredicateTooltip(NotPredicate.class, BlockPredicateTooltipUtils::getNotPredicateTooltip);
        registry.registerBlockPredicateTooltip(TrueBlockPredicate.class, BlockPredicateTooltipUtils::getTrueBlockPredicateTooltip);

        registry.registerPlacementModifierTooltip(BiomeFilter.class, PlacementModifierTooltipUtils::getBiomeFilterTooltip);
        registry.registerPlacementModifierTooltip(BlockPredicateFilter.class, PlacementModifierTooltipUtils::getBlockPredicateFilterTooltip);
        registry.registerPlacementModifierTooltip(CarvingMaskPlacement.class, PlacementModifierTooltipUtils::getCarvingMaskPlacementTooltip);
        registry.registerPlacementModifierTooltip(CountOnEveryLayerPlacement.class, PlacementModifierTooltipUtils::getCountOnEveryLayerPlacementTooltip);
        registry.registerPlacementModifierTooltip(CountPlacement.class, PlacementModifierTooltipUtils::getCountPlacementTooltip);
        registry.registerPlacementModifierTooltip(EnvironmentScanPlacement.class, PlacementModifierTooltipUtils::getEnvironmentScanPlacementTooltip);
        registry.registerPlacementModifierTooltip(HeightmapPlacement.class, PlacementModifierTooltipUtils::getHeightmapPlacementTooltip);
        registry.registerPlacementModifierTooltip(HeightRangePlacement.class, PlacementModifierTooltipUtils::getHeightRangePlacementTooltip);
        registry.registerPlacementModifierTooltip(InSquarePlacement.class, PlacementModifierTooltipUtils::getInSquarePlacementTooltip);
        registry.registerPlacementModifierTooltip(NoiseBasedCountPlacement.class, PlacementModifierTooltipUtils::getNoiseBasedCountPlacementTooltip);
        registry.registerPlacementModifierTooltip(NoiseThresholdCountPlacement.class, PlacementModifierTooltipUtils::getNoiseThresholdCountPlacementTooltip);
        registry.registerPlacementModifierTooltip(RarityFilter.class, PlacementModifierTooltipUtils::getRarityFilterTooltip);
        registry.registerPlacementModifierTooltip(RandomOffsetPlacement.class, PlacementModifierTooltipUtils::getRandomOffsetPlacementTooltip);
        registry.registerPlacementModifierTooltip(SurfaceRelativeThresholdFilter.class, PlacementModifierTooltipUtils::getSurfaceRelativeThresholdFilterTooltip);
        registry.registerPlacementModifierTooltip(SurfaceWaterDepthFilter.class, PlacementModifierTooltipUtils::getSurfaceWaterDepthFilterTooltip);

        registry.registerFeatureBlockCollector(BlockColumnConfiguration.class, FeatureConfigurationCollectorUtils::collectBlockColumnConfigurationBlocks);
        registry.registerFeatureBlockCollector(BlockPileConfiguration.class, FeatureConfigurationCollectorUtils::collectBlockPileConfigurationBlocks);
        registry.registerFeatureBlockCollector(BlockStateConfiguration.class, FeatureConfigurationCollectorUtils::collectBlockStateConfigurationBlocks);
        registry.registerFeatureBlockCollector(DeltaFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectDeltaFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(DiskConfiguration.class, FeatureConfigurationCollectorUtils::collectDiskConfigurationBlocks);
        registry.registerFeatureBlockCollector(HugeMushroomFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectHugeMushroomFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(LakeFeature.Configuration.class, FeatureConfigurationCollectorUtils::collectLakeFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(LayerConfiguration.class, FeatureConfigurationCollectorUtils::collectLayeredConfigurationBlocks);
        registry.registerFeatureBlockCollector(MultifaceGrowthConfiguration.class, FeatureConfigurationCollectorUtils::collectMultifaceGrowthConfigurationBlocks);
        registry.registerFeatureBlockCollector(OreConfiguration.class, FeatureConfigurationCollectorUtils::collectOreConfigurationBlocks);
        registry.registerFeatureBlockCollector(RandomBooleanFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectRandomBooleanFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(RandomFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectRandomFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(RandomPatchConfiguration.class, FeatureConfigurationCollectorUtils::collectRandomPatchConfigurationBlocks);
        registry.registerFeatureBlockCollector(ReplaceBlockConfiguration.class, FeatureConfigurationCollectorUtils::collectReplaceBlockConfigurationBlocks);
        registry.registerFeatureBlockCollector(ReplaceSphereConfiguration.class, FeatureConfigurationCollectorUtils::collectReplaceSphereConfigurationBlocks);
        registry.registerFeatureBlockCollector(RootSystemConfiguration.class, FeatureConfigurationCollectorUtils::collectRootSystemConfigurationBlocks);
        registry.registerFeatureBlockCollector(SimpleBlockConfiguration.class, FeatureConfigurationCollectorUtils::collectSimpleBlockConfigurationBlocks);
        registry.registerFeatureBlockCollector(SimpleRandomFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectSimpleRandomFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(SpringConfiguration.class, FeatureConfigurationCollectorUtils::collectSpringConfigurationBlocks);
        registry.registerFeatureBlockCollector(TreeConfiguration.class, FeatureConfigurationCollectorUtils::collectTreeConfigurationBlocks);
        registry.registerFeatureBlockCollector(VegetationPatchConfiguration.class, FeatureConfigurationCollectorUtils::collectVegetationPatchConfigurationBlocks);

        registry.registerStateProviderBlockCollector(SimpleStateProvider.class, BlockStateProviderCollectorUtils::collectSimple);
        registry.registerStateProviderBlockCollector(NoiseProvider.class, BlockStateProviderCollectorUtils::collectNoise);
        registry.registerStateProviderBlockCollector(NoiseThresholdProvider.class, BlockStateProviderCollectorUtils::collectNoiseThreshold);
        registry.registerStateProviderBlockCollector(RandomizedIntStateProvider.class, BlockStateProviderCollectorUtils::collectRandomized);
        registry.registerStateProviderBlockCollector(RotatedBlockProvider.class, BlockStateProviderCollectorUtils::collectRotated);
        registry.registerStateProviderBlockCollector(WeightedStateProvider.class, BlockStateProviderCollectorUtils::collectWeighted);
    }
}
