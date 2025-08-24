package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Map;

public interface IServerUtils extends ICommonUtils {
    <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry);

    <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function);

    <T extends LootPoolEntryContainer> IServerRegistry.EntryFactory<T> getEntryFactory(IServerUtils utils, T type);

    <T extends LootItemFunction> ITooltipNode getFunctionTooltip(IServerUtils utils, T function);

    <T extends LootItemCondition> ITooltipNode getConditionTooltip(IServerUtils utils, T condition);

    <T extends Ingredient> ITooltipNode getIngredientTooltip(IServerUtils utils, T ingredient);

    <T extends DataComponentPredicate> ITooltipNode getDataComponentPredicateTooltip(IServerUtils utils, T predicate);

    <T extends EntitySubPredicate> ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, T predicate);

    ITooltipNode getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value);

    <T extends ConsumeEffect> ITooltipNode getConsumeEffectTooltip(IServerUtils utils, T effect);

    <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count);

    <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance);

    <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack);

    <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, List<ITooltipNode> conditions);

    <T extends VillagerTrades.ItemListing> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T entry);

    RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider);

    @Nullable
    ServerLevel getServerLevel();

    LootContext getLootContext();

    LootTable getLootTable(Either<ResourceKey<LootTable>, LootTable> either);

    @Nullable
    HolderLookup.Provider lookupProvider();
}
