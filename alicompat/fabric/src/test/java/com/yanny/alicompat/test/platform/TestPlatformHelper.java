package com.yanny.alicompat.test.platform;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.platform.services.IPlatformHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TestPlatformHelper implements IPlatformHelper {
    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new com.yanny.ali.plugin.Plugin(), new com.yanny.alicompat.fabric.Plugin());
    }

    @Override
    public Path getConfiguration() {
        return null;
    }

    @Override
    public Optional<Holder<Item>> getSpawnEggItem(EntityType<?> entityType) {
        return Optional.empty();
    }
}
