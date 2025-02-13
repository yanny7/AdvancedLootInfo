package com.yanny.advanced_loot_info.compatibility.common;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import net.minecraft.world.level.block.Block;

public record BlockLootType(Block block, LootTableEntry entry) implements IType {
}
