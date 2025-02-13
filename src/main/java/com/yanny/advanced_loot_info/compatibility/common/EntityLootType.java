package com.yanny.advanced_loot_info.compatibility.common;

import com.yanny.advanced_loot_info.loot.LootTableEntry;
import net.minecraft.world.entity.Entity;

public record EntityLootType(Entity entity, LootTableEntry entry) implements IType {
}
