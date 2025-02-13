package com.yanny.advanced_loot_info.compatibility.common;

import com.yanny.advanced_loot_info.loot.LootTableEntry;

public record GameplayLootType(LootTableEntry entry, String id) implements IType {
}
