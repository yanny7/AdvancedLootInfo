package com.yanny.ali.plugin.common;

import com.yanny.ali.plugin.server.bytecode.VariantHint;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public final class EntityVariants {
    private static final int MAX_INSTANCES = 64;

    private EntityVariants() {}

    @NotNull
    public static List<Entity> create(EntityType<?> type, Level level, List<List<VariantHint>> groups, Set<Identifier> known, Function<Entity, Identifier> lootTables) {
        List<Entity> variants = new ArrayList<>();
        Set<Identifier> tables = new HashSet<>(known);
        Entity base = groups.isEmpty() ? null : EntityUtils.createEntity(type, level);

        if (base == null) {
            return variants;
        }

        for (List<VariantHint> group : groups) {
            List<List<Object>> domains = group.stream().map((hint) -> domain(base, hint)).toList();
            int combinations = 1;

            for (List<Object> domain : domains) {
                combinations *= Math.max(1, domain.size());

                if (combinations > MAX_INSTANCES) {
                    break;
                }
            }

            if (combinations > MAX_INSTANCES) {
                continue;
            }

            for (int index = 0; index < combinations; index++) {
                Entity entity = EntityUtils.createEntity(type, level);
                int rest = index;

                if (entity == null) {
                    return variants;
                }

                try {
                    for (int i = 0; i < group.size(); i++) {
                        List<Object> domain = domains.get(i);

                        if (!domain.isEmpty()) {
                            apply(entity, group.get(i), domain.get(rest % domain.size()));
                            rest /= domain.size();
                        }
                    }

                    Identifier table = lootTables.apply(entity);

                    if (table != null && tables.add(table)) {
                        variants.add(entity);
                    }
                } catch (Throwable ignored) {}
            }
        }

        return variants;
    }

    @Nullable
    public static List<SynchedEntityData.DataValue<?>> data(EntityType<?> type, Level level, Entity entity, Identifier lootTable, Function<Entity, Identifier> lootTables) {
        try {
            List<SynchedEntityData.DataValue<?>> values = Objects.requireNonNullElse(entity.getEntityData().getNonDefaultValues(), List.of());
            Entity fresh = EntityUtils.createEntity(type, level);

            if (fresh == null) {
                return null;
            }

            fresh.getEntityData().assignValues(values);
            return lootTable.equals(lootTables.apply(fresh)) ? values : null;
        } catch (Throwable e) {
            return null;
        }
    }

    @NotNull
    private static List<Object> domain(Entity base, VariantHint hint) {
        if (hint instanceof VariantHint.Setter setter) {
            return setter.values();
        }

        VariantHint.Data data = (VariantHint.Data) hint;
        Object current;

        try {
            current = base.getEntityData().get(data.accessor());
        } catch (Throwable e) {
            return List.of();
        }

        Set<Object> values = new LinkedHashSet<>();

        values.add(current);

        if (current instanceof Boolean flag) {
            values.add(!flag);
        }

        //noinspection ConstantValue - some modded mobs does return null
        data.values().stream().filter((v) -> current == null || v.getClass() == current.getClass()).forEach(values::add);
        return new ArrayList<>(values);
    }

    @SuppressWarnings("unchecked")
    private static void apply(Entity entity, VariantHint hint, Object value) throws ReflectiveOperationException {
        if (hint instanceof VariantHint.Setter setter) {
            Method method = setter.method();

            method.setAccessible(true);
            method.invoke(entity, value);
        } else {
            entity.getEntityData().set((EntityDataAccessor<Object>) ((VariantHint.Data) hint).accessor(), value);
        }
    }
}
