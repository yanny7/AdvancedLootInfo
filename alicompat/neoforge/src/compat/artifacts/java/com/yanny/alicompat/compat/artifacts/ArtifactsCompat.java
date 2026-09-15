package com.yanny.alicompat.compat.artifacts;

import artifacts.config.value.Value;
import artifacts.item.consumeeffects.HealConsumeEffect;
import artifacts.loot.ArtifactRarityAdjustedChance;
import artifacts.loot.ConfigValueChance;
import artifacts.loot.ConfigValueCondition;
import artifacts.loot.ReplaceWithLootTableFunction;
import artifacts.neoforge.loot.ReplaceWithTableLootModifier;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class ArtifactsCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return ArtifactsLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, ReplaceWithLootTableFunction.class, ReplaceWithLootTableFunctionAccessor.class);

        registry.registerConditionTooltip(ArtifactRarityAdjustedChance.class, ArtifactsCompat::getRarityAdjustedChanceTooltip);
        PluginUtils.registerConditionTooltip(registry, ConfigValueChance.class, ConfigValueChanceAccessor.class);
        PluginUtils.registerConditionTooltip(registry, ConfigValueCondition.class, ConfigValueConditionAccessor.class);

        PluginUtils.registerChanceModifier(registry, ConfigValueChance.class, ConfigValueChanceAccessor.class);

        registry.registerConsumeEffectTooltip(HealConsumeEffect.class, ArtifactsCompat::getHealTooltip);

        registry.registerValueTooltip(Value.ConfigValue.class, ArtifactsCompat::getConfigValueTooltip);
        registry.registerValueTooltip(Value.Constant.class, ArtifactsCompat::getConstantValueTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, ReplaceWithTableLootModifier.class, ReplaceWithTableLootModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getConfigValueTooltip(IServerUtils utils, Value.ConfigValue<?> value) {
        return utils.getValueTooltip(utils, value.getId());
    }

    @NotNull
    private static TooltipBuilder getConstantValueTooltip(IServerUtils utils, Value.Constant<?> value) {
        return utils.getValueTooltip(utils, value.get());
    }

    @NotNull
    private static TooltipBuilder getRarityAdjustedChanceTooltip(IServerUtils utils, ArtifactRarityAdjustedChance cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.defaultProbability()).build(ArtifactsLang.Value.DEFAULT_PROBABILITY)),
                ArtifactsLang.Conditions.ARTIFACT_RARITY_ADJUSTED_CHANCE);
    }

    @NotNull
    private static TooltipBuilder getHealTooltip(IServerUtils utils, HealConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, effect.amount()).build(Lang.Value.AMOUNT)), ArtifactsLang.ConsumeEffects.HEAL);
    }
}
