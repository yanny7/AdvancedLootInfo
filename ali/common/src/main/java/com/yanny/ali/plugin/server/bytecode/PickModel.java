package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
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
            if (args.get(0) instanceof Value.Choice choice && isPick(context, choice) && choice.options().stream().allMatch(VillagerTrades.ItemListing.class::isInstance)) {
                context.traceCase("undistributed.getOffer.choice", insn);
                return new Value.Pick(choice.options().stream().map(VillagerTrades.ItemListing.class::cast).toList(), choice.draw(), context.poolKey(choice.options()));
            }
            if (args.get(0) instanceof Value.Known known && known.value() instanceof VillagerTrades.ItemListing listing) {
                Object source = context.listingSource(listing);

                context.traceCase("undistributed.getOffer.known", insn);
                return new Value.Pick(List.of(listing), source != null ? source : listing, source != null ? context.levelKey(source) : null);
            }
        }
        if (insn.getOpcode() != Opcodes.INVOKESTATIC && args.get(0) instanceof Value.Choice choice && isPick(context, choice)
                && choice.options().stream().noneMatch(MerchantOffer.class::isInstance) && returnsOffer(insn, call.loader())) {
            List<Object> bound = bound(context, run, args.subList(1, args.size()));

            if (bound != null) {
                List<VillagerTrades.ItemListing> listings = choice.options().stream().<VillagerTrades.ItemListing>map((element) -> context.boundOffer(element, insn, bound)).toList();

                context.traceCase("undistributed.boundOffer", insn);
                return new Value.Pick(listings, choice.draw(), context.poolKey(choice.options()));
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
    private static List<Object> bound(CallContext context, TraderModel.TraderRun run, List<Value> args) {
        List<Object> bound = new ArrayList<>();

        for (Value arg : args) {
            if (run.isSelf(arg)) {
                bound.add(BoundOffer.Bound.TRADER);
            } else if (arg instanceof Value.Rng rng) {
                bound.add(rng.legacy() ? BoundOffer.Bound.LEGACY_RANDOM : BoundOffer.Bound.RANDOM);
            } else if (arg instanceof Value.Known known && context.isConcrete(arg)) {
                bound.add(known.value());
            } else {
                return null;
            }
        }

        return bound;
    }
}
