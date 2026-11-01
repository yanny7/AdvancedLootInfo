package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class LootTableModel implements MethodModel {
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        Value table = insn.getOpcode() != Opcodes.INVOKESTATIC ? table(call) : null;

        if (table == null) {
            return NO_OPINION;
        }

        LootSubject subject = (LootSubject) context.subject();

        if (subject != null) {
            observations(table, context.decisions()).forEach(subject::observe);
        }

        return Value.Unknown.of(Type.getReturnType(insn.desc), null);
    }

    static List<LootSubject.Observation> observations(Value table, List<Decision> decisions) {
        if (table instanceof Value.Known(Object value) && value != null) {
            return List.of(new LootSubject.Observation(location(value), decisions, 1));
        }
        if (table instanceof Value.Choice choice) {
            Map<Object, Double> weights = new LinkedHashMap<>();

            choice.options().stream().filter(Objects::nonNull).forEach((o) -> weights.merge(location(o), 1.0 / choice.options().size(), Double::sum));
            return weights.entrySet().stream().map((e) -> new LootSubject.Observation(e.getKey(), decisions, e.getValue())).toList();
        }

        return List.of();
    }

    @Nullable
    static Object location(@Nullable Object table) {
        return table instanceof ResourceKey<?> key ? key.location() : table;
    }

    @Nullable
    private static Value table(Call call) {
        List<Value> args = call.args();

        for (CallSites.Sink sink : LootTableAnalysis.SINKS) {
            if (sink.matches(call.insn(), call.loader()) && (sink.registry() == null || args.get(1) instanceof Value.Known(Object registry) && sink.registry().equals(registry))) {
                return args.get(sink.argument() + 1);
            }
        }

        return null;
    }
}
