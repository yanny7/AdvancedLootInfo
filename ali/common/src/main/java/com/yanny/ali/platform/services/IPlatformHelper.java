package com.yanny.ali.platform.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yanny.ali.api.IPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {
        List<LootPool> getLootPools(LootTable table);

        List<IPlugin> getPlugins();

        Path getConfiguration();

        SpawnEggItem getSpawnEggItem(EntityType<?> entityType);

        LootTable getLootTable(Gson gson, ResourceLocation location, JsonElement json);
}