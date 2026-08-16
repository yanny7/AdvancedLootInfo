package com.yanny.ali.api;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface IItemNode {
    /**
     * What the slot stands for: one stack - the entry's item with all of its functions already applied - or the tag
     * the entry picks an item from.
     */
    @NotNull
    Either<ItemStack, TagKey<? extends ItemLike>> getItem();

    /**
     * The stacks this node actually stands for - the single stack, or the tag's members resolved against the current
     * registry. Client-side this is what the collected recipe outputs and the reverse index use.
     *
     * <p>An empty stack stands for nothing and is never reported.
     */
    @NotNull
    List<ItemStack> getItems();

    /**
     * Drops the members the recipe viewer hides, so that {@link #getItems()} keeps agreeing with what is rendered. A
     * node standing for a single stack has nothing to narrow and can ignore it.
     */
    default void retainItems(Predicate<ItemStack> isVisible) {}

    @NotNull
    List<LootItemCondition> getConditions();

    @NotNull
    List<LootItemFunction> getFunctions();

    @NotNull
    RangeValue getCount();

    float getChance();

    /** Client-side flag: whether the entry is gated by conditions other than plain drop chance. */
    boolean hasPredicates();
}
