package com.yanny.ali.plugin.server.bytecode;

import java.util.List;

sealed interface LootSubject extends Subject permits EntitySubject, InstanceSubject {
    void observe(Observation observation);

    List<Observation> observations();

    record Observation(Object value, List<Decision> decisions, double weight) {
    }
}
