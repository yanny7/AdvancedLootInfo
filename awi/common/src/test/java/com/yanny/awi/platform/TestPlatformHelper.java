package com.yanny.awi.platform;

import com.yanny.awi.api.IPlugin;
import com.yanny.awi.platform.services.IPlatformHelper;
import com.yanny.awi.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TestPlatformHelper implements IPlatformHelper {
    public static Path CONFIG_DIR = null;

    @NotNull
    @Override
    public List<IPlugin> getPlugins() {
        return List.of(new Plugin());
    }

    @NotNull
    @Override
    public Path getConfiguration() {
        return CONFIG_DIR;
    }

    @Override
    public Optional<Holder<Item>> getSpawnEggItem(EntityType<?> entityType) {
        return Optional.empty();
    }
}
