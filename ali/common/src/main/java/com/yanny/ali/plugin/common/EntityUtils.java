package com.yanny.ali.plugin.common;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EntityUtils {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    @Nullable
    public static Entity createEntity(EntityType<?> type, Level level) {
        Entity entity;

        try {
            entity = type.create(level);
        } catch (Throwable e) {
            LOGGER.warn("Failed to create entity {}: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), e.getMessage());
            return null;
        }

        if (entity == null) {
            LOGGER.warn("Failed to create entity: {} (NULL)", type);
            return null;
        }

        // some renderers derive their state from position/rotation, make sure it is not left at NaN/uninitialized values
        try {
            entity.moveTo(0, 0, 0);
            entity.setYRot(0);
            entity.setXRot(0);
        } catch (Throwable e) {
            LOGGER.warn("Failed to initialize entity {}: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), e.getMessage(), e);
        }

        return entity;
    }
}
