package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

final class LootAggregation {
    static final double ALWAYS = 1 - 1e-9;

    private LootAggregation() {}

    @NotNull
    static Map<List<Decision>, Map<Identifier, Double>> tables(List<LootSubject.Observation> observations, Identifier typeDefault, boolean randomPage) {
        Map<List<Decision>, Map<Identifier, Double>> tables = new LinkedHashMap<>();
        Set<List<Object>> seen = new HashSet<>();

        for (LootSubject.Observation observation : observations) {
            if (!(observation.value() instanceof Identifier table) || !seen.add(List.of(table, observation.decisions()))) {
                continue;
            }

            List<Decision> world = observation.decisions().stream().filter((d) -> !isRandom(d)).toList();
            double probability = observation.weight();

            for (Decision decision : observation.decisions()) {
                if (isRandom(decision)) {
                    double chance = (Double) Objects.requireNonNull(decision.value());

                    probability *= decision.holds() ? chance : 1 - chance;
                }
            }

            tables.computeIfAbsent(world, (k) -> new LinkedHashMap<>()).merge(table, probability, Double::sum);
        }

        tables.values().forEach((byTable) -> {
            Double own = byTable.get(typeDefault);

            if (own != null && (!randomPage || own >= ALWAYS)) {
                byTable.remove(typeDefault);
            }
        });
        tables.values().removeIf(Map::isEmpty);
        return tables;
    }

    private static boolean isRandom(Decision decision) {
        return decision.kind() == Decision.Kind.RANDOM && decision.value() instanceof Double;
    }
}
