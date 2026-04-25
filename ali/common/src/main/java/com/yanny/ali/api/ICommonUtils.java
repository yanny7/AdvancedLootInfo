package com.yanny.ali.api;

import com.yanny.aci.api.ICoreCommonUtils;
import com.yanny.ali.configuration.AliConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICommonUtils extends ICoreCommonUtils<AliConfig> {
    @NotNull
    List<Entity> createEntities(EntityType<?> type, Level level);
}
