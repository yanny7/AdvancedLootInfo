package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;

import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class PickModel implements MethodModel {
    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        TraderModel.TraderRun run = context.traderRun();

        if (run == null) {
            return NO_OPINION;
        }
        if (RuntimeNames.is(RuntimeNames.GET_OFFER, insn)) {
            if (args.getFirst() instanceof Value.Choice choice && isPick(context, choice) && choice.options().stream().allMatch(VillagerTrade.class::isInstance)) {
                return new Value.Pick(List.copyOf(choice.options()), choice.draw(), context.poolKey(choice.options()));
            }
            if (args.getFirst() instanceof Value.Known(VillagerTrade listing)) {
                Object source = context.listingSource(listing);

                return new Value.Pick(List.of(listing), source != null ? source : listing, source != null ? context.levelKey(source) : null);
            }
        }
        if (insn.getOpcode() != Opcodes.INVOKESTATIC && args.getFirst() instanceof Value.Choice choice && isPick(context, choice)
                && choice.options().stream().noneMatch(MerchantOffer.class::isInstance) && returnsOffer(insn, call.loader())) {
            List<Object> bound = bound(context, run, insn, args.subList(1, args.size()), call.loader());

            if (bound != null) {
                List<Object> listings = choice.options().stream().<Object>map((element) -> context.boundOffer(element, insn, bound)).toList();

                return new Value.Pick(listings, choice.draw(), context.poolKey(choice.options()));
            }
        }
        if (insn.getOpcode() != Opcodes.INVOKESTATIC && args.getFirst() instanceof Value.Known known && known.value() != null && TradeTypes.isModType(known.value().getClass())
                && returnsOffer(insn, call.loader())) {
            Object source = context.listingSource(known.value());
            List<Object> bound = source != null ? bound(context, run, insn, args.subList(1, args.size()), call.loader()) : null;

            if (bound != null) {
                return new Value.Pick(List.of(context.boundOffer(known.value(), insn, bound)), source, context.levelKey(source));
            }
        }

        return NO_OPINION;
    }

    private static boolean isPick(CallContext context, Value.Choice choice) {
        return choice.draw() != null && context.isPool(choice.draw()) || isUniformPick(choice);
    }

    private static boolean isUniformPick(Value.Choice choice) {
        Set<Object> distinct = Collections.newSetFromMap(new IdentityHashMap<>());

        distinct.addAll(choice.options());
        return choice.draw() instanceof NumberExpr && distinct.size() == choice.options().size();
    }

    private static boolean returnsOffer(MethodInsnNode call, ClassLoader loader) {
        Type returnType = Type.getReturnType(call.desc);

        return returnType.getSort() == Type.OBJECT && MerchantOffer.class.isAssignableFrom(load(returnType.getInternalName(), loader));
    }

    @Nullable
    private static List<Object> bound(CallContext context, TraderModel.TraderRun run, MethodInsnNode insn, List<Value> args, ClassLoader loader) {
        List<Object> bound = new ArrayList<>();
        Type[] params = Type.getArgumentTypes(insn.desc);

        for (int i = 0; i < args.size(); i++) {
            Value arg = args.get(i);
            Class<?> param = params[i].getSort() == Type.OBJECT ? load(params[i].getInternalName(), loader) : null;

            if (run.isSelf(arg)) {
                bound.add(BoundOffer.Bound.TRADER);
            } else if (arg instanceof Value.Unknown unknown && Origin.WORLD.equals(unknown.origin())) {
                bound.add(BoundOffer.Bound.WORLD);
            } else if (arg instanceof Value.Rng(boolean legacy)) {
                bound.add(legacy ? BoundOffer.Bound.LEGACY_RANDOM : BoundOffer.Bound.RANDOM);
            } else if (arg instanceof Value.Known(Object value1) && context.isConcrete(arg)) {
                bound.add(value1);
            } else if (arg instanceof Value.Unknown && param != null && Level.class.isAssignableFrom(param)) {
                bound.add(BoundOffer.Bound.WORLD);
            } else if (arg instanceof Value.Unknown && param != null && Entity.class.isAssignableFrom(param)) {
                bound.add(BoundOffer.Bound.TRADER);
            } else {
                return null;
            }
        }

        return bound;
    }
}
