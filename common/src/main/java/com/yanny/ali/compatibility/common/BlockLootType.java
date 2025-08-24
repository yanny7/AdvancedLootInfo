package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record BlockLootType(Block block, IDataNode entry, List<ItemStack> inputs, List<ItemStack> outputs) implements IType {
}
