package com.yanny.ali.compatibility.common;

import com.yanny.ali.plugin.entry.LootTableEntry;

public record GameplayLootType(LootTableEntry entry, String id) implements IType {
}
