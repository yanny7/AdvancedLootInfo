package com.yanny.ali.compatibility.common;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public record BlockLootType(Block block, LootTable entry, List<Item> items) implements IType {
}
