package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class InstanceSubject implements LootSubject {
    private final Object instance;
    private final List<Observation> observations = new ArrayList<>();

    InstanceSubject(Object instance) {
        this.instance = instance;
    }

    @Override
    public Class<?> type() {
        return instance.getClass();
    }

    @Override
    public boolean isSelf(@Nullable Value value) {
        return value instanceof Value.Known known && known.value() == instance;
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
