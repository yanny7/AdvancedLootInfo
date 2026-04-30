package com.yanny.ali.platform.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yanny.aci.platform.ICorePlatformHelper;
import com.yanny.ali.api.IPlugin;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public interface IPlatformHelper extends ICorePlatformHelper<IPlugin> {
        List<IPlugin> getPlugins();

        Optional<Holder<Item>> getSpawnEggItem(EntityType<?> entityType);
}