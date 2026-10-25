package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

final class Fail extends RuntimeException {
    private static final Set<String> BUDGETS = Set.of("too many branches", "step budget", "call depth");

    @Nullable
    private String where;

    Fail(String message) {
        super(message, null, false, false);
    }

    Fail at(String method) {
        if (where == null) {
            where = method;
        }

        return this;
    }

    String describe() {
        return where != null ? getMessage() + " in " + where : getMessage();
    }

    boolean isBudget() {
        return BUDGETS.contains(getMessage());
    }
}
