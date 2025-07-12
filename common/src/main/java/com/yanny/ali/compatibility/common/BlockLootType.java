package com.yanny.ali.compatibility.common;

import com.yanny.ali.api.IDataNode;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record BlockLootType(Block block, IDataNode entry, List<Item> items) implements IType {
}
