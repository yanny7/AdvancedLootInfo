package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.yanny.ali.plugin.server.bytecode.Resolution.*;

final class Interpreter implements Opcodes {
    static final int MAX_OPTIONS = 1024;
    static final String CONSTRUCTOR = "<init>";
    static final String ITEM_STACK = Type.getInternalName(ItemStack.class);
    static final String MERCHANT_OFFER = Type.getInternalName(MerchantOffer.class);
    static final Value.Known NULL = new Value.Known(null);
    static final Set<String> STATIC_SAFE = Set.of("java/lang/Math", "java/lang/StrictMath", "java/lang/Integer", "java/lang/Long",
            "java/lang/Float", "java/lang/Double", "java/lang/Boolean", "java/lang/Character", "java/lang/Short", "java/lang/Byte",
            "java/lang/String", "java/lang/Enum", "java/util/Objects", "java/util/List", "java/util/Set", "java/util/Map",
            "java/util/Collections", "java/util/Arrays", "java/util/Optional", "java/util/Comparator", "java/util/stream/Stream",
            "java/util/stream/IntStream", "java/util/stream/Collectors");
    static final Set<String> STATIC_RANDOM = Set.of("random", "shuffle");
    static final Set<String> READ_ONLY = Stream.concat(Stream.of("get", "getOrDefault", "size", "isEmpty", "contains", "containsKey",
            "containsValue", "indexOf", "lastIndexOf", "keySet", "values", "entrySet", "stream", "iterator", "listIterator",
            "spliterator", "toArray", "equals", "hashCode", "toString", "length", "charAt", "substring", "startsWith", "endsWith",
            "trim", "toLowerCase", "toUpperCase", "concat", "getKey", "getValue", "intValue", "longValue", "floatValue",
            "doubleValue", "booleanValue", "byteValue", "shortValue", "charValue", "compareTo", "ordinal", "name", "getClass",
            "map", "filter", "subList", "first", "firstKey", "lastKey"), OptionalModel.READ_ONLY.stream()).collect(Collectors.toUnmodifiableSet());
    static final Set<String> ORDER_ONLY = Set.of("reverse", "shuffle", "sort");
    static final Set<String> FRESH_RESULTS = Set.of("stream", "iterator", "listIterator", "spliterator", "toArray");

    final Mode mode;
    final Dispatcher dispatcher = new Dispatcher(this);
    final Machine machine = new Machine(this);
    final int pick;
    final ValueStore store = new ValueStore();
    final Heap heap = new Heap(store);
    @Nullable
    TraderModel.TraderRun traderRun;
    final Map<Object, Integer> listingKeys = new IdentityHashMap<>();
    private final Map<Object, List<BoundOffer>> boundOffers = new IdentityHashMap<>();
    final Map<Object, Object> elementSources = new IdentityHashMap<>();
    @Nullable
    final RegistryAccess registries;
    final OpaqueCalls opaque;
    final NumberDomain numbers = new NumberDomain(store);
    final Decisions decisions;

    Interpreter(Mode mode, int pick) {
        this(mode, pick, null, null, new HashSet<>());
    }

    Interpreter(Mode mode, int pick, @Nullable List<Boolean> script, @Nullable RegistryAccess registries, Set<String> opaque) {
        this.mode = mode;
        this.pick = pick;
        this.registries = registries;
        this.opaque = new OpaqueCalls(opaque);
        this.decisions = new Decisions(script, this.opaque, numbers, store, heap);
    }

    @Nullable
    Value call(Object receiver, Class<?> iface, String name, String desc, List<Value> args) {
        return call(receiver, new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(iface), name, desc, true), args);
    }

    Value call(Object receiver, MethodInsnNode call, List<Value> args) {
        List<Value> all = new ArrayList<>();

        all.add(heap.wrap(receiver));
        all.addAll(args);
        return dispatcher.invoke(call, all, receiver.getClass().getClassLoader(), 0);
    }

    Value.Offers trades(Class<?> merchant) {
        Value self = Value.Unknown.of(Type.getType(merchant), Origin.TRADER);
        Declaration getOffers = TraderModel.ownDeclaration(merchant, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), false);
        TraderModel.TraderRun run = new TraderModel.TraderRun(merchant, getOffers != null);

        traderRun = run;

        if (getOffers != null) {
            if (machine.execute(getOffers.owner(), getOffers.method(), List.of(self), 0) instanceof Value.Offers offers) {
                return offers;
            }

            throw new Fail("offers of the trader");
        }

        Declaration updateTrades = TraderModel.ownDeclaration(merchant, RuntimeNames.UPDATE_TRADES.name(), RuntimeNames.UPDATE_TRADES.desc(), false);

        if (updateTrades == null) {
            throw new Fail("no trades of the trader");
        }

        machine.execute(updateTrades.owner(), updateTrades.method(), List.of(self), 0);
        return run.offers;
    }

    Value.Offers writtenTrades(Class<?> merchant) {
        Declaration writer = offersWriter(merchant);

        if (writer == null) {
            throw new Fail("no method writing the offers of the trader");
        }

        List<Value> args = new ArrayList<>();

        args.add(Value.Unknown.of(Type.getType(merchant), Origin.TRADER));
        Arrays.stream(Type.getArgumentTypes(writer.method().desc)).forEach((type) -> args.add(Value.Unknown.of(type, null)));
        TraderModel.TraderRun run = new TraderModel.TraderRun(merchant, TraderModel.ownDeclaration(merchant, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), false) != null);

        traderRun = run;
        machine.execute(writer.owner(), writer.method(), args, 0);
        return run.offers;
    }

    static boolean writesOffers(Class<?> merchant) {
        return offersWriter(merchant) != null;
    }

    @Nullable
    private static Declaration offersWriter(Class<?> merchant) {
        Map<String, Declaration> writers = new HashMap<>();

        for (Class<?> type = merchant; type != null && !type.getName().startsWith("net.minecraft."); type = type.getSuperclass()) {
            ClassNode node = classNode(type);

            if (node == null) {
                continue;
            }

            for (MethodNode method : node.methods) {
                if ((method.access & ACC_STATIC) != 0 || method.name.startsWith("<") || RuntimeNames.GET_OFFERS.name().equals(method.name)
                        && RuntimeNames.GET_OFFERS.desc().equals(method.desc)) {
                    continue;
                }

                Declaration own = TraderModel.ownDeclaration(merchant, method.name, method.desc, false);

                if (own != null && writesOffers(own.method(), own.owner().getClassLoader())) {
                    writers.put(method.name + method.desc, own);
                }
            }
        }

        return writers.size() == 1 ? writers.values().iterator().next() : null;
    }

    private static boolean writesOffers(MethodNode method, ClassLoader loader) {
        for (AbstractInsnNode insn : method.instructions) {
            try {
                if (insn.getOpcode() == PUTFIELD && FieldModel.isVillagerOffers((FieldInsnNode) insn, loader)) {
                    return true;
                }
            } catch (Fail ignored) {}
        }

        return false;
    }

    static boolean isPlainOffers(Class<?> type) {
        return MerchantOffers.class.isAssignableFrom(type) && declaration(type, "add", "(Ljava/lang/Object;)Z", false) instanceof JdkDeclaration;
    }

    boolean isSelf(@Nullable Value value) {
        return traderRun != null && traderRun.isSelf(value);
    }

    @Nullable
    Value invokeHandle(Handle handle, List<Value> args, ClassLoader loader, int depth) {
        return switch (handle.getTag()) {
            case H_INVOKESTATIC -> dispatcher.invoke(new MethodInsnNode(INVOKESTATIC, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface()), args, loader, depth);
            case H_INVOKEVIRTUAL -> dispatcher.invoke(new MethodInsnNode(INVOKEVIRTUAL, handle.getOwner(), handle.getName(), handle.getDesc(), false), args, loader, depth);
            case H_INVOKEINTERFACE -> dispatcher.invoke(new MethodInsnNode(INVOKEINTERFACE, handle.getOwner(), handle.getName(), handle.getDesc(), true), args, loader, depth);
            case H_INVOKESPECIAL -> dispatcher.invoke(new MethodInsnNode(INVOKESPECIAL, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface()), args, loader, depth);
            case H_NEWINVOKESPECIAL -> dispatcher.construct(handle.getOwner(), handle.getDesc(), args, loader, depth);
            default -> throw new Fail("unsupported method handle");
        };
    }

    @Nullable
    Value lambdaCall(Object lambda, List<Value> args, int depth) {
        LambdaResolver.Target target = LambdaResolver.resolve(lambda);

        if (target == null) {
            throw new Fail("unresolved lambda (" + LambdaResolver.reason(lambda) + ")");
        }

        List<Value> all = new ArrayList<>(target.captured().stream().map(heap::wrap).toList());

        all.addAll(args.subList(1, args.size()));
        return invokeHandle(target.impl(), all, target.owner().getClassLoader(), depth + 1);
    }

    @Nullable
    Value uncertainChange(MethodInsnNode call, @Nullable Object changed) {
        Type returnType = Type.getReturnType(call.desc);

        heap.taint(changed);
        return returnType.getSort() == Type.VOID ? null : Value.Unknown.of(returnType, null);
    }

    boolean isTainted(Value value) {
        return value instanceof Value.Known known && known.value() != null && heap.isTainted(known.value());
    }

    void taintOwned(List<Value> args) {
        args.forEach((v) -> heap.taint(v instanceof Value.Known known && known.value() != null && heap.isOwned(known.value()) ? known.value() : null));
    }

    @Nullable
    Value distribute(MethodInsnNode call, List<Value> args, int[] choices, ClassLoader loader, int depth) {
        List<Value> base = shadowed(args);
        Value.Choice first = (Value.Choice) args.get(choices[0]);
        int stackIndex = -1;

        for (int i = 0; i < base.size(); i++) {
            if (base.get(i) instanceof Value.Choice) {
                continue;
            }
            if (base.get(i) instanceof Value.Stack) {
                if (stackIndex >= 0) {
                    throw new Fail("choice over several stack arguments");
                }

                stackIndex = i;
            } else if (base.get(i) instanceof Value.Known known && known.value() instanceof ItemStack) {
                throw new Fail("choice over a call with a stack argument");
            }
        }

        Value.Stack original = stackIndex >= 0 ? (Value.Stack) base.get(stackIndex) : null;
        List<Value.Stack> branches = new ArrayList<>();
        List<Value> results = new ArrayList<>();

        try (Heap.Freeze ignored = heap.freeze()) {
            for (int option = 0; option < first.options().size(); option++) {
                List<Value> copy = select(base, choices, option);

                if (original != null) {
                    Value.Stack branch = original.copy();

                    copy.set(stackIndex, branch);
                    branches.add(branch);
                }

                results.add(dispatcher.invoke(call, copy, loader, depth));
            }
        }

        if (original != null) {
            merge(original, branches);
        }

        return sameDraw(combine(results), first);
    }

    List<Value> select(List<Value> args, int[] choices, int option) {
        List<Value> copy = new ArrayList<>(args);

        for (int index : choices) {
            copy.set(index, heap.wrap(((Value.Choice) args.get(index)).options().get(option)));
        }

        return copy;
    }

    @Nullable
    static Value sameDraw(@Nullable Value result, Value.Choice source) {
        if (source.draw() != null && result instanceof Value.Choice choice && choice.options().size() == source.options().size()) {
            return new Value.Choice(choice.options(), source.draw());
        }

        return result;
    }

    private void merge(Value.Stack original, List<Value.Stack> branches) {
        NumberExpr count = branches.get(0).count;

        if (branches.stream().anyMatch((b) -> !b.count.equals(count))) {
            throw new Fail("options with different counts");
        }

        original.options.clear();
        branches.forEach((b) -> original.options.addAll(b.options));
        original.count = count;
        original.randomTag |= branches.stream().anyMatch((b) -> b.randomTag);
        limit(original);
    }

    @Nullable
    Value combine(List<Value> results) {
        if (results.stream().allMatch(Objects::isNull)) {
            return null;
        }
        if (results.stream().anyMatch(Objects::isNull)) {
            throw new Fail("mixed results");
        }
        if (results.stream().allMatch((r) -> r instanceof Value.Stack || r instanceof Value.Known known && known.value() instanceof ItemStack)) {
            List<ItemStack> options = new ArrayList<>();
            NumberExpr count = null;

            for (Value result : results) {
                Value.Stack stack = result instanceof Value.Stack s ? s : stackOf((ItemStack) ((Value.Known) result).value());

                if (count != null && !count.equals(stack.count)) {
                    throw new Fail("options with different counts");
                }

                count = stack.count;
                options.addAll(stack.options);
            }

            return limit(new Value.Stack(options, count, results.stream().anyMatch((r) -> r instanceof Value.Stack s && s.randomTag)));
        }
        if (results.stream().allMatch((r) -> r instanceof Value.Known)) {
            List<Object> values = results.stream().map((r) -> ((Value.Known) r).value()).toList();

            return values.stream().allMatch((v) -> v == values.get(0)) ? results.get(0) : new Value.Choice(values);
        }
        if (results.stream().allMatch((r) -> r instanceof Value.Num || r instanceof Value.Known known && known.value() instanceof Number)) {
            Value.Kind kind = results.stream().filter(Value.Num.class::isInstance).map((r) -> ((Value.Num) r).kind()).findFirst().orElse(Value.Kind.INT);

            return new Value.Num(NumberExpr.weighted(results.stream().map((r) -> new NumberExpr.WeightedEntry(1, NumberDomain.expr(r))).toList()), kind);
        }

        throw new Fail("results cannot be combined");
    }

    static boolean isRandomType(Type type, ClassLoader loader) {
        try {
            Class<?> cls = load(type.getInternalName(), loader);

            return RandomSource.class.isAssignableFrom(cls) || Random.class.isAssignableFrom(cls);
        } catch (Fail fail) {
            return false;
        }
    }

    static boolean isLegacyRandom(Type type, ClassLoader loader) {
        return Random.class.isAssignableFrom(load(type.getInternalName(), loader));
    }

    List<Value> shadowed(List<Value> args) {
        return args.stream().map((v) -> v instanceof Value.Known known && known.value() instanceof ItemStack real && !heap.isOwned(real) ? heap.shadow(real) : v).toList();
    }

    static Value.Stack stackOf(ItemStack real) {
        return new Value.Stack(List.of(real.copy()), NumberExpr.constant(real.getCount()), false);
    }

    static Value limit(Value.Stack stack) {
        if (stack.options.size() > MAX_OPTIONS) {
            throw new Fail("too many options");
        }

        return stack;
    }

    @Nullable
    Object keyedSource(Object listing) {
        for (Object keyed : listingKeys.keySet()) {
            List<?> elements = keyed instanceof Object[] array ? Arrays.asList(array) : (List<?>) keyed;

            if (elements.stream().anyMatch((e) -> e == listing)) {
                return keyed;
            }
        }

        return null;
    }

    static boolean isUniformPick(Value.Choice choice) {
        Set<Object> distinct = Collections.newSetFromMap(new IdentityHashMap<>());

        distinct.addAll(choice.options());
        return choice.draw() instanceof NumberExpr && distinct.size() == choice.options().size();
    }

    static boolean returnsOffer(MethodInsnNode call, ClassLoader loader) {
        Type returnType = Type.getReturnType(call.desc);

        return returnType.getSort() == Type.OBJECT && MerchantOffer.class.isAssignableFrom(load(returnType.getInternalName(), loader));
    }

    @Nullable
    List<Object> bound(List<Value> args) {
        List<Object> bound = new ArrayList<>();

        for (Value arg : args) {
            if (isSelf(arg)) {
                bound.add(BoundOffer.Bound.TRADER);
            } else if (arg instanceof Value.Rng rng) {
                bound.add(rng.legacy() ? BoundOffer.Bound.LEGACY_RANDOM : BoundOffer.Bound.RANDOM);
            } else if (arg instanceof Value.Known known && isConcrete(arg)) {
                bound.add(known.value());
            } else {
                return null;
            }
        }

        return bound;
    }

    BoundOffer boundOffer(Object element, MethodInsnNode call, List<Object> args) {
        BoundOffer offer = new BoundOffer(element, call.owner, call.name, call.desc, call.itf, args);

        return boundOffers.computeIfAbsent(element, (e) -> new ArrayList<>()).stream().filter(offer::equals).findFirst().orElseGet(() -> {
            boundOffers.get(element).add(offer);
            return offer;
        });
    }

    @Nullable
    Integer poolKey(List<Object> listings) {
        for (Map.Entry<Object, Integer> entry : listingKeys.entrySet()) {
            List<?> keyed = entry.getKey() instanceof Object[] array ? Arrays.asList(array) : (List<?>) entry.getKey();

            if (keyed.equals(listings)) {
                return entry.getValue();
            }
        }

        return null;
    }

    @Nullable
    Value offersCall(MethodInsnNode call, Value.Offers offers, List<Value> args, int depth) {
        if (call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Pick pickValue) {
            Integer index = offers.picks.get(pickValue.pool());

            NumberExpr drawCount = heap.drawCount(pickValue.pool());

            if (index == null) {
                offers.picks.put(pickValue.pool(), offers.entries.size());
                offers.entries.add(new Value.Offers.Listings(pickValue.listings().toArray(new VillagerTrades.ItemListing[0]),
                        drawCount != null ? new Value.Num(drawCount, Value.Kind.INT) : new Value.Known(1), pickValue.key()));
            } else {
                if (!(offers.entries.get(index) instanceof Value.Offers.Listings listings)) {
                    throw new Fail("merchant offers call add");
                }

                List<VillagerTrades.ItemListing> union = new ArrayList<>(Arrays.asList(listings.listings()));

                for (VillagerTrades.ItemListing listing : pickValue.listings()) {
                    if (union.stream().noneMatch((l) -> l == listing)) {
                        union.add(listing);
                    }
                }

                Value count = drawCount != null ? listings.count() : new Value.Known((Integer) ((Value.Known) listings.count()).value() + 1);

                offers.entries.set(index, new Value.Offers.Listings(union.toArray(new VillagerTrades.ItemListing[0]), count, listings.key()));
            }

            return new Value.Known(1);
        }
        if (call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z")) {
            offers.entries.add(new Value.Offers.Single(args.get(1)));
            return new Value.Known(1);
        }
        Value.Offers source = call.name.equals("addAll") && call.desc.equals("(Ljava/util/Collection;)Z") ? offerList(args.get(1), false) : null;

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
                && known.value() instanceof Collection<?> collection && !heap.isTainted(collection)) {
            collection.forEach((offer) -> offers.entries.add(new Value.Offers.Single(heap.wrap(offer))));
            return new Value.Known(collection.isEmpty() ? 0 : 1);
        }
        if (call.name.equals("removeIf") && call.desc.equals("(Ljava/util/function/Predicate;)Z") && args.get(1) instanceof Value.Closure predicate) {
            return removeOffers(offers, predicate, depth);
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

    Value addPicked(Value.Offers offers, Value.Choice choice) {
        Integer index = offers.picks.get(choice.draw());
        NumberExpr drawCount = heap.drawCount(choice.draw());

        if (index == null) {
            offers.picks.put(choice.draw(), offers.entries.size());
            offers.entries.add(new Value.Offers.Picked(choice.options(), drawCount != null ? new Value.Num(drawCount, Value.Kind.INT) : new Value.Known(1)));
        } else if (offers.entries.get(index) instanceof Value.Offers.Picked picked) {
            List<Object> union = new ArrayList<>(picked.offers());

            for (Object offer : choice.options()) {
                if (union.stream().noneMatch((o) -> o == offer)) {
                    union.add(offer);
                }
            }

            Value count = drawCount != null ? picked.count() : new Value.Known((Integer) ((Value.Known) picked.count()).value() + 1);

            offers.entries.set(index, new Value.Offers.Picked(union, count));
        } else {
            throw new Fail("merchant offers call add");
        }

        return new Value.Known(1);
    }

    boolean isSymbolicOffer(Value value) {
        return value instanceof Value.Pick || value instanceof Value.Offer || value instanceof Value.Choice choice && choice.draw() != null
                && heap.isPool(choice.draw()) && !choice.options().isEmpty() && choice.options().stream().allMatch(MerchantOffer.class::isInstance);
    }

    @Nullable
    Value.Offers offerList(Value value, boolean create) {
        if (value instanceof Value.Offers offers) {
            return offers;
        }
        if (!(value instanceof Value.Known known) || !(known.value() instanceof Collection<?> collection) || !heap.isOwned(collection)) {
            return null;
        }

        Value.Offers collected = heap.offers(collection);

        if (collected != null || !create) {
            return collected;
        }
        if (heap.isFrozen(collection) || !collection.stream().allMatch(MerchantOffer.class::isInstance)) {
            throw new Fail("offers in a collection");
        }

        Value.Offers offers = new Value.Offers();

        collection.forEach((offer) -> offers.entries.add(new Value.Offers.Single(heap.wrap(offer))));
        collection.clear();
        heap.coll(collection).offers = offers;
        return offers;
    }

    private Value removeOffers(Value.Offers offers, Value.Closure predicate, int depth) {
        boolean nullCheck = predicate.impl().getOwner().equals("java/util/Objects") && predicate.impl().getName().equals("isNull");
        List<Integer> removedIndices = new ArrayList<>();

        for (int i = 0; i < offers.entries.size(); i++) {
            if (!(offers.entries.get(i) instanceof Value.Offers.Single single)) {
                if (!nullCheck) {
                    throw new Fail("merchant offers call removeIf");
                }

                continue;
            }

            List<Value> all = new ArrayList<>(predicate.captured());

            all.add(single.offer());

            if (!(invokeHandle(predicate.impl(), all, predicate.loader(), depth + 1) instanceof Value.Known known) || !(known.value() instanceof Integer test)) {
                throw new Fail("merchant offers call removeIf");
            }
            if (test != 0) {
                removedIndices.add(i);
            }
        }

        for (int i = removedIndices.size() - 1; i >= 0; i--) {
            offers.entries.remove((int) removedIndices.get(i));
        }

        offers.picks.replaceAll((pool, index) -> index - (int) removedIndices.stream().filter((removed) -> removed < index).count());
        return new Value.Known(removedIndices.isEmpty() ? 0 : 1);
    }

    @NotNull
    Object materialize(Value.Closure closure) {
        List<Value> captured = closure.captured();

        if (!captured.stream().allMatch(this::isConcrete)) {
            throw new Fail("closure over symbolic values");
        }

        ClassLoader loader = closure.iface().getClassLoader() != null ? closure.iface().getClassLoader() : Interpreter.class.getClassLoader();

        return Proxy.newProxyInstance(loader, new Class<?>[]{closure.iface()}, (proxy, method, methodArgs) -> {
            Object[] callArgs = methodArgs != null ? methodArgs : new Object[0];

            if (method.getName().equals(closure.samName()) && Modifier.isAbstract(method.getModifiers())) {
                return invokeHandleConcrete(closure.impl(), closure.loader(), captured, callArgs);
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, callArgs);
            }

            return switch (method.getName()) {
                case "equals" -> proxy == callArgs[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> closure.impl().getOwner() + "." + closure.impl().getName();
                default -> throw new UnsupportedOperationException(method.getName());
            };
        });
    }

    private Object invokeHandleConcrete(Handle handle, ClassLoader loader, List<Value> captured, Object[] callArgs) throws Throwable {
        Class<?> owner = load(handle.getOwner(), loader);
        boolean hasReceiver = handle.getTag() != H_INVOKESTATIC && handle.getTag() != H_NEWINVOKESPECIAL;
        Type[] params = Type.getArgumentTypes(handle.getDesc());
        List<Object> all = new ArrayList<>();

        for (int i = 0; i < captured.size(); i++) {
            int param = hasReceiver ? i - 1 : i;

            all.add(toJava(captured.get(i), param >= 0 && param < params.length ? Machine.javaType(params[param]) : Object.class));
        }

        all.addAll(Arrays.asList(callArgs));

        try {
            if (handle.getTag() == H_NEWINVOKESPECIAL) {
                Constructor<?> constructor = constructor(owner, handle.getDesc());

                constructor.setAccessible(true);
                return constructor.newInstance(all.toArray());
            }

            Method method = method(owner, handle.getName(), handle.getDesc());

            if (method == null) {
                throw new UnsupportedOperationException(handle.getName());
            }

            method.setAccessible(true);

            if (handle.getTag() == H_INVOKESTATIC) {
                return method.invoke(null, all.toArray());
            }

            return method.invoke(all.get(0), all.subList(1, all.size()).toArray());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    static Value pick(List<?> list, Value.Num index) {
        if (!(index.expr() instanceof NumberExpr.Fn fn) || !fn.id().equals(NumberFunctions.UNIFORM_INT)
                || !(fn.args().get(0) instanceof NumberExpr.Const lo) || !(fn.args().get(1) instanceof NumberExpr.Const hi)) {
            throw new Fail("non-uniform index");
        }

        int from = (int) lo.value();
        int to = (int) hi.value();

        if (from < 0 || to >= list.size() || from > to) {
            throw new Fail("index out of range");
        }
        if (to - from + 1 > MAX_OPTIONS) {
            throw new Fail("too many options");
        }

        return new Value.Choice(new ArrayList<>(list.subList(from, to + 1)), index.expr());
    }

    static Value drawnInt(Value value) {
        if (value instanceof Value.Num num && num.kind() == Value.Kind.INT && num.expr() instanceof NumberExpr.Fn fn && fn.id().equals(NumberFunctions.UNIFORM_INT)
                && fn.args().get(0) instanceof NumberExpr.Const lo && fn.args().get(1) instanceof NumberExpr.Const hi && hi.value() - lo.value() < MAX_OPTIONS) {
            List<Object> options = new ArrayList<>();

            for (int i = (int) lo.value(); i <= (int) hi.value(); i++) {
                options.add(i);
            }

            return new Value.Choice(options, num.expr());
        }

        return value;
    }

    @Nullable
    Value concreteCall(MethodInsnNode call, List<Value> args, ClassLoader loader, boolean ownedResult) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (args.stream().anyMatch(this::isSelection)) {
            throw new Fail("random selection passed to a call");
        }
        Method method = method(load(call.owner, loader), call.name, call.desc);

        if (method == null) {
            throw new Fail("no method " + call.owner + "." + call.name);
        }
        if (call.getOpcode() == INVOKESPECIAL && !Modifier.isPrivate(method.getModifiers()) && !Modifier.isFinal(method.getModifiers())) {
            throw new Fail("super call");
        }

        try {
            method.setAccessible(true);
        } catch (Throwable ignored) {}

        Object receiver = isStatic ? null : concrete(args.get(0));
        Object[] javaArgs = javaArgs(args, method.getParameterTypes(), isStatic ? 0 : 1);
        Object result;

        try {
            result = method.invoke(receiver, javaArgs);
        } catch (Throwable e) {
            throw new Fail("call failed: " + call.owner + "." + call.name);
        }

        if (method.getReturnType() == void.class) {
            return null;
        }
        if (ownedResult && result != null) {
            heap.own(result);
        }

        return wrap(result, method.getReturnType());
    }

    Object[] javaArgs(List<Value> args, Class<?>[] types, int offset) {
        Object[] result = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            result[i] = toJava(args.get(i + offset), types[i]);
        }

        return result;
    }

    @Nullable
    Object toJava(Value value, Class<?> type) {
        if (value instanceof Value.Closure closure) {
            return materialize(closure);
        }
        if (value instanceof Value.Rng rng) {
            if (mode != Mode.STUB) {
                throw new Fail("random argument");
            }

            return rng.legacy() ? new StubRandom.Legacy(pick) : new StubRandom(pick);
        }

        Object object = concrete(value);

        if (!(object instanceof Integer)) {
            return object;
        } else if (type == boolean.class) {
            return ((Integer) object) != 0;
        } else if (type == char.class) {
            return (char) (int) (Integer) object;
        } else if (type == byte.class) {
            return (byte) (int) (Integer) object;
        } else if (type == short.class) {
            return (short) (int) (Integer) object;
        }

        return object;
    }

    @Nullable
    static Object concrete(Value value) {
        if (value instanceof Value.Known known) {
            return known.value();
        }

        throw new Fail("symbolic value");
    }

    boolean isSelection(Value value) {
        return value instanceof Value.Known known && known.value() != null
                && (heap.shuffled(known.value()) != null || heap.randomIndexSet(known.value()) != null || heap.removals(known.value()) > 0);
    }

    boolean isConcrete(Value value) {
        return value instanceof Value.Known || value instanceof Value.Closure closure && closure.captured().stream().allMatch(this::isConcrete)
                || value instanceof Value.Rng && mode == Mode.STUB;
    }

    static boolean isScalar(Value value) {
        return value instanceof Value.Known known && (known.value() == null || known.value() instanceof Number || known.value() instanceof String
                || known.value() instanceof Boolean || known.value() instanceof Character);
    }

    Value wrap(@Nullable Object value, Class<?> type) {
        if (!type.isPrimitive()) {
            return heap.wrap(value);
        } else if (value instanceof Boolean b) {
            return new Value.Known(b ? 1 : 0);
        } else if (value instanceof Character c) {
            return new Value.Known((int) c);
        } else if (value instanceof Byte b) {
            return new Value.Known((int) b);
        } else if (value instanceof Short s) {
            return new Value.Known((int) s);
        }

        return new Value.Known(value);
    }

    @NotNull
    static Value num(NumberExpr expr, Value.Kind kind) {
        NumberInterval bounds = expr instanceof NumberExpr.Const ? null : expr.bounds();

        if (bounds != null && bounds.isPoint() && Double.isFinite(bounds.lo())) {
            return num(NumberExpr.constant(bounds.lo()), kind);
        }
        if (expr instanceof NumberExpr.Const c) {
            return new Value.Known(switch (kind) {
                case INT -> (int) c.value();
                case LONG -> (long) c.value();
                case FLOAT -> (float) c.value();
                case DOUBLE -> c.value();
            });
        }

        return new Value.Num(expr, kind);
    }

    static int[] choiceIndices(List<Value> args) {
        int[] indices = IntStream.range(0, args.size()).filter((i) -> args.get(i) instanceof Value.Choice).toArray();

        if (indices.length > 1) {
            Value.Choice first = (Value.Choice) args.get(indices[0]);

            for (int index : indices) {
                Value.Choice choice = (Value.Choice) args.get(index);

                if (first.draw() == null || choice.draw() != first.draw() || choice.options().size() != first.options().size()) {
                    throw new Fail("several random choices");
                }
            }
        }

        return indices;
    }

    enum Mode {
        SYMBOLIC,
        STUB,
    }
}
