package com.yanny.ali.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IClientUtils extends ICommonUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<LootPoolEntryContainer> entries, int x, int y,
                                                 List<LootItemFunction> functions, List<LootItemCondition> conditions);

    <T extends LootItemCondition> List<Component> getConditionTooltip(IClientUtils utils, int pad, T condition);
    <T extends LootItemFunction> List<Component> getFunctionTooltip(IClientUtils utils, int pad, T function);
    <T extends ItemSubPredicate> List<Component> getItemSubPredicateTooltip(IClientUtils utils, int pad, ItemSubPredicate.Type<?> type, T predicate);
    <T extends EntitySubPredicate> List<Component> getEntitySubPredicateTooltip(IClientUtils utils, int pad, T predicate);

    Rect getBounds(IClientUtils registry, List<LootPoolEntryContainer> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(LootPoolEntryContainer entry);

    LootContext getLootContext();

    List<Item> getItems(ResourceKey<LootTable> location);

    RangeValue convertNumber(IClientUtils utils, @Nullable NumberProvider numberProvider);
}
