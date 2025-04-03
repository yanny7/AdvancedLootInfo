package com.yanny.ali.compatibility.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTable;

public record EntityLootType(Entity entity, LootTable entry) implements IType {
}
