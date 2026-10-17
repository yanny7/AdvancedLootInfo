package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

record Decision(Kind kind, @Nullable Object subject, @Nullable Object value, boolean holds) {
    boolean sameTest(Decision other) {
        return kind == other.kind && subject != null && subject.equals(other.subject) && Objects.equals(value, other.value);
    }

    Decision negated() {
        return new Decision(kind, subject, value, !holds);
    }

    enum Kind {
        EQUALS,
        TEST,
        FOUND,
        RANDOM,
        UNKNOWN,
    }
}
