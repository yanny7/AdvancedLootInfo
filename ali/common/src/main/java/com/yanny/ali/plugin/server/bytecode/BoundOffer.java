package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;

record BoundOffer(Object receiver, String owner, String name, String desc, boolean isInterface, List<Object> args) implements VillagerTrades.ItemListing {
    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        return null;
    }

    Value call(Interpreter interpreter) {
        Type[] params = Type.getArgumentTypes(desc);
        List<Value> values = new ArrayList<>();

        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) == Bound.TRADER) {
                values.add(Value.Unknown.of(params[i], Origin.TRADER));
            } else if (args.get(i) == Bound.RANDOM || args.get(i) == Bound.LEGACY_RANDOM) {
                values.add(new Value.Rng(args.get(i) == Bound.LEGACY_RANDOM));
            } else {
                values.add(new Value.Known(args.get(i)));
            }
        }

        return interpreter.call(receiver, new MethodInsnNode(isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, owner, name, desc, isInterface), values);
    }

    enum Bound {
        TRADER,
        RANDOM,
        LEGACY_RANDOM,
    }
}
