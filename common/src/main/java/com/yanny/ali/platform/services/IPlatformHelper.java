package com.yanny.ali.platform.services;

import com.yanny.ali.api.IPlugin;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface IPlatformHelper {
        List<IPlugin> getPlugins();

        Path getConfiguration();

        Optional<Holder<Item>> getSpawnEggItem(EntityType<?> entityType);
}