package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.conditions.IsMinionCondition;
import twilightforest.loot.conditions.ModExistsCondition;
import twilightforest.loot.conditions.UncraftingTableEnabledCondition;
import twilightforest.loot.functions.ModItemSwap;
import twilightforest.loot.modifiers.FieryToolSmeltingModifier;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

public class TwilightForestCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return TwilightForestLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, GiantPickUsedCondition.class, GiantPickUsedConditionAccessor.class);
        PluginUtils.registerConditionTooltip(registry, ModExistsCondition.class, ModExistsConditionAccessor.class);
        registry.registerConditionTooltip(IsMinionCondition.class, TwilightForestCompat::getIsMinionTooltip);
        registry.registerConditionTooltip(UncraftingTableEnabledCondition.class, TwilightForestCompat::getUncraftingTableEnabledTooltip);

        registry.registerPageResolver(GiantPickUsedCondition.class, TwilightForestCompat::testGiantPickUsed);
        PluginUtils.registerPageResolver(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);

        PluginUtils.registerFunctionTooltip(registry, ModItemSwap.class, ModItemSwapAccessor.class);

        PluginUtils.registerItemStackModifier(registry, ModItemSwap.class, ModItemSwapAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GiantToolGroupingModifier.class, GiantToolGroupingModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getIsMinionTooltip(IServerUtils utils, IsMinionCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, !cond.inverse())), TwilightForestLang.Conditions.IS_MINION);
    }

    @NotNull
    private static TooltipBuilder getUncraftingTableEnabledTooltip(IServerUtils ignoredUtils, UncraftingTableEnabledCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, TwilightForestLang.Conditions.UNCRAFTING_TABLE_ENABLED);
    }

    @NotNull
    private static Verdict testGiantPickUsed(IServerUtils ignoredUtils, GiantPickUsedCondition ignoredCond, LootPage page) {
        return GlobalLootModifierUtils.testBlocks(page, GiantToolGroupingModifier.CONVERSIONS::containsKey, false);
    }
}
