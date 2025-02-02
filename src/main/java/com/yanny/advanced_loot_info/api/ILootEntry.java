package com.yanny.advanced_loot_info.api;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ILootEntry {
    @NotNull
    default List<Item> collectItems() {
        return List.of();
    }
}
