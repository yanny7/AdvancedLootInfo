package com.yanny.ali.platform.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yanny.aci.platform.ICorePlatformHelper;
import com.yanny.ali.api.IPlugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

import java.util.List;

public interface IPlatformHelper extends ICorePlatformHelper<IPlugin> {
        List<IPlugin> getPlugins();

        SpawnEggItem getSpawnEggItem(EntityType<?> entityType);
}