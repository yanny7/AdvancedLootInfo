package com.yanny.ali.compatibility.common;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

public record BlockLootType(Block block, LootTable entry) implements IType {
}
