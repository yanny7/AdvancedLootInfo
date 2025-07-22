package com.yanny.ali.manager;

import com.yanny.ali.api.IDataNode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Map;

@FunctionalInterface
public interface IOnDoneListener {
    void onDone(Map<ResourceKey<LootTable>, IDataNode> lootData);
}
