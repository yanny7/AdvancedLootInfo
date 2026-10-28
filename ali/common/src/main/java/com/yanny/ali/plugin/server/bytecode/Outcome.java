package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

sealed interface Outcome {
    List<Decision> decisions();

    record Success(@Nullable Value result, List<Decision> decisions) implements Outcome {
    }

    record Failure(String reason, List<Decision> decisions) implements Outcome {
    }
}
