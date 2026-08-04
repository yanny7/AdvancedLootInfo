package com.yanny.ali.manager;

import com.mojang.logging.LogUtils;
import com.yanny.aci.manager.CoreCommonRegistry;
import com.yanny.aci.manager.ManagedRegistry;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.configuration.ConfigUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

public class AliCommonRegistry extends CoreCommonRegistry<AliConfig> implements ICommonRegistry, ICommonUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ManagedRegistry<EntityType<?>, Function<Level, List<Entity>>> entityVariants = register("entity variants", false, HashMap::new, EntityType::toString, null);

    public AliCommonRegistry() {
        super();
    }

    @Override
    public <T extends Entity> void registerEntityVariants(EntityType<T> type, Function<Level, List<Entity>> factory) {
        entityVariants.put(type, factory);
    }

    @NotNull
    @Override
    public List<Entity> createEntities(EntityType<?> type, Level level) {
        Optional<Function<Level, List<Entity>>> factory = entityVariants.get(type);
        List<Entity> entities = new ArrayList<>();

        if (factory.isEmpty()) {
            factory = Optional.of((l) -> {
                Entity entity = type.create(l, EntitySpawnReason.LOAD);

                if (entity != null) {
                    // some renderers derive their state from position/rotation, make sure it is not left at NaN/uninitialized values
                    try {
                        entity.move(MoverType.SELF, new Vec3(0, 0, 0));
                        entity.setYRot(0);
                        entity.setXRot(0);
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to initialize entity {}: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), e.getMessage(), e);
                    }

                    return Collections.singletonList(entity);
                } else {
                    LOGGER.warn("Failed to create entity: {} (NULL)", type);
                    return Collections.emptyList();
                }
            });
        }

        try {
            entities.addAll(factory.get().apply(level));
        } catch (Throwable e) {
            LOGGER.warn("Failed to create entity {}: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), e.getMessage());
        }

        return entities;
    }

    @NotNull
    protected AliConfig loadConfiguration() {
        return ConfigUtils.readConfiguration();
    }
}
