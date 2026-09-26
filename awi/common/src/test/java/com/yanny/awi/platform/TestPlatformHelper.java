package com.yanny.awi.platform;

import com.yanny.awi.api.IPlugin;
import com.yanny.awi.platform.services.IPlatformHelper;
import com.yanny.awi.plugin.Plugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

import java.nio.file.Path;
import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    public static Path CONFIG_DIR = null;

    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new Plugin());
    }

    @Override
    public Path getConfiguration() {
        return CONFIG_DIR;
    }

    @Override
    public SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        return null;
    }
}
