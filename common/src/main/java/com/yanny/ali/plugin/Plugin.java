package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.common.EntityUtils;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.server.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.consume_effects.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.*;
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
        registry.registerItemCollector(NestedLootTable.class, ItemCollectorUtils::collectReference);

        registry.registerItemCollector(SmeltItemFunction.class, ItemCollectorUtils::collectFurnaceSmelt);
        registry.registerItemCollector(SetItemFunction.class, ItemCollectorUtils::collectSetItem);

        registry.registerNumberProvider(ConstantValue.class, Plugin::convertConstant);
        registry.registerNumberProvider(UniformGenerator.class, Plugin::convertUniform);
        registry.registerNumberProvider(BinomialDistributionGenerator.class, Plugin::convertBinomial);
        registry.registerNumberProvider(ScoreboardValue.class, Plugin::convertScore);
        registry.registerNumberProvider(StorageValue.class, Plugin::convertStorage);
        registry.registerNumberProvider(EnchantmentLevelProvider.class, Plugin::convertEnchantmentLevel);

        registry.registerEntry(LootItem.class, ItemNode::new);
        registry.registerEntry(TagEntry.class, TagNode::new);
        registry.registerEntry(AlternativesEntry.class, AlternativesNode::new);
        registry.registerEntry(SequentialEntry.class, SequenceNode::new);
        registry.registerEntry(EntryGroup.class, GroupNode::new);
        registry.registerEntry(EmptyLootItem.class, EmptyNode::new);
        registry.registerEntry(DynamicLoot.class, DynamicNode::new);
        registry.registerEntry(NestedLootTable.class, ReferenceNode::new);

        registry.registerConditionTooltip(AllOfCondition.class, ConditionTooltipUtils::getAllOfTooltip);
        registry.registerConditionTooltip(AnyOfCondition.class, ConditionTooltipUtils::getAnyOfTooltip);
        registry.registerConditionTooltip(LootItemBlockStatePropertyCondition.class, ConditionTooltipUtils::getBlockStatePropertyTooltip);
        registry.registerConditionTooltip(DamageSourceCondition.class, ConditionTooltipUtils::getDamageSourcePropertiesTooltip);
        registry.registerConditionTooltip(EnchantmentActiveCheck.class, ConditionTooltipUtils::getEnchantActiveCheckTooltip);
        registry.registerConditionTooltip(LootItemEntityPropertyCondition.class, ConditionTooltipUtils::getEntityPropertiesTooltip);
        registry.registerConditionTooltip(EntityHasScoreCondition.class, ConditionTooltipUtils::getEntityScoresTooltip);
        registry.registerConditionTooltip(InvertedLootItemCondition.class, ConditionTooltipUtils::getInvertedTooltip);
        registry.registerConditionTooltip(LootItemKilledByPlayerCondition.class, ConditionTooltipUtils::getKilledByPlayerTooltip);
        registry.registerConditionTooltip(LocationCheck.class, ConditionTooltipUtils::getLocationCheckTooltip);
        registry.registerConditionTooltip(MatchTool.class, ConditionTooltipUtils::getMatchToolTooltip);
        registry.registerConditionTooltip(LootItemRandomChanceCondition.class, ConditionTooltipUtils::getRandomChanceTooltip);
        registry.registerConditionTooltip(LootItemRandomChanceWithEnchantedBonusCondition.class, ConditionTooltipUtils::getRandomChanceWithEnchantedBonusTooltip);
        registry.registerConditionTooltip(ConditionReference.class, ConditionTooltipUtils::getReferenceTooltip);
        registry.registerConditionTooltip(ExplosionCondition.class, ConditionTooltipUtils::getSurvivesExplosionTooltip);
        registry.registerConditionTooltip(BonusLevelTableCondition.class, ConditionTooltipUtils::getTableBonusTooltip);
        registry.registerConditionTooltip(TimeCheck.class, ConditionTooltipUtils::getTimeCheckTooltip);
        registry.registerConditionTooltip(ValueCheckCondition.class, ConditionTooltipUtils::getValueCheckTooltip);
        registry.registerConditionTooltip(WeatherCheck.class, ConditionTooltipUtils::getWeatherCheckTooltip);

        registry.registerFunctionTooltip(ApplyBonusCount.class, FunctionTooltipUtils::getApplyBonusTooltip);
        registry.registerFunctionTooltip(CopyNameFunction.class, FunctionTooltipUtils::getCopyNameTooltip);
        registry.registerFunctionTooltip(CopyCustomDataFunction.class, FunctionTooltipUtils::getCopyCustomDataTooltip);
        registry.registerFunctionTooltip(CopyBlockState.class, FunctionTooltipUtils::getCopyStateTooltip);
        registry.registerFunctionTooltip(EnchantRandomlyFunction.class, FunctionTooltipUtils::getEnchantRandomlyTooltip);
        registry.registerFunctionTooltip(EnchantWithLevelsFunction.class, FunctionTooltipUtils::getEnchantWithLevelsTooltip);
        registry.registerFunctionTooltip(ExplorationMapFunction.class, FunctionTooltipUtils::getExplorationMapTooltip);
        registry.registerFunctionTooltip(ApplyExplosionDecay.class, FunctionTooltipUtils::getExplosionDecayTooltip);
        registry.registerFunctionTooltip(FillPlayerHead.class, FunctionTooltipUtils::getFillPlayerHeadTooltip);
        registry.registerFunctionTooltip(SmeltItemFunction.class, FunctionTooltipUtils::getFurnaceSmeltTooltip);
        registry.registerFunctionTooltip(LimitCount.class, FunctionTooltipUtils::getLimitCountTooltip);
        registry.registerFunctionTooltip(EnchantedCountIncreaseFunction.class, FunctionTooltipUtils::getEnchantedCountIncreaseTooltip);
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
        registry.registerFunctionTooltip(SetCustomDataFunction.class, FunctionTooltipUtils::getSetCustomDataTooltip);
        registry.registerFunctionTooltip(SetPotionFunction.class, FunctionTooltipUtils::getSetPotionTooltip);
        registry.registerFunctionTooltip(SetStewEffectFunction.class, FunctionTooltipUtils::getSetStewEffectTooltip);
        registry.registerFunctionTooltip(SetItemFunction.class, FunctionTooltipUtils::getSetItemTooltip);
        registry.registerFunctionTooltip(SetComponentsFunction.class, FunctionTooltipUtils::getSetComponentsTooltip);
        registry.registerFunctionTooltip(ModifyContainerContents.class, FunctionTooltipUtils::getModifyContentsTooltip);
        registry.registerFunctionTooltip(FilteredFunction.class, FunctionTooltipUtils::getFilteredTooltip);
        registry.registerFunctionTooltip(CopyComponentsFunction.class, FunctionTooltipUtils::getCopyComponentsTooltip);
        registry.registerFunctionTooltip(SetFireworksFunction.class, FunctionTooltipUtils::getSetFireworksTooltip);
        registry.registerFunctionTooltip(SetFireworkExplosionFunction.class, FunctionTooltipUtils::getSetFireworkExplosionTooltip);
        registry.registerFunctionTooltip(SetBookCoverFunction.class, FunctionTooltipUtils::getSetBookCoverTooltip);
        registry.registerFunctionTooltip(SetWrittenBookPagesFunction.class, FunctionTooltipUtils::getSetWrittenBookPagesTooltip);
        registry.registerFunctionTooltip(SetWritableBookPagesFunction.class, FunctionTooltipUtils::getSetWritableBookPagesTooltip);
        registry.registerFunctionTooltip(ToggleTooltips.class, FunctionTooltipUtils::getToggleTooltipsTooltip);
        registry.registerFunctionTooltip(SetOminousBottleAmplifierFunction.class, FunctionTooltipUtils::getSetOminousBottleAmplifierTooltip);
        registry.registerFunctionTooltip(SetCustomModelDataFunction.class, FunctionTooltipUtils::getSetCustomModelDataTooltip);

        registry.registerDataComponentPredicateTooltip(ItemDamagePredicate.class, ItemSubPredicateTooltipUtils::getItemDamagePredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemEnchantmentsPredicate.Enchantments.class, ItemSubPredicateTooltipUtils::getItemEnchantmentsPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemEnchantmentsPredicate.StoredEnchantments.class, ItemSubPredicateTooltipUtils::getItemStoredEnchantmentsPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemPotionsPredicate.class, ItemSubPredicateTooltipUtils::getItemPotionsPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemCustomDataPredicate.class, ItemSubPredicateTooltipUtils::getItemCustomDataPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemContainerPredicate.class, ItemSubPredicateTooltipUtils::getItemContainerPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemBundlePredicate.class, ItemSubPredicateTooltipUtils::getItemBundlePredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemFireworkExplosionPredicate.class, ItemSubPredicateTooltipUtils::getItemFireworkExplosionPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemFireworksPredicate.class, ItemSubPredicateTooltipUtils::getItemFireworksPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemWritableBookPredicate.class, ItemSubPredicateTooltipUtils::getItemWritableBookPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemWrittenBookPredicate.class, ItemSubPredicateTooltipUtils::getItemWrittenBookPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemAttributeModifiersPredicate.class, ItemSubPredicateTooltipUtils::getItemAttributeModifiersPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemTrimPredicate.class, ItemSubPredicateTooltipUtils::getItemTrimPredicateTooltip);
        registry.registerDataComponentPredicateTooltip(ItemJukeboxPlayablePredicate.class, ItemSubPredicateTooltipUtils::getItemJukeboxPlayableTooltip);

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

        registry.registerConsumeEffectTooltip(ApplyStatusEffectsConsumeEffect.class, ConsumeEffectTooltipUtils::getApplyEffectsTooltip);
        registry.registerConsumeEffectTooltip(RemoveStatusEffectsConsumeEffect.class, ConsumeEffectTooltipUtils::getRemoveEffectsTooltip);
        registry.registerConsumeEffectTooltip(ClearAllStatusEffectsConsumeEffect.class, ConsumeEffectTooltipUtils::getClearAllEffectsTooltip);
        registry.registerConsumeEffectTooltip(TeleportRandomlyConsumeEffect.class, ConsumeEffectTooltipUtils::getTeleportRandomlyTooltip);
        registry.registerConsumeEffectTooltip(PlaySoundConsumeEffect.class, ConsumeEffectTooltipUtils::getPlaySoundTooltip);


        registry.registerIngredientTooltip(Ingredient.class, IngredientTooltipUtils::getIngredientTooltip);

        registry.registerChanceModifier(LootItemRandomChanceCondition.class, TooltipUtils::applyRandomChance);
        registry.registerChanceModifier(LootItemRandomChanceWithEnchantedBonusCondition.class, TooltipUtils::applyRandomChanceWithLooting);
        registry.registerChanceModifier(BonusLevelTableCondition.class, TooltipUtils::applyTableBonus);

        registry.registerCountModifier(SetItemCountFunction.class, TooltipUtils::applySetCount);
        registry.registerCountModifier(ApplyBonusCount.class, TooltipUtils::applyBonus);
        registry.registerCountModifier(LimitCount.class, TooltipUtils::applyLimitCount);
        registry.registerCountModifier(EnchantedCountIncreaseFunction.class, TooltipUtils::applyLootingEnchant);

        registry.registerItemStackModifier(EnchantRandomlyFunction.class, TooltipUtils::applyEnchantRandomlyItemStackModifier);
        registry.registerItemStackModifier(EnchantWithLevelsFunction.class, TooltipUtils::applyEnchantWithLevelsItemStackModifier);
        registry.registerItemStackModifier(SetAttributesFunction.class, TooltipUtils::applySetAttributesItemStackModifier);
        registry.registerItemStackModifier(SetBannerPatternFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetNameFunction.class, TooltipUtils::applySetNameItemStackModifier);
        registry.registerItemStackModifier(SetCustomDataFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetPotionFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetComponentsFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetBookCoverFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetFireworkExplosionFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetFireworksFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetWritableBookPagesFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetWrittenBookPagesFunction.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(ToggleTooltips.class, TooltipUtils::applyItemStackModifier);
        registry.registerItemStackModifier(SetEnchantmentsFunction.class, TooltipUtils::applySetEnchantmentsItemStackModifier);
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
    private static RangeValue convertStorage(IServerUtils utils, StorageValue numberProvider) {
        return new RangeValue(false, true);
    }

    @NotNull
    private static RangeValue convertEnchantmentLevel(IServerUtils utils, EnchantmentLevelProvider numberProvider) {
        //TODO
        return new RangeValue(false, true);
    }
}
