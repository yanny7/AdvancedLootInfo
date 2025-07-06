package com.yanny.ali.plugin.kubejs;

import com.almostreliable.lootjs.core.*;
import com.almostreliable.lootjs.loot.condition.*;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinAbstractLootModification;
import com.yanny.ali.mixin.MixinLootModificationsAPI;
import com.yanny.ali.plugin.kubejs.modifier.CustomPlayerFunction;
import com.yanny.ali.plugin.kubejs.modifier.DropExperienceFunction;
import com.yanny.ali.plugin.kubejs.modifier.ExplodeFunction;
import com.yanny.ali.plugin.kubejs.modifier.LightningStrikeFunction;
import com.yanny.ali.plugin.kubejs.node.*;
import com.yanny.ali.plugin.kubejs.server.KubeJsConditionTooltipUtils;
import com.yanny.ali.plugin.kubejs.server.KubeJsFunctionTooltipUtils;
import com.yanny.ali.plugin.kubejs.widget.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@AliEntrypoint
public class KubeJsPlugin implements IPlugin {
    public static final String ID = "kubejs";

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
        registry.registerNode(LootEntryNode.ID, LootEntryNode::new);
        registry.registerNode(ItemStackNode.ID, ItemStackNode::new);
        registry.registerNode(GroupLootNode.ID, GroupLootNode::new);

        registry.registerWidget(AddLootNode.ID, WidgetDirection.VERTICAL, AddLootWidget::new, AddLootWidget::getBounds);
        registry.registerWidget(WeightedAddLootNode.ID, WidgetDirection.VERTICAL, WeightedAddLootWidget::new, WeightedAddLootWidget::getBounds);
        registry.registerWidget(LootEntryNode.ID, WidgetDirection.VERTICAL, LootEntryWidget::new, LootEntryWidget::getBounds);
        registry.registerWidget(ItemStackNode.ID, WidgetDirection.HORIZONTAL, ItemStackWidget::new, ItemStackWidget::getBounds);
        registry.registerWidget(GroupLootNode.ID, WidgetDirection.VERTICAL, GroupedLootWidget::new, GroupedLootWidget::getBounds);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(AndCondition.class, KubeJsConditionTooltipUtils::andConditionTooltip);
        registry.registerConditionTooltip(AnyBiomeCheck.class, KubeJsConditionTooltipUtils::anyBiomeCheckTooltip);
        registry.registerConditionTooltip(AnyDimension.class, KubeJsConditionTooltipUtils::anyDimensionTooltip);
        registry.registerConditionTooltip(BiomeCheck.class, KubeJsConditionTooltipUtils::biomeCheckTooltip);
        registry.registerConditionTooltip(ContainsLootCondition.class, KubeJsConditionTooltipUtils::containsLootConditionTooltip);
        registry.registerConditionTooltip(CustomParamPredicate.class, KubeJsConditionTooltipUtils::customParamPredicateTooltip);
        registry.registerConditionTooltip(IsLightLevel.class, KubeJsConditionTooltipUtils::isLightLevelTooltip);
        registry.registerConditionTooltip(LootItemConditionWrapper.class, KubeJsConditionTooltipUtils::lootItemConditionWrapperTooltip);
        registry.registerConditionTooltip(MainHandTableBonus.class, KubeJsConditionTooltipUtils::mainHandTableBonusTooltip);
        registry.registerConditionTooltip(MatchEquipmentSlot.class, KubeJsConditionTooltipUtils::getMatchEquipmentSlotTooltip);
        registry.registerConditionTooltip(MatchFluid.class, KubeJsConditionTooltipUtils::matchFluidTooltip);
        registry.registerConditionTooltip(MatchKillerDistance.class, KubeJsConditionTooltipUtils::matchKillerDistanceTooltip);
        registry.registerConditionTooltip(MatchPlayer.class, KubeJsConditionTooltipUtils::matchPlayerTooltip);
        registry.registerConditionTooltip(NotCondition.class, KubeJsConditionTooltipUtils::notConditionTooltip);
        registry.registerConditionTooltip(OrCondition.class, KubeJsConditionTooltipUtils::orConditionTooltip);
        registry.registerConditionTooltip(PlayerParamPredicate.class, KubeJsConditionTooltipUtils::playerParamPredicateTooltip);
        registry.registerConditionTooltip(WrappedDamageSourceCondition.class, KubeJsConditionTooltipUtils::wrapperDamageSourceConditionTooltip);

        registry.registerFunctionTooltip(CustomPlayerFunction.class, KubeJsFunctionTooltipUtils::customPlayerTooltip);
        registry.registerFunctionTooltip(DropExperienceFunction.class, KubeJsFunctionTooltipUtils::dropExperienceTooltip);
        registry.registerFunctionTooltip(ExplodeFunction.class, KubeJsFunctionTooltipUtils::explodeTooltip);
        registry.registerFunctionTooltip(LightningStrikeFunction.class, KubeJsFunctionTooltipUtils::lightningStrikeTooltip);

        registry.registerLootModifiers(KubeJsPlugin::registerModifiers);
    }
}
