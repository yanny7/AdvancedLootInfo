package com.yanny.ali.compatibility.common;

import net.minecraft.world.level.storage.loot.LootTable;

public record GameplayLootType(LootTable entry, String id) implements IType {
}
