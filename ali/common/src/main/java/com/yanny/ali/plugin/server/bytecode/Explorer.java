package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

final class Explorer {
    private Explorer() {}

    @NotNull
    static List<Outcome> trader(RegistryAccess registries, Class<?> merchant) {
        List<Outcome> outcomes = explore(registries, (i) -> i.trades(merchant));

        if (outcomes.stream().noneMatch(Explorer::offers) && outcomes.stream().allMatch(Outcome.Success.class::isInstance) && TraderModel.writesOffers(merchant)) {
            return explore(registries, (i) -> i.writtenTrades(merchant));
        }

        return outcomes;
    }

    @NotNull
    static List<Outcome> explore(RegistryAccess registries, Function<Interpreter, Value> run) {
        Set<String> opaque = new HashSet<>();
        int restarts = 0;
        List<Outcome> results = new ArrayList<>();
        Deque<List<Boolean>> scripts = new ArrayDeque<>();

        scripts.add(List.of());

        while (!scripts.isEmpty()) {
            if (results.size() >= Limits.PATHS) {
                results.add(new Outcome.Failure("too many branches", List.of()));
                break;
            }

            List<Boolean> script = scripts.poll();
            Interpreter interpreter = new Interpreter(Interpreter.Mode.SYMBOLIC, 0, script, registries, opaque);
            Value result = null;
            String failure = null;

            try {
                result = run.apply(interpreter);
            } catch (Fail fail) {
                failure = fail.describe();
            }
            if (interpreter.opaque.retry() && ++restarts <= Limits.RESTARTS) {
                results.clear();
                scripts.clear();
                scripts.add(List.of());
                continue;
            }

            List<Boolean> taken = interpreter.decisions.taken().stream().map(Decision::holds).toList();

            results.add(failure == null ? new Outcome.Success(result, interpreter.decisions.taken()) : new Outcome.Failure(failure, interpreter.decisions.taken()));

            for (int i = script.size(); i < taken.size(); i++) {
                List<Boolean> other = new ArrayList<>(taken.subList(0, i));

                other.add(!taken.get(i));
                scripts.add(other);
            }
        }

        return results;
    }

    private static boolean offers(Outcome outcome) {
        return outcome instanceof Outcome.Success success && success.result() instanceof Value.Offers offers && !offers.entries.isEmpty();
    }
}
