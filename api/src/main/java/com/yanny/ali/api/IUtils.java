package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
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

public interface IUtils {
    Pair<List<IEntryWidget>, Rect> createWidgets(IWidgetUtils registry, List<LootPoolEntryContainer> entries, int x, int y,
                                                 List<LootItemFunction> functions, List<LootItemCondition> conditions);

    List<Component> getConditionTooltip(IUtils utils, int pad, LootItemCondition condition);
    List<Component> getFunctionTooltip(IUtils utils, int pad, LootItemFunction function);
    List<Component> getItemSubPredicateTooltip(IUtils utils, int pad, ItemSubPredicate.Type<?> type, ItemSubPredicate predicate);
    List<Component> getEntitySubPredicateTooltip(IUtils utils, int pad, EntitySubPredicate predicate);
    List<Item> collectItems(IUtils utils, LootPoolEntryContainer entry);

    Rect getBounds(IUtils registry, List<LootPoolEntryContainer> entries, int x, int y);

    @Nullable
    WidgetDirection getWidgetDirection(LootPoolEntryContainer entry);

    LootContext getLootContext();

    @Nullable
    LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either);

    RangeValue convertNumber(IUtils utils, @Nullable NumberProvider numberProvider);
}
