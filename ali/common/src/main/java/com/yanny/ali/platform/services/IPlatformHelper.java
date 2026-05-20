package com.yanny.ali.platform.services;

import com.yanny.aci.platform.ICorePlatformHelper;
import com.yanny.ali.api.IPlugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

public interface IPlatformHelper extends ICorePlatformHelper<IPlugin> {
        @Nullable
        SpawnEggItem getSpawnEggItem(EntityType<?> entityType);
}