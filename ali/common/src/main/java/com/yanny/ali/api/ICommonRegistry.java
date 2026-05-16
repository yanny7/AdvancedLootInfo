package com.yanny.ali.api;

import com.yanny.aci.api.ICoreCommonRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;

public interface ICommonRegistry extends ICoreCommonRegistry {
    <T extends Entity> void registerEntityVariants(EntityType<T> type, Function<Level, List<Entity>> factory);
}
