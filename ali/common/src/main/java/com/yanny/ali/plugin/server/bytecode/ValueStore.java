package com.yanny.ali.plugin.server.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

final class ValueStore {
    private final List<Interpreter.Frame> frames = new ArrayList<>();
    private final Map<String, Value> statics = new HashMap<>();
    private final Map<String, Value> traderFields = new HashMap<>();

    void enter(Interpreter.Frame frame) {
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

    boolean hasTraderField(String field) {
        return traderFields.containsKey(field);
    }

    Value getTraderField(String field) {
        return traderFields.get(field);
    }

    void putTraderField(String field, Value value) {
        traderFields.put(field, value);
    }

    void replace(Predicate<Value> match, UnaryOperator<Value> with) {
        replaceInFrames(match, with);
        statics.replaceAll((k, v) -> match.test(v) ? with.apply(v) : v);
        traderFields.replaceAll((k, v) -> match.test(v) ? with.apply(v) : v);
    }

    void replaceInFrames(Predicate<Value> match, UnaryOperator<Value> with) {
        for (Interpreter.Frame frame : frames) {
            frame.replace(match, with);
        }
    }
}
