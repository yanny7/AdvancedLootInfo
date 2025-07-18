package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.loot.condition.*;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinLootModificationsAPI;
import com.yanny.ali.mixin.MixinLootModifier;
import com.yanny.ali.plugin.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.plugin.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.plugin.lootjs.node.ItemStackNode;
import com.yanny.ali.plugin.lootjs.node.ItemTagNode;
import com.yanny.ali.plugin.lootjs.node.ModifiedNode;
import com.yanny.ali.plugin.lootjs.server.LootJsConditionTooltipUtils;
import com.yanny.ali.plugin.lootjs.server.LootJsFunctionTooltipUtils;
import com.yanny.ali.plugin.lootjs.widget.ItemStackWidget;
import com.yanny.ali.plugin.lootjs.widget.ItemTagWidget;
import com.yanny.ali.plugin.lootjs.widget.ModifiedWidget;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@AliEntrypoint
public class LootJsPlugin implements IPlugin {
    public static final String ID = "lootjs";

    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static List<ILootModifier<?>> registerModifiers(IServerUtils utils) {
        List<ILootModifier<?>> modifiers = new ArrayList<>();
        List<LootModifier> actions = MixinLootModificationsAPI.getModifiers();

        for (LootModifier action : actions) {
            Predicate<LootContext> predicate = ((MixinLootModifier) action).getShouldRun();

            try {
                switch (predicate) {
                    case LootModifier.BlockFiltered blockFiltered ->
                            modifiers.add(new BlockLootModifier(utils, action, blockFiltered));
                    case LootModifier.EntityFiltered entityFiltered ->
                            modifiers.add(new EntityLootModifier(utils, action, entityFiltered));
                    case LootModifier.TableFiltered tableFiltered ->
                            modifiers.add(new TableLootModifier(utils, action, tableFiltered));
                    case LootModifier.TypeFiltered typeFiltered ->
                            modifiers.add(new TableLootModifier(utils, action, typeFiltered));
                    case null, default ->
                            LOGGER.error("Skipping unexpected modification type {}", actions.getClass().getCanonicalName());
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to process loot modification {}: {}", action.getName(), e.getMessage());
            }
        }

        return modifiers;
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerNode(ItemStackNode.ID, ItemStackNode::new);
        registry.registerNode(ItemTagNode.ID, ItemTagNode::new);
        registry.registerNode(ModifiedNode.ID, ModifiedNode::new);

        registry.registerWidget(ItemStackNode.ID, ItemStackWidget::new);
        registry.registerWidget(ItemTagNode.ID, ItemTagWidget::new);
        registry.registerWidget(ModifiedNode.ID, ModifiedWidget::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(MatchBiome.class, LootJsConditionTooltipUtils::matchBiomeTooltip);
        registry.registerConditionTooltip(MatchDimension.class, LootJsConditionTooltipUtils::matchDimensionTooltip);
        registry.registerConditionTooltip(CustomParamPredicate.class, LootJsConditionTooltipUtils::customParamPredicateTooltip);
        registry.registerConditionTooltip(IsLightLevel.class, LootJsConditionTooltipUtils::isLightLevelTooltip);
        registry.registerConditionTooltip(MatchEquipmentSlot.class, LootJsConditionTooltipUtils::getMatchEquipmentSlotTooltip);
        registry.registerConditionTooltip(MatchKillerDistance.class, LootJsConditionTooltipUtils::matchKillerDistanceTooltip);
        registry.registerConditionTooltip(MatchPlayer.class, LootJsConditionTooltipUtils::matchPlayerTooltip);
        registry.registerConditionTooltip(PlayerParamPredicate.class, LootJsConditionTooltipUtils::playerParamPredicateTooltip);
        registry.registerConditionTooltip(MatchAnyInventorySlot.class, LootJsConditionTooltipUtils::matchAnyInventorySlot);

        registry.registerFunctionTooltip(CustomPlayerFunction.class, LootJsFunctionTooltipUtils::customPlayerTooltip);
        registry.registerFunctionTooltip(ModifiedItemFunction.class, LootJsFunctionTooltipUtils::modifiedItemTooltip);

        registry.registerLootModifiers(LootJsPlugin::registerModifiers);
    }
}
