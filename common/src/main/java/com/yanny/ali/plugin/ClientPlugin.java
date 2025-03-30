package com.yanny.ali.plugin;

import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.WidgetDirection;
import com.yanny.ali.plugin.condition.*;
import com.yanny.ali.plugin.entry.*;
import com.yanny.ali.plugin.function.*;
import com.yanny.ali.plugin.widget.*;

public class ClientPlugin {
    public static void initialize(IClientRegistry registry) {
        registry.registerWidget(ItemEntry.class, WidgetDirection.HORIZONTAL, ItemWidget::new, ItemWidget::getBounds);
        registry.registerWidget(EmptyEntry.class, WidgetDirection.HORIZONTAL, EmptyWidget::new, EmptyWidget::getBounds);
        registry.registerWidget(ReferenceEntry.class, WidgetDirection.VERTICAL, ReferenceWidget::new, ReferenceWidget::getBounds);
        registry.registerWidget(DynamicEntry.class, WidgetDirection.VERTICAL, DynamicWidget::new, DynamicWidget::getBounds);
        registry.registerWidget(TagEntry.class, WidgetDirection.HORIZONTAL, TagWidget::new, TagWidget::getBounds);
        registry.registerWidget(AlternativesEntry.class, WidgetDirection.VERTICAL, AlternativesWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(SequentialEntry.class, WidgetDirection.VERTICAL, SequentialWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(GroupEntry.class, WidgetDirection.VERTICAL, GroupWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(UnknownEntry.class, WidgetDirection.VERTICAL, UnknownWidget::new, UnknownWidget::getBounds);

        registry.registerConditionTooltip(AllOfAliCondition.class, ConditionTooltipUtils::getAllOfTooltip);
        registry.registerConditionTooltip(AnyOfAliCondition.class, ConditionTooltipUtils::getAnyOfTooltip);
        registry.registerConditionTooltip(BlockStatePropertyAliCondition.class, ConditionTooltipUtils::getBlockStatePropertyTooltip);
        registry.registerConditionTooltip(DamageSourcePropertiesAliCondition.class, ConditionTooltipUtils::getDamageSourcePropertiesTooltip);
        registry.registerConditionTooltip(EntityPropertiesAliCondition.class, ConditionTooltipUtils::getEntityPropertiesTooltip);
        registry.registerConditionTooltip(EntityScoresAliCondition.class, ConditionTooltipUtils::getEntityScoresTooltip);
        registry.registerConditionTooltip(InvertedAliCondition.class, ConditionTooltipUtils::getInvertedTooltip);
        registry.registerConditionTooltip(KilledByPlayerAliCondition.class, ConditionTooltipUtils::getKilledByPlayerTooltip);
        registry.registerConditionTooltip(LocationCheckAliCondition.class, ConditionTooltipUtils::getLocationCheckTooltip);
        registry.registerConditionTooltip(MatchToolAliCondition.class, ConditionTooltipUtils::getMatchToolTooltip);
        registry.registerConditionTooltip(RandomChanceAliCondition.class, ConditionTooltipUtils::getRandomChanceTooltip);
        registry.registerConditionTooltip(RandomChanceWithLootingAliCondition.class, ConditionTooltipUtils::getRandomChanceWithLootingTooltip);
        registry.registerConditionTooltip(ReferenceAliCondition.class, ConditionTooltipUtils::getReferenceTooltip);
        registry.registerConditionTooltip(SurvivesExplosionAliCondition.class, ConditionTooltipUtils::getSurvivesExplosionTooltip);
        registry.registerConditionTooltip(TableBonusAliCondition.class, ConditionTooltipUtils::getTableBonusTooltip);
        registry.registerConditionTooltip(TimeCheckAliCondition.class, ConditionTooltipUtils::getTimeCheckTooltip);
        registry.registerConditionTooltip(ValueCheckAliCondition.class, ConditionTooltipUtils::getValueCheckTooltip);
        registry.registerConditionTooltip(WeatherCheckAliCondition.class, ConditionTooltipUtils::getWeatherCheckTooltip);
        registry.registerConditionTooltip(UnknownAliCondition.class, ConditionTooltipUtils::getUnknownTooltip);

        registry.registerFunctionTooltip(ApplyBonusAliFunction.class, FunctionTooltipUtils::getApplyBonusTooltip);
        registry.registerFunctionTooltip(CopyNameAliFunction.class, FunctionTooltipUtils::getCopyNameTooltip);
        registry.registerFunctionTooltip(CopyNbtAliFunction.class, FunctionTooltipUtils::getCopyNbtTooltip);
        registry.registerFunctionTooltip(CopyStateAliFunction.class, FunctionTooltipUtils::getCopyStateTooltip);
        registry.registerFunctionTooltip(EnchantRandomlyAliFunction.class, FunctionTooltipUtils::getEnchantRandomlyTooltip);
        registry.registerFunctionTooltip(EnchantWithLevelsAliFunction.class, FunctionTooltipUtils::getEnchantWithLevelsTooltip);
        registry.registerFunctionTooltip(ExplorationMapAliFunction.class, FunctionTooltipUtils::getExplorationMapTooltip);
        registry.registerFunctionTooltip(ExplosionDecayAliFunction.class, FunctionTooltipUtils::getExplosionDecayTooltip);
        registry.registerFunctionTooltip(FillPlayerHeadAliFunction.class, FunctionTooltipUtils::getFillPlayerHeadTooltip);
        registry.registerFunctionTooltip(FurnaceSmeltAliFunction.class, FunctionTooltipUtils::getFurnaceSmeltTooltip);
        registry.registerFunctionTooltip(LimitCountAliFunction.class, FunctionTooltipUtils::getLimitCountTooltip);
        registry.registerFunctionTooltip(LootingEnchantAliFunction.class, FunctionTooltipUtils::getLootingEnchantTooltip);
        registry.registerFunctionTooltip(ReferenceAliFunction.class, FunctionTooltipUtils::getReferenceTooltip);
        registry.registerFunctionTooltip(SetAttributesAliFunction.class, FunctionTooltipUtils::getSetAttributesTooltip);
        registry.registerFunctionTooltip(SetBannerPatternAliFunction.class, FunctionTooltipUtils::getSetBannerPatternTooltip);
        registry.registerFunctionTooltip(SetContentsAliFunction.class, FunctionTooltipUtils::getSetContentsTooltip);
        registry.registerFunctionTooltip(SetCountAliFunction.class, FunctionTooltipUtils::getSetCountTooltip);
        registry.registerFunctionTooltip(SetDamageAliFunction.class, FunctionTooltipUtils::getSetDamageTooltip);
        registry.registerFunctionTooltip(SetEnchantmentsAliFunction.class, FunctionTooltipUtils::getSetEnchantmentsTooltip);
        registry.registerFunctionTooltip(SetInstrumentAliFunction.class, FunctionTooltipUtils::getSetInstrumentTooltip);
        registry.registerFunctionTooltip(SetLootTableAliFunction.class, FunctionTooltipUtils::getSetLootTableTooltip);
        registry.registerFunctionTooltip(SetLoreAliFunction.class, FunctionTooltipUtils::getSetLoreTooltip);
        registry.registerFunctionTooltip(SetNameAliFunction.class, FunctionTooltipUtils::getSetNameTooltip);
        registry.registerFunctionTooltip(SetNbtAliFunction.class, FunctionTooltipUtils::getSetNbtTooltip);
        registry.registerFunctionTooltip(SetPotionAliFunction.class, FunctionTooltipUtils::getSetPotionTooltip);
        registry.registerFunctionTooltip(SetStewEffectAliFunction.class, FunctionTooltipUtils::getSetStewEffectTooltip);
        registry.registerFunctionTooltip(UnknownAliFunction.class, FunctionTooltipUtils::getUnknownTooltip);
    }
}
