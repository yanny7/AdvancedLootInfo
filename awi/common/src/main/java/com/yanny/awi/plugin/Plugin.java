package com.yanny.awi.plugin;

import com.yanny.aci.tooltip.CommonValueTooltip;
import com.yanny.awi.api.*;
import com.yanny.awi.datagen.LanguageHolder;
import com.yanny.awi.plugin.client.widget.*;
import com.yanny.awi.plugin.common.nodes.*;
import com.yanny.awi.plugin.server.*;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
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
        registry.registerValueTooltip(OreConfiguration.TargetBlockState.class, ValueTooltipUtils::getTargetBlockStateTooltip);
        registry.registerValueTooltip(BlockState.class, ValueTooltipUtils::getBlockStateTooltip);
        registry.registerValueTooltip(NodeUtils.BlockInfo.class, ValueTooltipUtils::getBlockInfoTooltip);

        registry.registerValueTooltip(Block.class, RegistriesTooltipUtils::getBlockTooltip);
        registry.registerValueTooltip(Fluid.class, RegistriesTooltipUtils::getFluidTooltip);

        registry.registerFeatureTooltip(CountConfiguration.class, FeatureConfigurationTooltipUtils::getCountConfigurationTooltip);
        registry.registerFeatureTooltip(OreConfiguration.class, FeatureConfigurationTooltipUtils::getOreConfigurationTooltip);

        registry.registerIntProviderTooltip(ConstantInt.class, IntProviderTooltipUtils::getConstantIntTooltip);
        registry.registerIntProviderTooltip(UniformInt.class, IntProviderTooltipUtils::getUniformIntTooltip);

        registry.registerRuleTestTooltip(AlwaysTrueTest.class, RuleTestTooltipUtils::getAlwaysTrueTooltip);

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
