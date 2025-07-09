package com.yanny.ali.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;

public interface ICommonRegistry {
    <T extends Entity> void registerEntityVariants(EntityType<?> type, Function<Level, List<Entity>> factory);
}
