package com.yanny.ali.platform.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yanny.ali.api.IPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {
        List<IPlugin> getPlugins();

        Path getConfiguration();

        SpawnEggItem getSpawnEggItem(EntityType<?> entityType);

        LootTable getLootTable(Gson gson, ResourceLocation location, JsonElement json);
}