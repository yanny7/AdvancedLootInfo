package com.yanny.ali.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public interface ICommonUtils {
    List<Entity> createEntities(EntityType<?> type, Level level);
}
