package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.classNode;

final class TraderModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        TraderSubject subject = (TraderSubject) context.subject();
        Value receiver = insn.getOpcode() == Opcodes.INVOKESTATIC ? null : args.getFirst();

        if (subject != null && subject.isSelf(receiver)) {
            if (!subject.ownGetOffers && RuntimeNames.is(RuntimeNames.GET_OFFERS, insn)) {
                return subject.offers;
            }
            if (RuntimeNames.is(RuntimeNames.ADD_OFFERS_FROM_TRADE_SET, insn)) {
                if (!(args.get(2) instanceof Value.Offers offers) || !(args.get(3) instanceof Value.Known(ResourceKey<?> tradeSet))) {
                    throw new Fail("offers from trade set");
                }

                offers.entries.add(new Value.Offers.Listings(new Object[]{tradeSet}, new Value.Known(1), null));
                return null;
            }
        }

        return NO_OPINION;
    }

    static boolean writesOffers(Class<?> merchant) {
        return offersWriter(merchant) != null;
    }

    private static boolean writesOffers(MethodNode method, ClassLoader loader) {
        for (AbstractInsnNode insn : method.instructions) {
            try {
                if (insn.getOpcode() == Opcodes.PUTFIELD && FieldModel.isVillagerOffers((FieldInsnNode) insn, loader)) {
                    return true;
                }
            } catch (Fail ignored) {}
        }

        return false;
    }

    @Nullable
    static Declaration offersWriter(Class<?> merchant) {
        Map<String, Declaration> writers = new HashMap<>();

        for (Class<?> type = merchant; type != null && !type.getName().startsWith("net.minecraft."); type = type.getSuperclass()) {
            ClassNode node = classNode(type);

            if (node == null) {
                continue;
            }

            for (MethodNode method : node.methods) {
                if ((method.access & Opcodes.ACC_STATIC) != 0 || method.name.startsWith("<") || RuntimeNames.GET_OFFERS.name().equals(method.name)
                        && RuntimeNames.GET_OFFERS.desc().equals(method.desc)) {
                    continue;
                }

                Declaration own = SelfModel.ownDeclaration(merchant, method.name, method.desc, false);

                if (own != null && writesOffers(own.method(), own.owner().getClassLoader())) {
                    writers.put(method.name + method.desc, own);
                }
            }
        }

        return writers.size() == 1 ? writers.values().iterator().next() : null;
    }
}
