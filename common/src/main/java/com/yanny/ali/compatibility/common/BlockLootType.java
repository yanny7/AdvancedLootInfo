package com.yanny.ali.compatibility.common;

import com.yanny.ali.plugin.entry.LootTableEntry;
import net.minecraft.world.level.block.Block;

public record BlockLootType(Block block, LootTableEntry entry) implements IType {
}
