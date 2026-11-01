package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class EntitySubject implements LootSubject {
    private final EntityType<?> entityType;
    private final Class<?> entityClass;
    @Nullable
    private final Object instance;
    private final List<Observation> observations = new ArrayList<>();

    EntitySubject(EntityType<?> entityType, Class<?> entityClass, @Nullable Object instance) {
        this.entityType = entityType;
        this.entityClass = entityClass;
        this.instance = instance;
    }

    EntityType<?> entityType() {
        return entityType;
    }

    @Override
    public Class<?> type() {
        return entityClass;
    }

    @Override
    public boolean isSelf(@Nullable Value value) {
        if (instance != null) {
            return value instanceof Value.Known(Object known) && known == instance;
        }

        return LootSubject.super.isSelf(value);
    }

    @Override
    public void observe(Observation observation) {
        observations.add(observation);
    }

    @Override
    public List<Observation> observations() {
        return observations;
    }
}
