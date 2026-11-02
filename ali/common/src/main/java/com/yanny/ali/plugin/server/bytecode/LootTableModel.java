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
    private static final Origin LOOT_TABLES = new Origin(null, "lootTables", List.of());

    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
            return NO_OPINION;
        }
        if (CallSites.isLootTableLookup(insn, call.loader()) && CallSites.LOOT_TABLES.equals(traced(call.args().get(1)))) {
            return Value.Unknown.of(Type.getReturnType(insn.desc), LOOT_TABLES);
        }

        Value table = table(call);

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
            if (sink.matches(call.insn(), call.loader()) && accepts(sink, args.get(sink.qualifier() + 1))) {
                return args.get(sink.argument() + 1);
            }
        }

        return null;
    }

    private static boolean accepts(CallSites.Sink sink, Value qualifier) {
        if (qualifier instanceof Value.Choice choice) {
            return choice.options().stream().allMatch((o) -> sink.accepts(new CallSites.Constant(o)));
        }

        return sink.accepts(traced(qualifier));
    }

    @Nullable
    private static Object traced(Value value) {
        if (value instanceof Value.Known(Object known)) {
            return new CallSites.Constant(known);
        }
        if (value instanceof Value.Unknown unknown && unknown.origin() == LOOT_TABLES) {
            return CallSites.LootView.INSTANCE;
        }

        return null;
    }
}
