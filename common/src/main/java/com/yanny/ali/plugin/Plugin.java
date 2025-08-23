package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.client.widget.*;
import com.yanny.ali.plugin.client.widget.trades.EmeraldForVillagerTypeItemWidget;
import com.yanny.ali.plugin.client.widget.trades.ItemListingWidget;
import com.yanny.ali.plugin.client.widget.trades.TradeLevelWidget;
import com.yanny.ali.plugin.client.widget.trades.TradeWidget;
import com.yanny.ali.plugin.common.EntityUtils;
import com.yanny.ali.plugin.common.nodes.*;
import com.yanny.ali.plugin.common.trades.*;
import com.yanny.ali.plugin.server.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
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

        registry.registerWidget(TradeNode.ID, TradeWidget::new);
        registry.registerWidget(TradeLevelNode.ID, TradeLevelWidget::new);
        registry.registerWidget(ItemStackNode.ID, ItemStackWidget::new);
        registry.registerWidget(EmeraldForItemsNode.ID, ItemListingWidget::new);
        registry.registerWidget(ItemsForEmeraldsNode.ID, ItemListingWidget::new);
        registry.registerWidget(ItemsAndEmeraldsToItemsNode.ID, ItemListingWidget::new);
        registry.registerWidget(SuspiciousStewForEmeraldNode.ID, ItemListingWidget::new);
        registry.registerWidget(EnchantedItemForEmeraldsNode.ID, ItemListingWidget::new);
        registry.registerWidget(TippedArrowForItemsAndEmeraldsNode.ID, ItemListingWidget::new);
        registry.registerWidget(TreasureMapForEmeraldsNode.ID, ItemListingWidget::new);
        registry.registerWidget(EnchantBookForEmeraldsNode.ID, ItemListingWidget::new);
        registry.registerWidget(DyedArmorForEmeraldsNode.ID, ItemListingWidget::new);
        registry.registerWidget(EmeraldsForVillagerTypeItemNode.ID, EmeraldForVillagerTypeItemWidget::new);

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

        registry.registerNode(TradeNode.ID, TradeNode::new);
        registry.registerNode(TradeLevelNode.ID, TradeLevelNode::new);
        registry.registerNode(ItemStackNode.ID, ItemStackNode::new);
        registry.registerNode(EmeraldForItemsNode.ID, EmeraldForItemsNode::new);
        registry.registerNode(ItemsForEmeraldsNode.ID, ItemsForEmeraldsNode::new);
        registry.registerNode(ItemsAndEmeraldsToItemsNode.ID, ItemsAndEmeraldsToItemsNode::new);
        registry.registerNode(SuspiciousStewForEmeraldNode.ID, SuspiciousStewForEmeraldNode::new);
        registry.registerNode(EnchantedItemForEmeraldsNode.ID, EnchantedItemForEmeraldsNode::new);
        registry.registerNode(TippedArrowForItemsAndEmeraldsNode.ID, TippedArrowForItemsAndEmeraldsNode::new);
        registry.registerNode(TreasureMapForEmeraldsNode.ID, TreasureMapForEmeraldsNode::new);
        registry.registerNode(EnchantBookForEmeraldsNode.ID, EnchantBookForEmeraldsNode::new);
        registry.registerNode(DyedArmorForEmeraldsNode.ID, DyedArmorForEmeraldsNode::new);
        registry.registerNode(EmeraldsForVillagerTypeItemNode.ID, EmeraldsForVillagerTypeItemNode::new);
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

        registry.registerItemListing(VillagerTrades.DyedArmorForEmeralds.class, DyedArmorForEmeraldsNode::new);
        registry.registerItemListing(VillagerTrades.EmeraldForItems.class, EmeraldForItemsNode::new);
        registry.registerItemListing(VillagerTrades.EmeraldsForVillagerTypeItem.class, EmeraldsForVillagerTypeItemNode::new);
        registry.registerItemListing(VillagerTrades.EnchantBookForEmeralds.class, EnchantBookForEmeraldsNode::new);
        registry.registerItemListing(VillagerTrades.EnchantedItemForEmeralds.class, EnchantedItemForEmeraldsNode::new);
        registry.registerItemListing(VillagerTrades.ItemsAndEmeraldsToItems.class, ItemsAndEmeraldsToItemsNode::new);
        registry.registerItemListing(VillagerTrades.ItemsForEmeralds.class, ItemsForEmeraldsNode::new);
        registry.registerItemListing(VillagerTrades.SuspiciousStewForEmerald.class, SuspiciousStewForEmeraldNode::new);
        registry.registerItemListing(VillagerTrades.TippedArrowForItemsAndEmeralds.class, TippedArrowForItemsAndEmeraldsNode::new);
        registry.registerItemListing(VillagerTrades.TreasureMapForEmeralds.class, TreasureMapForEmeraldsNode::new);

        registry.registerItemListingCollector(VillagerTrades.DyedArmorForEmeralds.class, DyedArmorForEmeraldsNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EmeraldForItems.class, EmeraldForItemsNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EmeraldsForVillagerTypeItem.class, EmeraldsForVillagerTypeItemNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EnchantBookForEmeralds.class, EnchantBookForEmeraldsNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.EnchantedItemForEmeralds.class, EnchantedItemForEmeraldsNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.ItemsAndEmeraldsToItems.class, ItemsAndEmeraldsToItemsNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.ItemsForEmeralds.class, ItemsForEmeraldsNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.SuspiciousStewForEmerald.class, SuspiciousStewForEmeraldNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.TippedArrowForItemsAndEmeralds.class, TippedArrowForItemsAndEmeraldsNode::collectItems);
        registry.registerItemListingCollector(VillagerTrades.TreasureMapForEmeralds.class, TreasureMapForEmeraldsNode::collectItems);
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
