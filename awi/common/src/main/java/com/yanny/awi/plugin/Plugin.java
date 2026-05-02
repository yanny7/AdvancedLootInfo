package com.yanny.awi.plugin;

import com.yanny.aci.tooltip.CommonValueTooltip;
import com.yanny.awi.api.*;
import com.yanny.awi.plugin.server.*;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

@AwiEntrypoint
public class Plugin implements IPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "awi";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        new CommonValueTooltip<ITooltipNode, IKeyTooltipNode, IServerUtils, IServerRegistry>().registerAll(registry);

        registry.registerValueTooltip(IntProvider.class, ValueTooltipUtils::getIntProviderTooltip);
        registry.registerValueTooltip(RuleTest.class, ValueTooltipUtils::getRuleTestTooltip);
        registry.registerValueTooltip(OreConfiguration.TargetBlockState.class, ValueTooltipUtils::getTargetBlockStateTooltip);
        registry.registerValueTooltip(BlockState.class, ValueTooltipUtils::getBlockStateTooltip);

        registry.registerValueTooltip(Block.class, RegistriesTooltipUtils::getBlockTooltip);

        registry.registerFeatureTooltip(CountConfiguration.class, FeatureConfigurationTooltipUtils::getCountConfigurationTooltip);
        registry.registerFeatureTooltip(OreConfiguration.class, FeatureConfigurationTooltipUtils::getOreConfigurationTooltip);

        registry.registerIntProviderTooltip(ConstantInt.class, IntProviderTooltipUtils::getConstantIntTooltip);
        registry.registerIntProviderTooltip(UniformInt.class, IntProviderTooltipUtils::getUniformIntTooltip);

        registry.registerRuleTestTooltip(AlwaysTrueTest.class, RuleTestTooltipUtils::getAlwaysTrueTooltip);
    }
}
