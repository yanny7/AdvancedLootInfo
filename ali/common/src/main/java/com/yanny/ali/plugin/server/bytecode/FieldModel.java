package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.NULL;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isLegacyRandom;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isRandomType;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.sameDraw;
import static com.yanny.ali.plugin.server.bytecode.Resolution.field;
import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class FieldModel {
    private FieldModel() {}

    static Value get(CallContext context, FieldInsnNode insn, Value receiver, ClassLoader loader) {
        Subject subject = context.subject();
        boolean self = subject != null && subject.isSelf(receiver);
        Value written = self ? context.selfField(insn.owner + "." + insn.name) : null;

        if (receiver instanceof Value.Unknown && RuntimeNames.is(RuntimeNames.LEVEL_IS_CLIENT_SIDE, insn)
                && Level.class.isAssignableFrom(load(insn.owner, loader))) {
            return new Value.Known(0);
        }
        if (self && subject instanceof TraderSubject trader && !trader.ownGetOffers && isVillagerOffers(insn, loader)) {
            return trader.offers;
        }
        if (written != null) {
            return written;
        }
        if (self && subject instanceof TraderSubject && insn.desc.equals(Type.getDescriptor(MerchantOffers.class))) {
            return NULL;
        }
        if (receiver instanceof Value.Unknown && Type.getType(insn.desc).getSort() == Type.OBJECT && isRandomType(Type.getType(insn.desc), loader)) {
            return new Value.Rng(isLegacyRandom(Type.getType(insn.desc), loader));
        }
        if (receiver instanceof Value.Unknown) {
            return Value.Unknown.of(Type.getType(insn.desc), Origin.of(receiver, insn.name, List.of()));
        }
        if (receiver instanceof Value.Choice choice) {
            List<Value> results = new ArrayList<>();

            for (Object option : choice.options()) {
                results.add(get(context, insn, context.wrap(option), loader));
            }

            return Objects.requireNonNull(sameDraw(context.combine(results), choice));
        }
        if (!(receiver instanceof Value.Known known) || known.value() == null) {
            throw new Fail("field of a symbolic value");
        }

        Field field = field(load(insn.owner, loader), insn.name);

        try {
            Object value = field.get(known.value());

            if (context.isTainted(known) && TradeTypes.isData(value)) {
                context.taint(value);
            }

            return context.wrap(value, field.getType());
        } catch (Throwable e) {
            throw new Fail("field " + insn.owner + "." + insn.name);
        }
    }

    static void put(CallContext context, FieldInsnNode insn, Value receiver, Value value, ClassLoader loader) {
        Subject subject = context.subject();

        if (subject != null && subject.isSelf(receiver)) {
            if (subject instanceof TraderSubject trader && isVillagerOffers(insn, loader) && value instanceof Value.Offers offers) {
                trader.offers = offers;

                if (!trader.ownGetOffers) {
                    return;
                }
            }

            context.putSelfField(insn.owner + "." + insn.name, value);
            return;
        }
        if (!(receiver instanceof Value.Known known) || !context.isOwned(known.value()) || !(value instanceof Value.Known known2)) {
            throw new Fail("write to a field");
        }
        if (known.value() != null && context.isFrozen(known.value())) {
            throw new Fail("change under a random choice");
        }

        Field field = field(load(insn.owner, loader), insn.name);

        try {
            field.set(known.value(), context.toJava(known2, field.getType()));
        } catch (Throwable e) {
            throw new Fail("field write " + insn.owner + "." + insn.name);
        }
    }

    static boolean isVillagerOffers(FieldInsnNode insn, ClassLoader loader) {
        return RuntimeNames.is(RuntimeNames.OFFERS, insn) && field(load(insn.owner, loader), insn.name).getDeclaringClass() == AbstractVillager.class;
    }
}
