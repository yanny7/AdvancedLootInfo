package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.ICoreServerUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.resources.ResourceLocation;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Map;

public interface IServerUtils extends ICoreServerUtils<IServerUtils>, ICommonUtils {
    @NotNull
    <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry);

    @NotNull
    <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function);

    @NotNull
    <T extends LootPoolEntryContainer> IServerRegistry.EntryFactory<T> getEntryFactory(IServerUtils utils, T type);

    @NotNull
    <T extends LootItemFunction> TooltipNode getFunctionTooltip(IServerUtils utils, T function);

    @NotNull
    <T extends LootItemCondition> TooltipNode getConditionTooltip(IServerUtils utils, T condition);

    @NotNull
    <T extends Ingredient> TooltipNode getIngredientTooltip(IServerUtils utils, T ingredient);

    @NotNull
    <T extends ItemSubPredicate> TooltipNode getItemSubPredicateTooltip(IServerUtils utils, T predicate);

    @NotNull
    <T extends DataComponentPredicate> TooltipNode getDataComponentPredicateTooltip(IServerUtils utils, T predicate);

    @NotNull
    TooltipNode getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value);

    <T extends ConsumeEffect> ITooltipNode getConsumeEffectTooltip(IServerUtils utils, T effect);

    <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count);

    <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance);

    @NotNull
    <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack);

    @NotNull
    <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, TooltipNode condition);

    @NotNull
    <T extends VillagerTrades.ItemListing> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T entry);

    @NotNull
    RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider);

    @Nullable
    LootContext getLootContext();

    @Nullable
    ResourceLocation getCurrentLootTable();

    @Nullable
    LootTable getLootTable(Either<ResourceLocation, LootTable> either);
}
