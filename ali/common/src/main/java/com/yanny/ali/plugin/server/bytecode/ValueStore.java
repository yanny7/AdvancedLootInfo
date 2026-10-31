package com.yanny.ali.plugin.server.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

final class ValueStore {
    private final List<Machine.Frame> frames = new ArrayList<>();
    private final Map<String, Value> statics = new HashMap<>();
    private final Map<String, Value> selfFields = new HashMap<>();

    void enter(Machine.Frame frame) {
        frames.add(frame);
    }

    void leave() {
        frames.remove(frames.size() - 1);
    }

    boolean hasStatic(String field) {
        return statics.containsKey(field);
    }

    Value getStatic(String field) {
        return statics.get(field);
    }

    void putStatic(String field, Value value) {
        statics.put(field, value);
    }

    boolean hasSelfField(String field) {
        return selfFields.containsKey(field);
    }

    Value getSelfField(String field) {
        return selfFields.get(field);
    }

    void putSelfField(String field, Value value) {
        selfFields.put(field, value);
    }

    void replace(Predicate<Value> match, UnaryOperator<Value> with) {
        replaceInFrames(match, with);
        statics.replaceAll((k, v) -> match.test(v) ? with.apply(v) : v);
        selfFields.replaceAll((k, v) -> match.test(v) ? with.apply(v) : v);
    }

    void replaceInFrames(Predicate<Value> match, UnaryOperator<Value> with) {
        for (Machine.Frame frame : frames) {
            frame.replace(match, with);
        }
    }
}
