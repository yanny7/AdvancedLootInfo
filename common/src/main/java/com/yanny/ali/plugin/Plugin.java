package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.common.EntityUtils;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.server.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ScoreboardValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public void registerCommon(ICommonRegistry registry) {
        registry.registerEntityVariants(EntityType.SHEEP, EntityUtils::getSheepVariants);
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        registry.registerWidget(LootTableNode.ID, LootTableWidget::new);
        registry.registerWidget(LootPoolNode.ID, LootPoolWidget::new);
        registry.registerWidget(ItemNode.ID, ItemWidget::new);
        registry.registerWidget(EmptyNode.ID, EmptyWidget::new);
        registry.registerWidget(ReferenceNode.ID, ReferenceWidget::new);
        registry.registerWidget(DynamicNode.ID, DynamicWidget::new);
        registry.registerWidget(TagNode.ID, TagWidget::new);
        registry.registerWidget(AlternativesNode.ID, AlternativesWidget::new);
        registry.registerWidget(SequenceNode.ID, SequentialWidget::new);
        registry.registerWidget(GroupNode.ID, GroupWidget::new);
        registry.registerWidget(MissingNode.ID, MissingWidget::new);

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
        registry.registerItemCollector(LootItem.class, ItemCollectorUtils::collectItems);
        registry.registerItemCollector(TagEntry.class, ItemCollectorUtils::collectTags);
        registry.registerItemCollector(AlternativesEntry.class, ItemCollectorUtils::collectComposite);
        registry.registerItemCollector(EntryGroup.class, ItemCollectorUtils::collectComposite);
        registry.registerItemCollector(SequentialEntry.class, ItemCollectorUtils::collectComposite);
        registry.registerItemCollector(EmptyLootItem.class, ItemCollectorUtils::collectSingleton);
        registry.registerItemCollector(DynamicLoot.class, ItemCollectorUtils::collectSingleton);
        registry.registerItemCollector(LootTableReference.class, ItemCollectorUtils::collectReference);

        registry.registerItemCollector(SmeltItemFunction.class, ItemCollectorUtils::collectFurnaceSmelt);

        registry.registerNumberProvider(ConstantValue.class, Plugin::convertConstant);
        registry.registerNumberProvider(UniformGenerator.class, Plugin::convertUniform);
        registry.registerNumberProvider(BinomialDistributionGenerator.class, Plugin::convertBinomial);
        registry.registerNumberProvider(ScoreboardValue.class, Plugin::convertScore);

        registry.registerEntry(LootItem.class, ItemNode::new);
        registry.registerEntry(TagEntry.class, TagNode::new);
        registry.registerEntry(AlternativesEntry.class, AlternativesNode::new);
        registry.registerEntry(SequentialEntry.class, SequenceNode::new);
        registry.registerEntry(EntryGroup.class, GroupNode::new);
        registry.registerEntry(EmptyLootItem.class, EmptyNode::new);
        registry.registerEntry(DynamicLoot.class, DynamicNode::new);
        registry.registerEntry(LootTableReference.class, ReferenceNode::new);

        registry.registerConditionTooltip(AllOfCondition.class, ConditionTooltipUtils::getAllOfTooltip);
        registry.registerConditionTooltip(AnyOfCondition.class, ConditionTooltipUtils::getAnyOfTooltip);
        registry.registerConditionTooltip(LootItemBlockStatePropertyCondition.class, ConditionTooltipUtils::getBlockStatePropertyTooltip);
        registry.registerConditionTooltip(DamageSourceCondition.class, ConditionTooltipUtils::getDamageSourcePropertiesTooltip);
        registry.registerConditionTooltip(LootItemEntityPropertyCondition.class, ConditionTooltipUtils::getEntityPropertiesTooltip);
        registry.registerConditionTooltip(EntityHasScoreCondition.class, ConditionTooltipUtils::getEntityScoresTooltip);
        registry.registerConditionTooltip(InvertedLootItemCondition.class, ConditionTooltipUtils::getInvertedTooltip);
        registry.registerConditionTooltip(LootItemKilledByPlayerCondition.class, ConditionTooltipUtils::getKilledByPlayerTooltip);
        registry.registerConditionTooltip(LocationCheck.class, ConditionTooltipUtils::getLocationCheckTooltip);
        registry.registerConditionTooltip(MatchTool.class, ConditionTooltipUtils::getMatchToolTooltip);
        registry.registerConditionTooltip(LootItemRandomChanceCondition.class, ConditionTooltipUtils::getRandomChanceTooltip);
        registry.registerConditionTooltip(LootItemRandomChanceWithLootingCondition.class, ConditionTooltipUtils::getRandomChanceWithLootingTooltip);
        registry.registerConditionTooltip(ConditionReference.class, ConditionTooltipUtils::getReferenceTooltip);
        registry.registerConditionTooltip(ExplosionCondition.class, ConditionTooltipUtils::getSurvivesExplosionTooltip);
        registry.registerConditionTooltip(BonusLevelTableCondition.class, ConditionTooltipUtils::getTableBonusTooltip);
        registry.registerConditionTooltip(TimeCheck.class, ConditionTooltipUtils::getTimeCheckTooltip);
        registry.registerConditionTooltip(ValueCheckCondition.class, ConditionTooltipUtils::getValueCheckTooltip);
        registry.registerConditionTooltip(WeatherCheck.class, ConditionTooltipUtils::getWeatherCheckTooltip);

        registry.registerFunctionTooltip(ApplyBonusCount.class, FunctionTooltipUtils::getApplyBonusTooltip);
        registry.registerFunctionTooltip(CopyNameFunction.class, FunctionTooltipUtils::getCopyNameTooltip);
        registry.registerFunctionTooltip(CopyNbtFunction.class, FunctionTooltipUtils::getCopyNbtTooltip);
        registry.registerFunctionTooltip(CopyBlockState.class, FunctionTooltipUtils::getCopyStateTooltip);
        registry.registerFunctionTooltip(EnchantRandomlyFunction.class, FunctionTooltipUtils::getEnchantRandomlyTooltip);
        registry.registerFunctionTooltip(EnchantWithLevelsFunction.class, FunctionTooltipUtils::getEnchantWithLevelsTooltip);
        registry.registerFunctionTooltip(ExplorationMapFunction.class, FunctionTooltipUtils::getExplorationMapTooltip);
        registry.registerFunctionTooltip(ApplyExplosionDecay.class, FunctionTooltipUtils::getExplosionDecayTooltip);
        registry.registerFunctionTooltip(FillPlayerHead.class, FunctionTooltipUtils::getFillPlayerHeadTooltip);
        registry.registerFunctionTooltip(SmeltItemFunction.class, FunctionTooltipUtils::getFurnaceSmeltTooltip);
        registry.registerFunctionTooltip(LimitCount.class, FunctionTooltipUtils::getLimitCountTooltip);
        registry.registerFunctionTooltip(LootingEnchantFunction.class, FunctionTooltipUtils::getLootingEnchantTooltip);
        registry.registerFunctionTooltip(FunctionReference.class, FunctionTooltipUtils::getReferenceTooltip);
        registry.registerFunctionTooltip(SequenceFunction.class, FunctionTooltipUtils::getSequenceTooltip);
        registry.registerFunctionTooltip(SetAttributesFunction.class, FunctionTooltipUtils::getSetAttributesTooltip);
        registry.registerFunctionTooltip(SetBannerPatternFunction.class, FunctionTooltipUtils::getSetBannerPatternTooltip);
        registry.registerFunctionTooltip(SetContainerContents.class, FunctionTooltipUtils::getSetContentsTooltip);
        registry.registerFunctionTooltip(SetItemCountFunction.class, FunctionTooltipUtils::getSetCountTooltip);
        registry.registerFunctionTooltip(SetItemDamageFunction.class, FunctionTooltipUtils::getSetDamageTooltip);
        registry.registerFunctionTooltip(SetEnchantmentsFunction.class, FunctionTooltipUtils::getSetEnchantmentsTooltip);
        registry.registerFunctionTooltip(SetInstrumentFunction.class, FunctionTooltipUtils::getSetInstrumentTooltip);
        registry.registerFunctionTooltip(SetContainerLootTable.class, FunctionTooltipUtils::getSetLootTableTooltip);
        registry.registerFunctionTooltip(SetLoreFunction.class, FunctionTooltipUtils::getSetLoreTooltip);
        registry.registerFunctionTooltip(SetNameFunction.class, FunctionTooltipUtils::getSetNameTooltip);
        registry.registerFunctionTooltip(SetNbtFunction.class, FunctionTooltipUtils::getSetNbtTooltip);
        registry.registerFunctionTooltip(SetPotionFunction.class, FunctionTooltipUtils::getSetPotionTooltip);
        registry.registerFunctionTooltip(SetStewEffectFunction.class, FunctionTooltipUtils::getSetStewEffectTooltip);

        registry.registerIngredientTooltip(Ingredient.class, IngredientTooltipUtils::getIngredientTooltip);

        registry.registerChanceModifier(LootItemRandomChanceCondition.class, TooltipUtils::applyRandomChance);
        registry.registerChanceModifier(LootItemRandomChanceWithLootingCondition.class, TooltipUtils::applyRandomChanceWithLooting);
        registry.registerChanceModifier(BonusLevelTableCondition.class, TooltipUtils::applyTableBonus);

        registry.registerCountModifier(SetItemCountFunction.class, TooltipUtils::applySetCount);
        registry.registerCountModifier(ApplyBonusCount.class, TooltipUtils::applyBonus);
        registry.registerCountModifier(LimitCount.class, TooltipUtils::applyLimitCount);
        registry.registerCountModifier(LootingEnchantFunction.class, TooltipUtils::applyLootingEnchant);

        registry.registerItemStackModifier(EnchantRandomlyFunction.class, TooltipUtils::applyEnchantRandomlyItemStackModifier);
        registry.registerItemStackModifier(EnchantWithLevelsFunction.class, TooltipUtils::applyEnchantWithLevelsItemStackModifier);
        registry.registerItemStackModifier(SetAttributesFunction.class, TooltipUtils::applySetAttributesItemStackModifier);
        registry.registerItemStackModifier(SetBannerPatternFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetNameFunction.class, TooltipUtils::applySetNameItemStackModifier);
        registry.registerItemStackModifier(SetNbtFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetPotionFunction.class, TooltipUtils::applyItemStackModifier);
    }

    @NotNull
    private static RangeValue convertConstant(IServerUtils utils, ConstantValue numberProvider) {
        return new RangeValue(numberProvider.getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertUniform(IServerUtils utils, UniformGenerator numberProvider) {
        return new RangeValue(utils.convertNumber(utils, numberProvider.min()).min(),
                utils.convertNumber(utils, numberProvider.max()).max());
    }

    @NotNull
    private static RangeValue convertBinomial(IServerUtils utils, BinomialDistributionGenerator numberProvider) {
        return new RangeValue(0, numberProvider.n().getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertScore(IServerUtils utils, ScoreboardValue numberProvider) {
        return new RangeValue(true, false);
    }

    @NotNull
    private static RangeValue convertStorage(IClientUtils utils, StorageValue numberProvider) {
        return new RangeValue(false, true);
    }
}

/*
        registry.registerWidget(LootPoolEntries.ITEM, WidgetDirection.HORIZONTAL, ItemWidget::new, ItemWidget::getBounds);
        registry.registerWidget(LootPoolEntries.EMPTY, WidgetDirection.HORIZONTAL, EmptyWidget::new, EmptyWidget::getBounds);
        registry.registerWidget(LootPoolEntries.LOOT_TABLE, WidgetDirection.VERTICAL, ReferenceWidget::new, ReferenceWidget::getBounds);
        registry.registerWidget(LootPoolEntries.DYNAMIC, WidgetDirection.VERTICAL, DynamicWidget::new, DynamicWidget::getBounds);
        registry.registerWidget(LootPoolEntries.TAG, WidgetDirection.HORIZONTAL, TagWidget::new, TagWidget::getBounds);
        registry.registerWidget(LootPoolEntries.ALTERNATIVES, WidgetDirection.VERTICAL, AlternativesWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(LootPoolEntries.SEQUENCE, WidgetDirection.VERTICAL, SequentialWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(LootPoolEntries.GROUP, WidgetDirection.VERTICAL, GroupWidget::new, CompositeWidget::getBounds);

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
        registry.registerFunctionTooltip(LootItemFunctions.COPY_CUSTOM_DATA, FunctionTooltipUtils::getCopyCustomDataTooltip);
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
        registry.registerFunctionTooltip(LootItemFunctions.SEQUENCE, FunctionTooltipUtils::getSequenceTooltip);
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
        registry.registerFunctionTooltip(LootItemFunctions.SET_CUSTOM_DATA, FunctionTooltipUtils::getSetCustomDataTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_POTION, FunctionTooltipUtils::getSetPotionTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_STEW_EFFECT, FunctionTooltipUtils::getSetStewEffectTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_ITEM, FunctionTooltipUtils::getSetItemTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_COMPONENTS, FunctionTooltipUtils::getSetComponentsTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.MODIFY_CONTENTS, FunctionTooltipUtils::getModifyContentsTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.FILTERED, FunctionTooltipUtils::getFilteredTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.COPY_COMPONENTS, FunctionTooltipUtils::getCopyComponentsTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_FIREWORKS, FunctionTooltipUtils::getSetFireworksTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_FIREWORK_EXPLOSION, FunctionTooltipUtils::getSetFireworkExplosionTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_BOOK_COVER, FunctionTooltipUtils::getSetBookCoverTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_WRITTEN_BOOK_PAGES, FunctionTooltipUtils::getSetWrittenBookPagesTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_WRITABLE_BOOK_PAGES, FunctionTooltipUtils::getSetWritableBookPagesTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.TOGGLE_TOOLTIPS, FunctionTooltipUtils::getToggleTooltipsTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_OMINOUS_BOTTLE_AMPLIFIER, FunctionTooltipUtils::getSetOminousBottleAmplifierTooltip);
        registry.registerFunctionTooltip(LootItemFunctions.SET_CUSTOM_MODEL_DATA, FunctionTooltipUtils::getSetCustomModelDataTooltip);

        registry.registerItemSubPredicateTooltip(ItemSubPredicates.DAMAGE, ItemSubPredicateTooltipUtils::getItemDamagePredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.ENCHANTMENTS, ItemSubPredicateTooltipUtils::getItemEnchantmentsPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.STORED_ENCHANTMENTS, ItemSubPredicateTooltipUtils::getItemStoredEnchantmentsPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.POTIONS, ItemSubPredicateTooltipUtils::getItemPotionsPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.CUSTOM_DATA, ItemSubPredicateTooltipUtils::getItemCustomDataPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.CONTAINER, ItemSubPredicateTooltipUtils::getItemContainerPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.BUNDLE_CONTENTS, ItemSubPredicateTooltipUtils::getItemBundlePredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.FIREWORK_EXPLOSION, ItemSubPredicateTooltipUtils::getItemFireworkExplosionPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.FIREWORKS, ItemSubPredicateTooltipUtils::getItemFireworksPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.WRITABLE_BOOK, ItemSubPredicateTooltipUtils::getItemWritableBookPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.WRITTEN_BOOK, ItemSubPredicateTooltipUtils::getItemWrittenBookPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.ATTRIBUTE_MODIFIERS, ItemSubPredicateTooltipUtils::getItemAttributeModifiersPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.ARMOR_TRIM, ItemSubPredicateTooltipUtils::getItemTrimPredicateTooltip);

        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.LIGHTNING, EntitySubPredicateTooltipUtils::getLightningBoltPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.FISHING_HOOK, EntitySubPredicateTooltipUtils::getFishingHookPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.PLAYER, EntitySubPredicateTooltipUtils::getPlayerPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.SLIME, EntitySubPredicateTooltipUtils::getSlimePredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.RAIDER, EntitySubPredicateTooltipUtils::getRaiderPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.AXOLOTL.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.BOAT.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.FOX.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.MOOSHROOM.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.RABBIT.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.HORSE.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.LLAMA.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.VILLAGER.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.PARROT.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.TROPICAL_FISH.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.PAINTING.codec, EntitySubPredicateTooltipUtils::getHolderVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.CAT.codec, EntitySubPredicateTooltipUtils::getHolderVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.FROG.codec, EntitySubPredicateTooltipUtils::getHolderVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.WOLF.codec, EntitySubPredicateTooltipUtils::getHolderVariantPredicateTooltip);

        registry.registerDataComponentTypeTooltip(DataComponents.CUSTOM_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAX_STACK_SIZE, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAX_DAMAGE, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DAMAGE, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.UNBREAKABLE, DataComponentTooltipUtils::getUnbreakableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CUSTOM_NAME, DataComponentTooltipUtils::getCustomNameTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ITEM_NAME, DataComponentTooltipUtils::getItemNameTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LORE, DataComponentTooltipUtils::getItemLoreTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.RARITY, DataComponentTooltipUtils::getRarityTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENCHANTMENTS, DataComponentTooltipUtils::getItemEnchantmentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CAN_PLACE_ON, DataComponentTooltipUtils::getAdventureModePredicateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CAN_BREAK, DataComponentTooltipUtils::getAdventureModePredicateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ATTRIBUTE_MODIFIERS, DataComponentTooltipUtils::getAttributeModifiersTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CUSTOM_MODEL_DATA, DataComponentTooltipUtils::getCustomModelDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.HIDE_ADDITIONAL_TOOLTIP, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.HIDE_TOOLTIP, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.REPAIR_COST, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CREATIVE_SLOT_LOCK, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, DataComponentTooltipUtils::getBoolTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.INTANGIBLE_PROJECTILE, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FOOD, DataComponentTooltipUtils::getFoodTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FIRE_RESISTANT, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TOOL, DataComponentTooltipUtils::getToolTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.STORED_ENCHANTMENTS, DataComponentTooltipUtils::getItemEnchantmentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DYED_COLOR, DataComponentTooltipUtils::getDyedColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_COLOR, DataComponentTooltipUtils::getMapColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_ID, DataComponentTooltipUtils::getMapIdTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_DECORATIONS, DataComponentTooltipUtils::getMapDecorationsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_POST_PROCESSING, DataComponentTooltipUtils::getMapPostProcessingTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CHARGED_PROJECTILES, DataComponentTooltipUtils::getChargedProjectilesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BUNDLE_CONTENTS, DataComponentTooltipUtils::getBundleContentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.POTION_CONTENTS, DataComponentTooltipUtils::getPotionContentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.SUSPICIOUS_STEW_EFFECTS, DataComponentTooltipUtils::getSuspiciousStewEffectsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WRITABLE_BOOK_CONTENT, DataComponentTooltipUtils::getWritableBookContentTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WRITTEN_BOOK_CONTENT, DataComponentTooltipUtils::getWrittenBookContentTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TRIM, DataComponentTooltipUtils::getTrimTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DEBUG_STICK_STATE, DataComponentTooltipUtils::getDebugStickStateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENTITY_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BUCKET_ENTITY_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BLOCK_ENTITY_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.INSTRUMENT, DataComponentTooltipUtils::getInstrumentTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.RECIPES, DataComponentTooltipUtils::getRecipesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LODESTONE_TRACKER, DataComponentTooltipUtils::getLodestoneTrackerTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FIREWORK_EXPLOSION, DataComponentTooltipUtils::getFireworkExplosionTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FIREWORKS, DataComponentTooltipUtils::getFireworksTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PROFILE, DataComponentTooltipUtils::getProfileTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.NOTE_BLOCK_SOUND, DataComponentTooltipUtils::getNoteBlockSoundTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BANNER_PATTERNS, DataComponentTooltipUtils::getBannerPatternsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BASE_COLOR, DataComponentTooltipUtils::getBaseColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.POT_DECORATIONS, DataComponentTooltipUtils::getPotDecorationsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CONTAINER, DataComponentTooltipUtils::getContainerTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BLOCK_STATE, DataComponentTooltipUtils::getBlockStateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BEES, DataComponentTooltipUtils::getBeesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LOCK, DataComponentTooltipUtils::getLockTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CONTAINER_LOOT, DataComponentTooltipUtils::getContainerLootTooltip);

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
        registry.registerItemStackModifier(LootItemFunctions.SET_COMPONENTS, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_POTION, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_BOOK_COVER, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_CUSTOM_DATA, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_FIREWORK_EXPLOSION, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_FIREWORKS, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_WRITABLE_BOOK_PAGES, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_WRITTEN_BOOK_PAGES, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.TOGGLE_TOOLTIPS, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(LootItemFunctions.SET_ENCHANTMENTS, TooltipUtils::applySetEnchantmentsItemStackModifier);

        registry.registerNumberProvider(NumberProviders.CONSTANT, Plugin::convertConstant);
        registry.registerNumberProvider(NumberProviders.UNIFORM, Plugin::convertUniform);
        registry.registerNumberProvider(NumberProviders.BINOMIAL, Plugin::convertBinomial);
        registry.registerNumberProvider(NumberProviders.SCORE, Plugin::convertScore);
        registry.registerNumberProvider(NumberProviders.STORAGE, Plugin::convertStorage);
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
        registry.registerItemCollector(LootPoolEntries.LOOT_TABLE, ItemCollectorUtils::collectReference);

        registry.registerItemCollector(LootItemFunctions.FURNACE_SMELT, ItemCollectorUtils::collectFurnaceSmelt);
        registry.registerItemCollector(LootItemFunctions.SET_ITEM, ItemCollectorUtils::collectSetItem);
    }
 */