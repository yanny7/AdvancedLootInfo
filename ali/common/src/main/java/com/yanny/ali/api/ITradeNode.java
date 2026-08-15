package com.yanny.ali.api;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A single villager trade. Implemented so that the flat input/output lists a recipe viewer indexes can be derived from
 * the node tree itself - the tree is the only place that knows which side of the trade a stack sat on, and a plain
 * {@link IItemNode} walk cannot tell a cost from a result.
 */
public interface ITradeNode {
    /** The stacks the player has to hand over. */
    @NotNull
    List<ItemStack> getInputItems();

    /** The stacks the player gets back. */
    @NotNull
    List<ItemStack> getOutputItems();
}
