package com.yanny.ali.plugin.common;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.ICommonUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Predicate;

public class TraderDetector {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final String LAMBDA_MARKER = "$$Lambda";
    private static final int WRAPPER_DEPTH = 3;

    private final ICommonUtils utils;
    private final Level level;
    @Nullable
    private final Field factoryField;
    private final Map<Class<?>, Map<EntityType<?>, Class<?>>> ownerIndex = new HashMap<>();
    private int createdTypes;

    public TraderDetector(ICommonUtils utils, Level level) {
        this.utils = utils;
        this.level = level;
        factoryField = findFactoryField();
    }

    @NotNull
    public Map<EntityType<?>, Class<?>> findTraders(Predicate<EntityType<?>> wanted) {
        Map<EntityType<?>, Class<?>> traders = new LinkedHashMap<>();

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type != EntityTypes.PLAYER && wanted.test(type)) {
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
        Class<?> owner = getFactoryOwner(type);
        Class<?> entityClass = owner != null ? ownerIndex.computeIfAbsent(owner, TraderDetector::indexOwner).get(type) : null;

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

    @Nullable
    private Class<?> getFactoryOwner(EntityType<?> type) {
        if (factoryField == null) {
            return null;
        }

        try {
            Class<?> factoryClass = factoryField.get(type).getClass();
            String name = factoryClass.getName();
            int lambda = name.indexOf(LAMBDA_MARKER);

            return lambda >= 0 ? Class.forName(name.substring(0, lambda), false, factoryClass.getClassLoader()) : null;
        } catch (Throwable e) {
            return null;
        }
    }

    @NotNull
    private static Map<EntityType<?>, Class<?>> indexOwner(Class<?> owner) {
        Map<EntityType<?>, Class<?>> index = new IdentityHashMap<>();
        Field[] fields;

        try {
            fields = owner.getDeclaredFields();
        } catch (Throwable e) {
            return index;
        }

        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers()) && field.getGenericType() instanceof ParameterizedType generic) {
                    Class<?> entityClass = findEntityClass(generic);

                    if (entityClass != null) {
                        Set<EntityType<?>> types = Collections.newSetFromMap(new IdentityHashMap<>());

                        field.setAccessible(true);
                        collectEntityTypes(field.get(null), WRAPPER_DEPTH, Collections.newSetFromMap(new IdentityHashMap<>()), types);

                        if (types.size() == 1) {
                            index.putIfAbsent(types.iterator().next(), entityClass);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        return index;
    }

    @Nullable
    private static Class<?> findEntityClass(Type type) {
        Type[] candidates;

        if (type instanceof Class<?> c) {
            return Entity.class.isAssignableFrom(c) ? c : null;
        } else if (type instanceof ParameterizedType parameterized) {
            candidates = parameterized.getActualTypeArguments();
        } else if (type instanceof WildcardType wildcard) {
            candidates = wildcard.getUpperBounds();
        } else {
            return null;
        }

        for (Type candidate : candidates) {
            Class<?> entityClass = findEntityClass(candidate);

            if (entityClass != null) {
                return entityClass;
            }
        }

        return null;
    }

    private static void collectEntityTypes(@Nullable Object value, int depth, Set<Object> seen, Set<EntityType<?>> types) {
        if (value instanceof EntityType<?> type) {
            types.add(type);
            return;
        }

        if (value == null || depth == 0 || !seen.add(value) || value.getClass().isArray() || value instanceof Collection<?>
                || value instanceof Map<?, ?> || value instanceof CharSequence || value instanceof Class<?>) {
            return;
        }

        for (Class<?> c = value.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
                    try {
                        field.setAccessible(true);
                        collectEntityTypes(field.get(value), depth - 1, seen, types);
                    } catch (Throwable ignored) {}
                }
            }
        }
    }

    @Nullable
    private static Field findFactoryField() {
        for (Field field : EntityType.class.getDeclaredFields()) {
            if (field.getType() == EntityType.EntityFactory.class) {
                try {
                    field.setAccessible(true);
                    return field;
                } catch (Throwable e) {
                    LOGGER.warn("Failed to access entity factory: {}", e.getMessage());
                    return null;
                }
            }
        }

        LOGGER.warn("Entity factory field not found, trading entities are detected by creating them");
        return null;
    }
}
