package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.ICommonUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

public class AliCommonRegistry implements ICommonRegistry, ICommonUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<EntityType<?>, Function<Level, List<Entity>>> entityVariantsMap = new HashMap<>();

    @Override
    public <T extends Entity> void registerEntityVariants(EntityType<T> type, Function<Level, List<Entity>> factory) {
        entityVariantsMap.put(type, factory);
    }

    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        Function<Level, List<Entity>> factory = entityVariantsMap.get(type);
        List<Entity> entities = new ArrayList<>();

        if (factory == null) {
            factory = (l) -> {
                Entity entity = type.create(l);

                if (entity != null) {
                    return Collections.singletonList(entity);
                } else {
                    LOGGER.warn("Failed to create entity: {} (NULL)", type);
                    return Collections.emptyList();
                }
            };
        }

        try {
            entities.addAll(factory.apply(level));
        } catch (Throwable e) {
            LOGGER.warn("Failed to create entity: {}", e.getMessage());
        }

        return entities;
    }

    public void printRegistrationInfo() {
        LOGGER.info("Registered {} entity variants", entityVariantsMap.size());
    }
}
