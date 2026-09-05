package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.conditions.IsMinionCondition;
import twilightforest.loot.conditions.ModExistsCondition;
import twilightforest.loot.conditions.UncraftingTableEnabledCondition;
import twilightforest.loot.functions.ModItemSwap;
import twilightforest.loot.modifiers.FieryToolSmeltingModifier;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

import java.util.Set;

public class TwilightForestCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return TwilightForestLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(GiantPickUsedCondition.class,
                (utils, condition) -> ReflectionUtils.copyClassData(GiantPickUsedConditionAccessor.class, condition, GiantPickUsedCondition.class).getTooltip(utils));
        registry.registerConditionTooltip(ModExistsCondition.class,
                (utils, condition) -> ReflectionUtils.copyClassData(ModExistsConditionAccessor.class, condition, ModExistsCondition.class).getTooltip(utils));
        registry.registerConditionTooltip(IsMinionCondition.class,
                (utils, condition) -> utils.getValueTooltip(utils, !condition.inverse()).key(TwilightForestLang.Conditions.IS_MINION));
        registry.registerConditionTooltip(UncraftingTableEnabledCondition.class,
                (ignoredUtils, ignoredCondition) -> TooltipBuilder.keyOnly(TwilightForestLang.Conditions.UNCRAFTING_TABLE_ENABLED));

        registry.registerDestination(GiantPickUsedCondition.class, (ignoredUtils, ignoredCondition) ->
                new Destination.Blocks(Set.copyOf(GiantToolGroupingModifier.CONVERSIONS.keySet()), false));

        registry.registerFunctionTooltip(ModItemSwap.class, (utils, function) -> accessor(function).getTooltip(utils));
        registry.registerItemStackModifier(ModItemSwap.class, (utils, function, itemStack) -> accessor(function).applyItemStackModifier(utils, itemStack));
        registry.registerItemCollector(ModItemSwap.class, (utils, items, function) -> accessor(function).collectItems(utils, items));
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GiantToolGroupingModifier.class, GiantToolGroupingModifierAccessor.class);
    }

    @NotNull
    private static ModItemSwapAccessor accessor(ModItemSwap function) {
        return ReflectionUtils.copyClassData(ModItemSwapAccessor.class, function, ModItemSwap.class);
    }
}
