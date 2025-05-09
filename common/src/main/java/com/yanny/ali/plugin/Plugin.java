package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.server.ItemCollectorUtils;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
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

        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.DAMAGE, DataComponentPredicateTooltipUtils::getDamagePredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.ENCHANTMENTS, DataComponentPredicateTooltipUtils::getEnchantmentsPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.STORED_ENCHANTMENTS, DataComponentPredicateTooltipUtils::getStoredEnchantmentsPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.POTIONS, DataComponentPredicateTooltipUtils::getPotionsPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.CUSTOM_DATA, DataComponentPredicateTooltipUtils::getCustomDataPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.CONTAINER, DataComponentPredicateTooltipUtils::getContainerPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.BUNDLE_CONTENTS, DataComponentPredicateTooltipUtils::getBundlePredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.FIREWORK_EXPLOSION, DataComponentPredicateTooltipUtils::getFireworkExplosionPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.FIREWORKS, DataComponentPredicateTooltipUtils::getFireworksPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.WRITABLE_BOOK, DataComponentPredicateTooltipUtils::getWritableBookPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.WRITTEN_BOOK, DataComponentPredicateTooltipUtils::getWrittenBookPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.ATTRIBUTE_MODIFIERS, DataComponentPredicateTooltipUtils::getAttributeModifiersPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.ARMOR_TRIM, DataComponentPredicateTooltipUtils::getTrimPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(DataComponentPredicates.JUKEBOX_PLAYABLE, DataComponentPredicateTooltipUtils::getJukeboxPlayableTooltip);

        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.LIGHTNING, EntitySubPredicateTooltipUtils::getLightningBoltPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.FISHING_HOOK, EntitySubPredicateTooltipUtils::getFishingHookPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.PLAYER, EntitySubPredicateTooltipUtils::getPlayerPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.SLIME, EntitySubPredicateTooltipUtils::getSlimePredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.RAIDER, EntitySubPredicateTooltipUtils::getRaiderPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.SHEEP, EntitySubPredicateTooltipUtils::getSheepPredicateTooltip);

        registry.registerDataComponentTypeTooltip(DataComponents.CUSTOM_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAX_STACK_SIZE, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAX_DAMAGE, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DAMAGE, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.UNBREAKABLE, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CUSTOM_NAME, DataComponentTooltipUtils::getCustomNameTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ITEM_NAME, DataComponentTooltipUtils::getItemNameTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ITEM_MODEL, DataComponentTooltipUtils::getResourceLocationTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LORE, DataComponentTooltipUtils::getItemLoreTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.RARITY, DataComponentTooltipUtils::getRarityTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENCHANTMENTS, DataComponentTooltipUtils::getItemEnchantmentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CAN_PLACE_ON, DataComponentTooltipUtils::getAdventureModePredicateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CAN_BREAK, DataComponentTooltipUtils::getAdventureModePredicateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ATTRIBUTE_MODIFIERS, DataComponentTooltipUtils::getAttributeModifiersTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CUSTOM_MODEL_DATA, DataComponentTooltipUtils::getCustomModelDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TOOLTIP_DISPLAY, DataComponentTooltipUtils::getTooltipDisplayTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.REPAIR_COST, DataComponentTooltipUtils::getIntTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CREATIVE_SLOT_LOCK, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, DataComponentTooltipUtils::getBoolTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.INTANGIBLE_PROJECTILE, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FOOD, DataComponentTooltipUtils::getFoodTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CONSUMABLE, DataComponentTooltipUtils::getConsumableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.USE_REMAINDER, DataComponentTooltipUtils::getUseRemainderTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.USE_COOLDOWN, DataComponentTooltipUtils::getUseCooldownTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DAMAGE_RESISTANT, DataComponentTooltipUtils::getDamageResistantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TOOL, DataComponentTooltipUtils::getToolTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WEAPON, DataComponentTooltipUtils::getWeaponTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENCHANTABLE, DataComponentTooltipUtils::getEnchantableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.EQUIPPABLE, DataComponentTooltipUtils::getEquipableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.REPAIRABLE, DataComponentTooltipUtils::getRepairableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.GLIDER, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TOOLTIP_STYLE, DataComponentTooltipUtils::getResourceLocationTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DEATH_PROTECTION, DataComponentTooltipUtils::getDeathProtectionTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BLOCKS_ATTACKS, DataComponentTooltipUtils::getBlockAttacksTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.STORED_ENCHANTMENTS, DataComponentTooltipUtils::getItemEnchantmentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DYED_COLOR, DataComponentTooltipUtils::getDyedColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_COLOR, DataComponentTooltipUtils::getMapColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_ID, DataComponentTooltipUtils::getMapIdTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_DECORATIONS, DataComponentTooltipUtils::getMapDecorationsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MAP_POST_PROCESSING, DataComponentTooltipUtils::getMapPostProcessingTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CHARGED_PROJECTILES, DataComponentTooltipUtils::getChargedProjectilesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BUNDLE_CONTENTS, DataComponentTooltipUtils::getBundleContentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.POTION_CONTENTS, DataComponentTooltipUtils::getPotionContentsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.POTION_DURATION_SCALE, DataComponentTooltipUtils::getFloatValueTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.SUSPICIOUS_STEW_EFFECTS, DataComponentTooltipUtils::getSuspiciousStewEffectsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WRITABLE_BOOK_CONTENT, DataComponentTooltipUtils::getWritableBookContentTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WRITTEN_BOOK_CONTENT, DataComponentTooltipUtils::getWrittenBookContentTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TRIM, DataComponentTooltipUtils::getTrimTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DEBUG_STICK_STATE, DataComponentTooltipUtils::getDebugStickStateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENTITY_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BUCKET_ENTITY_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BLOCK_ENTITY_DATA, DataComponentTooltipUtils::getCustomDataTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.INSTRUMENT, DataComponentTooltipUtils::getInstrumentTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PROVIDES_TRIM_MATERIAL, DataComponentTooltipUtils::getProvidesTrimMaterialTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, DataComponentTooltipUtils::getOminousBottleAmplifierTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.JUKEBOX_PLAYABLE, DataComponentTooltipUtils::getJukeboxPlayableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PROVIDES_BANNER_PATTERNS, DataComponentTooltipUtils::getProvidesBannerPatternsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.RECIPES, DataComponentTooltipUtils::getRecipesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LODESTONE_TRACKER, DataComponentTooltipUtils::getLodestoneTrackerTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FIREWORK_EXPLOSION, DataComponentTooltipUtils::getFireworkExplosionTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FIREWORKS, DataComponentTooltipUtils::getFireworksTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PROFILE, DataComponentTooltipUtils::getProfileTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.NOTE_BLOCK_SOUND, DataComponentTooltipUtils::getResourceLocationTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BANNER_PATTERNS, DataComponentTooltipUtils::getBannerPatternsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BASE_COLOR, DataComponentTooltipUtils::getDyeColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.POT_DECORATIONS, DataComponentTooltipUtils::getPotDecorationsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CONTAINER, DataComponentTooltipUtils::getContainerTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BLOCK_STATE, DataComponentTooltipUtils::getBlockStateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BEES, DataComponentTooltipUtils::getBeesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LOCK, DataComponentTooltipUtils::getLockTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CONTAINER_LOOT, DataComponentTooltipUtils::getContainerLootTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BREAK_SOUND, DataComponentTooltipUtils::getBreakSoundTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.VILLAGER_VARIANT, DataComponentTooltipUtils::getVillagerVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WOLF_VARIANT, DataComponentTooltipUtils::getWolfVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WOLF_SOUND_VARIANT, DataComponentTooltipUtils::getWolfSoundVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.WOLF_COLLAR, DataComponentTooltipUtils::getDyeColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FOX_VARIANT, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.SALMON_SIZE, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PARROT_VARIANT, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TROPICAL_FISH_PATTERN, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TROPICAL_FISH_BASE_COLOR, DataComponentTooltipUtils::getDyeColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TROPICAL_FISH_PATTERN_COLOR, DataComponentTooltipUtils::getDyeColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.MOOSHROOM_VARIANT, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.RABBIT_VARIANT, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PIG_VARIANT, DataComponentTooltipUtils::getPigVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.COW_VARIANT, DataComponentTooltipUtils::getCowVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CHICKEN_VARIANT, DataComponentTooltipUtils::getChickenVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FROG_VARIANT, DataComponentTooltipUtils::getFrogVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.HORSE_VARIANT, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PAINTING_VARIANT, DataComponentTooltipUtils::getPaintingVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LLAMA_VARIANT, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.AXOLOTL_VARIANT, DataComponentTooltipUtils::getEnumTypeTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CAT_VARIANT, DataComponentTooltipUtils::getCatVariantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CAT_COLLAR, DataComponentTooltipUtils::getDyeColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.SHEEP_COLOR, DataComponentTooltipUtils::getDyeColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.SHULKER_COLOR, DataComponentTooltipUtils::getDyeColorTooltip);

        registry.registerConsumeEffectTooltip(ConsumeEffect.Type.APPLY_EFFECTS, ConsumeEffectTooltipUtils::getApplyEffectsTooltip);
        registry.registerConsumeEffectTooltip(ConsumeEffect.Type.REMOVE_EFFECTS, ConsumeEffectTooltipUtils::getRemoveEffectsTooltip);
        registry.registerConsumeEffectTooltip(ConsumeEffect.Type.CLEAR_ALL_EFFECTS, ConsumeEffectTooltipUtils::getClearAllEffectsTooltip);
        registry.registerConsumeEffectTooltip(ConsumeEffect.Type.TELEPORT_RANDOMLY, ConsumeEffectTooltipUtils::getTeleportRandomlyTooltip);
        registry.registerConsumeEffectTooltip(ConsumeEffect.Type.PLAY_SOUND, ConsumeEffectTooltipUtils::getPlaySoundTooltip);

        registry.registerChanceModifier(LootItemConditions.RANDOM_CHANCE, TooltipUtils::applyRandomChance);
        registry.registerChanceModifier(LootItemConditions.RANDOM_CHANCE_WITH_ENCHANTED_BONUS, TooltipUtils::applyRandomChanceWithLooting);
        registry.registerChanceModifier(LootItemConditions.TABLE_BONUS, TooltipUtils::applyTableBonus);

        registry.registerCountModifier(LootItemFunctions.SET_COUNT, TooltipUtils::applySetCount);
        registry.registerCountModifier(LootItemFunctions.APPLY_BONUS, TooltipUtils::applyBonus);
        registry.registerCountModifier(LootItemFunctions.LIMIT_COUNT, TooltipUtils::applyLimitCount);
        registry.registerCountModifier(LootItemFunctions.ENCHANTED_COUNT_INCREASE, TooltipUtils::applyLootingEnchant);

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
