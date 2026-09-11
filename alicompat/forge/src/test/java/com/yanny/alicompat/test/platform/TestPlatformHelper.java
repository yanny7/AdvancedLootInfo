package com.yanny.alicompat.test.platform;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.platform.services.IPlatformHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

import java.nio.file.Path;
import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new com.yanny.ali.plugin.Plugin(), new com.yanny.alicompat.forge.Plugin());
    }

    @Override
    public Path getConfiguration() {
        return null;
    }

    @Override
    public HolderLookup.Provider getLookupProvider() {
        return null;
    }

    @Override
    public SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        return null;
    }
}
