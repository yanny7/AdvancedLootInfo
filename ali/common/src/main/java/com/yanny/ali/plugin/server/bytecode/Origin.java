package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

record Origin(@Nullable Object receiver, String member, List<Object> args) {
    static final Origin SELF = new Origin(null, "self", List.of());
    private static final String EQUALS = "equals(Ljava/lang/Object;)Z";
    private static final Object NONE = new Object();

    Origin {
        args = Collections.unmodifiableList(new ArrayList<>(args));
    }

    @Nullable
    static Origin of(@Nullable Value receiver, String member, List<Value> args) {
        Object from = receiver == null ? null : part(receiver);
        List<Object> parts = new ArrayList<>();

        if (from == NONE) {
            return null;
        }

        for (Value arg : args) {
            Object part = part(arg);

            if (part == NONE) {
                return null;
            }

            parts.add(part);
        }

        return new Origin(from, member, parts);
    }

    @Nullable
    static Origin equality(Value.Unknown unknown, @Nullable Object value) {
        return unknown.origin() == null ? null : new Origin(unknown.origin(), EQUALS, Collections.singletonList(value));
    }

    boolean isBoolean() {
        return member.endsWith(")Z");
    }

    boolean isEquality() {
        return member.equals(EQUALS);
    }

    @Nullable
    private static Object part(Value value) {
        if (value instanceof Value.Known known) {
            return known.value();
        }
        if (value instanceof Value.Unknown unknown && unknown.origin() != null) {
            return unknown.origin();
        }

        return NONE;
    }
}
