package com.yanny.ali.compatibility.common;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public record GameplayLootType(LootTable entry, String id, List<Item> items) implements IType {
}
