package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.server.ConditionTooltipUtils;
import com.yanny.ali.plugin.server.FunctionTooltipUtils;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.*;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(ItemNode.ID, WidgetDirection.HORIZONTAL, ItemWidget::new, ItemWidget::getBounds);
        registry.registerWidget(EmptyNode.ID, WidgetDirection.HORIZONTAL, EmptyWidget::new, EmptyWidget::getBounds);
        registry.registerWidget(ReferenceNode.ID, WidgetDirection.VERTICAL, ReferenceWidget::new, ReferenceWidget::getBounds);
        registry.registerWidget(DynamicNode.ID, WidgetDirection.VERTICAL, DynamicWidget::new, DynamicWidget::getBounds);
        registry.registerWidget(TagNode.ID, WidgetDirection.HORIZONTAL, TagWidget::new, TagWidget::getBounds);
        registry.registerWidget(AlternativesNode.ID, WidgetDirection.VERTICAL, AlternativesWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(SequenceNode.ID, WidgetDirection.VERTICAL, SequentialWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(GroupNode.ID, WidgetDirection.VERTICAL, GroupWidget::new, CompositeWidget::getBounds);

        registry.registerNode(LootTableNode.ID, LootTableNode::new);
        registry.registerNode(LootPoolNode.ID, LootPoolNode::new);
        registry.registerNode(ItemNode.ID, ItemNode::new);
        registry.registerNode(TagNode.ID, TagNode::new);
        registry.registerNode(AlternativesNode.ID, AlternativesNode::new);
        registry.registerNode(SequenceNode.ID, SequenceNode::new);
        registry.registerNode(GroupNode.ID, GroupNode::new);
        registry.registerNode(EmptyNode.ID, EmptyNode::new);
        registry.registerNode(DynamicNode.ID, DynamicNode::new);
        registry.registerNode(ReferenceNode.ID, ReferenceNode::new);
        registry.registerNode(MissingNode.ID, MissingNode::new);
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemCollector(LootPoolEntries.ITEM, ItemCollectorUtils::collectItems);
        registry.registerItemCollector(LootPoolEntries.TAG, ItemCollectorUtils::collectTags);
        registry.registerItemCollector(LootPoolEntries.ALTERNATIVES, ItemCollectorUtils::collectComposite);
        registry.registerItemCollector(LootPoolEntries.GROUP, ItemCollectorUtils::collectComposite);
        registry.registerItemCollector(LootPoolEntries.SEQUENCE, ItemCollectorUtils::collectComposite);
        registry.registerItemCollector(LootPoolEntries.EMPTY, ItemCollectorUtils::collectSingleton);
        registry.registerItemCollector(LootPoolEntries.DYNAMIC, ItemCollectorUtils::collectSingleton);
        registry.registerItemCollector(LootPoolEntries.REFERENCE, ItemCollectorUtils::collectReference);

        registry.registerItemCollector(LootItemFunctions.FURNACE_SMELT, ItemCollectorUtils::collectFurnaceSmelt);

        registry.registerNumberProvider(NumberProviders.CONSTANT, Plugin::convertConstant);
        registry.registerNumberProvider(NumberProviders.UNIFORM, Plugin::convertUniform);
        registry.registerNumberProvider(NumberProviders.BINOMIAL, Plugin::convertBinomial);
        registry.registerNumberProvider(NumberProviders.SCORE, Plugin::convertScore);

        registry.<LootItem>registerEntry(LootPoolEntries.ITEM, ItemNode::new);
        registry.<TagEntry>registerEntry(LootPoolEntries.TAG, TagNode::new);
        registry.<AlternativesEntry>registerEntry(LootPoolEntries.ALTERNATIVES, AlternativesNode::new);
        registry.<SequentialEntry>registerEntry(LootPoolEntries.SEQUENCE, SequenceNode::new);
        registry.<EntryGroup>registerEntry(LootPoolEntries.GROUP, GroupNode::new);
        registry.<EmptyLootItem>registerEntry(LootPoolEntries.EMPTY, EmptyNode::new);
        registry.<DynamicLoot>registerEntry(LootPoolEntries.DYNAMIC, DynamicNode::new);
        registry.<LootTableReference>registerEntry(LootPoolEntries.REFERENCE, ReferenceNode::new);

        registry.registerConditionTooltip(LootItemConditions.ALL_OF, ConditionTooltipUtils::getAllOfTooltip);
        registry.registerConditionTooltip(LootItemConditions.ANY_OF, ConditionTooltipUtils::getAnyOfTooltip);
        registry.registerConditionTooltip(LootItemConditions.BLOCK_STATE_PROPERTY, ConditionTooltipUtils::getBlockStatePropertyTooltip);
        registry.registerConditionTooltip(LootItemConditions.DAMAGE_SOURCE_PROPERTIES, ConditionTooltipUtils::getDamageSourcePropertiesTooltip);
        registry.registerConditionTooltip(LootItemConditions.ENTITY_PROPERTIES, ConditionTooltipUtils::getEntityPropertiesTooltip);
        registry.registerConditionTooltip(LootItemConditions.ENTITY_SCORES, ConditionTooltipUtils::getEntityScoresTooltip);
        registry.registerConditionTooltip(LootItemConditions.INVERTED, ConditionTooltipUtils::getInvertedTooltip);
        registry.registerConditionTooltip(LootItemConditions.KILLED_BY_PLAYER, ConditionTooltipUtils::getKilledByPlayerTooltip);
        registry.registerConditionTooltip(LootItemConditions.LOCATION_CHECK, ConditionTooltipUtils::getLocationCheckTooltip);
        registry.registerConditionTooltip(LootItemConditions.MATCH_TOOL, ConditionTooltipUtils::getMatchToolTooltip);
        registry.registerConditionTooltip(LootItemConditions.RANDOM_CHANCE, ConditionTooltipUtils::getRandomChanceTooltip);
        registry.registerConditionTooltip(LootItemConditions.RANDOM_CHANCE_WITH_LOOTING, ConditionTooltipUtils::getRandomChanceWithLootingTooltip);
        registry.registerConditionTooltip(LootItemConditions.REFERENCE, ConditionTooltipUtils::getReferenceTooltip);
        registry.registerConditionTooltip(LootItemConditions.SURVIVES_EXPLOSION, ConditionTooltipUtils::getSurvivesExplosionTooltip);
        registry.registerConditionTooltip(LootItemConditions.TABLE_BONUS, ConditionTooltipUtils::getTableBonusTooltip);
        registry.registerConditionTooltip(LootItemConditions.TIME_CHECK, ConditionTooltipUtils::getTimeCheckTooltip);
        registry.registerConditionTooltip(LootItemConditions.VALUE_CHECK, ConditionTooltipUtils::getValueCheckTooltip);
        registry.registerConditionTooltip(LootItemConditions.WEATHER_CHECK, ConditionTooltipUtils::getWeatherCheckTooltip);

        registry.registerFunctionTooltip(LootItemFunctions.APPLY_BONUS, FunctionTooltipUtils::getApplyBonusTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.COPY_NAME, FunctionTooltipUtils::getCopyNameTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.COPY_NBT, FunctionTooltipUtils::getCopyNbtTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.COPY_STATE, FunctionTooltipUtils::getCopyStateTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.ENCHANT_RANDOMLY, FunctionTooltipUtils::getEnchantRandomlyTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.ENCHANT_WITH_LEVELS, FunctionTooltipUtils::getEnchantWithLevelsTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.EXPLORATION_MAP, FunctionTooltipUtils::getExplorationMapTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.EXPLOSION_DECAY, FunctionTooltipUtils::getExplosionDecayTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.FILL_PLAYER_HEAD, FunctionTooltipUtils::getFillPlayerHeadTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.FURNACE_SMELT, FunctionTooltipUtils::getFurnaceSmeltTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.LIMIT_COUNT, FunctionTooltipUtils::getLimitCountTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.LOOTING_ENCHANT, FunctionTooltipUtils::getLootingEnchantTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.REFERENCE, FunctionTooltipUtils::getReferenceTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_ATTRIBUTES, FunctionTooltipUtils::getSetAttributesTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_BANNER_PATTERN, FunctionTooltipUtils::getSetBannerPatternTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_CONTENTS, FunctionTooltipUtils::getSetContentsTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_COUNT, FunctionTooltipUtils::getSetCountTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_DAMAGE, FunctionTooltipUtils::getSetDamageTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_ENCHANTMENTS, FunctionTooltipUtils::getSetEnchantmentsTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_INSTRUMENT, FunctionTooltipUtils::getSetInstrumentTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_LOOT_TABLE, FunctionTooltipUtils::getSetLootTableTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_LORE, FunctionTooltipUtils::getSetLoreTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_NAME, FunctionTooltipUtils::getSetNameTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_NBT, FunctionTooltipUtils::getSetNbtTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_POTION, FunctionTooltipUtils::getSetPotionTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_STEW_EFFECT, FunctionTooltipUtils::getSetStewEffectTooltip);

        registry.registerChanceModifier(LootItemConditions.RANDOM_CHANCE, TooltipUtils::applyRandomChance);
        registry.registerChanceModifier(LootItemConditions.RANDOM_CHANCE_WITH_LOOTING, TooltipUtils::applyRandomChanceWithLooting);
        registry.registerChanceModifier(LootItemConditions.TABLE_BONUS, TooltipUtils::applyTableBonus);

        registry.registerCountModifier(LootItemFunctions.SET_COUNT, TooltipUtils::applySetCount);
        registry.registerCountModifier(LootItemFunctions.APPLY_BONUS, TooltipUtils::applyBonus);
        registry.registerCountModifier(LootItemFunctions.LIMIT_COUNT, TooltipUtils::applyLimitCount);
        registry.registerCountModifier(LootItemFunctions.LOOTING_ENCHANT, TooltipUtils::applyLootingEnchant);

        registry.registerItemStackModifier(LootItemFunctions.ENCHANT_RANDOMLY, TooltipUtils::applyEnchantRandomlyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.ENCHANT_WITH_LEVELS, TooltipUtils::applyEnchantWithLevelsItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_ATTRIBUTES, TooltipUtils::applySetAttributesItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_BANNER_PATTERN, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_NAME, TooltipUtils::applySetNameItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_NBT, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_POTION, TooltipUtils::applyItemStackModifier);
    }

    @NotNull
    private static RangeValue convertConstant(IServerUtils utils, ConstantValue numberProvider) {
        return new RangeValue(numberProvider.getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertUniform(IServerUtils utils, UniformGenerator numberProvider) {
        return new RangeValue(utils.convertNumber(utils, numberProvider.min).min(),
                utils.convertNumber(utils, numberProvider.max).max());
    }

    @NotNull
    private static RangeValue convertBinomial(IServerUtils utils, BinomialDistributionGenerator numberProvider) {
        return new RangeValue(0, numberProvider.n.getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertScore(IServerUtils utils, ScoreboardValue numberProvider) {
        return new RangeValue(true, false);
    }
}
