package com.yanny.ali.lootjs;

import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.condition.*;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.lootjs.mixin.MixinLootModificationsAPI;
import com.yanny.ali.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.lootjs.node.ItemStackNode;
import com.yanny.ali.lootjs.node.ItemTagNode;
import com.yanny.ali.lootjs.server.LootJsConditionTooltipUtils;
import com.yanny.ali.lootjs.server.LootJsFunctionTooltipUtils;
import com.yanny.ali.lootjs.server.LootJsGenericTooltipUtils;
import com.yanny.ali.lootjs.widget.ItemStackWidget;
import com.yanny.ali.lootjs.widget.ItemTagWidget;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@AliEntrypoint
public class LootJsPlugin implements IPlugin {
    public static final String ID = "lootjs";

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public String getModId() {
        return "lootjs";
    }

    @NotNull
    public static List<ILootModifier<?>> registerModifiers(IServerUtils utils) {
        List<ILootModifier<?>> modifiers = new ArrayList<>();
        List<LootModifier> actions = MixinLootModificationsAPI.getModifiers();

        for (LootModifier action : actions) {
            Predicate<LootContext> predicate = action.runPredicate();

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
                LOGGER.error("Failed to process loot modification {}: {}", action.name(), e.getMessage(), e);
            }
        }

        return modifiers;
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerDataNode(ItemStackNode.ID, ItemStackNode::new);
        registry.registerDataNode(ItemTagNode.ID, ItemTagNode::new);

        registry.registerWidget(ItemStackNode.ID, ItemStackWidget::new);
        registry.registerWidget(ItemTagNode.ID, ItemTagWidget::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(MatchBiome.class, LootJsConditionTooltipUtils::matchBiomeTooltip);
        registry.registerConditionTooltip(MatchDimension.class, LootJsConditionTooltipUtils::matchDimensionTooltip);
        registry.registerConditionTooltip(MatchStructure.class, LootJsConditionTooltipUtils::matchStructureTooltip);
        registry.registerConditionTooltip(CustomParamPredicate.class, LootJsConditionTooltipUtils::customParamPredicateTooltip);
        registry.registerConditionTooltip(IsLightLevel.class, LootJsConditionTooltipUtils::isLightLevelTooltip);
        registry.registerConditionTooltip(MatchEquipmentSlot.class, LootJsConditionTooltipUtils::getMatchEquipmentSlotTooltip);
        registry.registerConditionTooltip(MatchKillerDistance.class, LootJsConditionTooltipUtils::matchKillerDistanceTooltip);
        registry.registerConditionTooltip(MatchPlayer.class, LootJsConditionTooltipUtils::matchPlayerTooltip);
        registry.registerConditionTooltip(PlayerParamPredicate.class, LootJsConditionTooltipUtils::playerParamPredicateTooltip);
        registry.registerConditionTooltip(MatchAnyInventorySlot.class, LootJsConditionTooltipUtils::matchAnyInventorySlot);

        registry.registerFunctionTooltip(CustomPlayerFunction.class, LootJsFunctionTooltipUtils::customPlayerTooltip);
        registry.registerFunctionTooltip(ModifiedItemFunction.class, LootJsFunctionTooltipUtils::modifiedItemTooltip);

        registry.registerValueTooltip(ItemFilter.class, LootJsGenericTooltipUtils::getItemFilterTooltip);
        registry.registerValueTooltip(IdFilter.class, LootJsGenericTooltipUtils::getIdFilterTooltip);
        registry.registerValueTooltip(ItemAbility.class, LootJsGenericTooltipUtils::getItemAbilityTooltip);

        registry.registerLootModifiers(LootJsPlugin::registerModifiers);
    }
}
