package com.yanny.ali.compatibility.common;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface IType {
    LootTable entry();
    List<Item> items();
}
