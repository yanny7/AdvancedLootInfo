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
import java.util.Optional;

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
        if (table instanceof Value.Known(Object value) && unwrap(value) != null) {
            return List.of(new LootSubject.Observation(location(unwrap(value)), decisions, 1));
        }
        if (table instanceof Value.Choice choice) {
            Map<Object, Double> weights = new LinkedHashMap<>();

            choice.options().stream().map(LootTableModel::unwrap).filter(Objects::nonNull).forEach((o) -> weights.merge(location(o), 1.0 / choice.options().size(), Double::sum));
            return weights.entrySet().stream().map((e) -> new LootSubject.Observation(e.getKey(), decisions, e.getValue())).toList();
        }

        return List.of();
    }

    @Nullable
    private static Object unwrap(@Nullable Object table) {
        return table instanceof Optional<?> optional ? optional.orElse(null) : table;
    }

    @Nullable
    static Object location(@Nullable Object table) {
        return table instanceof ResourceKey<?> key ? key.identifier() : table;
    }

    @Nullable
    private static Value table(Call call) {
        List<Value> args = call.args();

        for (CallSites.Sink sink : LootTableAnalysis.SINKS) {
            if (sink.matches(call.insn(), call.loader())) {
                Value key = args.get(sink.argument() + 1);

                if (sink.registry() == null || key instanceof Value.Known(Object value) && sink.isFor(value)
                        || key instanceof Value.Choice choice && choice.options().stream().allMatch(sink::isFor)) {
                    return key;
                }
            }
        }

        return null;
    }
}
