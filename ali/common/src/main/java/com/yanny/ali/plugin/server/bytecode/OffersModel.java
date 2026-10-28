package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;

import static com.yanny.ali.plugin.server.bytecode.Resolution.JdkDeclaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class OffersModel implements MethodModel {
    static final MethodModel NEW = OffersModel::newOffers;
    static final MethodModel COLLECTED = OffersModel::collected;
    static final MethodModel ORDER = OffersModel::order;
    private static final Set<String> ORDER_ONLY = Set.of("reverse", "shuffle", "sort");

    @Nullable
    @Override
    public Value apply(CallContext context, Call call) {
        if (call.insn().getOpcode() != Opcodes.INVOKESTATIC && call.args().getFirst() instanceof Value.Offers offers) {
            return offersCall(context, call, offers);
        }

        return NO_OPINION;
    }

    private static Value newOffers(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.desc.equals("()V") && isPlainOffers(load(insn.owner, call.loader()))) {
            return new Value.Offers();
        }

        return NO_OPINION;
    }

    @Nullable
    private static Value collected(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();
        List<Value> args = call.args();
        boolean isStatic = insn.getOpcode() == Opcodes.INVOKESTATIC;
        Value.Offers collected = !isStatic && insn.name.equals("add") && insn.desc.equals("(Ljava/lang/Object;)Z") && isSymbolicOffer(context, args.get(1))
                ? offerList(context, args.getFirst(), true) : null;

        if (collected != null) {
            return args.get(1) instanceof Value.Choice choice ? addPicked(context, collected, choice) : offersCall(context, call, collected);
        }
        if (!isStatic && args.getFirst() instanceof Value.Known(Object value1) && value1 != null && context.offers(value1) != null) {
            return offersCall(context, call, context.offers(value1));
        }

        return NO_OPINION;
    }

    @Nullable
    private static Value order(CallContext context, Call call) {
        MethodInsnNode insn = call.insn();

        if (insn.getOpcode() == Opcodes.INVOKESTATIC && insn.owner.equals("java/util/Collections") && ORDER_ONLY.contains(insn.name) && call.args().getFirst() instanceof Value.Offers) {
            return null;
        }

        return NO_OPINION;
    }

    @Nullable
    private static Value offersCall(CallContext context, Call invocation, Value.Offers offers) {
        MethodInsnNode call = invocation.insn();
        List<Value> args = invocation.args();

        if (call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Pick(List<VillagerTrades.ItemListing> listings1, Object pool, Integer key)) {
            Integer index = offers.picks.get(pool);

            NumberExpr drawCount = context.drawCount(pool);

            if (index == null) {
                offers.picks.put(pool, offers.entries.size());
                offers.entries.add(new Value.Offers.Listings(listings1.toArray(new VillagerTrades.ItemListing[0]),
                        drawCount != null ? new Value.Num(drawCount, Value.Kind.INT) : new Value.Known(1), key));
            } else {
                if (!(offers.entries.get(index) instanceof Value.Offers.Listings(VillagerTrades.ItemListing[] listings, Value count1, Integer key1))) {
                    throw new Fail("merchant offers call add");
                }

                List<VillagerTrades.ItemListing> union = new ArrayList<>(Arrays.asList(listings));

                for (VillagerTrades.ItemListing listing : listings1) {
                    if (union.stream().noneMatch((l) -> l == listing)) {
                        union.add(listing);
                    }
                }

                Value count = drawCount != null ? count1 : new Value.Known((Integer) ((Value.Known) count1).value() + 1);

                offers.entries.set(index, new Value.Offers.Listings(union.toArray(new VillagerTrades.ItemListing[0]), count, key1));
            }

            return new Value.Known(1);
        }
        if (call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z")) {
            offers.entries.add(new Value.Offers.Single(args.get(1)));
            return new Value.Known(1);
        }
        Value.Offers source = call.name.equals("addAll") && call.desc.equals("(Ljava/util/Collection;)Z") ? offerList(context, args.get(1), false) : null;

        if (source != null) {
            int start = offers.entries.size();

            for (Map.Entry<Object, Integer> pick : source.picks.entrySet()) {
                if (offers.picks.putIfAbsent(pick.getKey(), start + pick.getValue()) != null) {
                    throw new Fail("merchant offers call addAll");
                }
            }

            offers.entries.addAll(source.entries);
            return new Value.Known(source.entries.isEmpty() ? 0 : 1);
        }
        if (call.name.equals("addAll") && call.desc.equals("(Ljava/util/Collection;)Z") && args.get(1) instanceof Value.Known known
                && known.value() instanceof Collection<?> collection && !context.isTainted(known)) {
            collection.forEach((offer) -> offers.entries.add(new Value.Offers.Single(context.wrap(offer))));
            return new Value.Known(collection.isEmpty() ? 0 : 1);
        }
        if (call.name.equals("removeIf") && call.desc.equals("(Ljava/util/function/Predicate;)Z") && args.get(1) instanceof Value.Closure predicate) {
            return removeOffers(context, offers, predicate, invocation.depth());
        }
        if (call.name.equals("isEmpty") && call.desc.equals("()Z")) {
            return new Value.Known(offers.entries.isEmpty() ? 1 : 0);
        }
        if (call.name.equals("clear") && call.desc.equals("()V")) {
            offers.entries.clear();
            offers.picks.clear();
            return null;
        }
        if (call.name.equals("sort") && call.desc.equals("(Ljava/util/Comparator;)V")) {
            return null;
        }

        throw new Fail("merchant offers call " + call.name);
    }

    private static Value addPicked(CallContext context, Value.Offers offers, Value.Choice choice) {
        Integer index = offers.picks.get(choice.draw());
        NumberExpr drawCount = context.drawCount(choice.draw());

        if (index == null) {
            offers.picks.put(choice.draw(), offers.entries.size());
            offers.entries.add(new Value.Offers.Picked(choice.options(), drawCount != null ? new Value.Num(drawCount, Value.Kind.INT) : new Value.Known(1)));
        } else if (offers.entries.get(index) instanceof Value.Offers.Picked(List<Object> offers1, Value count1)) {
            List<Object> union = new ArrayList<>(offers1);

            for (Object offer : choice.options()) {
                if (union.stream().noneMatch((o) -> o == offer)) {
                    union.add(offer);
                }
            }

            Value count = drawCount != null ? count1 : new Value.Known((Integer) ((Value.Known) count1).value() + 1);

            offers.entries.set(index, new Value.Offers.Picked(union, count));
        } else {
            throw new Fail("merchant offers call add");
        }

        return new Value.Known(1);
    }

    private static boolean isSymbolicOffer(CallContext context, Value value) {
        return value instanceof Value.Pick || value instanceof Value.Offer || value instanceof Value.Choice(List<Object> options, Object draw) && draw != null
                && context.isPool(draw) && !options.isEmpty() && options.stream().allMatch(MerchantOffer.class::isInstance);
    }

    @Nullable
    private static Value.Offers offerList(CallContext context, Value value, boolean create) {
        if (value instanceof Value.Offers offers) {
            return offers;
        }
        if (!(value instanceof Value.Known(Object value1)) || !(value1 instanceof Collection<?> collection) || !context.isOwned(collection)) {
            return null;
        }

        Value.Offers collected = context.offers(collection);

        if (collected != null || !create) {
            return collected;
        }
        if (context.isFrozen(collection) || !collection.stream().allMatch(MerchantOffer.class::isInstance)) {
            throw new Fail("offers in a collection");
        }

        Value.Offers offers = new Value.Offers();

        collection.forEach((offer) -> offers.entries.add(new Value.Offers.Single(context.wrap(offer))));
        collection.clear();
        context.coll(collection).offers = offers;
        return offers;
    }

    private static Value removeOffers(CallContext context, Value.Offers offers, Value.Closure predicate, int depth) {
        boolean nullCheck = predicate.impl().getOwner().equals("java/util/Objects") && predicate.impl().getName().equals("isNull");
        List<Integer> removedIndices = new ArrayList<>();

        for (int i = 0; i < offers.entries.size(); i++) {
            if (!(offers.entries.get(i) instanceof Value.Offers.Single(Value offer))) {
                if (!nullCheck) {
                    throw new Fail("merchant offers call removeIf");
                }

                continue;
            }

            if (!(context.invoke(predicate, List.of(offer), depth) instanceof Value.Known(Integer test))) {
                throw new Fail("merchant offers call removeIf");
            }
            if (test != 0) {
                removedIndices.add(i);
            }
        }

        for (int index : removedIndices.reversed()) {
            offers.entries.remove(index);
        }

        offers.picks.replaceAll((pool, index) -> index - (int) removedIndices.stream().filter((removed) -> removed < index).count());
        return new Value.Known(removedIndices.isEmpty() ? 0 : 1);
    }

    private static boolean isPlainOffers(Class<?> type) {
        return MerchantOffers.class.isAssignableFrom(type) && declaration(type, "add", "(Ljava/lang/Object;)Z", false) instanceof JdkDeclaration;
    }
}
