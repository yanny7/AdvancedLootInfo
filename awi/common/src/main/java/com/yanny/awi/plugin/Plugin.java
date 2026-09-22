package com.yanny.awi.plugin;

import com.yanny.aci.tooltip.CommonValueTooltip;
import com.yanny.awi.Utils;
import com.yanny.awi.api.*;
import com.yanny.awi.datagen.LanguageHolder;
import com.yanny.awi.plugin.client.widget.*;
import com.yanny.awi.plugin.common.nodes.*;
import com.yanny.awi.plugin.server.*;
import com.yanny.awi.plugin.server.summary.HeightSpanPropagatorUtils;
import com.yanny.awi.plugin.server.summary.IntSpanPropagatorUtils;
import com.yanny.awi.plugin.server.summary.PlacementPropagatorUtils;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.*;
import net.minecraft.world.level.levelgen.feature.*;
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
        registry.registerCacheCleaner(FeatureBytecodeScanner::clearCaches);
        registry.registerCacheCleaner(SurfaceRuleSpecializer::clearLoggedRules);

        new CommonValueTooltip<IServerUtils, IServerRegistry>().registerAll(registry);

        EnumTypes.TRANSLATED_ENUMS.forEach((type, owner) -> registry.registerEnumTranslation(type, Utils.MOD_ID, owner));

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
        registry.registerValueTooltip(Feature.class, ValueTooltipUtils::getFeatureTooltip);

        registry.registerValueTooltip(BlockReplacement.class, ValueTooltipUtils::getBlockReplacementTooltip);
        registry.registerValueTooltip(BlockState.class, ValueTooltipUtils::getBlockStateTooltip);
        registry.registerValueTooltip(FluidState.class, ValueTooltipUtils::getFluidStateTooltip);
        registry.registerValueTooltip(Weighted.class, ValueTooltipUtils::getWeightedTooltip);
        registry.registerValueTooltip(BlockColumnFeature.Layer.class, ValueTooltipUtils::getBlockColumnLayerTooltip);
        registry.registerValueTooltip(GeodeBlockSettings.class, ValueTooltipUtils::getGeodeBlockSettingsTooltip);
        registry.registerValueTooltip(GeodeLayerSettings.class, ValueTooltipUtils::getGeodeLayerSettingsTooltip);
        registry.registerValueTooltip(GeodeCrackSettings.class, ValueTooltipUtils::getGeodeCrackSettingsTooltip);
        registry.registerValueTooltip(WeightedPlacedFeature.class, ValueTooltipUtils::getWeightedPlacedFeatureTooltip);
        registry.registerValueTooltip(EndSpikeFeature.EndSpike.class, ValueTooltipUtils::getEndSpikeTooltip);
        registry.registerValueTooltip(PlacedFeature.class, ValueTooltipUtils::getPlacedFeatureTooltip);
        registry.registerValueTooltip(RuleBasedStateProvider.Rule.class, ValueTooltipUtils::getRuleBasedBlockStateProviderRuleTooltip);
        registry.registerValueTooltip(StructureProcessorList.class, ValueTooltipUtils::getStructureProcessorListTooltip);
        registry.registerValueTooltip(StructureProcessor.class, ValueTooltipUtils::getStructureProcessorTooltip);
        registry.registerValueTooltip(ProcessorRule.class, ValueTooltipUtils::getProcessorRuleTooltip);
        registry.registerValueTooltip(MangroveRootPlacement.class, ValueTooltipUtils::getMangroveRootPlacementTooltip);
        registry.registerValueTooltip(AboveRootPlacement.class, ValueTooltipUtils::getAboveRootPlacementTooltip);
        registry.registerValueTooltip(WeightedList.class, ValueTooltipUtils::getWeightedListTooltip);
        registry.registerValueTooltip(PosRuleTest.class, ValueTooltipUtils::getPosRuleTestTooltip);
        registry.registerValueTooltip(RuleBlockEntityModifier.class, ValueTooltipUtils::getRuleBlockEntityModifierTooltip);
        registry.registerValueTooltip(TemplateFeature.TemplateEntry.class, ValueTooltipUtils::getTemplateEntryTooltip);

        registry.registerValueTooltip(Block.class, RegistriesTooltipUtils::getBlockTooltip);
        registry.registerValueTooltip(Fluid.class, RegistriesTooltipUtils::getFluidTooltip);
        registry.registerValueTooltip(RuleTestType.class, RegistriesTooltipUtils::getRuleTestTypeTooltip);
        registry.registerValueTooltip(HeightProviderType.class, RegistriesTooltipUtils::getHeightProviderTooltip);
        registry.registerValueTooltip(BlockPredicateType.class, RegistriesTooltipUtils::getBlockPredicateTooltip);
        registry.registerValueTooltip(TreeDecoratorType.class, RegistriesTooltipUtils::getTreeDecoratorTooltip);
        registry.registerValueTooltip(FeatureSizeType.class, RegistriesTooltipUtils::getFeatureSizeTooltip);
        registry.registerValueTooltip(RootPlacerType.class, RegistriesTooltipUtils::getRootPlacerTooltip);
        registry.registerValueTooltip(FoliagePlacerType.class, RegistriesTooltipUtils::getFoliagePlacerTooltip);
        registry.registerValueTooltip(TrunkPlacerType.class, RegistriesTooltipUtils::getTrunkPlacerTooltip);
        registry.registerValueTooltip(PosRuleTestType.class, RegistriesTooltipUtils::getPosRuleTestTypeTooltip);
        registry.registerValueTooltip(RuleBlockEntityModifierType.class, RegistriesTooltipUtils::getRuleBlockEntityModifierTooltip);

        registry.registerFeatureTooltip(BambooFeature.CODEC, FeatureTooltipUtils::getBambooTooltip);
        registry.registerFeatureTooltip(BlockBlobFeature.CODEC, FeatureTooltipUtils::getBlockBlobTooltip);
        registry.registerFeatureTooltip(BlockColumnFeature.CODEC, FeatureTooltipUtils::getBlockColumnTooltip);
        registry.registerFeatureTooltip(BlockPileFeature.CODEC, FeatureTooltipUtils::getBlockPileTooltip);
        registry.registerFeatureTooltip(BlueIceFeature.CODEC, FeatureTooltipUtils::getBlueIceTooltip);
        registry.registerFeatureTooltip(BonusChestFeature.CODEC, FeatureTooltipUtils::getBonusChestTooltip);
        registry.registerFeatureTooltip(ChorusPlantFeature.CODEC, FeatureTooltipUtils::getChorusPlantTooltip);
        registry.registerFeatureTooltip(CoralClawFeature.CODEC, FeatureTooltipUtils::getCoralClawTooltip);
        registry.registerFeatureTooltip(CoralTreeFeature.CODEC, FeatureTooltipUtils::getCoralTreeTooltip);
        registry.registerFeatureTooltip(DeltaFeature.CODEC, FeatureTooltipUtils::getDeltaTooltip);
        registry.registerFeatureTooltip(DiskFeature.CODEC, FeatureTooltipUtils::getDiskTooltip);
        registry.registerFeatureTooltip(EndGatewayFeature.CODEC, FeatureTooltipUtils::getEndGatewayTooltip);
        registry.registerFeatureTooltip(EndIslandFeature.CODEC, FeatureTooltipUtils::getEndIslandTooltip);
        registry.registerFeatureTooltip(EndPlatformFeature.CODEC, FeatureTooltipUtils::getEndPlatformTooltip);
        registry.registerFeatureTooltip(EndPodiumFeature.CODEC, FeatureTooltipUtils::getEndPodiumTooltip);
        registry.registerFeatureTooltip(EndSpikeFeature.CODEC, FeatureTooltipUtils::getEndSpikeTooltip);
        registry.registerFeatureTooltip(FallenTreeFeature.CODEC, FeatureTooltipUtils::getFallenTreeTooltip);
        registry.registerFeatureTooltip(FillLayerFeature.CODEC, FeatureTooltipUtils::getFillLayerTooltip);
        registry.registerFeatureTooltip(FossilFeature.CODEC, FeatureTooltipUtils::getFossilTooltip);
        registry.registerFeatureTooltip(SnowAndFreezeFeature.CODEC, FeatureTooltipUtils::getFreezeTopLayerTooltip);
        registry.registerFeatureTooltip(GeodeFeature.CODEC, FeatureTooltipUtils::getGeodeTooltip);
        registry.registerFeatureTooltip(HugeBrownMushroomFeature.CODEC, FeatureTooltipUtils::getHugeBrownMushroomTooltip);
        registry.registerFeatureTooltip(HugeFungusFeature.CODEC, FeatureTooltipUtils::getHugeFungusTooltip);
        registry.registerFeatureTooltip(HugeRedMushroomFeature.CODEC, FeatureTooltipUtils::getHugeRedMushroomTooltip);
        registry.registerFeatureTooltip(IcebergFeature.CODEC, FeatureTooltipUtils::getIcebergTooltip);
        registry.registerFeatureTooltip(LakeFeature.CODEC, FeatureTooltipUtils::getLakeTooltip);
        registry.registerFeatureTooltip(LargeDripstoneFeature.CODEC, FeatureTooltipUtils::getLargeDripstoneTooltip);
        registry.registerFeatureTooltip(MonsterRoomFeature.CODEC, FeatureTooltipUtils::getMonsterRoomTooltip);
        registry.registerFeatureTooltip(MultifaceGrowthFeature.CODEC, FeatureTooltipUtils::getMultifaceGrowthTooltip);
        registry.registerFeatureTooltip(ReplaceBlobsFeature.CODEC, FeatureTooltipUtils::getNetherrackReplaceBlobsTooltip);
        registry.registerFeatureTooltip(NoOpFeature.CODEC, FeatureTooltipUtils::getNoOpTooltip);
        registry.registerFeatureTooltip(OreFeature.CODEC, FeatureTooltipUtils::getOreTooltip);
        registry.registerFeatureTooltip(OverlayFeature.CODEC, FeatureTooltipUtils::getOverlayTooltip);
        registry.registerFeatureTooltip(ProjectedRandomPatchySquare.CODEC, FeatureTooltipUtils::getProjectedRandomPatchySquareTooltip);
        registry.registerFeatureTooltip(RandomBooleanSelectorFeature.CODEC, FeatureTooltipUtils::getRandomBooleanSelectorTooltip);
        registry.registerFeatureTooltip(RandomNeighborSpreadFeature.CODEC, FeatureTooltipUtils::getRandomNeighborSpreadTooltip);
        registry.registerFeatureTooltip(RandomSelectorFeature.CODEC, FeatureTooltipUtils::getRandomSelectorTooltip);
        registry.registerFeatureTooltip(ReplaceBlockFeature.CODEC, FeatureTooltipUtils::getReplaceSingleBlockTooltip);
        registry.registerFeatureTooltip(RootSystemFeature.CODEC, FeatureTooltipUtils::getRootSystemTooltip);
        registry.registerFeatureTooltip(ScatteredOreFeature.CODEC, FeatureTooltipUtils::getScatteredOreTooltip);
        registry.registerFeatureTooltip(SculkPatchFeature.CODEC, FeatureTooltipUtils::getSculkPatchTooltip);
        registry.registerFeatureTooltip(SequenceFeature.CODEC, FeatureTooltipUtils::getSequenceTooltip);
        registry.registerFeatureTooltip(SimpleBlockFeature.CODEC, FeatureTooltipUtils::getSimpleBlockTooltip);
        registry.registerFeatureTooltip(SimpleRandomSelectorFeature.CODEC, FeatureTooltipUtils::getSimpleRandomSelectorTooltip);
        registry.registerFeatureTooltip(SingleBlockPillarFeature.CODEC, FeatureTooltipUtils::getSingleBlockPillarTooltip);
        registry.registerFeatureTooltip(SpeleothemFeature.CODEC, FeatureTooltipUtils::getSpeleothemTooltip);
        registry.registerFeatureTooltip(SpeleothemClusterFeature.CODEC, FeatureTooltipUtils::getSpeleothemClusterTooltip);
        registry.registerFeatureTooltip(SpikeFeature.CODEC, FeatureTooltipUtils::getSpikeTooltip);
        registry.registerFeatureTooltip(SpringFeature.CODEC, FeatureTooltipUtils::getSpringTooltip);
        registry.registerFeatureTooltip(SteppedColumnClusterFeature.CODEC, FeatureTooltipUtils::getSteppedColumnClusterTooltip);
        registry.registerFeatureTooltip(TemplateFeature.CODEC, FeatureTooltipUtils::getTemplateTooltip);
        registry.registerFeatureTooltip(TreeFeature.CODEC, FeatureTooltipUtils::getTreeTooltip);
        registry.registerFeatureTooltip(UnderwaterMagmaFeature.CODEC, FeatureTooltipUtils::getUnderwaterMagmaTooltip);
        registry.registerFeatureTooltip(VegetationPatchFeature.CODEC, FeatureTooltipUtils::getVegetationPatchTooltip);
        registry.registerFeatureTooltip(VinesFeature.CODEC, FeatureTooltipUtils::getVinesTooltip);
        registry.registerFeatureTooltip(VoidStartPlatformFeature.CODEC, FeatureTooltipUtils::getVoidStartPlatformTooltip);
        registry.registerFeatureTooltip(WaterloggedVegetationPatchFeature.CODEC, FeatureTooltipUtils::getWaterloggedVegetationPatchTooltip);
        registry.registerFeatureTooltip(WeightedRandomSelectorFeature.CODEC, FeatureTooltipUtils::getWeightedRandomSelectorTooltip);

        registry.registerIntProviderTooltip(ConstantInt.class, IntProviderTooltipUtils::getConstantIntTooltip);
        registry.registerIntProviderTooltip(UniformInt.class, IntProviderTooltipUtils::getUniformIntTooltip);
        registry.registerIntProviderTooltip(BiasedToBottomInt.class, IntProviderTooltipUtils::getBiasedToBottomIntTooltip);
        registry.registerIntProviderTooltip(ClampedInt.class, IntProviderTooltipUtils::getClampedIntTooltip);
        registry.registerIntProviderTooltip(WeightedListInt.class, IntProviderTooltipUtils::getWeightedListIntTooltip);
        registry.registerIntProviderTooltip(ClampedNormalInt.class, IntProviderTooltipUtils::getClampedNormalIntTooltip);
        registry.registerIntProviderTooltip(TrapezoidInt.class, IntProviderTooltipUtils::getTrapezoidIntTooltip);

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
        registry.registerRuleTestTooltip(AllOfRuleTest.class, RuleTestTooltipUtils::getAllOfRuleTestTooltip);
        registry.registerRuleTestTooltip(AnyOfRuleTest.class, RuleTestTooltipUtils::getAnyOfRuleTestTooltip);
        registry.registerRuleTestTooltip(NotRuleTest.class, RuleTestTooltipUtils::getNotRuleTestTooltip);
        registry.registerRuleTestTooltip(HeightMatchTest.class, RuleTestTooltipUtils::getHeightMatchTestTooltip);
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
        registry.registerBlockPredicateTooltip(UnobstructedPredicate.class, BlockPredicateTooltipUtils::getUnobstructedPredicateTooltip);
        registry.registerBlockPredicateTooltip(MatchingBiomesPredicate.class, BlockPredicateTooltipUtils::getMatchingBiomesPredicateTooltip);
        registry.registerBlockPredicateTooltip(HeightRangePredicate.class, BlockPredicateTooltipUtils::getHeightRangePredicateTooltip);
        registry.registerBlockPredicateTooltip(VolumeMatchPredicate.class, BlockPredicateTooltipUtils::getVolumeMatchPredicateTooltip);

        registry.registerPlacementModifierTooltip(BiomeFilter.class, PlacementModifierTooltipUtils::getBiomeFilterTooltip);
        registry.registerPlacementModifierTooltip(BlockPredicateFilter.class, PlacementModifierTooltipUtils::getBlockPredicateFilterTooltip);
        registry.registerPlacementModifierTooltip(CountOnEveryLayerPlacement.class, PlacementModifierTooltipUtils::getCountOnEveryLayerPlacementTooltip);
        registry.registerPlacementModifierTooltip(CountPlacement.class, PlacementModifierTooltipUtils::getCountPlacementTooltip);
        registry.registerPlacementModifierTooltip(EnvironmentScanPlacement.class, PlacementModifierTooltipUtils::getEnvironmentScanPlacementTooltip);
        registry.registerPlacementModifierTooltip(HeightmapPlacement.class, PlacementModifierTooltipUtils::getHeightmapPlacementTooltip);
        registry.registerPlacementModifierTooltip(HeightRangePlacement.class, PlacementModifierTooltipUtils::getHeightRangePlacementTooltip);
        registry.registerPlacementModifierTooltip(InSquarePlacement.class, PlacementModifierTooltipUtils::getInSquarePlacementTooltip);
        registry.registerPlacementModifierTooltip(NoiseBasedCountPlacement.class, PlacementModifierTooltipUtils::getNoiseBasedCountPlacementTooltip);
        registry.registerPlacementModifierTooltip(NoiseThresholdCountPlacement.class, PlacementModifierTooltipUtils::getNoiseThresholdCountPlacementTooltip);
        registry.registerPlacementModifierTooltip(RarityFilter.class, PlacementModifierTooltipUtils::getRarityFilterTooltip);
        registry.registerPlacementModifierTooltip(OffsetPlacement.class, PlacementModifierTooltipUtils::getOffsetPlacementTooltip);
        registry.registerPlacementModifierTooltip(SurfaceRelativeThresholdFilter.class, PlacementModifierTooltipUtils::getSurfaceRelativeThresholdFilterTooltip);
        registry.registerPlacementModifierTooltip(SurfaceWaterDepthFilter.class, PlacementModifierTooltipUtils::getSurfaceWaterDepthFilterTooltip);
        registry.registerPlacementModifierTooltip(FixedPlacement.class, PlacementModifierTooltipUtils::getFixedPlacementTooltip);
        registry.registerPlacementModifierTooltip(CuboidPlacement.class, PlacementModifierTooltipUtils::getCuboidPlacementTooltip);
        registry.registerPlacementModifierTooltip(RandomChancePlacement.class, PlacementModifierTooltipUtils::getRandomChancePlacementTooltip);
        registry.registerPlacementModifierTooltip(RandomlySelectedPlacement.class, PlacementModifierTooltipUtils::getRandomlySelectedPlacementTooltip);

        registry.registerIntSpanPropagator(ConstantInt.class, IntSpanPropagatorUtils::getConstantInt);
        registry.registerIntSpanPropagator(UniformInt.class, IntSpanPropagatorUtils::getUniformInt);
        registry.registerIntSpanPropagator(BiasedToBottomInt.class, IntSpanPropagatorUtils::getBiasedToBottomInt);
        registry.registerIntSpanPropagator(ClampedInt.class, IntSpanPropagatorUtils::getClampedInt);
        registry.registerIntSpanPropagator(ClampedNormalInt.class, IntSpanPropagatorUtils::getClampedNormalInt);
        registry.registerIntSpanPropagator(WeightedListInt.class, IntSpanPropagatorUtils::getWeightedListInt);
        registry.registerIntSpanPropagator(TrapezoidInt.class, IntSpanPropagatorUtils::getTrapezoidInt);

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
        registry.registerPlacementPropagator(FixedPlacement.class, PlacementPropagatorUtils::getFixedPlacement);
        registry.registerPlacementPropagator(CuboidPlacement.class, PlacementPropagatorUtils::getCuboidPlacement);
        registry.registerPlacementPropagator(RandomChancePlacement.class, PlacementPropagatorUtils::getRandomChancePlacement);
        registry.registerPlacementPropagator(RandomlySelectedPlacement.class, PlacementPropagatorUtils::getRandomlySelectedPlacement);

        registry.registerFeatureBlockCollector(BambooFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(BlockBlobFeature.CODEC, FeatureCollectorUtils::collectBlockBlob);
        registry.registerFeatureBlockCollector(BlockColumnFeature.CODEC, FeatureCollectorUtils::collectBlockColumn);
        registry.registerFeatureBlockCollector(BlockPileFeature.CODEC, FeatureCollectorUtils::collectBlockPile);
        registry.registerFeatureBlockCollector(BlueIceFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(BonusChestFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(ChorusPlantFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(CoralClawFeature.CODEC, FeatureCollectorUtils::collectCoralClaw);
        registry.registerFeatureBlockCollector(CoralTreeFeature.CODEC, FeatureCollectorUtils::collectCoralTree);
        registry.registerFeatureBlockCollector(DeltaFeature.CODEC, FeatureCollectorUtils::collectDelta);
        registry.registerFeatureBlockCollector(DiskFeature.CODEC, FeatureCollectorUtils::collectDisk);
        registry.registerFeatureBlockCollector(EndGatewayFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(EndIslandFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(EndPlatformFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(EndPodiumFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(EndSpikeFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(FallenTreeFeature.CODEC, FeatureCollectorUtils::collectFallenTree);
        registry.registerFeatureBlockCollector(FillLayerFeature.CODEC, FeatureCollectorUtils::collectFillLayer);
        registry.registerFeatureBlockCollector(FossilFeature.CODEC, FeatureCollectorUtils::collectFossil);
        registry.registerFeatureBlockCollector(SnowAndFreezeFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(GeodeFeature.CODEC, FeatureCollectorUtils::collectGeode);
        registry.registerFeatureBlockCollector(HugeBrownMushroomFeature.CODEC, FeatureCollectorUtils::collectHugeBrownMushroom);
        registry.registerFeatureBlockCollector(HugeFungusFeature.CODEC, FeatureCollectorUtils::collectHugeFungus);
        registry.registerFeatureBlockCollector(HugeRedMushroomFeature.CODEC, FeatureCollectorUtils::collectHugeRedMushroom);
        registry.registerFeatureBlockCollector(IcebergFeature.CODEC, FeatureCollectorUtils::collectIceberg);
        registry.registerFeatureBlockCollector(LakeFeature.CODEC, FeatureCollectorUtils::collectLake);
        registry.registerFeatureBlockCollector(LargeDripstoneFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(MonsterRoomFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(MultifaceGrowthFeature.CODEC, FeatureCollectorUtils::collectMultifaceGrowth);
        registry.registerFeatureBlockCollector(ReplaceBlobsFeature.CODEC, FeatureCollectorUtils::collectNetherrackReplaceBlobs);
        registry.registerFeatureBlockCollector(NoOpFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(OreFeature.CODEC, FeatureCollectorUtils::collectOre);
        registry.registerFeatureBlockCollector(OverlayFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(ProjectedRandomPatchySquare.CODEC, FeatureCollectorUtils::collectProjectedRandomPatchySquare);
        registry.registerFeatureBlockCollector(RandomBooleanSelectorFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(RandomNeighborSpreadFeature.CODEC, FeatureCollectorUtils::collectRandomNeighborSpread);
        registry.registerFeatureBlockCollector(RandomSelectorFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(ReplaceBlockFeature.CODEC, FeatureCollectorUtils::collectReplaceSingleBlock);
        registry.registerFeatureBlockCollector(RootSystemFeature.CODEC, FeatureCollectorUtils::collectRootSystem);
        registry.registerFeatureBlockCollector(ScatteredOreFeature.CODEC, FeatureCollectorUtils::collectScatteredOre);
        registry.registerFeatureBlockCollector(SculkPatchFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(SequenceFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(SimpleBlockFeature.CODEC, FeatureCollectorUtils::collectSimpleBlock);
        registry.registerFeatureBlockCollector(SimpleRandomSelectorFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(SingleBlockPillarFeature.CODEC, FeatureCollectorUtils::collectSingleBlockPillar);
        registry.registerFeatureBlockCollector(SpeleothemFeature.CODEC, FeatureCollectorUtils::collectSpeleothem);
        registry.registerFeatureBlockCollector(SpeleothemClusterFeature.CODEC, FeatureCollectorUtils::collectSpeleothemCluster);
        registry.registerFeatureBlockCollector(SpikeFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(SpringFeature.CODEC, FeatureCollectorUtils::collectSpring);
        registry.registerFeatureBlockCollector(SteppedColumnClusterFeature.CODEC, FeatureCollectorUtils::collectSteppedColumnCluster);
        registry.registerFeatureBlockCollector(TemplateFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(TreeFeature.CODEC, FeatureCollectorUtils::collectTree);
        registry.registerFeatureBlockCollector(UnderwaterMagmaFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(VegetationPatchFeature.CODEC, FeatureCollectorUtils::collectVegetationPatch);
        registry.registerFeatureBlockCollector(VinesFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(VoidStartPlatformFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);
        registry.registerFeatureBlockCollector(WaterloggedVegetationPatchFeature.CODEC, FeatureCollectorUtils::collectWaterloggedVegetationPatch);
        registry.registerFeatureBlockCollector(WeightedRandomSelectorFeature.CODEC, FeatureCollectorUtils::collectSubFeatures);

        registry.registerStateProviderBlockCollector(SimpleStateProvider.class, BlockStateProviderCollectorUtils::collectSimple);
        registry.registerStateProviderBlockCollector(NoiseProvider.class, BlockStateProviderCollectorUtils::collectNoise);
        registry.registerStateProviderBlockCollector(DualNoiseProvider.class, BlockStateProviderCollectorUtils::collectDualNoise);
        registry.registerStateProviderBlockCollector(NoiseThresholdProvider.class, BlockStateProviderCollectorUtils::collectNoiseThreshold);
        registry.registerStateProviderBlockCollector(RandomizedIntStateProvider.class, BlockStateProviderCollectorUtils::collectRandomized);
        registry.registerStateProviderBlockCollector(RotatedBlockProvider.class, BlockStateProviderCollectorUtils::collectRotated);
        registry.registerStateProviderBlockCollector(WeightedStateProvider.class, BlockStateProviderCollectorUtils::collectWeighted);
        registry.registerStateProviderBlockCollector(RuleBasedStateProvider.class, BlockStateProviderCollectorUtils::collectRuleBased);

        registry.registerTreeDecoratorBlockCollector(TrunkVineDecorator.class, TreeDecoratorCollectorUtils::collectTrunkVine);
        registry.registerTreeDecoratorBlockCollector(LeaveVineDecorator.class, TreeDecoratorCollectorUtils::collectLeaveVine);
        registry.registerTreeDecoratorBlockCollector(PaleMossDecorator.class, TreeDecoratorCollectorUtils::collectLPaleMoss);
        registry.registerTreeDecoratorBlockCollector(CreakingHeartDecorator.class, TreeDecoratorCollectorUtils::collectCreakingHeart);
        registry.registerTreeDecoratorBlockCollector(CocoaDecorator.class, TreeDecoratorCollectorUtils::collectCocoa);
        registry.registerTreeDecoratorBlockCollector(BeehiveDecorator.class, TreeDecoratorCollectorUtils::collectBeehive);
        registry.registerTreeDecoratorBlockCollector(AlterGroundDecorator.class, TreeDecoratorCollectorUtils::collectAlterGround);
        registry.registerTreeDecoratorBlockCollector(AttachedToLeavesDecorator.class, TreeDecoratorCollectorUtils::collectAttachedToLeaves);
        registry.registerTreeDecoratorBlockCollector(PlaceOnGroundDecorator.class, TreeDecoratorCollectorUtils::collectPlaceOnGround);
        registry.registerTreeDecoratorBlockCollector(AttachedToLogsDecorator.class, TreeDecoratorCollectorUtils::collectAttachedToLogs);
        registry.registerTreeDecoratorBlockCollector(ShelfMushroomDecorator.class, TreeDecoratorCollectorUtils::collectShelfMushroom);

        registry.registerRootPlacerBlockCollector(MangroveRootPlacer.class, RootPlacerCollectorUtils::collectMangrove);

        registry.registerBlockStateProviderTooltip(SimpleStateProvider.class, BlockStateProviderTooltipUtils::getSimpleStateProviderTooltip);
        registry.registerBlockStateProviderTooltip(WeightedStateProvider.class, BlockStateProviderTooltipUtils::getWeightedStateProviderTooltip);
        registry.registerBlockStateProviderTooltip(NoiseThresholdProvider.class, BlockStateProviderTooltipUtils::getNoiseThresholdProviderTooltip);
        registry.registerBlockStateProviderTooltip(NoiseProvider.class, BlockStateProviderTooltipUtils::getNoiseProviderTooltip);
        registry.registerBlockStateProviderTooltip(DualNoiseProvider.class, BlockStateProviderTooltipUtils::getDualNoiseProviderTooltip);
        registry.registerBlockStateProviderTooltip(RotatedBlockProvider.class, BlockStateProviderTooltipUtils::getRotatedBlockProviderTooltip);
        registry.registerBlockStateProviderTooltip(RandomizedIntStateProvider.class, BlockStateProviderTooltipUtils::getRandomizedIntStateProviderTooltip);
        registry.registerBlockStateProviderTooltip(RuleBasedStateProvider.class, BlockStateProviderTooltipUtils::getRuleBasedStateProviderTooltip);

        registry.registerTrunkPlacerTooltip(StraightTrunkPlacer.class, TrunkPlacerTooltipUtils::getStraightTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(ForkingTrunkPlacer.class, TrunkPlacerTooltipUtils::getForkingTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(GiantTrunkPlacer.class, TrunkPlacerTooltipUtils::getGiantTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(MegaJungleTrunkPlacer.class, TrunkPlacerTooltipUtils::getMegaJungleTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(DarkOakTrunkPlacer.class, TrunkPlacerTooltipUtils::getDarkOakTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(FancyTrunkPlacer.class, TrunkPlacerTooltipUtils::getFancyTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(BendingTrunkPlacer.class, TrunkPlacerTooltipUtils::getBendingTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(UpwardsBranchingTrunkPlacer.class, TrunkPlacerTooltipUtils::getUpwardBranchingTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(CherryTrunkPlacer.class, TrunkPlacerTooltipUtils::getCherryTrunkPlacerTooltip);
        registry.registerTrunkPlacerTooltip(PoplarTrunkPlacer.class, TrunkPlacerTooltipUtils::getPoplarTrunkPlacerTooltip);

        registry.registerTreeDecoratorTooltip(TrunkVineDecorator.class, TreeDecoratorTooltipUtils::getTrunkVineDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(LeaveVineDecorator.class, TreeDecoratorTooltipUtils::getLeaveVineDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(PaleMossDecorator.class, TreeDecoratorTooltipUtils::getPaleMossDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(CreakingHeartDecorator.class, TreeDecoratorTooltipUtils::getCreakingHeartDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(CocoaDecorator.class, TreeDecoratorTooltipUtils::getCocoaDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(BeehiveDecorator.class, TreeDecoratorTooltipUtils::getBeehiveDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(AlterGroundDecorator.class, TreeDecoratorTooltipUtils::getAlterGroundDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(AttachedToLeavesDecorator.class, TreeDecoratorTooltipUtils::getAttachedToLeavesDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(PlaceOnGroundDecorator.class, TreeDecoratorTooltipUtils::getPlaceOnGroundDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(AttachedToLogsDecorator.class, TreeDecoratorTooltipUtils::getAttachedToLogsDecoratorTooltip);
        registry.registerTreeDecoratorTooltip(ShelfMushroomDecorator.class, TreeDecoratorTooltipUtils::getShelfMushroomDecoratorTooltip);

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
        registry.registerFoliagePlacerTooltip(PoplarFoliagePlacer.class, FoliagePlacerTooltipUtils::getPoplarFoliagePlacerTooltip);

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
