package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.LootTable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class LootTableModel implements MethodModel {
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.getOpcode() == Opcodes.INVOKESTATIC || !RuntimeNames.is(RuntimeNames.LOOT_DATA_GET_LOOT_TABLE, insn) || !isResolver(insn.owner, call.loader())) {
            return NO_OPINION;
        }

        LootSubject subject = (LootSubject) context.subject();

        if (subject != null) {
            observations(call.args().get(1), context.decisions()).forEach(subject::observe);
        }

        return Value.Unknown.of(Type.getType(LootTable.class), null);
    }

    static List<LootSubject.Observation> observations(Value table, List<Decision> decisions) {
        if (table instanceof Value.Known known && known.value() != null) {
            return List.of(new LootSubject.Observation(known.value(), decisions, 1));
        }
        if (table instanceof Value.Choice choice) {
            Map<Object, Double> weights = new LinkedHashMap<>();

            choice.options().stream().filter(Objects::nonNull).forEach((o) -> weights.merge(o, 1.0 / choice.options().size(), Double::sum));
            return weights.entrySet().stream().map((e) -> new LootSubject.Observation(e.getKey(), decisions, e.getValue())).toList();
        }

        return List.of();
    }

    private static boolean isResolver(String owner, ClassLoader loader) {
        try {
            return LootDataResolver.class.isAssignableFrom(load(owner, loader));
        } catch (Fail fail) {
            return false;
        }
    }
}
