package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;

final class MapModel implements MethodModel {
    private static final Set<String> LOOKUPS = Set.of("get", "getOrDefault", "containsKey");

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();

        if (insn.getOpcode() != Opcodes.INVOKESTATIC && args.get(0) instanceof Value.Known known && known.value() instanceof Map<?, ?> map && LOOKUPS.contains(insn.name)
                && args.get(1) instanceof Value.Unknown key && key.origin() != null && args.subList(2, args.size()).stream().allMatch(context::isConcrete)) {
            return lookup(context, call, map, key.origin());
        }

        return NO_OPINION;
    }

    @Nullable
    private static Value lookup(CallContext context, Call call, Map<?, ?> map, Origin key) {
        List<Value> args = call.args();
        List<Object> candidates = new ArrayList<>(map.keySet());

        candidates.sort(Comparator.comparing(String::valueOf));

        for (Object candidate : candidates) {
            if (candidate != null && context.decide(Decision.Kind.EQUALS, key, candidate, "map lookup by a value from the trader or world")) {
                List<Value> concrete = new ArrayList<>(args);

                concrete.set(1, new Value.Known(candidate));
                return context.jdk(new Call(call.insn(), concrete, call.loader(), call.depth()), map.getClass());
            }
        }

        return switch (call.insn().name) {
            case "get" -> Interpreter.NULL;
            case "getOrDefault" -> args.get(2);
            default -> new Value.Known(0);
        };
    }
}
