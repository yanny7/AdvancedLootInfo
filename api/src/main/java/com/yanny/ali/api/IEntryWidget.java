package com.yanny.ali.api;

import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public interface IEntryWidget extends IWidget {
    LootPoolEntryContainer getLootEntry();
}
