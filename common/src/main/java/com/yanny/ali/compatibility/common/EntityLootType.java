package com.yanny.ali.compatibility.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public record EntityLootType(Entity entity, LootTable entry, List<Item> items) implements IType {
}
