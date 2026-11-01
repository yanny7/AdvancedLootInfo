package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

final class EntitySubject implements LootSubject {
    private final EntityType<?> entityType;
    private final Class<?> entityClass;
    private final List<Observation> observations = new ArrayList<>();

    EntitySubject(EntityType<?> entityType, Class<?> entityClass) {
        this.entityType = entityType;
        this.entityClass = entityClass;
    }

    EntityType<?> entityType() {
        return entityType;
    }

    @Override
    public Class<?> type() {
        return entityClass;
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
