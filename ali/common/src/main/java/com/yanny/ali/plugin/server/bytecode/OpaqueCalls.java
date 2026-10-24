package com.yanny.ali.plugin.server.bytecode;

import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class OpaqueCalls {
    private final Set<String> opaque;
    private final Map<String, Integer> heavy = new HashMap<>();
    private boolean retry;

    OpaqueCalls(Set<String> opaque) {
        this.opaque = opaque;
    }

    boolean isOpaque(MethodInsnNode call) {
        return opaque.contains(key(call));
    }

    void count(MethodInsnNode call, int decisions) {
        heavy.merge(key(call), decisions, Math::max);
    }

    void markHeaviest() {
        heavy.entrySet().stream().filter((e) -> e.getValue() > 1 && !opaque.contains(e.getKey())).max(Map.Entry.comparingByValue())
                .ifPresent((e) -> retry = opaque.add(e.getKey()));
    }

    void mark(MethodInsnNode call) {
        retry = retry || opaque.add(key(call));
    }

    boolean retry() {
        return retry;
    }

    private static String key(MethodInsnNode call) {
        return call.owner + "." + call.name + call.desc;
    }
}
