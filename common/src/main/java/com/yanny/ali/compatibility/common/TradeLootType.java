package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public record TradeLootType(Set<Block> pois, Set<Item> accepts, IDataNode entry, String id, List<ItemStack> inputs, List<ItemStack> outputs) implements IType {
}
