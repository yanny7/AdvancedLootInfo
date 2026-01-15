package com.yanny.ali.api;

import com.yanny.ali.configuration.AliConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

public interface ICommonUtils {
    List<Entity> createEntities(EntityType<?> type, Level level);

    AliConfig getConfiguration();
}
