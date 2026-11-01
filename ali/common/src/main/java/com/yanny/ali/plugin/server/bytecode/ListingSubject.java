package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

final class ListingSubject extends TradeSubject {
    private final Class<?> type;

    ListingSubject(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public boolean isSelf(@Nullable Value value) {
        return false;
    }
}
