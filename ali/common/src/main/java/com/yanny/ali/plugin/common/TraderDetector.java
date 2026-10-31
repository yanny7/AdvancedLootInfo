package com.yanny.ali.plugin.common;

import com.yanny.ali.api.ICommonUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class TraderDetector {
    private final ICommonUtils utils;
    private final Level level;
    private final EntityClasses entityClasses = new EntityClasses();
    private int createdTypes;

    public TraderDetector(ICommonUtils utils, Level level) {
        this.utils = utils;
        this.level = level;
    }

    @NotNull
    public Map<EntityType<?>, Class<?>> findTraders(Predicate<EntityType<?>> wanted) {
        Map<EntityType<?>, Class<?>> traders = new LinkedHashMap<>();

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type != EntityType.PLAYER && wanted.test(type)) {
                Class<?> merchantClass = getMerchantClass(type);

                if (merchantClass != null) {
                    traders.put(type, merchantClass);
                }
            }
        }

        return traders;
    }

    public int getCreatedTypes() {
        return createdTypes;
    }

    @Nullable
    private Class<?> getMerchantClass(EntityType<?> type) {
        Class<?> entityClass = entityClasses.find(type);

        if (entityClass != null) {
            return Merchant.class.isAssignableFrom(entityClass) ? entityClass : null;
        }

        createdTypes++;
        return utils.createEntities(type, level).stream()
                .filter((e) -> e instanceof Merchant)
                .findFirst()
                .<Class<?>>map(Entity::getClass)
                .orElse(null);
    }
}
