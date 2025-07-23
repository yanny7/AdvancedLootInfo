package com.yanny.ali.plugin.common;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EntityUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static List<Entity> getSheepVariants(Level level) {
        List<Entity> entities = new ArrayList<>();

        for (DyeColor color : DyeColor.values()) {
            Sheep sheep;

            try {
                sheep = EntityType.SHEEP.create(level, EntitySpawnReason.LOAD);
            } catch (Throwable e) {
                LOGGER.warn("Failed to create colored sheep with color {}: {}", color.getSerializedName(), e.getMessage());
                continue;
            }

            if (sheep != null) {
                sheep.setColor(color);
                entities.add(sheep);
            }
        }

        Sheep sheep;

        try {
            sheep = EntityType.SHEEP.create(level, EntitySpawnReason.LOAD);
        } catch (Throwable e) {
            LOGGER.warn("Failed to create sheep: {}", e.getMessage());
            return entities;
        }

        if (sheep != null) {
            sheep.setSheared(true);
            entities.add(sheep);
        }

        return entities;
    }
}
