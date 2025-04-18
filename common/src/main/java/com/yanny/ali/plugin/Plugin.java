package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.*;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public void registerClient(IClientRegistry registry) {
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
        registry.registerConditionTooltip(LootItemConditions.ENCHANTMENT_ACTIVE_CHECK, ConditionTooltipUtils::getEnchantActiveCheckTooltip);
        registry.registerConditionTooltip(LootItemConditions.ENTITY_PROPERTIES, ConditionTooltipUtils::getEntityPropertiesTooltip);
        registry.registerConditionTooltip(LootItemConditions.ENTITY_SCORES, ConditionTooltipUtils::getEntityScoresTooltip);
        registry.registerConditionTooltip(LootItemConditions.INVERTED, ConditionTooltipUtils::getInvertedTooltip);
        registry.registerConditionTooltip(LootItemConditions.KILLED_BY_PLAYER, ConditionTooltipUtils::getKilledByPlayerTooltip);
        registry.registerConditionTooltip(LootItemConditions.LOCATION_CHECK, ConditionTooltipUtils::getLocationCheckTooltip);
        registry.registerConditionTooltip(LootItemConditions.MATCH_TOOL, ConditionTooltipUtils::getMatchToolTooltip);
        registry.registerConditionTooltip(LootItemConditions.RANDOM_CHANCE, ConditionTooltipUtils::getRandomChanceTooltip);
        registry.registerConditionTooltip(LootItemConditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS, ConditionTooltipUtils::getRandomChanceWithEnchantedBonusTooltip);
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
        registry.registerFunctionTooltip(LootItemFunctions.ENCHANTED_COUNT_INCREASE, FunctionTooltipUtils::getEnchantedCountIncreaseTooltip);
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
        registry.registerItemSubPredicateTooltip(ItemSubPredicates.JUKEBOX_PLAYABLE, ItemSubPredicateTooltipUtils::getItemJukeboxPlayableTooltip);

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

        registry.registerChanceModifier(LootItemConditions.RANDOM_CHANCE, TooltipUtils::applyRandomChance);
        registry.registerChanceModifier(LootItemConditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS, TooltipUtils::applyRandomChanceWithLooting);
        registry.registerChanceModifier(LootItemConditions.TABLE_BONUS, TooltipUtils::applyTableBonus);

        registry.registerCountModifier(LootItemFunctions.SET_COUNT, TooltipUtils::applySetCount);
        registry.registerCountModifier(LootItemFunctions.APPLY_BONUS, TooltipUtils::applyBonus);
        registry.registerCountModifier(LootItemFunctions.LIMIT_COUNT, TooltipUtils::applyLimitCount);
        registry.registerCountModifier(LootItemFunctions.ENCHANTED_COUNT_INCREASE, TooltipUtils::applyLootingEnchant);

        registry.registerNumberProvider(NumberProviders.CONSTANT, Plugin::convertConstant);
        registry.registerNumberProvider(NumberProviders.UNIFORM, Plugin::convertUniform);
        registry.registerNumberProvider(NumberProviders.BINOMIAL, Plugin::convertBinomial);
        registry.registerNumberProvider(NumberProviders.SCORE, Plugin::convertScore);
        registry.registerNumberProvider(NumberProviders.STORAGE, Plugin::convertStorage);
        registry.registerNumberProvider(NumberProviders.ENCHANTMENT_LEVEL, Plugin::convertEnchantmentLevel);
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

    @NotNull
    private static RangeValue convertConstant(IClientUtils utils, ConstantValue numberProvider) {
        return new RangeValue(numberProvider.getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertUniform(IClientUtils utils, UniformGenerator numberProvider) {
        return new RangeValue(utils.convertNumber(utils, numberProvider.min()).min(),
                utils.convertNumber(utils, numberProvider.max()).max());
    }

    @NotNull
    private static RangeValue convertBinomial(IClientUtils utils, BinomialDistributionGenerator numberProvider) {
        return new RangeValue(0, numberProvider.n().getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertScore(IClientUtils utils, ScoreboardValue numberProvider) {
        return new RangeValue(true, false);
    }

    @NotNull
    private static RangeValue convertStorage(IClientUtils utils, StorageValue numberProvider) {
        return new RangeValue(false, true);
    }

    @NotNull
    private static RangeValue convertEnchantmentLevel(IClientUtils utils, EnchantmentLevelProvider numberProvider) {
        //TODO
        return new RangeValue(false, true);
    }
}
