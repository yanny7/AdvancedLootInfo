package com.yanny.ali.compatibility.common;

import com.yanny.ali.plugin.entry.LootTableEntry;
import net.minecraft.world.entity.Entity;

public record EntityLootType(Entity entity, LootTableEntry entry) implements IType {
}
