package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.CONSTRUCTOR;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.LOOKUPS;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.OPTIONAL;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.choiceIndices;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isLegacyRandom;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isRandomType;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isUniformPick;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ownDeclaration;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.returnsOffer;
import static com.yanny.ali.plugin.server.bytecode.Resolution.*;

final class Dispatcher implements Opcodes {
    private static final boolean TRACE = Boolean.getBoolean("ali.debug.dispatch");
    private static final Map<String, LongAdder> TRACED = new ConcurrentHashMap<>();

    private final Interpreter interpreter;

    Dispatcher(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    static List<String> dispatchTrace() {
        List<String> lines = TRACED.entrySet().stream().sorted(Map.Entry.comparingByKey()).map((e) -> e.getKey() + " " + e.getValue().sum()).toList();

        TRACED.clear();
        return lines;
    }

    static void trace(String label, MethodInsnNode call) {
        if (TRACE) {
            TRACED.computeIfAbsent(label + " " + call.owner + "." + call.name + call.desc, (k) -> new LongAdder()).increment();
        }
    }

    static void trace(String label, String type, String desc) {
        if (TRACE) {
            TRACED.computeIfAbsent(label + " " + type + "." + CONSTRUCTOR + desc, (k) -> new LongAdder()).increment();
        }
    }

    @Nullable
    Value invoke(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        Value result = dispatch(call, args, loader, depth);

        if (result instanceof Value.Known known && TradeTypes.isData(known.value()) && args.stream().anyMatch(interpreter::isTainted)) {
            trace("after.taint", call);
            interpreter.taint(known.value());
        }

        if (call.getOpcode() != INVOKESTATIC && call.name.equals("get") && args.size() == 2 && args.get(0) instanceof Value.Known map && map.value() instanceof Map<?, ?>
                && args.get(1) instanceof Value.Known key && result instanceof Value.Known listings && isKeyedList(listings.value())) {
            if (key.value() instanceof Integer level) {
                trace("after.levelKey", call);
                interpreter.listingKeys.put(listings.value(), level);
            } else if (key.value() instanceof Enum<?> constant) {
                trace("after.levelKey", call);
                interpreter.listingKeys.put(listings.value(), constant.ordinal() + 1);
            }
        }

        return result;
    }

    @Nullable
    private Value dispatch(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (call.name.equals(CONSTRUCTOR)) {
            if (!(args.get(0) instanceof Value.Uninit uninit)) {
                trace("constructor.chaining.fail", call);
                throw new Fail("constructor chaining");
            }

            interpreter.replace(uninit, interpreter.construct(uninit.type, call.desc, args.subList(1, args.size()), loader));
            return null;
        }
        if (isLogging(call.owner)) {
            Type returnType = Type.getReturnType(call.desc);

            if (returnType.getSort() == Type.VOID) {
                trace("owner.logger", call);
                return null;
            }

            trace("owner.logger", call);
            return returnType.getSort() == Type.BOOLEAN ? new Value.Known(0) : Value.Unknown.of(returnType, null);
        }
        if (RuntimeNames.is(RuntimeNames.FIND_MAP_STRUCTURE, call)) {
            trace("owner.findMapStructure", call);
            return new Value.Unknown(false, Type.getReturnType(call.desc).getInternalName(), args.get(1) instanceof Value.Known known ? known.value() : null,
                    Origin.of(args.get(0), call.name + call.desc, args.subList(1, args.size())));
        }
        if (RuntimeNames.is(RuntimeNames.MAP_CREATE, call)) {
            trace("owner.mapCreate", call);
            return new Value.Stack(List.of(new ItemStack(Items.FILLED_MAP)), NumberExpr.constant(1), false);
        }
        if (RuntimeNames.is(RuntimeNames.MAP_RENDER_BIOME_PREVIEW, call)) {
            trace("owner.renderBiomePreview", call);
            return null;
        }
        if (RuntimeNames.is(RuntimeNames.MAP_ADD_TARGET_DECORATION, call)) {
            if (!(args.get(0) instanceof Value.Stack stack) || !(args.get(3) instanceof Value.Known decoration)) {
                trace("owner.mapDecoration.fail", call);
                throw new Fail("map decoration");
            }

            stack.destination = args.get(1) instanceof Value.Unknown pos ? pos.found() : null;
            stack.decoration = decoration.value();
            trace("owner.mapDecoration", call);
            return null;
        }

        if (!isStatic && call.name.equals("equals") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(0) instanceof Value.Known known
                && args.get(1) instanceof Value.Unknown unknown) {
            trace("owner.equalsUnknown", call);
            return new Value.Unknown(false, null, null, Origin.equality(unknown, known.value()));
        }

        if (interpreter.merchant != null && RuntimeNames.is(RuntimeNames.GET_OFFER, call)) {
            if (args.get(0) instanceof Value.Choice choice && (choice.draw() != null && interpreter.pools.contains(choice.draw()) || isUniformPick(choice))
                    && choice.options().stream().allMatch(VillagerTrades.ItemListing.class::isInstance)) {
                trace("undistributed.getOffer.choice", call);
                return new Value.Pick(choice.options().stream().map(VillagerTrades.ItemListing.class::cast).toList(), choice.draw(), interpreter.poolKey(choice.options()));
            }
            if (args.get(0) instanceof Value.Known known && known.value() instanceof VillagerTrades.ItemListing listing) {
                Object source = interpreter.elementSources.containsKey(listing) ? interpreter.elementSources.get(listing) : interpreter.keyedSource(listing);

                trace("undistributed.getOffer.known", call);
                return new Value.Pick(List.of(listing), source != null ? source : listing, source != null ? interpreter.listingKeys.get(source) : null);
            }
        }
        if (interpreter.merchant != null && !isStatic && args.get(0) instanceof Value.Choice choice && (choice.draw() != null && interpreter.pools.contains(choice.draw()) || isUniformPick(choice))
                && choice.options().stream().noneMatch(MerchantOffer.class::isInstance) && returnsOffer(call, loader)) {
            List<Object> bound = interpreter.bound(args.subList(1, args.size()));

            if (bound != null) {
                List<VillagerTrades.ItemListing> listings = choice.options().stream().<VillagerTrades.ItemListing>map((element) -> interpreter.boundOffer(element, call, bound)).toList();

                trace("undistributed.boundOffer", call);
                return new Value.Pick(listings, choice.draw(), interpreter.poolKey(choice.options()));
            }
        }
        if (call.name.equals("intValue") && call.desc.equals("()I") && args.get(0) instanceof Value.Choice choice && choice.options().stream().allMatch(Integer.class::isInstance)) {
            trace("undistributed.intValue", call);
            return choice;
        }
        if (isStatic && call.owner.equals(OPTIONAL) && (call.name.equals("of") || call.name.equals("ofNullable")) && args.get(0) instanceof Value.Stack stack) {
            trace("undistributed.optionalOf", call);
            return stack;
        }
        if (!isStatic && call.owner.equals(OPTIONAL) && args.get(0) instanceof Value.Stack stack) {
            trace("undistributed.optionalStack", call);
            return interpreter.optionalCall(call, stack, args, depth);
        }
        if (!isStatic && call.owner.equals(OPTIONAL) && args.get(0) instanceof Value.Known known && known.value() instanceof Optional<?> optional
                && args.stream().skip(1).anyMatch((arg) -> arg instanceof Value.Closure || !interpreter.isConcrete(arg))) {
            trace("undistributed.optionalConcrete", call);
            return interpreter.optionalCall(call, optional.isPresent() ? interpreter.wrap(optional.get()) : null, args, depth);
        }

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && interpreter.owned.containsKey(list)
                && call.name.equals("remove") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Choice choice
                && choice.options().size() == list.size() - interpreter.removed.getOrDefault(list, 0) && new HashSet<Object>(list).containsAll(choice.options())) {
            interpreter.removed.merge(list, 1, Integer::sum);
            trace("undistributed.removeChoice", call);
            return new Value.Known(1);
        }
        Value.Offers collected = !isStatic && call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && interpreter.isSymbolicOffer(args.get(1))
                ? interpreter.offerList(args.get(0), true) : null;

        if (collected != null) {
            trace("undistributed.addOffer", call);
            return args.get(1) instanceof Value.Choice choice ? interpreter.addPicked(collected, choice) : interpreter.offersCall(call, collected, args, depth);
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && interpreter.offerLists.containsKey(known.value())) {
            trace("undistributed.collectedOffers", call);
            return interpreter.offersCall(call, interpreter.offerLists.get(known.value()), args, depth);
        }

        int[] choices = choiceIndices(args);

        if (choices.length > 0) {
            trace("distribute", call);
            return interpreter.distribute(call, args, choices, loader, depth);
        }

        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Rng rng) {
            trace("receiver.rng", call);
            return interpreter.random(call, rng, args, depth);
        }
        if (receiver instanceof Value.Stack stack) {
            trace("receiver.stack", call);
            return interpreter.stackCall(call, stack, args, loader);
        }
        if (receiver instanceof Value.Known known && known.value() instanceof ItemStack real) {
            trace("receiver.realStack", call);
            return interpreter.knownStackCall(call, real, args, loader);
        }
        if (receiver instanceof Value.Num num) {
            trace("receiver.num", call);
            return interpreter.unbox(call, num);
        }
        if (receiver instanceof Value.Closure closure) {
            if (!call.name.equals(closure.samName())) {
                trace("receiver.closure.fail", call);
                throw new Fail("closure default method");
            }

            List<Value> all = new ArrayList<>(closure.captured());

            all.addAll(args.subList(1, args.size()));
            trace("receiver.closure", call);
            return interpreter.invokeHandle(closure.impl(), all, closure.loader(), depth + 1);
        }
        if (interpreter.isSelf(receiver)) {
            if (!interpreter.ownGetOffers && RuntimeNames.is(RuntimeNames.GET_OFFERS, call)) {
                trace("receiver.trader.getOffers", call);
                return interpreter.merchantOffers;
            }
            if (RuntimeNames.is(RuntimeNames.ADD_OFFERS_FROM_ITEM_LISTINGS, call)) {
                if (!(args.get(1) instanceof Value.Offers offers) || !(args.get(2) instanceof Value.Known known)
                        || !(known.value() instanceof VillagerTrades.ItemListing[] listings)) {
                    trace("receiver.trader.addOffers.fail", call);
                    throw new Fail("offers from item listings");
                }

                offers.entries.add(new Value.Offers.Listings(listings, args.get(3), interpreter.listingKeys.get(listings)));
                trace("receiver.trader.addOffers", call);
                return null;
            }

            boolean special = call.getOpcode() == INVOKESPECIAL;
            Class<?> start = special ? load(call.owner, loader) : interpreter.merchant;

            if (start != null) {
                Declaration own = ownDeclaration(start, call.name, call.desc, special);

                if (own != null) {
                    trace("receiver.trader.own", call);
                    return interpreter.execute(own.owner(), own.method(), args, depth + 1);
                }
            }
        }
        if (receiver instanceof Value.Offers offers) {
            trace("receiver.offers", call);
            return interpreter.offersCall(call, offers, args, depth);
        }
        if (receiver instanceof Value.Draws draws) {
            if (call.name.equals("hasNext") && call.desc.equals("()Z")) {
                trace("receiver.draws.hasNext", call);
                return new Value.Known(draws.left > 0 ? 1 : 0);
            }
            if (call.name.equals("next") && call.desc.equals("()Ljava/lang/Object;") && draws.left > 0) {
                draws.left--;
                trace("receiver.draws.next", call);
                return new Value.Choice(draws.options, draws.pool);
            }

            trace("receiver.draws.fail", call);
            throw new Fail("iteration over random draws");
        }
        if (receiver instanceof Value.Unknown && interpreter.registries != null && RuntimeNames.is(RuntimeNames.REGISTRY_ACCESS, call)) {
            trace("receiver.registryAccess", call);
            return new Value.Known(interpreter.registries);
        }
        if (receiver instanceof Value.Unknown) {
            Type returnType = Type.getReturnType(call.desc);

            if (returnType.getSort() == Type.OBJECT && isRandomType(returnType, loader)) {
                trace("receiver.unknown.random", call);
                return new Value.Rng(isLegacyRandom(returnType, loader));
            }

            interpreter.taintOwned(args);
            trace("receiver.unknown", call);
            return Value.Unknown.of(returnType, Origin.of(receiver, call.name + call.desc, args.subList(1, args.size())));
        }
        if (receiver instanceof Value.Offer || receiver instanceof Value.Uninit || receiver instanceof Value.Cmp) {
            trace("receiver.unsupported.fail", call);
            throw new Fail("unsupported receiver");
        }
        if (!isStatic && receiver instanceof Value.Known known && known.value() instanceof Map<?, ?> map && LOOKUPS.contains(call.name)
                && args.get(1) instanceof Value.Unknown key && key.origin() != null && args.subList(2, args.size()).stream().allMatch(interpreter::isConcrete)) {
            trace("receiver.lookup", call);
            return interpreter.lookup(call, map.getClass(), map, key.origin(), args, loader);
        }
        if (receiver instanceof Value.Known known && known.value() == null) {
            trace("receiver.null.fail", call);
            throw new Fail("null receiver");
        }
        if (receiver instanceof Value.Known known && LambdaResolver.isLambda(known.value())) {
            trace("receiver.lambda", call);
            return interpreter.lambdaCall(known.value(), args, depth);
        }

        Class<?> start;

        if (isStatic || call.getOpcode() == INVOKESPECIAL) {
            start = load(call.owner, loader);
        } else if (receiver instanceof Value.Known known) {
            start = known.value().getClass();
        } else {
            trace("resolve.unsupported.fail", call);
            throw new Fail("unsupported receiver");
        }

        Declared declared = declaration(start, call.name, call.desc, call.getOpcode() == INVOKESPECIAL);

        if (declared instanceof JdkDeclaration jdk) {
            return interpreter.jdkCall(call, jdk.owner(), args, loader);
        }

        if (isStatic && isJdk(start)) {
            return interpreter.jdkCall(call, start, args, loader);
        }

        if (args.stream().anyMatch(this::holdsStack)) {
            trace("arguments.stacks", call);
            return interpreter.stackArgumentCall(call, args, loader);
        }

        if (declared != null && interpreter.opaque.contains(call.owner + "." + call.name + call.desc) && isWorldCall(call, args, loader)) {
            trace("arguments.opaqueWorldCall", call);
            return worldResult(call, args);
        }
        if (!isStatic && receiver instanceof Value.Known known && args.stream().skip(1).anyMatch(interpreter::isTainted) && !TradeTypes.returnsTradeData(call)) {
            trace("arguments.tainted", call);
            return interpreter.uncertainChange(call, known.value());
        }

        Value result;

        if (declared instanceof Declaration declaration) {
            int before = interpreter.decisions.size();

            try {
                trace("execution.interpret", call);
                result = interpreter.execute(declaration.owner(), declaration.method(), args, depth + 1);
            } catch (Fail fail) {
                result = fallbackCall(call, args, loader, fail);
            }
            if (interpreter.decisions.size() - before > 1 && isWorldCall(call, args, loader)) {
                interpreter.heavyWorldCalls.merge(call.owner + "." + call.name + call.desc, interpreter.decisions.size() - before, Math::max);
            }
        } else {
            trace("execution.noBytecode", call);
            result = fallbackCall(call, args, loader, new Fail("no bytecode for " + call.owner + "." + call.name));
        }

        return result;
    }

    @Nullable
    private Value fallbackCall(MethodInsnNode call, List<Value> args, ClassLoader loader, Fail fail) {
        if (args.stream().allMatch(interpreter::isConcrete)) {
            trace("fallback.concrete", call);
            return interpreter.concreteCall(call, args, loader, false);
        }
        if (isWorldCall(call, args, loader)) {
            if (fail.isBudget()) {
                trace("fallback.opaqueBudget", call);
                interpreter.retry = interpreter.retry || interpreter.opaque.add(call.owner + "." + call.name + call.desc);
                throw fail;
            }

            trace("fallback.worldResult", call);
            return worldResult(call, args);
        }
        if (!fail.isBudget() && call.getOpcode() != INVOKESTATIC && args.get(0) instanceof Value.Known known && known.value() != null
                && interpreter.owned.containsKey(known.value()) && args.stream().skip(1).allMatch((v) -> interpreter.isConcrete(v) || v instanceof Value.Num || v instanceof Value.Unknown)
                && !TradeTypes.returnsTradeData(call)) {
            trace("fallback.uncertain", call);
            return interpreter.uncertainChange(call, known.value());
        }

        trace("fallback.rethrow", call);
        throw fail;
    }

    private boolean isWorldCall(MethodInsnNode call, List<Value> args, ClassLoader loader) {
        Type returnType = Type.getReturnType(call.desc);

        if (TradeTypes.returnsTradeData(call) || returnType.getSort() == Type.OBJECT && isRandomType(returnType, loader)) {
            return false;
        }

        return args.stream().anyMatch((v) -> v instanceof Value.Unknown || v instanceof Value.Rng)
                && args.stream().allMatch((v) -> v instanceof Value.Unknown || v instanceof Value.Rng || v instanceof Value.Known);
    }

    @Nullable
    private Value worldResult(MethodInsnNode call, List<Value> args) {
        Type returnType = Type.getReturnType(call.desc);

        interpreter.taintOwned(args);

        if (returnType.getSort() == Type.VOID) {
            return null;
        }
        if (call.getOpcode() == INVOKESTATIC) {
            return Value.Unknown.of(returnType, Origin.of(null, call.owner + "." + call.name + call.desc, args));
        }

        return Value.Unknown.of(returnType, Origin.of(args.get(0), call.name + call.desc, args.subList(1, args.size())));
    }

    private boolean holdsStack(Value value) {
        return value instanceof Value.Stack;
    }

    private static boolean isKeyedList(@Nullable Object value) {
        return value instanceof Object[] array && array.length > 0 || value instanceof List<?> list && !list.isEmpty();
    }

    private static boolean isLogging(String owner) {
        return owner.startsWith("org/slf4j/") || owner.startsWith("org/apache/logging/log4j/") || owner.startsWith("java/util/logging/");
    }
}
