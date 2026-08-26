package com.yanny.awi.plugin;

import com.yanny.aci.tooltip.CommonValueTooltip;
import com.yanny.awi.api.*;
import com.yanny.awi.datagen.LanguageHolder;
import com.yanny.awi.plugin.client.widget.*;
import com.yanny.awi.plugin.common.nodes.*;
import com.yanny.awi.plugin.server.*;
import com.yanny.awi.plugin.server.summary.HeightSpanPropagatorUtils;
import com.yanny.awi.plugin.server.summary.IntSpanPropagatorUtils;
import com.yanny.awi.plugin.server.summary.PlacementPropagatorUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.*;
import net.minecraft.world.level.levelgen.feature.rootplacers.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.feature.treedecorators.*;
import net.minecraft.world.level.levelgen.feature.trunkplacers.*;
import net.minecraft.world.level.levelgen.heightproviders.*;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
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

        EnumTypes.TRANSLATED_ENUMS.forEach(registry::registerEnumTranslation);

        registry.registerValueTooltip(Enum.class, ValueTooltipUtils::getEnumTooltip);
        registry.registerValueTooltip(IntProvider.class, ValueTooltipUtils::getIntProviderTooltip);
        registry.registerValueTooltip(RuleTest.class, ValueTooltipUtils::getRuleTestTooltip);
        registry.registerValueTooltip(HeightProvider.class, ValueTooltipUtils::getHeightProviderTooltip);
        registry.registerValueTooltip(BlockPredicate.class, ValueTooltipUtils::getBlockPredicateTooltip);
        registry.registerValueTooltip(BlockStateProvider.class, ValueTooltipUtils::getBlockStateProviderTooltip);
        registry.registerValueTooltip(TreeDecorator.class, ValueTooltipUtils::getTreeDecoratorTooltip);
        registry.registerValueTooltip(FeatureSize.class, ValueTooltipUtils::getFeatureSizeTooltip);
        registry.registerValueTooltip(RootPlacer.class, ValueTooltipUtils::getRootPlacerTooltip);
        registry.registerValueTooltip(FoliagePlacer.class, ValueTooltipUtils::getFoliagePlacerTooltip);
        registry.registerValueTooltip(TrunkPlacer.class, ValueTooltipUtils::getTrunkPlacerTooltip);
        registry.registerValueTooltip(FloatProvider.class, ValueTooltipUtils::getFloatProviderTooltip);
        registry.registerValueTooltip(PlacementModifier.class, ValueTooltipUtils::getPlacementModifierTooltip);
        registry.registerValueTooltip(FeatureConfiguration.class, ValueTooltipUtils::getFeatureConfigurationTooltip);

        registry.registerValueTooltip(OreConfiguration.TargetBlockState.class, ValueTooltipUtils::getTargetBlockStateTooltip);
        registry.registerValueTooltip(BlockState.class, ValueTooltipUtils::getBlockStateTooltip);
        registry.registerValueTooltip(FluidState.class, ValueTooltipUtils::getFluidStateTooltip);
        registry.registerValueTooltip(Vec3i.class, ValueTooltipUtils::getVec3iTooltip);
        registry.registerValueTooltip(WeightedEntry.Wrapper.class, ValueTooltipUtils::getWeightedEntryWrapperTooltip);
        registry.registerValueTooltip(Weight.class, ValueTooltipUtils::getWeightTooltip);
        registry.registerValueTooltip(BlockColumnConfiguration.Layer.class, ValueTooltipUtils::getBlockColumnConfigurationLayerTooltip);
        registry.registerValueTooltip(GeodeBlockSettings.class, ValueTooltipUtils::getGeodeBlockSettingsTooltip);
        registry.registerValueTooltip(GeodeLayerSettings.class, ValueTooltipUtils::getGeodeLayerSettingsTooltip);
        registry.registerValueTooltip(GeodeCrackSettings.class, ValueTooltipUtils::getGeodeCrackSettingsTooltip);
        registry.registerValueTooltip(WeightedPlacedFeature.class, ValueTooltipUtils::getWeightedPlacedFeatureTooltip);
        registry.registerValueTooltip(SpikeFeature.EndSpike.class, ValueTooltipUtils::getEndSpikeTooltip);
        registry.registerValueTooltip(RuleBasedBlockStateProvider.class, ValueTooltipUtils::getRuleBasedBlockStateProviderTooltip);
        registry.registerValueTooltip(PlacedFeature.class, ValueTooltipUtils::getPlacedFeatureTooltip);
        registry.registerValueTooltip(ConfiguredFeature.class, ValueTooltipUtils::getConfiguredFeatureTooltip);
        registry.registerValueTooltip(RuleBasedBlockStateProvider.Rule.class, ValueTooltipUtils::getRuleBasedBlockStateProviderRuleTooltip);
        registry.registerValueTooltip(StructureProcessorList.class, ValueTooltipUtils::getStructureProcessorListTooltip);
        registry.registerValueTooltip(StructureProcessor.class, ValueTooltipUtils::getStructureProcessorTooltip);
        registry.registerValueTooltip(ProcessorRule.class, ValueTooltipUtils::getProcessorRuleTooltip);
        registry.registerValueTooltip(MangroveRootPlacement.class, ValueTooltipUtils::getMangroveRootPlacementTooltip);
        registry.registerValueTooltip(AboveRootPlacement.class, ValueTooltipUtils::getAboveRootPlacementTooltip);
        registry.registerValueTooltip(SimpleWeightedRandomList.class, ValueTooltipUtils::getSimpleWeightedRandomListTooltip);
        registry.registerValueTooltip(PosRuleTest.class, ValueTooltipUtils::getPosRuleTestTooltip);
        registry.registerValueTooltip(RuleBlockEntityModifier.class, ValueTooltipUtils::getRuleBlockEntityModifierTooltip);

        registry.registerValueTooltip(Block.class, RegistriesTooltipUtils::getBlockTooltip);
        registry.registerValueTooltip(Fluid.class, RegistriesTooltipUtils::getFluidTooltip);
        registry.registerValueTooltip(PlacementModifierType.class, RegistriesTooltipUtils::getPlacementModifierTooltip);
        registry.registerValueTooltip(IntProviderType.class, RegistriesTooltipUtils::getIntProviderTooltip);
        registry.registerValueTooltip(RuleTestType.class, RegistriesTooltipUtils::getRuleTestTypeTooltip);
        registry.registerValueTooltip(HeightProviderType.class, RegistriesTooltipUtils::getHeightProviderTooltip);
        registry.registerValueTooltip(BlockPredicateType.class, RegistriesTooltipUtils::getBlockPredicateTooltip);
        registry.registerValueTooltip(Feature.class, RegistriesTooltipUtils::getFeatureTypeTooltip);
        registry.registerValueTooltip(BlockStateProviderType.class, RegistriesTooltipUtils::getBlockStateProviderTooltip);
        registry.registerValueTooltip(TreeDecoratorType.class, RegistriesTooltipUtils::getTreeDecoratorTooltip);
        registry.registerValueTooltip(FeatureSizeType.class, RegistriesTooltipUtils::getFeatureSizeTooltip);
        registry.registerValueTooltip(RootPlacerType.class, RegistriesTooltipUtils::getRootPlacerTooltip);
        registry.registerValueTooltip(FoliagePlacerType.class, RegistriesTooltipUtils::getFoliagePlacerTooltip);
        registry.registerValueTooltip(TrunkPlacerType.class, RegistriesTooltipUtils::getTrunkPlacerTooltip);
        registry.registerValueTooltip(FloatProviderType.class, RegistriesTooltipUtils::getFloatProviderTooltip);
        registry.registerValueTooltip(StructureProcessorType.class, RegistriesTooltipUtils::getStructureProcessorTypeTooltip);
        registry.registerValueTooltip(PosRuleTestType.class, RegistriesTooltipUtils::getPosRuleTestTypeTooltip);
        registry.registerValueTooltip(RuleBlockEntityModifierType.class, RegistriesTooltipUtils::getRuleBlockEntityModifierTooltip);

        registry.registerFeatureTooltip(BlockColumnConfiguration.class, FeatureConfigurationTooltipUtils::getBlockColumnConfigurationTooltip);
        registry.registerFeatureTooltip(BlockPileConfiguration.class, FeatureConfigurationTooltipUtils::getBlockPileConfigurationTooltip);
        registry.registerFeatureTooltip(BlockStateConfiguration.class, FeatureConfigurationTooltipUtils::getBlockStateConfigurationTooltip);
        registry.registerFeatureTooltip(ColumnFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getColumnFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(CountConfiguration.class, FeatureConfigurationTooltipUtils::getCountConfigurationTooltip);
        registry.registerFeatureTooltip(DeltaFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getDeltaFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(DiskConfiguration.class, FeatureConfigurationTooltipUtils::getDiscConfigurationTooltip);
        registry.registerFeatureTooltip(DripstoneClusterConfiguration.class, FeatureConfigurationTooltipUtils::getDripstoneClusterConfigurationTooltip);
        registry.registerFeatureTooltip(EndGatewayConfiguration.class, FeatureConfigurationTooltipUtils::getEndGatewayConfigurationTooltip);
        registry.registerFeatureTooltip(GeodeConfiguration.class, FeatureConfigurationTooltipUtils::getGeodeConfigurationTooltip);
        registry.registerFeatureTooltip(HugeMushroomFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getHugeMushroomFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(LargeDripstoneConfiguration.class, FeatureConfigurationTooltipUtils::getLargeDripstoneConfigurationTooltip);
        registry.registerFeatureTooltip(LayerConfiguration.class, FeatureConfigurationTooltipUtils::getLayeredConfigurationTooltip);
        registry.registerFeatureTooltip(MultifaceGrowthConfiguration.class, FeatureConfigurationTooltipUtils::getMultifaceGrowthConfigurationTooltip);
        registry.registerFeatureTooltip(NetherForestVegetationConfig.class, FeatureConfigurationTooltipUtils::getNetherForestVegetationConfigurationTooltip);
        registry.registerFeatureTooltip(NoneFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getNoneFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(OreConfiguration.class, FeatureConfigurationTooltipUtils::getOreConfigurationTooltip);
        registry.registerFeatureTooltip(PointedDripstoneConfiguration.class, FeatureConfigurationTooltipUtils::getPointedDripstoneConfigurationTooltip);
        registry.registerFeatureTooltip(ProbabilityFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getProbabilityFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(RandomBooleanFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getRandomBooleanFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(RandomFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getRandomFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(RandomPatchConfiguration.class, FeatureConfigurationTooltipUtils::getRandomPatchConfigurationTooltip);
        registry.registerFeatureTooltip(ReplaceBlockConfiguration.class, FeatureConfigurationTooltipUtils::getReplaceableBlockConfigurationTooltip);
        registry.registerFeatureTooltip(ReplaceSphereConfiguration.class, FeatureConfigurationTooltipUtils::getReplaceableSphereConfigurationTooltip);
        registry.registerFeatureTooltip(RootSystemConfiguration.class, FeatureConfigurationTooltipUtils::getRootSystemConfigurationTooltip);
        registry.registerFeatureTooltip(SculkPatchConfiguration.class, FeatureConfigurationTooltipUtils::getSculkPatchConfigurationTooltip);
        registry.registerFeatureTooltip(SimpleBlockConfiguration.class, FeatureConfigurationTooltipUtils::getSimpleBlockConfigurationTooltip);
        registry.registerFeatureTooltip(SimpleRandomFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getSimpleRandomFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(SpikeConfiguration.class, FeatureConfigurationTooltipUtils::getSpikeConfigurationTooltip);
        registry.registerFeatureTooltip(SpringConfiguration.class, FeatureConfigurationTooltipUtils::getSpringConfigurationTooltip);
        registry.registerFeatureTooltip(TreeConfiguration.class, FeatureConfigurationTooltipUtils::getTreeConfigurationTooltip);
        registry.registerFeatureTooltip(TwistingVinesConfig.class, FeatureConfigurationTooltipUtils::getTwistingVinesConfigurationTooltip);
        registry.registerFeatureTooltip(UnderwaterMagmaConfiguration.class, FeatureConfigurationTooltipUtils::getUnderwaterMagmaConfigurationTooltip);
        registry.registerFeatureTooltip(VegetationPatchConfiguration.class, FeatureConfigurationTooltipUtils::getVegetationPatchConfigurationTooltip);
        registry.registerFeatureTooltip(LakeFeature.Configuration.class, FeatureConfigurationTooltipUtils::getLakeConfigurationTooltip);
        registry.registerFeatureTooltip(FossilFeatureConfiguration.class, FeatureConfigurationTooltipUtils::getFossilFeatureConfigurationTooltip);
        registry.registerFeatureTooltip(HugeFungusConfiguration.class, FeatureConfigurationTooltipUtils::getHugeFungusConfigurationTooltip);

        registry.registerIntProviderTooltip(ConstantInt.class, IntProviderTooltipUtils::getConstantIntTooltip);
        registry.registerIntProviderTooltip(UniformInt.class, IntProviderTooltipUtils::getUniformIntTooltip);
        registry.registerIntProviderTooltip(BiasedToBottomInt.class, IntProviderTooltipUtils::getBiasedToBottomIntTooltip);
        registry.registerIntProviderTooltip(ClampedInt.class, IntProviderTooltipUtils::getClampedIntTooltip);
        registry.registerIntProviderTooltip(WeightedListInt.class, IntProviderTooltipUtils::getWeightedListIntTooltip);
        registry.registerIntProviderTooltip(ClampedNormalInt.class, IntProviderTooltipUtils::getClampedNormalIntTooltip);

        registry.registerFloatProviderTooltip(ConstantFloat.class, FloatProviderTooltipUtils::getConstantFloatTooltip);
        registry.registerFloatProviderTooltip(UniformFloat.class, FloatProviderTooltipUtils::getUniformFloatTooltip);
        registry.registerFloatProviderTooltip(ClampedNormalFloat.class, FloatProviderTooltipUtils::getClampedNormalFloatTooltip);
        registry.registerFloatProviderTooltip(TrapezoidFloat.class, FloatProviderTooltipUtils::getTrapezoidFloatTooltip);

        registry.registerHeightProviderTooltip(ConstantHeight.class, HeightProviderTooltipUtils::getConstantHeightTooltip);
        registry.registerHeightProviderTooltip(UniformHeight.class, HeightProviderTooltipUtils::getUniformHeightTooltip);
        registry.registerHeightProviderTooltip(BiasedToBottomHeight.class, HeightProviderTooltipUtils::getBiasedToBottomHeightTooltip);
        registry.registerHeightProviderTooltip(VeryBiasedToBottomHeight.class, HeightProviderTooltipUtils::getVeryBiasedToBottomHeightTooltip);
        registry.registerHeightProviderTooltip(TrapezoidHeight.class, HeightProviderTooltipUtils::getTrapezoidHeightTooltip);
        registry.registerHeightProviderTooltip(WeightedListHeight.class, HeightProviderTooltipUtils::getWeightedListHeightTooltip);

        registry.registerRuleTestTooltip(AlwaysTrueTest.class, RuleTestTooltipUtils::getAlwaysTrueTestTooltip);
        registry.registerRuleTestTooltip(BlockMatchTest.class, RuleTestTooltipUtils::getBlockMatchTestTooltip);
        registry.registerRuleTestTooltip(BlockStateMatchTest.class, RuleTestTooltipUtils::getBlockStateMatchTestTooltip);
        registry.registerRuleTestTooltip(TagMatchTest.class, RuleTestTooltipUtils::getTagMatchTestTooltip);
        registry.registerRuleTestTooltip(RandomBlockMatchTest.class, RuleTestTooltipUtils::getRandomBlockMatchTestTooltip);
        registry.registerRuleTestTooltip(RandomBlockStateMatchTest.class, RuleTestTooltipUtils::getRandomBlockStateMatchTestTooltip);

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

        registry.registerIntSpanPropagator(ConstantInt.class, IntSpanPropagatorUtils::getConstantInt);
        registry.registerIntSpanPropagator(UniformInt.class, IntSpanPropagatorUtils::getUniformInt);
        registry.registerIntSpanPropagator(BiasedToBottomInt.class, IntSpanPropagatorUtils::getBiasedToBottomInt);
        registry.registerIntSpanPropagator(ClampedInt.class, IntSpanPropagatorUtils::getClampedInt);
        registry.registerIntSpanPropagator(ClampedNormalInt.class, IntSpanPropagatorUtils::getClampedNormalInt);
        registry.registerIntSpanPropagator(WeightedListInt.class, IntSpanPropagatorUtils::getWeightedListInt);

        registry.registerHeightSpanPropagator(ConstantHeight.class, HeightSpanPropagatorUtils::getConstantHeight);
        registry.registerHeightSpanPropagator(UniformHeight.class, HeightSpanPropagatorUtils::getUniformHeight);
        registry.registerHeightSpanPropagator(TrapezoidHeight.class, HeightSpanPropagatorUtils::getTrapezoidHeight);
        registry.registerHeightSpanPropagator(BiasedToBottomHeight.class, HeightSpanPropagatorUtils::getBiasedToBottomHeight);
        registry.registerHeightSpanPropagator(VeryBiasedToBottomHeight.class, HeightSpanPropagatorUtils::getVeryBiasedToBottomHeight);
        registry.registerHeightSpanPropagator(WeightedListHeight.class, HeightSpanPropagatorUtils::getWeightedListHeight);

        registry.registerPlacementPropagator(CountPlacement.class, PlacementPropagatorUtils::getCountPlacement);
        registry.registerPlacementPropagator(CountOnEveryLayerPlacement.class, PlacementPropagatorUtils::getCountOnEveryLayerPlacement);
        registry.registerPlacementPropagator(NoiseBasedCountPlacement.class, PlacementPropagatorUtils::getNoiseBasedCountPlacement);
        registry.registerPlacementPropagator(NoiseThresholdCountPlacement.class, PlacementPropagatorUtils::getNoiseThresholdCountPlacement);
        registry.registerPlacementPropagator(RarityFilter.class, PlacementPropagatorUtils::getRarityFilter);
        registry.registerPlacementPropagator(HeightRangePlacement.class, PlacementPropagatorUtils::getHeightRangePlacement);
        registry.registerPlacementPropagator(HeightmapPlacement.class, PlacementPropagatorUtils::getHeightmapPlacement);
        registry.registerPlacementPropagator(SurfaceRelativeThresholdFilter.class, PlacementPropagatorUtils::getSurfaceRelativeThresholdFilter);

        registry.registerFeatureBlockCollector(BlockColumnConfiguration.class, FeatureConfigurationCollectorUtils::collectBlockColumnConfigurationBlocks);
        registry.registerFeatureBlockCollector(BlockPileConfiguration.class, FeatureConfigurationCollectorUtils::collectBlockPileConfigurationBlocks);
        registry.registerFeatureBlockCollector(BlockStateConfiguration.class, FeatureConfigurationCollectorUtils::collectBlockStateConfigurationBlocks);
        registry.registerFeatureBlockCollector(ColumnFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectColumnFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(CountConfiguration.class, FeatureConfigurationCollectorUtils::collectCountConfigurationBlocks);
        registry.registerFeatureBlockCollector(DeltaFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectDeltaFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(DiskConfiguration.class, FeatureConfigurationCollectorUtils::collectDiskConfigurationBlocks);
        registry.registerFeatureBlockCollector(DripstoneClusterConfiguration.class, FeatureConfigurationCollectorUtils::collectDripstoneClusterConfigurationBlocks);
        registry.registerFeatureBlockCollector(EndGatewayConfiguration.class, FeatureConfigurationCollectorUtils::collectEndGatewayConfigurationBlocks);
        registry.registerFeatureBlockCollector(GeodeConfiguration.class, FeatureConfigurationCollectorUtils::collectGeodeConfigurationBlocks);
        registry.registerFeatureBlockCollector(HugeMushroomFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectHugeMushroomFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(LargeDripstoneConfiguration.class, FeatureConfigurationCollectorUtils::collectLargeDripstoneConfigurationBlocks);
        registry.registerFeatureBlockCollector(LayerConfiguration.class, FeatureConfigurationCollectorUtils::collectLayeredConfigurationBlocks);
        registry.registerFeatureBlockCollector(MultifaceGrowthConfiguration.class, FeatureConfigurationCollectorUtils::collectMultifaceGrowthConfigurationBlocks);
        registry.registerFeatureBlockCollector(NetherForestVegetationConfig.class, FeatureConfigurationCollectorUtils::collectNetherForestVegetationConfigurationBlocks);
        registry.registerFeatureBlockCollector(NoneFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectNoneFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(OreConfiguration.class, FeatureConfigurationCollectorUtils::collectOreConfigurationBlocks);
        registry.registerFeatureBlockCollector(PointedDripstoneConfiguration.class, FeatureConfigurationCollectorUtils::collectPointedDripstoneConfigurationBlocks);
        registry.registerFeatureBlockCollector(ProbabilityFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectProbabilityFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(RandomBooleanFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectRandomBooleanFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(RandomFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectRandomFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(RandomPatchConfiguration.class, FeatureConfigurationCollectorUtils::collectRandomPatchConfigurationBlocks);
        registry.registerFeatureBlockCollector(ReplaceBlockConfiguration.class, FeatureConfigurationCollectorUtils::collectReplaceBlockConfigurationBlocks);
        registry.registerFeatureBlockCollector(ReplaceSphereConfiguration.class, FeatureConfigurationCollectorUtils::collectReplaceSphereConfigurationBlocks);
        registry.registerFeatureBlockCollector(RootSystemConfiguration.class, FeatureConfigurationCollectorUtils::collectRootSystemConfigurationBlocks);
        registry.registerFeatureBlockCollector(SculkPatchConfiguration.class, FeatureConfigurationCollectorUtils::collectSculkPatchConfigurationBlocks);
        registry.registerFeatureBlockCollector(SimpleBlockConfiguration.class, FeatureConfigurationCollectorUtils::collectSimpleBlockConfigurationBlocks);
        registry.registerFeatureBlockCollector(SimpleRandomFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectSimpleRandomFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(SpikeConfiguration.class, FeatureConfigurationCollectorUtils::collectSpikeConfigurationBlocks);
        registry.registerFeatureBlockCollector(SpringConfiguration.class, FeatureConfigurationCollectorUtils::collectSpringConfigurationBlocks);
        registry.registerFeatureBlockCollector(TreeConfiguration.class, FeatureConfigurationCollectorUtils::collectTreeConfigurationBlocks);
        registry.registerFeatureBlockCollector(TwistingVinesConfig.class, FeatureConfigurationCollectorUtils::collectTwistingVinesConfigurationBlocks);
        registry.registerFeatureBlockCollector(UnderwaterMagmaConfiguration.class, FeatureConfigurationCollectorUtils::collectUnderwaterMagmaConfigurationBlocks);
        registry.registerFeatureBlockCollector(VegetationPatchConfiguration.class, FeatureConfigurationCollectorUtils::collectVegetationPatchConfigurationBlocks);
        registry.registerFeatureBlockCollector(LakeFeature.Configuration.class, FeatureConfigurationCollectorUtils::collectLakeFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(FossilFeatureConfiguration.class, FeatureConfigurationCollectorUtils::collectFossilFeatureConfigurationBlocks);
        registry.registerFeatureBlockCollector(HugeFungusConfiguration.class, FeatureConfigurationCollectorUtils::collectHugeFungusConfigurationBlocks);

        registry.registerStateProviderBlockCollector(SimpleStateProvider.class, BlockStateProviderCollectorUtils::collectSimple);
        registry.registerStateProviderBlockCollector(NoiseProvider.class, BlockStateProviderCollectorUtils::collectNoise);
        registry.registerStateProviderBlockCollector(DualNoiseProvider.class, BlockStateProviderCollectorUtils::collectDualNoise);
        registry.registerStateProviderBlockCollector(NoiseThresholdProvider.class, BlockStateProviderCollectorUtils::collectNoiseThreshold);
        registry.registerStateProviderBlockCollector(RandomizedIntStateProvider.class, BlockStateProviderCollectorUtils::collectRandomized);
        registry.registerStateProviderBlockCollector(RotatedBlockProvider.class, BlockStateProviderCollectorUtils::collectRotated);
        registry.registerStateProviderBlockCollector(WeightedStateProvider.class, BlockStateProviderCollectorUtils::collectWeighted);

        registry.registerTreeDecoratorBlockCollector(TrunkVineDecorator.class, TreeDecoratorCollectorUtils::collectTrunkVine);
        registry.registerTreeDecoratorBlockCollector(LeaveVineDecorator.class, TreeDecoratorCollectorUtils::collectLeaveVine);
        registry.registerTreeDecoratorBlockCollector(CocoaDecorator.class, TreeDecoratorCollectorUtils::collectCocoa);
        registry.registerTreeDecoratorBlockCollector(BeehiveDecorator.class, TreeDecoratorCollectorUtils::collectBeehive);
        registry.registerTreeDecoratorBlockCollector(AlterGroundDecorator.class, TreeDecoratorCollectorUtils::collectAlterGround);
        registry.registerTreeDecoratorBlockCollector(AttachedToLeavesDecorator.class, TreeDecoratorCollectorUtils::collectAttachedToLeaves);

        registry.registerRootPlacerBlockCollector(MangroveRootPlacer.class, RootPlacerCollectorUtils::collectMangrove);

        registry.registerBlockStateProviderTooltip(SimpleStateProvider.class, BlockStateProviderTooltipUtils::getSimpleStateProviderTooltip);
        registry.registerBlockStateProviderTooltip(WeightedStateProvider.class, BlockStateProviderTooltipUtils::getWeightedStateProviderTooltip);
        registry.registerBlockStateProviderTooltip(NoiseThresholdProvider.class, BlockStateProviderTooltipUtils::getNoiseThresholdProviderTooltip);
        registry.registerBlockStateProviderTooltip(NoiseProvider.class, BlockStateProviderTooltipUtils::getNoiseProviderTooltip);
        registry.registerBlockStateProviderTooltip(DualNoiseProvider.class, BlockStateProviderTooltipUtils::getDualNoiseProviderTooltip);
        registry.registerBlockStateProviderTooltip(RotatedBlockProvider.class, BlockStateProviderTooltipUtils::getRotatedBlockProviderTooltip);
        registry.registerBlockStateProviderTooltip(RandomizedIntStateProvider.class, BlockStateProviderTooltipUtils::getRandomizedIntStateProviderTooltip);

        registry.registerTrunkPlacerTooltip(StraightTrunkPlacer.class, TrunkPlacerTooltipUtils::getStraightTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(ForkingTrunkPlacer.class, TrunkPlacerTooltipUtils::getForkingTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(GiantTrunkPlacer.class, TrunkPlacerTooltipUtils::getGiantTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(MegaJungleTrunkPlacer.class, TrunkPlacerTooltipUtils::getMegaJungleTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(DarkOakTrunkPlacer.class, TrunkPlacerTooltipUtils::getDarkOakTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(FancyTrunkPlacer.class, TrunkPlacerTooltipUtils::getFancyTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(BendingTrunkPlacer.class, TrunkPlacerTooltipUtils::getBendingTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(UpwardsBranchingTrunkPlacer.class, TrunkPlacerTooltipUtils::getUpwardBranchingTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(CherryTrunkPlacer.class, TrunkPlacerTooltipUtils::getCherryTrunkPlacerTooltip);

        registry.registerTreeDecoratorTooltip(TrunkVineDecorator.class, TreeDecoratorTooltipUtils::getTrunkVineDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(LeaveVineDecorator.class, TreeDecoratorTooltipUtils::getLeaveVineDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(CocoaDecorator.class, TreeDecoratorTooltipUtils::getCocoaDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(BeehiveDecorator.class, TreeDecoratorTooltipUtils::getBeehiveDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(AlterGroundDecorator.class, TreeDecoratorTooltipUtils::getAlterGroundDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(AttachedToLeavesDecorator.class, TreeDecoratorTooltipUtils::getAttachedToLeavesDecoratorTooltip);

        registry.registerFoliagePlacerTooltip(BlobFoliagePlacer.class, FoliagePlacerTooltipUtils::getBlobFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(SpruceFoliagePlacer.class, FoliagePlacerTooltipUtils::getSpruceFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(PineFoliagePlacer.class, FoliagePlacerTooltipUtils::getPineFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(AcaciaFoliagePlacer.class, FoliagePlacerTooltipUtils::getAcaciaFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(BushFoliagePlacer.class, FoliagePlacerTooltipUtils::getBushFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(FancyFoliagePlacer.class, FoliagePlacerTooltipUtils::getFancyFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(MegaJungleFoliagePlacer.class, FoliagePlacerTooltipUtils::getMegaJungleFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(MegaPineFoliagePlacer.class, FoliagePlacerTooltipUtils::getMegaPineFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(DarkOakFoliagePlacer.class, FoliagePlacerTooltipUtils::getDarkOakFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(RandomSpreadFoliagePlacer.class, FoliagePlacerTooltipUtils::getRandomSpreadFoliagePlacerTooltip);
        registry.registerFoliagePlacerTooltip(CherryFoliagePlacer.class, FoliagePlacerTooltipUtils::getCherryFoliagePlacerTooltip);

        registry.registerFeatureSizeTooltip(TwoLayersFeatureSize.class, FeatureSizeTooltipUtils::getTwoLayersFeatureSizeTooltip);
        registry.registerFeatureSizeTooltip(ThreeLayersFeatureSize.class, FeatureSizeTooltipUtils::getThreeLayersFeatureSizeTooltip);

        registry.registerRootPlacerTooltip(MangroveRootPlacer.class, RootPlacerTooltipUtils::getMangroveRootPlacerTooltip);

        registry.registerStructureProcessorTooltip(BlackstoneReplaceProcessor.class, StructureProcessorTooltipUtils::getBlackstoneReplaceProcessorTooltip);
        registry.registerStructureProcessorTooltip(BlockAgeProcessor.class, StructureProcessorTooltipUtils::getBlockAgeProcessorTooltip);
        registry.registerStructureProcessorTooltip(BlockIgnoreProcessor.class, StructureProcessorTooltipUtils::getBlockIgnoreProcessorTooltip);
        registry.registerStructureProcessorTooltip(BlockRotProcessor.class, StructureProcessorTooltipUtils::getBlockRotProcessorTooltip);
        registry.registerStructureProcessorTooltip(CappedProcessor.class, StructureProcessorTooltipUtils::getCappedProcessorTooltip);
        registry.registerStructureProcessorTooltip(GravityProcessor.class, StructureProcessorTooltipUtils::getGravityProcessorTooltip);
        registry.registerStructureProcessorTooltip(JigsawReplacementProcessor.class, StructureProcessorTooltipUtils::getJigsawReplacementProcessorTooltip);
        registry.registerStructureProcessorTooltip(LavaSubmergedBlockProcessor.class, StructureProcessorTooltipUtils::getLavaSubmergedBlockProcessorTooltip);
        registry.registerStructureProcessorTooltip(NopProcessor.class, StructureProcessorTooltipUtils::getNopProcessorTooltip);
        registry.registerStructureProcessorTooltip(ProtectedBlockProcessor.class, StructureProcessorTooltipUtils::getProtectedBlockProcessorTooltip);
        registry.registerStructureProcessorTooltip(RuleProcessor.class, StructureProcessorTooltipUtils::getRuleProcessorTooltip);
    }
}
