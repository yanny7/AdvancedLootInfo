package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.client.widget.trades.ItemListingWidget;
import com.yanny.ali.plugin.client.widget.trades.SubTradesWidget;
import com.yanny.ali.plugin.client.widget.trades.TradeLevelWidget;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import com.yanny.ali.plugin.common.EntityUtils;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.common.tooltip.*;
import com.yanny.ali.plugin.common.trades.*;
import com.yanny.ali.plugin.server.*;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.consume_effects.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.number.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "ali";
    }

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

        registry.registerWidget(TradeNode.ID, TradeWidget::new);
        registry.registerWidget(TradeLevelNode.ID, TradeLevelWidget::new);
        registry.registerWidget(ItemStackNode.ID, ItemStackWidget::new);
        registry.registerWidget(SubTradesNode.ID, SubTradesWidget::new);
        registry.registerWidget(ItemsToItemsNode.ID, ItemListingWidget::new);

        registry.registerDataNode(LootTableNode.ID, LootTableNode::new);
        registry.registerDataNode(LootPoolNode.ID, LootPoolNode::new);
        registry.registerDataNode(ItemNode.ID, ItemNode::new);
        registry.registerDataNode(TagNode.ID, TagNode::new);
        registry.registerDataNode(AlternativesNode.ID, AlternativesNode::new);
        registry.registerDataNode(SequenceNode.ID, SequenceNode::new);
        registry.registerDataNode(GroupNode.ID, GroupNode::new);
        registry.registerDataNode(EmptyNode.ID, EmptyNode::new);
        registry.registerDataNode(DynamicNode.ID, DynamicNode::new);
        registry.registerDataNode(ReferenceNode.ID, ReferenceNode::new);
        registry.registerDataNode(MissingNode.ID, MissingNode::new);

        registry.registerDataNode(TradeNode.ID, TradeNode::new);
        registry.registerDataNode(TradeLevelNode.ID, TradeLevelNode::new);
        registry.registerDataNode(ItemStackNode.ID, ItemStackNode::new);
        registry.registerDataNode(SubTradesNode.ID, SubTradesNode::new);
        registry.registerDataNode(ItemsToItemsNode.ID, ItemsToItemsNode::new);

        registry.registerTooltipNode(ArrayTooltipNode.ID, ArrayTooltipNode::decode);
        registry.registerTooltipNode(BranchTooltipNode.ID, BranchTooltipNode::decode);
        registry.registerTooltipNode(ComponentTooltipNode.ID, ComponentTooltipNode::decode);
        registry.registerTooltipNode(EmptyTooltipNode.ID, EmptyTooltipNode::decode);
        registry.registerTooltipNode(ErrorTooltipNode.ID, ErrorTooltipNode::decode);
        registry.registerTooltipNode(LiteralTooltipNode.ID, LiteralTooltipNode::decode);
        registry.registerTooltipNode(ValueTooltipNode.ID, ValueTooltipNode::decode);
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

        registry.registerItemSubPredicateTooltip(ItemDamagePredicate.class, ItemSubPredicateTooltipUtils::getItemDamagePredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemEnchantmentsPredicate.Enchantments.class, ItemSubPredicateTooltipUtils::getItemEnchantmentsPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemEnchantmentsPredicate.StoredEnchantments.class, ItemSubPredicateTooltipUtils::getItemStoredEnchantmentsPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemPotionsPredicate.class, ItemSubPredicateTooltipUtils::getItemPotionsPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemCustomDataPredicate.class, ItemSubPredicateTooltipUtils::getItemCustomDataPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemContainerPredicate.class, ItemSubPredicateTooltipUtils::getItemContainerPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemBundlePredicate.class, ItemSubPredicateTooltipUtils::getItemBundlePredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemFireworkExplosionPredicate.class, ItemSubPredicateTooltipUtils::getItemFireworkExplosionPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemFireworksPredicate.class, ItemSubPredicateTooltipUtils::getItemFireworksPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemWritableBookPredicate.class, ItemSubPredicateTooltipUtils::getItemWritableBookPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemWrittenBookPredicate.class, ItemSubPredicateTooltipUtils::getItemWrittenBookPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemAttributeModifiersPredicate.class, ItemSubPredicateTooltipUtils::getItemAttributeModifiersPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemTrimPredicate.class, ItemSubPredicateTooltipUtils::getItemTrimPredicateTooltip);
        registry.registerItemSubPredicateTooltip(ItemJukeboxPlayablePredicate.class, ItemSubPredicateTooltipUtils::getItemJukeboxPlayableTooltip);

        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.LIGHTNING, EntitySubPredicateTooltipUtils::getLightningBoltPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.FISHING_HOOK, EntitySubPredicateTooltipUtils::getFishingHookPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.PLAYER, EntitySubPredicateTooltipUtils::getPlayerPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.SLIME, EntitySubPredicateTooltipUtils::getSlimePredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.RAIDER, EntitySubPredicateTooltipUtils::getRaiderPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.SHEEP, EntitySubPredicateTooltipUtils::getSheepPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.AXOLOTL.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.FOX.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.MOOSHROOM.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.RABBIT.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.HORSE.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.LLAMA.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.VILLAGER.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.PARROT.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.TROPICAL_FISH.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
        registry.registerEntitySubPredicateTooltip(EntitySubPredicates.SALMON.codec, EntitySubPredicateTooltipUtils::getVariantPredicateTooltip);
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
        registry.registerDataComponentTypeTooltip(DataComponents.ITEM_MODEL, DataComponentTooltipUtils::getResourceLocationTooltip);
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
        registry.registerDataComponentTypeTooltip(DataComponents.CONSUMABLE, DataComponentTooltipUtils::getConsumableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.USE_REMAINDER, DataComponentTooltipUtils::getUseRemainderTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.USE_COOLDOWN, DataComponentTooltipUtils::getUseCooldownTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DAMAGE_RESISTANT, DataComponentTooltipUtils::getDamageResistantTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TOOL, DataComponentTooltipUtils::getToolTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.ENCHANTABLE, DataComponentTooltipUtils::getEnchantableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.EQUIPPABLE, DataComponentTooltipUtils::getEquipableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.REPAIRABLE, DataComponentTooltipUtils::getRepairableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.GLIDER, DataComponentTooltipUtils::getEmptyTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.TOOLTIP_STYLE, DataComponentTooltipUtils::getResourceLocationTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.DEATH_PROTECTION, DataComponentTooltipUtils::getDeathProtectionTooltip);
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
        registry.registerDataComponentTypeTooltip(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, DataComponentTooltipUtils::getOminousBottleAmplifierTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.JUKEBOX_PLAYABLE, DataComponentTooltipUtils::getJukeboxPlayableTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.RECIPES, DataComponentTooltipUtils::getRecipesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LODESTONE_TRACKER, DataComponentTooltipUtils::getLodestoneTrackerTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FIREWORK_EXPLOSION, DataComponentTooltipUtils::getFireworkExplosionTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.FIREWORKS, DataComponentTooltipUtils::getFireworksTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.PROFILE, DataComponentTooltipUtils::getProfileTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.NOTE_BLOCK_SOUND, DataComponentTooltipUtils::getResourceLocationTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BANNER_PATTERNS, DataComponentTooltipUtils::getBannerPatternsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BASE_COLOR, DataComponentTooltipUtils::getBaseColorTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.POT_DECORATIONS, DataComponentTooltipUtils::getPotDecorationsTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CONTAINER, DataComponentTooltipUtils::getContainerTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BLOCK_STATE, DataComponentTooltipUtils::getBlockStateTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.BEES, DataComponentTooltipUtils::getBeesTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.LOCK, DataComponentTooltipUtils::getLockTooltip);
        registry.registerDataComponentTypeTooltip(DataComponents.CONTAINER_LOOT, DataComponentTooltipUtils::getContainerLootTooltip);

        registry.registerConsumeEffectTooltip(ApplyStatusEffectsConsumeEffect.class, ConsumeEffectTooltipUtils::getApplyEffectsTooltip);
        registry.registerConsumeEffectTooltip(RemoveStatusEffectsConsumeEffect.class, ConsumeEffectTooltipUtils::getRemoveEffectsTooltip);
        registry.registerConsumeEffectTooltip(ClearAllStatusEffectsConsumeEffect.class, ConsumeEffectTooltipUtils::getClearAllEffectsTooltip);
        registry.registerConsumeEffectTooltip(TeleportRandomlyConsumeEffect.class, ConsumeEffectTooltipUtils::getTeleportRandomlyTooltip);
        registry.registerConsumeEffectTooltip(PlaySoundConsumeEffect.class, ConsumeEffectTooltipUtils::getPlaySoundTooltip);


        registry.registerIngredientTooltip(Ingredient.class, IngredientTooltipUtils::getIngredientTooltip);

        registry.registerValueTooltip(LootPoolEntryType.class, RegistriesTooltipUtils::getEntryTypeTooltip);
        registry.registerValueTooltip(LootItemFunctionType.class, RegistriesTooltipUtils::getFunctionTypeTooltip);
        registry.registerValueTooltip(LootItemConditionType.class, RegistriesTooltipUtils::getConditionTypeTooltip);
        registry.registerValueTooltip(Block.class, RegistriesTooltipUtils::getBlockTooltip);
        registry.registerValueTooltip(Item.class, RegistriesTooltipUtils::getItemTooltip);
        registry.registerValueTooltip(EntityType.class, RegistriesTooltipUtils::getEntityTypeTooltip);
        registry.registerValueTooltip(BannerPattern.class, RegistriesTooltipUtils::getBannerPatternTooltip);
        registry.registerValueTooltip(BlockEntityType.class, RegistriesTooltipUtils::getBlockEntityTypeTooltip);
        registry.registerValueTooltip(Potion.class, RegistriesTooltipUtils::getPotionTooltip);
        registry.registerValueTooltip(MobEffect.class, RegistriesTooltipUtils::getMobEffectTooltip);
        registry.registerValueTooltip(LootNbtProviderType.class, RegistriesTooltipUtils::getLootNbtProviderTypeTooltip);
        registry.registerValueTooltip(Fluid.class, RegistriesTooltipUtils::getFluidTooltip);
        registry.registerValueTooltip(Enchantment.class, RegistriesTooltipUtils::getEnchantmentTooltip);
        registry.registerValueTooltip(Attribute.class, RegistriesTooltipUtils::getAttributeTooltip);
        registry.registerValueTooltip(DataComponentType.class, RegistriesTooltipUtils::getDataComponentTypeTooltip);
        registry.registerValueTooltip(Instrument.class, RegistriesTooltipUtils::getInstrumentTooltip);
        registry.registerValueTooltip(EntitySubPredicate.class, RegistriesTooltipUtils::getEntitySubPredicateTooltip);
        registry.registerValueTooltip(CatVariant.class, RegistriesTooltipUtils::getCatVariantTooltip);
        registry.registerValueTooltip(PaintingVariant.class, RegistriesTooltipUtils::getPaintingVariantTooltip);
        registry.registerValueTooltip(FrogVariant.class, RegistriesTooltipUtils::getFrogVariantTooltip);
        registry.registerValueTooltip(WolfVariant.class, RegistriesTooltipUtils::getWolfVariantTooltip);
        registry.registerValueTooltip(MapDecorationType.class, RegistriesTooltipUtils::getMapDecorationTypeTooltip);
        registry.registerValueTooltip(Biome.class, RegistriesTooltipUtils::getBiomeTooltip);
        registry.registerValueTooltip(Structure.class, RegistriesTooltipUtils::getStructureTooltip);
        registry.registerValueTooltip(TrimMaterial.class, RegistriesTooltipUtils::getTrimMaterialTooltip);
        registry.registerValueTooltip(TrimPattern.class, RegistriesTooltipUtils::getTrimPatternTooltip);
        registry.registerValueTooltip(JukeboxSong.class, RegistriesTooltipUtils::getJukeboxSongTooltip);
        registry.registerValueTooltip(SoundEvent.class, RegistriesTooltipUtils::getSoundEventTooltip);

        registry.registerValueTooltip(ResourceLocation.class, ValueTooltipUtils::getResourceLocationTooltip);
        registry.registerValueTooltip(Pair.class, ValueTooltipUtils::getPairTooltip);
        registry.registerValueTooltip(Holder.class, ValueTooltipUtils::getHolderTooltip);
        registry.registerValueTooltip(HolderSet.class, ValueTooltipUtils::getHolderSetTooltip);
        registry.registerValueTooltip(Optional.class, ValueTooltipUtils::getOptionalTooltip);
        registry.registerValueTooltip(StatePropertiesPredicate.class, ValueTooltipUtils::getStatePropertiesPredicateTooltip);
        registry.registerValueTooltip(DamageSourcePredicate.class, ValueTooltipUtils::getDamageSourcePredicateTooltip);
        registry.registerValueTooltip(TagPredicate.class, ValueTooltipUtils::getTagPredicateTooltip);
        registry.registerValueTooltip(EntityPredicate.class, ValueTooltipUtils::getEntityPredicateTooltip);
        registry.registerValueTooltip(EntityTypePredicate.class, ValueTooltipUtils::getEntityTypePredicateTooltip);
        registry.registerValueTooltip(DistancePredicate.class, ValueTooltipUtils::getDistancePredicateTooltip);
        registry.registerValueTooltip(LocationPredicate.class, ValueTooltipUtils::getLocationPredicateTooltip);
        registry.registerValueTooltip(LightPredicate.class, ValueTooltipUtils::getLightPredicateTooltip);
        registry.registerValueTooltip(BlockPredicate.class, ValueTooltipUtils::getBlockPredicateTooltip);
        registry.registerValueTooltip(NbtPredicate.class, ValueTooltipUtils::getNbtPredicateTooltip);
        registry.registerValueTooltip(FluidPredicate.class, ValueTooltipUtils::getFluidPredicateTooltip);
        registry.registerValueTooltip(MobEffectsPredicate.class, ValueTooltipUtils::getMobEffectPredicateTooltip);
        registry.registerValueTooltip(EntityFlagsPredicate.class, ValueTooltipUtils::getEntityFlagsPredicateTooltip);
        registry.registerValueTooltip(EntityEquipmentPredicate.class, ValueTooltipUtils::getEntityEquipmentPredicateTooltip);
        registry.registerValueTooltip(ItemPredicate.class, ValueTooltipUtils::getItemPredicateTooltip);
        registry.registerValueTooltip(EnchantmentPredicate.class, ValueTooltipUtils::getEnchantmentPredicateTooltip);
        registry.registerValueTooltip(EntitySubPredicate.class, ValueTooltipUtils::getEntitySubPredicateTooltip);
        registry.registerValueTooltip(BlockPos.class, ValueTooltipUtils::getBlockPosTooltip);
        registry.registerValueTooltip(CopyCustomDataFunction.CopyOperation.class, ValueTooltipUtils::getCopyOperationTooltip);
        registry.registerValueTooltip(CompoundTag.class, ValueTooltipUtils::getCompoundTagTooltip);
        registry.registerValueTooltip(ItemStack.class, ValueTooltipUtils::getItemStackTooltip);
        registry.registerValueTooltip(MinMaxBounds.Ints.class, ValueTooltipUtils::getMinMaxBoundsTooltip);
        registry.registerValueTooltip(MinMaxBounds.Doubles.class, ValueTooltipUtils::getMinMaxBoundsTooltip);
        registry.registerValueTooltip(ResourceKey.class, ValueTooltipUtils::getResourceKeyTooltip);
        registry.registerValueTooltip(TagKey.class, ValueTooltipUtils::getTagKeyTooltip);
        registry.registerValueTooltip(ApplyBonusCount.Formula.class, ValueTooltipUtils::getFormulaTooltip);
        registry.registerValueTooltip(Property.class, ValueTooltipUtils::getPropertyTooltip);
        registry.registerValueTooltip(SetAttributesFunction.Modifier.class, ValueTooltipUtils::getModifierTooltip);
        registry.registerValueTooltip(UUID.class, ValueTooltipUtils::getUUIDTooltip);
        registry.registerValueTooltip(NumberProvider.class, ValueTooltipUtils::getNumberProviderTooltip);
        registry.registerValueTooltip(Collection.class, ValueTooltipUtils::getCollectionTooltip);
        registry.registerValueTooltip(IntRange.class, ValueTooltipUtils::getIntRangeTooltip);
        registry.registerValueTooltip(Component.class, ValueTooltipUtils::getComponentTooltip);
        registry.registerValueTooltip(String.class, ValueTooltipUtils::getStringTooltip);
        registry.registerValueTooltip(Boolean.class, ValueTooltipUtils::getBooleanTooltip);
        registry.registerValueTooltip(Integer.class, ValueTooltipUtils::getIntegerTooltip);
        registry.registerValueTooltip(Long.class, ValueTooltipUtils::getLongTooltip);
        registry.registerValueTooltip(Float.class, ValueTooltipUtils::getFloatTooltip);
        registry.registerValueTooltip(Double.class, ValueTooltipUtils::getDoubleTooltip);
        registry.registerValueTooltip(Byte.class, ValueTooltipUtils::getByteTooltip);
        registry.registerValueTooltip(Enum.class, ValueTooltipUtils::getEnumTooltip);
        registry.registerValueTooltip(LocationPredicate.PositionPredicate.class, ValueTooltipUtils::getPositionPredicateTooltip);
        registry.registerValueTooltip(SetStewEffectFunction.EffectEntry.class, ValueTooltipUtils::getEffectEntryTooltip);
        registry.registerValueTooltip(ListOperation.class, ValueTooltipUtils::getListOperationTooltip);
        registry.registerValueTooltip(Filterable.class, ValueTooltipUtils::getFilterableTooltip);
        registry.registerValueTooltip(ItemFireworkExplosionPredicate.FireworkPredicate.class, ValueTooltipUtils::getFireworkPredicateTooltip);
        registry.registerValueTooltip(ContainerComponentManipulator.class, ValueTooltipUtils::getContainerComponentManipulatorTooltip);
        registry.registerValueTooltip(NbtPathArgument.NbtPath.class, ValueTooltipUtils::getNbtPathTooltip);
        registry.registerValueTooltip(DataComponentPredicate.class, ValueTooltipUtils::getDataComponentPredicateTooltip);
        registry.registerValueTooltip(TypedDataComponent.class, ValueTooltipUtils::getTypedDataComponentTooltip);
        registry.registerValueTooltip(ItemWrittenBookPredicate.PagePredicate.class, ValueTooltipUtils::getPagePredicateTooltip);
        registry.registerValueTooltip(ItemWritableBookPredicate.PagePredicate.class, ValueTooltipUtils::getPagePredicateTooltip);
        registry.registerValueTooltip(ItemAttributeModifiersPredicate.EntryPredicate.class, ValueTooltipUtils::getEntryPredicateTooltip);
        registry.registerValueTooltip(DataComponentPatch.class, ValueTooltipUtils::getDataComponentPatchTooltip);
        registry.registerValueTooltip(FireworkExplosion.class, ValueTooltipUtils::getFireworkExplosionTooltip);
        registry.registerValueTooltip(IntList.class, ValueTooltipUtils::getIntListTooltip);
        registry.registerValueTooltip(ItemAttributeModifiers.Entry.class, ValueTooltipUtils::getItemAttributeModifiersEntryTooltip);
        registry.registerValueTooltip(AttributeModifier.class, ValueTooltipUtils::getAttributeModifierTooltip);
        registry.registerValueTooltip(MobEffectInstance.class, ValueTooltipUtils::getMobEffectInstanceTooltip);
        registry.registerValueTooltip(Tool.Rule.class, ValueTooltipUtils::getRuleTooltip);
        registry.registerValueTooltip(MapDecorations.Entry.class, ValueTooltipUtils::getMapDecorationEntryTooltip);
        registry.registerValueTooltip(DataComponentMap.class, ValueTooltipUtils::getDataComponentMapTooltip);
        registry.registerValueTooltip(SuspiciousStewEffects.Entry.class, ValueTooltipUtils::getSuspiciousStewEffectEntryTooltip);
        registry.registerValueTooltip(GlobalPos.class, ValueTooltipUtils::getGlobalPosTooltip);
        registry.registerValueTooltip(BeehiveBlockEntity.Occupant.class, ValueTooltipUtils::getBeehiveBlockEntityOccupantTooltip);
        registry.registerValueTooltip(com.mojang.authlib.properties.Property.class, ValueTooltipUtils::getAuthPropertyTooltip);
        registry.registerValueTooltip(CollectionCountsPredicate.Entry.class, ValueTooltipUtils::getCollectionCountsPredicateEntryTooltip);
        registry.registerValueTooltip(BannerPatternLayers.class, ValueTooltipUtils::getBannerPatternLayersTooltip);
        registry.registerValueTooltip(BannerPatternLayers.Layer.class, ValueTooltipUtils::getBannerPatternLayerTooltip);
        registry.registerValueTooltip(GameTypePredicate.class, ValueTooltipUtils::getGameTypePredicateTooltip);
        registry.registerValueTooltip(LevelBasedValue.class, ValueTooltipUtils::getLevelBasedValueTooltip);
        registry.registerValueTooltip(EntityPredicate.LocationWrapper.class, ValueTooltipUtils::getLocationWrapperTooltip);
        registry.registerValueTooltip(MovementPredicate.class, ValueTooltipUtils::getMovementPredicateTooltip);
        registry.registerValueTooltip(SlotsPredicate.class, ValueTooltipUtils::getSlotPredicateTooltip);
        registry.registerValueTooltip(EitherHolder.class, ValueTooltipUtils::getEitherHolderTooltip);
        registry.registerValueTooltip(InputPredicate.class, ValueTooltipUtils::getInputPredicateTooltip);
        registry.registerValueTooltip(ListOperation.StandAlone.class, ValueTooltipUtils::getStandaloneTooltip);

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

        registry.registerItemListing(VillagerTrades.DyedArmorForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.EmeraldForItems.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.EmeraldsForVillagerTypeItem.class, EmeraldsForVillagerTypeItemNode::new);
        registry.registerItemListing(VillagerTrades.EnchantBookForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.EnchantedItemForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.ItemsAndEmeraldsToItems.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.ItemsForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.SuspiciousStewForEmerald.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.TippedArrowForItemsAndEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(VillagerTrades.TreasureMapForEmeralds.class, TradeUtils::getNode);

        registry.registerItemListingCollector(VillagerTrades.DyedArmorForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EmeraldForItems.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EmeraldsForVillagerTypeItem.class, EmeraldsForVillagerTypeItemNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EnchantBookForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EnchantedItemForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.ItemsAndEmeraldsToItems.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.ItemsForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.SuspiciousStewForEmerald.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.TippedArrowForItemsAndEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(VillagerTrades.TreasureMapForEmeralds.class, TradeUtils::collectItems);
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
