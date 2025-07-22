package com.yanny.ali.plugin.lootjs;

import com.almostreliable.lootjs.core.*;
import com.almostreliable.lootjs.loot.condition.*;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinAbstractLootModification;
import com.yanny.ali.mixin.MixinLootModificationsAPI;
import com.yanny.ali.plugin.lootjs.modifier.CustomPlayerFunction;
import com.yanny.ali.plugin.lootjs.modifier.ModifiedItemFunction;
import com.yanny.ali.plugin.lootjs.node.*;
import com.yanny.ali.plugin.lootjs.server.LootJsConditionTooltipUtils;
import com.yanny.ali.plugin.lootjs.server.LootJsFunctionTooltipUtils;
import com.yanny.ali.plugin.lootjs.widget.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@AliEntrypoint
public class LootJsPlugin implements IPlugin {
    public static final String ID = "lootjs";

    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static List<ILootModifier<?>> registerModifiers(IServerUtils utils) {
        List<ILootModifier<?>> modifiers = new ArrayList<>();
        List<ILootAction> actions = MixinLootModificationsAPI.getActions();

        for (ILootAction action : actions) {
            if (action instanceof AbstractLootModification lootModification) {
                try {
                    if (lootModification instanceof LootModificationByBlock byBlock) {
                        modifiers.add(new BlockLootModifier(utils, byBlock));
                    } else if (lootModification instanceof LootModificationByEntity byEntity) {
                        modifiers.add(new EntityLootModifier(utils, byEntity));
                    } else if (lootModification instanceof LootModificationByTable byTable) {
                        modifiers.add(new TableLootModifier(utils, byTable));
                    } else if (lootModification instanceof LootModificationByType byType) {
                        modifiers.add(new TypeLootModifier(utils, byType));
                    } else {
                        LOGGER.error("Skipping unexpected modification type {}", lootModification.getClass().getCanonicalName());
                    }
                } catch (Throwable e) {
                    LOGGER.error("Failed to process loot modification {}: {}", ((MixinAbstractLootModification) lootModification).getName(), e.getMessage());
                }
            } else {
                LOGGER.error("Unexpected action type {}", action.getClass().getCanonicalName());
            }
        }

        return modifiers;
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerNode(AddLootNode.ID, AddLootNode::new);
        registry.registerNode(WeightedAddLootNode.ID, WeightedAddLootNode::new);
        registry.registerNode(ItemStackNode.ID, ItemStackNode::new);
        registry.registerNode(ItemTagNode.ID, ItemTagNode::new);
        registry.registerNode(GroupLootNode.ID, GroupLootNode::new);
        registry.registerNode(ModifiedNode.ID, ModifiedNode::new);

        registry.registerWidget(AddLootNode.ID, AddLootWidget::new);
        registry.registerWidget(WeightedAddLootNode.ID, WeightedAddLootWidget::new);
        registry.registerWidget(ItemStackNode.ID, ItemStackWidget::new);
        registry.registerWidget(ItemTagNode.ID, ItemTagWidget::new);
        registry.registerWidget(GroupLootNode.ID, GroupedLootWidget::new);
        registry.registerWidget(ModifiedNode.ID, ModifiedWidget::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(AndCondition.class, LootJsConditionTooltipUtils::andConditionTooltip);
        registry.registerConditionTooltip(AnyBiomeCheck.class, LootJsConditionTooltipUtils::anyBiomeCheckTooltip);
        registry.registerConditionTooltip(AnyDimension.class, LootJsConditionTooltipUtils::anyDimensionTooltip);
        registry.registerConditionTooltip(AnyStructure.class, LootJsConditionTooltipUtils::anyStructureTooltip);
        registry.registerConditionTooltip(BiomeCheck.class, LootJsConditionTooltipUtils::biomeCheckTooltip);
        registry.registerConditionTooltip(ContainsLootCondition.class, LootJsConditionTooltipUtils::containsLootConditionTooltip);
        registry.registerConditionTooltip(CustomParamPredicate.class, LootJsConditionTooltipUtils::customParamPredicateTooltip);
        registry.registerConditionTooltip(IsLightLevel.class, LootJsConditionTooltipUtils::isLightLevelTooltip);
        registry.registerConditionTooltip(LootItemConditionWrapper.class, LootJsConditionTooltipUtils::lootItemConditionWrapperTooltip);
        registry.registerConditionTooltip(MainHandTableBonus.class, LootJsConditionTooltipUtils::mainHandTableBonusTooltip);
        registry.registerConditionTooltip(MatchEquipmentSlot.class, LootJsConditionTooltipUtils::getMatchEquipmentSlotTooltip);
        registry.registerConditionTooltip(MatchKillerDistance.class, LootJsConditionTooltipUtils::matchKillerDistanceTooltip);
        registry.registerConditionTooltip(MatchPlayer.class, LootJsConditionTooltipUtils::matchPlayerTooltip);
        registry.registerConditionTooltip(NotCondition.class, LootJsConditionTooltipUtils::notConditionTooltip);
        registry.registerConditionTooltip(OrCondition.class, LootJsConditionTooltipUtils::orConditionTooltip);
        registry.registerConditionTooltip(PlayerParamPredicate.class, LootJsConditionTooltipUtils::playerParamPredicateTooltip);
        registry.registerConditionTooltip(WrappedDamageSourceCondition.class, LootJsConditionTooltipUtils::wrapperDamageSourceConditionTooltip);

        registry.registerFunctionTooltip(CustomPlayerFunction.class, LootJsFunctionTooltipUtils::customPlayerTooltip);
        registry.registerFunctionTooltip(ModifiedItemFunction.class, LootJsFunctionTooltipUtils::modifiedItemTooltip);

        registry.registerLootModifiers(LootJsPlugin::registerModifiers);
    }
}
