package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

sealed interface Subject permits TradeSubject, LootSubject {
    Class<?> type();

    default boolean isSelf(@Nullable Value value) {
        return value instanceof Value.Unknown unknown && Origin.SELF.equals(unknown.origin());
    }

    default void keyedRead(@Nullable Object key, @Nullable Object value) {
    }

    default void elementRead(@Nullable Object element, Object array) {
    }
}
