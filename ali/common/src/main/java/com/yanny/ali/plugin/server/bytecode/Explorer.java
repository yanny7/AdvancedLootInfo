package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

final class Explorer {
    private Explorer() {}

    @NotNull
    static List<Path> trader(RegistryAccess registries, Class<?> merchant) {
        List<Path> paths = explore(registries, (i) -> i.trades(merchant));

        if (paths.stream().noneMatch(Explorer::offers) && paths.stream().allMatch((p) -> p.failure() == null) && TraderModel.writesOffers(merchant)) {
            return explore(registries, (i) -> i.writtenTrades(merchant));
        }

        return paths;
    }

    @NotNull
    static List<Path> explore(RegistryAccess registries, Function<Interpreter, Value> run) {
        Set<String> opaque = new HashSet<>();
        int restarts = 0;
        List<Path> results = new ArrayList<>();
        Deque<List<Boolean>> scripts = new ArrayDeque<>();

        scripts.add(List.of());

        while (!scripts.isEmpty()) {
            if (results.size() >= Limits.PATHS) {
                results.add(new Path(null, List.of(), "too many branches"));
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
            if (interpreter.opaque.retry() && ++restarts < Limits.MAX_RESTARTS) {
                results.clear();
                scripts.clear();
                scripts.add(List.of());
                continue;
            }

            List<Boolean> taken = interpreter.decisions.taken().stream().map(Decision::holds).toList();

            results.add(new Path(result, interpreter.decisions.taken(), failure));

            for (int i = script.size(); i < taken.size(); i++) {
                List<Boolean> other = new ArrayList<>(taken.subList(0, i));

                other.add(!taken.get(i));
                scripts.add(other);
            }
        }

        return results;
    }

    private static boolean offers(Path path) {
        return path.result() instanceof Value.Offers offers && !offers.entries.isEmpty();
    }

    record Path(@Nullable Value result, List<Decision> decisions, @Nullable String failure) {
    }
}
