package com.yanny.alicompat.compat.artifacts;

import artifacts.loot.ArtifactRarityAdjustedChance;
import artifacts.loot.ConfigValueChance;
import artifacts.loot.IsAprilFools;
import artifacts.loot.ReplaceWithLootTableFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class ArtifactsCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return ArtifactsLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, ReplaceWithLootTableFunction.class, ReplaceWithLootTableFunctionAccessor.class);

        registry.registerConditionTooltip(ArtifactRarityAdjustedChance.class, ArtifactsCompat::getRarityAdjustedChanceTooltip);
        registry.registerConditionTooltip(IsAprilFools.class, ArtifactsCompat::getAprilFoolsTooltip);
        PluginUtils.registerConditionTooltip(registry, ConfigValueChance.class, ConfigValueChanceAccessor.class);

        PluginUtils.registerChanceModifier(registry, ConfigValueChance.class, ConfigValueChanceAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getRarityAdjustedChanceTooltip(IServerUtils utils, ArtifactRarityAdjustedChance cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.defaultProbability()).build(ArtifactsLang.Value.DEFAULT_PROBABILITY)),
                ArtifactsLang.Conditions.ARTIFACT_RARITY_ADJUSTED_CHANCE);
    }

    @NotNull
    private static TooltipBuilder getAprilFoolsTooltip(IServerUtils ignoredUtils, IsAprilFools ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ArtifactsLang.Conditions.IS_APRIL_FOOLS);
    }
}
