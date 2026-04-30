package com.yanny.ali.platform.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yanny.aci.platform.ICorePlatformHelper;
import com.yanny.ali.api.IPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public interface IPlatformHelper extends ICorePlatformHelper<IPlugin> {
        List<LootPool> getLootPools(LootTable table);

        SpawnEggItem getSpawnEggItem(EntityType<?> entityType);

        LootTable getLootTable(Gson gson, ResourceLocation location, JsonElement json);
}