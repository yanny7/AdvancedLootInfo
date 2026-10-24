package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.BaseStream;
import java.util.stream.IntStream;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.CONSTRUCTOR;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.FRESH_RESULTS;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ITEM_COST;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ITEM_STACK;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.LOOKUPS;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.MAX_OPTIONS;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.MERCHANT_OFFER;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ORDER_ONLY;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.READ_ONLY;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.STATIC_RANDOM;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.STATIC_SAFE;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.choiceIndices;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isForwardingOffer;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isLegacyRandom;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isPlainOffers;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isRandomType;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isScalar;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.isUniformPick;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.ownDeclaration;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.returnsOffer;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.sameDraw;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.withoutCap;
import static com.yanny.ali.plugin.server.bytecode.MethodModel.NO_OPINION;
import static com.yanny.ali.plugin.server.bytecode.Resolution.*;

final class Dispatcher implements Opcodes, CallContext {
    private static final boolean TRACE = Boolean.getBoolean("ali.debug.dispatch");
    private static final Map<String, LongAdder> TRACED = new ConcurrentHashMap<>();
    private static final Map<Phase, List<MethodModel>> MODELS = new EnumMap<>(Phase.class);

    private final Interpreter interpreter;

    static {
        MODELS.put(Phase.CONSTRUCTOR, List.of(
                construction(Dispatcher::newOffers),
                construction(Dispatcher::newStack),
                construction(Dispatcher::newItemCost),
                construction(Dispatcher::newOffer),
                construction(Dispatcher::newPerChoice),
                construction(Dispatcher::newUnknown)
        ));
        MODELS.put(Phase.OWNER, List.of(
                new LoggerModel(),
                new MapItemModel(),
                branch(Dispatcher::equalsUnknown)
        ));
        MODELS.put(Phase.UNDISTRIBUTED, List.of(
                branch(Dispatcher::getOffer),
                branch(Dispatcher::boundOffer),
                branch(Dispatcher::intValue),
                new OptionalModel(),
                branch(Dispatcher::removeChoice),
                branch(Dispatcher::addOffer),
                branch(Dispatcher::collectedOffers)
        ));
        MODELS.put(Phase.RECEIVER, List.of(
                branch(Dispatcher::rng),
                branch(Dispatcher::cost),
                branch(Dispatcher::stack),
                branch(Dispatcher::realStack),
                branch(Dispatcher::num),
                branch(Dispatcher::closure),
                branch(Dispatcher::trader),
                branch(Dispatcher::offers),
                branch(Dispatcher::draws),
                branch(Dispatcher::registryAccess),
                branch(Dispatcher::unknown),
                branch(Dispatcher::unsupported),
                branch(Dispatcher::lookup),
                branch(Dispatcher::nullReceiver),
                branch(Dispatcher::lambda)
        ));
        MODELS.put(Phase.JDK, List.of(
                branch(Dispatcher::requireNonNull),
                branch(Dispatcher::isNull),
                branch(Dispatcher::selectionGuard),
                branch(Dispatcher::threadLocalRandom),
                branch(Dispatcher::math),
                branch(Dispatcher::valueOf),
                branch(Dispatcher::listPick),
                branch(Dispatcher::removeRandom),
                branch(Dispatcher::orderOffers),
                branch(Dispatcher::shuffle),
                branch(Dispatcher::shuffled),
                branch(Dispatcher::addRandom),
                branch(Dispatcher::randomSet),
                branch(Dispatcher::removed),
                branch(Dispatcher::uncertain),
                branch(Dispatcher::staticUnknown)
        ));
    }

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
            interpreter.heap.taint(known.value());
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

    Value construct(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        Value answer = apply(Phase.CONSTRUCTOR, new Call(new MethodInsnNode(INVOKESPECIAL, type, CONSTRUCTOR, desc, false), args, loader, depth));

        return answer != NO_OPINION ? answer : realConstructor(type, desc, args, loader);
    }

    @Nullable
    Value jdk(MethodInsnNode call, Class<?> declaring, List<Value> args, ClassLoader loader, int depth) {
        Value answer = apply(Phase.JDK, new Call(call, args, loader, depth));

        return answer != NO_OPINION ? answer : jdkReal(call, declaring, args, loader);
    }

    @Override
    public void traceCase(String label, MethodInsnNode call) {
        trace(label, call);
    }

    @Override
    public boolean isConcrete(Value value) {
        return interpreter.isConcrete(value);
    }

    @Override
    public Value wrap(@Nullable Object value) {
        return interpreter.wrap(value);
    }

    @Nullable
    @Override
    public Value invoke(Value.Closure closure, List<Value> args, int depth) {
        List<Value> all = new ArrayList<>(closure.captured());

        all.addAll(args);
        return interpreter.invokeHandle(closure.impl(), all, closure.loader(), depth + 1);
    }

    @Nullable
    private Value apply(Phase phase, Call call) {
        for (MethodModel model : MODELS.get(phase)) {
            Value answer = model.apply(this, call);

            if (answer != NO_OPINION) {
                return answer;
            }
        }

        return NO_OPINION;
    }

    @Nullable
    private Value dispatch(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (call.name.equals(CONSTRUCTOR)) {
            if (!(args.get(0) instanceof Value.Uninit uninit)) {
                trace("constructor.chaining.fail", call);
                throw new Fail("constructor chaining");
            }

            Value created = construct(uninit.type, call.desc, args.subList(1, args.size()), loader, depth);

            interpreter.store.replaceInFrames((v) -> v == uninit, (v) -> created);
            return null;
        }

        Call invocation = new Call(call, args, loader, depth);
        Value answer = apply(Phase.OWNER, invocation);

        if (answer != NO_OPINION) {
            return answer;
        }

        answer = apply(Phase.UNDISTRIBUTED, invocation);

        if (answer != NO_OPINION) {
            return answer;
        }

        int[] choices = choiceIndices(args);

        if (choices.length > 0) {
            trace("distribute", call);
            return interpreter.distribute(call, args, choices, loader, depth);
        }

        answer = apply(Phase.RECEIVER, invocation);

        if (answer != NO_OPINION) {
            return answer;
        }

        Value receiver = isStatic ? null : args.get(0);

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
            return jdk(call, jdk.owner(), args, loader, depth);
        }

        if (isStatic && isJdk(start)) {
            return jdk(call, start, args, loader, depth);
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

    private Value realConstructor(String type, String desc, List<Value> args, ClassLoader loader) {
        if (!args.stream().allMatch(interpreter::isConcrete)) {
            trace("constructor.symbolic.fail", type, desc);
            throw new Fail("symbolic constructor arguments");
        }
        if (args.stream().anyMatch(interpreter::isSelection)) {
            trace("constructor.guard.fail", type, desc);
            throw new Fail("random selection passed to a call");
        }

        Class<?> owner = load(type, loader);
        Constructor<?> constructor = constructor(owner, desc);

        try {
            constructor.setAccessible(true);

            Object created = constructor.newInstance(interpreter.javaArgs(args, constructor.getParameterTypes(), 0));

            interpreter.heap.own(created);

            if (args.stream().anyMatch(interpreter::isTainted)) {
                interpreter.heap.taint(created);
            }

            trace("constructor.real", type, desc);
            return interpreter.wrap(created);
        } catch (Throwable e) {
            trace("constructor.real.fail", type, desc);
            throw new Fail("constructor failed");
        }
    }

    @Nullable
    private Value jdkReal(MethodInsnNode call, Class<?> declaring, List<Value> args, ClassLoader loader) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!args.stream().allMatch(interpreter::isConcrete)) {
            trace("jdk.symbolic.fail", call);
            throw new Fail("symbolic argument of " + call.owner + "." + call.name);
        }
        if (isStatic) {
            if (!STATIC_SAFE.contains(call.owner) || STATIC_RANDOM.contains(call.name)) {
                trace("jdk.static.fail", call);
                throw new Fail("JDK call " + call.owner + "." + call.name);
            }

            trace("jdk.static", call);
            return interpreter.concreteCall(call, args, loader, false);
        }

        Object receiver = ((Value.Known) args.get(0)).value();
        boolean ownedReceiver = interpreter.heap.isOwned(receiver) || receiver instanceof BaseStream<?, ?>;

        if (READ_ONLY.contains(call.name) && receiver != null && interpreter.heap.isTainted(receiver)) {
            trace("jdk.taintedRead.fail", call);
            throw new Fail("uncertain contents of " + declaring.getName());
        }

        if (!ownedReceiver && !READ_ONLY.contains(call.name)) {
            trace("jdk.notOwned.fail", call);
            throw new Fail("JDK call " + declaring.getName() + "." + call.name);
        }
        if (!READ_ONLY.contains(call.name) && receiver != null && interpreter.heap.isFrozen(receiver)) {
            trace("jdk.frozen.fail", call);
            throw new Fail("change under a random choice");
        }

        trace("jdk.real", call);
        Value result = interpreter.concreteCall(call, args, loader, false);

        if (result instanceof Value.Known known && known.value() != null && (ownedReceiver || FRESH_RESULTS.contains(call.name))) {
            interpreter.heap.own(known.value());
        }
        if (!READ_ONLY.contains(call.name) && args.stream().skip(1).anyMatch(interpreter::isTainted)) {
            interpreter.heap.taint(receiver);
        }

        return result;
    }

    private Value newOffers(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        if (desc.equals("()V") && isPlainOffers(load(type, loader))) {
            trace("constructor.offers", type, desc);
            return new Value.Offers();
        }

        return NO_OPINION;
    }

    private Value newStack(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        if (type.equals(ITEM_STACK)) {
            trace("constructor.stack", type, desc);
            return interpreter.constructStack(ItemStack.class, desc, args);
        }

        return NO_OPINION;
    }

    private Value newItemCost(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        if (type.equals(ITEM_COST)) {
            trace("constructor.itemCost", type, desc);
            return interpreter.constructStack(ItemCost.class, desc, args);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value newOffer(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        if (type.equals(MERCHANT_OFFER) || !args.stream().allMatch(interpreter::isConcrete) && isForwardingOffer(load(type, loader), desc)) {
            trace("constructor.offer", type, desc);
            return interpreter.constructOffer(desc, args);
        }

        return NO_OPINION;
    }

    private Value newPerChoice(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        List<Value> drawn = args.stream().map(Interpreter::drawnInt).toList();
        int[] choices = choiceIndices(drawn);

        if (choices.length > 0) {
            Value.Choice first = (Value.Choice) drawn.get(choices[0]);
            List<Value> results = new ArrayList<>();

            for (int option = 0; option < first.options().size(); option++) {
                results.add(construct(type, desc, interpreter.select(drawn, choices, option), loader, depth));
            }

            trace("constructor.perChoice", type, desc);
            return Objects.requireNonNull(sameDraw(interpreter.combine(results), first));
        }

        return NO_OPINION;
    }

    private Value newUnknown(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        if (args.stream().anyMatch(Value.Unknown.class::isInstance) && args.stream().allMatch((v) -> v instanceof Value.Unknown || interpreter.isConcrete(v))) {
            interpreter.taintOwned(args);
            trace("constructor.unknown", type, desc);
            return Value.Unknown.of(Type.getObjectType(type), Origin.of(null, type + "." + CONSTRUCTOR + desc, args));
        }

        return NO_OPINION;
    }

    private Value equalsUnknown(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && call.name.equals("equals") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(0) instanceof Value.Known known
                && args.get(1) instanceof Value.Unknown unknown) {
            trace("owner.equalsUnknown", call);
            return new Value.Unknown(false, null, null, Origin.equality(unknown, known.value()));
        }

        return NO_OPINION;
    }

    private Value getOffer(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        if (interpreter.merchant != null && RuntimeNames.is(RuntimeNames.GET_OFFER, call)) {
            if (args.get(0) instanceof Value.Choice choice && (choice.draw() != null && interpreter.heap.isPool(choice.draw()) || isUniformPick(choice))
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

        return NO_OPINION;
    }

    @Nullable
    private Value boundOffer(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (interpreter.merchant != null && !isStatic && args.get(0) instanceof Value.Choice choice && (choice.draw() != null && interpreter.heap.isPool(choice.draw()) || isUniformPick(choice))
                && choice.options().stream().noneMatch(MerchantOffer.class::isInstance) && returnsOffer(call, loader)) {
            List<Object> bound = interpreter.bound(args.subList(1, args.size()));

            if (bound != null) {
                List<VillagerTrades.ItemListing> listings = choice.options().stream().<VillagerTrades.ItemListing>map((element) -> interpreter.boundOffer(element, call, bound)).toList();

                trace("undistributed.boundOffer", call);
                return new Value.Pick(listings, choice.draw(), interpreter.poolKey(choice.options()));
            }
        }

        return NO_OPINION;
    }

    private Value intValue(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        if (call.name.equals("intValue") && call.desc.equals("()I") && args.get(0) instanceof Value.Choice choice && choice.options().stream().allMatch(Integer.class::isInstance)) {
            trace("undistributed.intValue", call);
            return choice;
        }

        return NO_OPINION;
    }

    private Value removeChoice(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && interpreter.heap.isOwned(list)
                && call.name.equals("remove") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Choice choice
                && choice.options().size() == list.size() - interpreter.heap.removals(list) && new HashSet<Object>(list).containsAll(choice.options())) {
            interpreter.heap.coll(list).removals++;
            trace("undistributed.removeChoice", call);
            return new Value.Known(1);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value addOffer(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        Value.Offers collected = !isStatic && call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && interpreter.isSymbolicOffer(args.get(1))
                ? interpreter.offerList(args.get(0), true) : null;

        if (collected != null) {
            trace("undistributed.addOffer", call);
            return args.get(1) instanceof Value.Choice choice ? interpreter.addPicked(collected, choice) : interpreter.offersCall(call, collected, args, depth);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value collectedOffers(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && interpreter.heap.offers(known.value()) != null) {
            trace("undistributed.collectedOffers", call);
            return interpreter.offersCall(call, interpreter.heap.offers(known.value()), args, depth);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value rng(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Rng rng) {
            trace("receiver.rng", call);
            return interpreter.random(call, rng, args, depth);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value cost(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Stack stack && call.owner.equals(ITEM_COST)) {
            trace("receiver.cost", call);
            return interpreter.costCall(call, stack, args, loader);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value stack(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Stack stack) {
            trace("receiver.stack", call);
            return interpreter.stackCall(call, stack, args, loader);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value realStack(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Known known && known.value() instanceof ItemStack real) {
            trace("receiver.realStack", call);
            return interpreter.knownStackCall(call, real, args, loader);
        }

        return NO_OPINION;
    }

    private Value num(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Num num) {
            trace("receiver.num", call);
            return interpreter.unbox(call, num);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value closure(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

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

        return NO_OPINION;
    }

    @Nullable
    private Value trader(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

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

        return NO_OPINION;
    }

    @Nullable
    private Value offers(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Offers offers) {
            trace("receiver.offers", call);
            return interpreter.offersCall(call, offers, args, depth);
        }

        return NO_OPINION;
    }

    private Value draws(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

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

        return NO_OPINION;
    }

    private Value registryAccess(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Unknown && interpreter.registries != null && (RuntimeNames.is(RuntimeNames.REGISTRY_ACCESS, call) || RuntimeNames.is(RuntimeNames.ENTITY_REGISTRY_ACCESS, call))) {
            trace("receiver.registryAccess", call);
            return new Value.Known(interpreter.registries);
        }

        return NO_OPINION;
    }

    private Value unknown(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

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

        return NO_OPINION;
    }

    private Value unsupported(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Offer || receiver instanceof Value.Uninit || receiver instanceof Value.Cmp) {
            trace("receiver.unsupported.fail", call);
            throw new Fail("unsupported receiver");
        }

        return NO_OPINION;
    }

    @Nullable
    private Value lookup(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (!isStatic && receiver instanceof Value.Known known && known.value() instanceof Map<?, ?> map && LOOKUPS.contains(call.name)
                && args.get(1) instanceof Value.Unknown key && key.origin() != null && args.subList(2, args.size()).stream().allMatch(interpreter::isConcrete)) {
            trace("receiver.lookup", call);
            return interpreter.lookup(call, map.getClass(), map, key.origin(), args, loader, depth);
        }

        return NO_OPINION;
    }

    private Value nullReceiver(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Known known && known.value() == null) {
            trace("receiver.null.fail", call);
            throw new Fail("null receiver");
        }

        return NO_OPINION;
    }

    @Nullable
    private Value lambda(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Known known && LambdaResolver.isLambda(known.value())) {
            trace("receiver.lambda", call);
            return interpreter.lambdaCall(known.value(), args, depth);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value requireNonNull(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/Objects") && call.name.startsWith("requireNonNull")) {
            if (interpreter.isNull(args.get(0))) {
                trace("jdk.requireNonNull.fail", call);
                throw new Fail("requireNonNull");
            }

            trace("jdk.requireNonNull", call);
            return args.get(0);
        }

        return NO_OPINION;
    }

    private Value isNull(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/Objects") && (call.name.equals("isNull") || call.name.equals("nonNull"))) {
            trace("jdk.isNull", call);
            return new Value.Known(interpreter.isNull(args.get(0)) == call.name.equals("isNull") ? 1 : 0);
        }

        return NO_OPINION;
    }

    private Value selectionGuard(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (args.stream().skip(isStatic ? 0 : 1).anyMatch(interpreter::isSelection)) {
            trace("jdk.guard.fail", call);
            throw new Fail("random selection passed to a call");
        }

        return NO_OPINION;
    }

    private Value threadLocalRandom(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/concurrent/ThreadLocalRandom") && call.name.equals("current")) {
            trace("jdk.threadLocalRandom", call);
            return new Value.Rng(true);
        }

        return NO_OPINION;
    }

    private Value math(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Type returnType = Type.getReturnType(call.desc);

        if (isStatic && (call.owner.equals("java/lang/Math") || call.owner.equals("java/lang/StrictMath")) && args.stream().anyMatch((v) -> v instanceof Value.Num)) {
            trace("jdk.math", call);
            return interpreter.math(call, args, returnType);
        }

        return NO_OPINION;
    }

    private Value valueOf(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.name.equals("valueOf") && args.size() == 1 && args.get(0) instanceof Value.Num num) {
            trace("jdk.valueOf", call);
            return call.owner.equals("java/lang/String") ? Value.UNKNOWN : num;
        }

        return NO_OPINION;
    }

    private Value listPick(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && call.name.equals("get")
                && call.desc.equals("(I)Ljava/lang/Object;") && args.get(1) instanceof Value.Num num) {
            trace("jdk.listPick", call);
            return interpreter.pick(list, num);
        }

        return NO_OPINION;
    }

    private Value removeRandom(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && interpreter.heap.isOwned(list)
                && call.name.equals("remove") && call.desc.equals("(I)Ljava/lang/Object;")
                && (args.get(1) instanceof Value.Num || interpreter.heap.removals(list) > 0 || interpreter.merchant != null && list.size() == 1)) {
            trace("jdk.removeRandom", call);
            return interpreter.removeRandom(list, args.get(1));
        }

        return NO_OPINION;
    }

    @Nullable
    private Value orderOffers(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/Collections") && ORDER_ONLY.contains(call.name) && args.get(0) instanceof Value.Offers) {
            trace("jdk.orderOffers", call);
            return null;
        }

        return NO_OPINION;
    }

    @Nullable
    private Value shuffle(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/Collections") && call.name.equals("shuffle") && args.get(0) instanceof Value.Known known
                && known.value() instanceof List<?> list && interpreter.heap.isOwned(list) && list.size() <= MAX_OPTIONS) {
            if (!list.isEmpty()) {
                CollState coll = interpreter.heap.coll(list);

                coll.shuffled = new Value.Draws(list, new ArrayList<>(list), list.size());
                coll.pool = true;
            }

            trace("jdk.shuffle", call);
            return null;
        }

        return NO_OPINION;
    }

    private Value shuffled(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && interpreter.heap.shuffled(known.value()) != null) {
            Value.Draws draws = interpreter.heap.shuffled(known.value());

            switch (call.name + call.desc) {
                case "size()I" -> {
                    trace("jdk.shuffled", call);
                    return new Value.Known(draws.left);
                }
                case "isEmpty()Z" -> {
                    trace("jdk.shuffled", call);
                    return new Value.Known(draws.left == 0 ? 1 : 0);
                }
                case "iterator()Ljava/util/Iterator;" -> {
                    trace("jdk.shuffled", call);
                    return new Value.Draws(draws.pool, draws.options, draws.left);
                }
                case "get(I)Ljava/lang/Object;" -> {
                    if (args.get(1) instanceof Value.Known index && (Integer) index.value() < draws.left) {
                        trace("jdk.shuffled", call);
                        return new Value.Choice(draws.options, draws.pool);
                    }
                }
                case "subList(II)Ljava/util/List;" -> {
                    if (args.get(1) instanceof Value.Known from && Integer.valueOf(0).equals(from.value())
                            && (args.get(2) instanceof Value.Known || args.get(2) instanceof Value.Num)) {
                        NumberExpr to = interpreter.expr(args.get(2));
                        Object view = new ArrayList<>();

                        if (args.get(2) instanceof Value.Num) {
                            interpreter.heap.coll(draws.pool).drawCount = withoutCap(to, draws.left);
                        }

                        interpreter.heap.own(view);
                        interpreter.heap.coll(view).shuffled = new Value.Draws(draws.pool, draws.options, Math.min(draws.left, (int) Math.floor(to.bounds().hi())));
                        trace("jdk.shuffled", call);
                        return new Value.Known(view);
                    }
                }
                default -> {
                }
            }

            trace("jdk.shuffled.fail", call);
            throw new Fail("change under a random choice");
        }

        return NO_OPINION;
    }

    private Value addRandom(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof Set<?> set && interpreter.heap.isOwned(set)
                && call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Num num) {
            trace("jdk.addRandom", call);
            return interpreter.addRandom(set, num);
        }

        return NO_OPINION;
    }

    private Value randomSet(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && interpreter.heap.randomIndexSet(known.value()) != null) {
            int[] draws = interpreter.heap.randomIndexSet(known.value());

            trace("jdk.randomSet", call);
            return switch (call.name + call.desc) {
                case "size()I" -> new Value.Known(draws[1]);
                case "iterator()Ljava/util/Iterator;" -> new Value.Draws(known.value(), IntStream.range(0, draws[0]).boxed().map(Object.class::cast).toList(), draws[1]);
                default -> throw new Fail("change under a random choice");
            };
        }

        return NO_OPINION;
    }

    private Value removed(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && interpreter.heap.removals(known.value()) > 0) {
            int remaining = ((List<?>) known.value()).size() - interpreter.heap.removals(known.value());

            trace("jdk.removed", call);
            return switch (call.name + call.desc) {
                case "size()I" -> new Value.Known(remaining);
                case "isEmpty()Z" -> new Value.Known(remaining == 0 ? 1 : 0);
                default -> throw new Fail("change under a random choice");
            };
        }

        return NO_OPINION;
    }

    @Nullable
    private Value uncertain(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && interpreter.heap.isOwned(known.value())
                && args.stream().skip(1).anyMatch((v) -> v instanceof Value.Unknown || v instanceof Value.Num)
                && args.stream().skip(1).allMatch((v) -> interpreter.isConcrete(v) || v instanceof Value.Unknown || v instanceof Value.Num) && !TradeTypes.returnsTradeData(call)) {
            trace("jdk.uncertain", call);
            return interpreter.uncertainChange(call, READ_ONLY.contains(call.name) ? null : known.value());
        }

        return NO_OPINION;
    }

    private Value staticUnknown(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Type returnType = Type.getReturnType(call.desc);

        if (isStatic && STATIC_SAFE.contains(call.owner) && !STATIC_RANDOM.contains(call.name) && returnType.getSort() != Type.VOID
                && args.stream().anyMatch(Value.Unknown.class::isInstance) && args.stream().allMatch((v) -> v instanceof Value.Unknown || isScalar(v))) {
            trace("jdk.staticUnknown", call);
            return Value.Unknown.of(returnType, null);
        }

        return NO_OPINION;
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
                && interpreter.heap.isOwned(known.value()) && args.stream().skip(1).allMatch((v) -> interpreter.isConcrete(v) || v instanceof Value.Num || v instanceof Value.Unknown)
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

    private static MethodModel branch(Branch branch) {
        return (context, call) -> branch.apply((Dispatcher) context, call.insn(), call.args(), call.loader(), call.depth());
    }

    private static MethodModel construction(ConstructorBranch branch) {
        return (context, call) -> branch.apply((Dispatcher) context, call.insn().owner, call.insn().desc, call.args(), call.loader(), call.depth());
    }

    enum Phase {
        CONSTRUCTOR,
        OWNER,
        UNDISTRIBUTED,
        RECEIVER,
        JDK
    }

    private interface Branch {
        @Nullable
        Value apply(Dispatcher dispatcher, MethodInsnNode call, List<Value> args, ClassLoader loader, int depth);
    }

    private interface ConstructorBranch {
        Value apply(Dispatcher dispatcher, String type, String desc, List<Value> args, ClassLoader loader, int depth);
    }
}
