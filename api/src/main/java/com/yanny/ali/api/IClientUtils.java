package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IClientUtils extends ICommonUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<LootPoolEntryContainer> entries, int x, int y,
                                                 List<LootItemFunction> functions, List<LootItemCondition> conditions);

    <T extends LootItemCondition> List<Component> getConditionTooltip(IClientUtils utils, int pad, T condition);

    <T extends LootItemFunction> List<Component> getFunctionTooltip(IClientUtils utils, int pad, T function);

    <T extends DataComponentPredicate> List<Component> getDataComponenetPredicateTooltip(IClientUtils utils, int pad, DataComponentPredicate.Type<?> type, T predicate);

    <T extends EntitySubPredicate> List<Component> getEntitySubPredicateTooltip(IClientUtils utils, int pad, T predicate);

    List<Component> getDataComponentTypeTooltip(IClientUtils utils, int pad, DataComponentType<?> type, Object value);

    <T extends ConsumeEffect> List<Component> getConsumeEffectTooltip(IClientUtils utils, int pad, T effect);

    <T extends LootItemFunction> void applyCountModifier(IClientUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count);

    <T extends LootItemCondition> void applyChanceModifier(IClientUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance);

    <T extends LootItemFunction> ItemStack applyItemStackModifier(IClientUtils utils, T function, ItemStack itemStack);

    Rect getBounds(IClientUtils registry, List<LootPoolEntryContainer> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(LootPoolEntryContainer entry);

    LootContext getLootContext();

    List<Item> getItems(ResourceKey<LootTable> location);

    RangeValue convertNumber(IClientUtils utils, @Nullable NumberProvider numberProvider);

    @Nullable
    HolderLookup.Provider lookupProvider();
}
