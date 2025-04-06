package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.widget.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public void register(IRegistry registry) {
        registry.registerWidget(LootItem.class, WidgetDirection.HORIZONTAL, ItemWidget::new, ItemWidget::getBounds);
        registry.registerWidget(EmptyLootItem.class, WidgetDirection.HORIZONTAL, EmptyWidget::new, EmptyWidget::getBounds);
        registry.registerWidget(NestedLootTable.class, WidgetDirection.VERTICAL, ReferenceWidget::new, ReferenceWidget::getBounds);
        registry.registerWidget(DynamicLoot.class, WidgetDirection.VERTICAL, DynamicWidget::new, DynamicWidget::getBounds);
        registry.registerWidget(TagEntry.class, WidgetDirection.HORIZONTAL, TagWidget::new, TagWidget::getBounds);
        registry.registerWidget(AlternativesEntry.class, WidgetDirection.VERTICAL, AlternativesWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(SequentialEntry.class, WidgetDirection.VERTICAL, SequentialWidget::new, CompositeWidget::getBounds);
        registry.registerWidget(EntryGroup.class, WidgetDirection.VERTICAL, GroupWidget::new, CompositeWidget::getBounds);

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
        registry.registerFunctionTooltip(CopyCustomDataFunction.class, FunctionTooltipUtils::getCopyCustomDataTooltip);
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
        registry.registerFunctionTooltip(SetCustomDataFunction.class, FunctionTooltipUtils::getSetCustomDataTooltip);
        registry.registerFunctionTooltip(SetPotionFunction.class, FunctionTooltipUtils::getSetPotionTooltip);
        registry.registerFunctionTooltip(SetStewEffectFunction.class, FunctionTooltipUtils::getSetStewEffectTooltip);

        registry.registerNumberProvider(getKey(NumberProviders.CONSTANT), Plugin::convertConstant);
        registry.registerNumberProvider(getKey(NumberProviders.UNIFORM), Plugin::convertUniform);
        registry.registerNumberProvider(getKey(NumberProviders.BINOMIAL), Plugin::convertBinomial);
        registry.registerNumberProvider(getKey(NumberProviders.SCORE), Plugin::convertScore);

        registry.registerItemCollector(LootItem.class, Plugin::collectItems);
        registry.registerItemCollector(TagEntry.class, Plugin::collectTags);
        registry.registerItemCollector(AlternativesEntry.class, Plugin::collectComposite);
        registry.registerItemCollector(EntryGroup.class, Plugin::collectComposite);
        registry.registerItemCollector(SequentialEntry.class, Plugin::collectComposite);
        registry.registerItemCollector(EmptyLootItem.class, Plugin::collectEmpty);
        registry.registerItemCollector(DynamicLoot.class, Plugin::collectEmpty);
        registry.registerItemCollector(NestedLootTable.class, Plugin::collectReference);
    }

    @Unmodifiable
    @NotNull
    private static List<Item> collectEmpty(IUtils utils, LootPoolEntryContainer entry) {
        return List.of();
    }

    @Unmodifiable
    @NotNull
    private static List<Item> collectItems(IUtils utils, LootPoolEntryContainer entry) {
        return List.of(((LootItem) entry).item.value());
    }

    @Unmodifiable
    @NotNull
    private static List<Item> collectTags(IUtils utils, LootPoolEntryContainer entry) {
        return BuiltInRegistries.ITEM.getTag(((TagEntry) entry).tag).map((tag) -> tag.stream().map(Holder::value).toList()).orElse(List.of());
    }

    @NotNull
    private static List<Item> collectComposite(IUtils utils, LootPoolEntryContainer entry) {
        List<Item> items = new LinkedList<>();

        for (LootPoolEntryContainer child : ((CompositeEntryBase) entry).children) {
            items.addAll(utils.collectItems(utils, child));
        }

        return items;
    }

    @NotNull
    private static List<Item> collectReference(IUtils utils, LootPoolEntryContainer entry) {
        List<Item> items = new LinkedList<>();

        LootTable lootTable = utils.getLootTable(((NestedLootTable) entry).contents);

        if (lootTable != null) {
            items.addAll(TooltipUtils.collectItems(utils, lootTable));
        }

        return items;
    }

    @NotNull
    private static RangeValue convertConstant(IUtils utils, NumberProvider numberProvider) {
        return new RangeValue(numberProvider.getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertUniform(IUtils utils, NumberProvider numberProvider) {
        UniformGenerator uniformGenerator = (UniformGenerator) numberProvider;
        return new RangeValue(utils.convertNumber(utils, uniformGenerator.min()).min(),
                utils.convertNumber(utils, uniformGenerator.max()).max());
    }

    @NotNull
    private static RangeValue convertBinomial(IUtils utils, NumberProvider numberProvider) {
        BinomialDistributionGenerator binomialGenerator = (BinomialDistributionGenerator) numberProvider;
        return new RangeValue(0, binomialGenerator.n().getFloat(utils.getLootContext()));
    }

    @NotNull
    private static RangeValue convertScore(IUtils utils, NumberProvider numberProvider) {
        return new RangeValue(true, false);
    }

    @NotNull
    static ResourceLocation getKey(LootNumberProviderType key) {
        return getKey(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, key);
    }

    @NotNull
    private static <T> ResourceLocation getKey(Registry<T> registry, T key) {
        ResourceLocation location = registry.getKey(key);
        return Objects.requireNonNull(location);
    }
}
