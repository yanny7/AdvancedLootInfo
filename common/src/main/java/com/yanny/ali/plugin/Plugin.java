package com.yanny.ali.plugin;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.client.widget.trades.ItemListingWidget;
import com.yanny.ali.plugin.client.widget.trades.SubTradesWidget;
import com.yanny.ali.plugin.client.widget.trades.TradeLevelWidget;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import com.yanny.ali.plugin.common.EntityUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.common.tooltip.*;
import com.yanny.ali.plugin.common.trades.*;
import com.yanny.ali.plugin.server.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
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
        registry.registerWidget(AlternativesNode.ID, AlternativesWidget::new);
        registry.registerWidget(SequenceNode.ID, SequentialWidget::new);
        registry.registerWidget(GroupNode.ID, GroupWidget::new);
        registry.registerWidget(MissingNode.ID, MissingWidget::new);

        registry.registerWidget(TradeNode.ID, TradeWidget::new);
        registry.registerWidget(TradeLevelNode.ID, TradeLevelWidget::new);
        registry.registerWidget(SubTradesNode.ID, SubTradesWidget::new);
        registry.registerWidget(ItemsToItemsNode.ID, ItemListingWidget::new);

        registry.registerDataNode(LootTableNode.ID, LootTableNode::new);
        registry.registerDataNode(LootPoolNode.ID, LootPoolNode::new);
        registry.registerDataNode(ItemNode.ID, ItemNode::new);
        registry.registerDataNode(AlternativesNode.ID, AlternativesNode::new);
        registry.registerDataNode(SequenceNode.ID, SequenceNode::new);
        registry.registerDataNode(GroupNode.ID, GroupNode::new);
        registry.registerDataNode(EmptyNode.ID, EmptyNode::new);
        registry.registerDataNode(DynamicNode.ID, DynamicNode::new);
        registry.registerDataNode(ReferenceNode.ID, ReferenceNode::new);
        registry.registerDataNode(MissingNode.ID, MissingNode::new);

        registry.registerDataNode(TradeNode.ID, TradeNode::new);
        registry.registerDataNode(TradeLevelNode.ID, TradeLevelNode::new);
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
        registry.registerItemCollector(LootTableReference.class, ItemCollectorUtils::collectReference);

        registry.registerItemCollector(SmeltItemFunction.class, ItemCollectorUtils::collectFurnaceSmelt);

        registry.registerNumberProvider(ConstantValue.class, Plugin::convertConstant);
        registry.registerNumberProvider(UniformGenerator.class, Plugin::convertUniform);
        registry.registerNumberProvider(BinomialDistributionGenerator.class, Plugin::convertBinomial);
        registry.registerNumberProvider(ScoreboardValue.class, Plugin::convertScore);

        registry.registerEntry(LootItem.class, NodeUtils::getItemNode);
        registry.registerEntry(TagEntry.class, NodeUtils::getTagNode);
        registry.registerEntry(AlternativesEntry.class, NodeUtils::getAlternativesNode);
        registry.registerEntry(SequentialEntry.class, NodeUtils::getSequenceNode);
        registry.registerEntry(EntryGroup.class, NodeUtils::getGroupNode);
        registry.registerEntry(EmptyLootItem.class, NodeUtils::getEmptyNode);
        registry.registerEntry(DynamicLoot.class, NodeUtils::getDynamicNode);
        registry.registerEntry(LootTableReference.class, NodeUtils::getReferenceNode);

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

        registry.registerValueTooltip(ResourceLocation.class, ValueTooltipUtils::getResourceLocationTooltip);
        registry.registerValueTooltip(Pair.class, ValueTooltipUtils::getPairTooltip);
        registry.registerValueTooltip(Holder.class, ValueTooltipUtils::getHolderTooltip);
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
        registry.registerValueTooltip(CopyNbtFunction.CopyOperation.class, ValueTooltipUtils::getCopyOperationTooltip);
        registry.registerValueTooltip(CompoundTag.class, ValueTooltipUtils::getCompoundTagTooltip);
        registry.registerValueTooltip(PlayerPredicate.AdvancementPredicate.class, ValueTooltipUtils::getAdvancementPredicateTooltip);
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
