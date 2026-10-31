package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

sealed interface Subject permits TraderSubject {
    Class<?> type();

    default boolean isSelf(@Nullable Value value) {
        return value instanceof Value.Unknown unknown && Origin.SELF.equals(unknown.origin());
    }
}
