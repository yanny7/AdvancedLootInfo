package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
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
import twilightforest.advancements.predicate.ItemColorPredicate;
import twilightforest.loot.LootingEnchantNumberProvider;
import twilightforest.loot.MultiplayerBasedAdditionLootFunction;
import twilightforest.loot.MultiplayerBasedNumberProvider;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.conditions.IsMinionCondition;
import twilightforest.loot.conditions.ModExistsCondition;
import twilightforest.loot.conditions.UncraftingTableEnabledCondition;
import twilightforest.loot.modifiers.FieryToolSmeltingModifier;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

import java.util.List;

public class TwilightForestCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return TwilightForestLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(GiantPickUsedCondition.class, TwilightForestCompat::getGiantPickUsedTooltip);
        registry.registerConditionTooltip(IsMinionCondition.class, TwilightForestCompat::getIsMinionTooltip);
        PluginUtils.registerConditionTooltip(registry, ModExistsCondition.class, ModExistsConditionAccessor.class);
        registry.registerConditionTooltip(UncraftingTableEnabledCondition.class, TwilightForestCompat::getUncraftingTableEnabledTooltip);

        PluginUtils.registerFunctionTooltip(registry, MultiplayerBasedAdditionLootFunction.class, MultiplayerBasedAdditionAccessor.class);

        registry.registerNumberProvider(MultiplayerBasedNumberProvider.class, TwilightForestCompat::convertMultiplayerRolls);
        PluginUtils.registerNumberProvider(registry, LootingEnchantNumberProvider.class, LootingEnchantNumberProviderAccessor.class);

        registry.registerItemSubPredicateTooltip(ItemColorPredicate.class, TwilightForestCompat::getItemColorTooltip);

        registry.registerPageResolver(GiantPickUsedCondition.class, TwilightForestCompat::testGiantPickUsed);
        PluginUtils.registerPageResolver(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, FieryToolSmeltingModifier.class, FieryToolSmeltingModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, GiantToolGroupingModifier.class, GiantToolGroupingModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getGiantPickUsedTooltip(IServerUtils utils, GiantPickUsedCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.target())), TwilightForestLang.Conditions.GIANT_PICK_USED);
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
    private static TooltipBuilder getItemColorTooltip(IServerUtils utils, ItemColorPredicate predicate) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, predicate.color())), TwilightForestLang.ItemSubPredicates.ITEM_COLOR);
    }

    @NotNull
    private static Verdict testGiantPickUsed(IServerUtils ignoredUtils, GiantPickUsedCondition ignoredCond, LootPage page) {
        return GlobalLootModifierUtils.testBlocks(page, GiantToolGroupingModifier.CONVERSIONS::containsKey, false);
    }

    @NotNull
    private static NumberExpr convertMultiplayerRolls(IServerUtils utils, MultiplayerBasedNumberProvider provider, List<TooltipNode> conditions) {
        NumberExpr defaultRolls = utils.convertNumber(utils, provider.defaultRolls(), conditions);
        NumberExpr perPlayer = NumberExpr.max(NumberExpr.constant(0), utils.convertNumber(utils, provider.rollsPerPlayer(), conditions));

        return NumberExpr.add(defaultRolls, NumberExpr.mul(NumberExpr.atLeast(0), perPlayer));
    }
}
