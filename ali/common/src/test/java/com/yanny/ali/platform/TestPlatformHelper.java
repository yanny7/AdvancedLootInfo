package com.yanny.ali.platform;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.platform.services.IPlatformHelper;
import com.yanny.ali.plugin.Plugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class TestPlatformHelper implements IPlatformHelper {
    @NotNull
    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new Plugin());
    }

    @NotNull
    @Override
    public Path getConfiguration() {
        return null;
    }

    @Nullable
    @Override
    public SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        return null;
    }
}
