package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.yanny.ali.plugin.server.bytecode.Resolution.*;

final class Interpreter implements Opcodes {
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
    static final Set<String> FRESH_RESULTS = Set.of("stream", "iterator", "listIterator", "spliterator", "toArray");

    final Profile profile;
    final Mode mode;
    final Dispatcher dispatcher = new Dispatcher(this);
    final Machine machine = new Machine(this);
    final int pick;
    final ValueStore store = new ValueStore();
    final Heap heap = new Heap(store);
    @Nullable
    Subject subject;
    final PickSources pickSources = new PickSources();
    @Nullable
    final RegistryAccess registries;
    final OpaqueCalls opaque;
    final NumberDomain numbers = new NumberDomain(store);
    final Decisions decisions;

    Interpreter(Profile profile, Mode mode, int pick) {
        this(profile, mode, pick, null, null, new HashSet<>());
    }

    Interpreter(Profile profile, Mode mode, int pick, @Nullable List<Boolean> script, @Nullable RegistryAccess registries, Set<String> opaque) {
        this.profile = profile;
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
        Value self = Value.Unknown.of(Type.getType(merchant), Origin.SELF);
        Declaration getOffers = SelfModel.ownDeclaration(merchant, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), false);
        TraderSubject run = new TraderSubject(merchant, getOffers != null);

        subject = run;

        if (getOffers != null) {
            if (machine.execute(getOffers.owner(), getOffers.method(), List.of(self), 0) instanceof Value.Offers offers) {
                return offers;
            }

            throw new Fail("offers of the trader");
        }

        Declaration updateTrades = SelfModel.ownDeclaration(merchant, RuntimeNames.UPDATE_TRADES.name(), RuntimeNames.UPDATE_TRADES.desc(), false);

        if (updateTrades != null) {
            machine.execute(updateTrades.owner(), updateTrades.method(), List.of(self), 0);
        }

        return run.offers;
    }

    Value.Offers writtenTrades(Class<?> merchant) {
        Declaration writer = TraderModel.offersWriter(merchant);

        if (writer == null) {
            throw new Fail("no method writing the offers of the trader");
        }

        List<Value> args = new ArrayList<>();

        args.add(Value.Unknown.of(Type.getType(merchant), Origin.SELF));
        Arrays.stream(Type.getArgumentTypes(writer.method().desc)).forEach((type) -> args.add(Value.Unknown.of(type, null)));
        TraderSubject run = new TraderSubject(merchant, SelfModel.ownDeclaration(merchant, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), false) != null);

        subject = run;
        machine.execute(writer.owner(), writer.method(), args, 0);
        return run.offers;
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
        return value instanceof Value.Known(Object value1) && value1 != null && heap.isTainted(value1);
    }

    void taintOwned(List<Value> args) {
        // javac 21.0.12 crashes on a record pattern here (in a ternary passed as an argument inside a lambda)
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
            } else if (base.get(i) instanceof Value.Known(ItemStack ignored)) {
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
        NumberExpr count = branches.getFirst().count;

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
        if (results.stream().allMatch((r) -> r instanceof Value.Stack || r instanceof Value.Known(ItemStack ignored))) {
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

            Value.Stack stack = new Value.Stack(options, count, results.stream().anyMatch((r) -> r instanceof Value.Stack s && s.randomTag));

            stack.tag = ItemStackModel.tagOf(heap.tags(), options);
            return limit(stack);
        }
        if (results.stream().allMatch((r) -> r instanceof Value.Known)) {
            List<Object> values = results.stream().map((r) -> ((Value.Known) r).value()).toList();

            return values.stream().allMatch((v) -> v == values.getFirst()) ? results.getFirst() : new Value.Choice(values);
        }
        if (results.stream().allMatch((r) -> r instanceof Value.Num || r instanceof Value.Known(Number ignored))) {
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
        return args.stream().map((v) -> v instanceof Value.Known(ItemStack real) && !heap.isOwned(real) ? heap.shadow(real) : v).toList();
    }

    static Value.Stack stackOf(ItemStack real) {
        return new Value.Stack(List.of(real.copy()), NumberExpr.constant(real.getCount()), false);
    }

    static Value limit(Value.Stack stack) {
        if (stack.options.size() > Limits.OPTIONS) {
            throw new Fail("too many options");
        }

        return stack;
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

            return method.invoke(all.getFirst(), all.subList(1, all.size()).toArray());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    static Value pick(List<?> list, Value.Num index) {
        if (!(index.expr() instanceof NumberExpr.Fn(ResourceLocation id, List<NumberExpr> args)) || !id.equals(NumberFunctions.UNIFORM_INT)
                || !(args.getFirst() instanceof NumberExpr.Const(double lo)) || !(args.get(1) instanceof NumberExpr.Const(double hi))) {
            throw new Fail("non-uniform index");
        }

        int from = (int) lo;
        int to = (int) hi;

        if (from < 0 || to >= list.size() || from > to) {
            throw new Fail("index out of range");
        }
        if (to - from + 1 > Limits.OPTIONS) {
            throw new Fail("too many options");
        }

        return new Value.Choice(new ArrayList<>(list.subList(from, to + 1)), index.expr());
    }

    static Value drawnInt(Value value) {
        if (value instanceof Value.Num(NumberExpr expr, Value.Kind kind) && kind == Value.Kind.INT && expr instanceof NumberExpr.Fn(ResourceLocation id, List<NumberExpr> args)
                && id.equals(NumberFunctions.UNIFORM_INT) && args.getFirst() instanceof NumberExpr.Const(double lo) && args.get(1) instanceof NumberExpr.Const(double hi)
                && hi - lo < Limits.OPTIONS) {
            List<Object> options = new ArrayList<>();

            for (int i = (int) lo; i <= (int) hi; i++) {
                options.add(i);
            }

            return new Value.Choice(options, expr);
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

        Object receiver = isStatic ? null : concrete(args.getFirst());
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
        if (value instanceof Value.Rng(boolean legacy)) {
            if (mode != Mode.STUB) {
                throw new Fail("random argument");
            }

            return legacy ? new StubRandom.Legacy(pick) : new StubRandom(pick);
        }

        Object object = concrete(value);

        if (!(object instanceof Integer integer)) {
            return object;
        } else if (type == boolean.class) {
            return integer != 0;
        } else if (type == char.class) {
            return (char) (int) integer;
        } else if (type == byte.class) {
            return (byte) (int) integer;
        } else if (type == short.class) {
            return (short) (int) integer;
        }

        return object;
    }

    @Nullable
    static Object concrete(Value value) {
        if (value instanceof Value.Known(Object value1)) {
            return value1;
        }

        throw new Fail("symbolic value");
    }

    boolean isSelection(Value value) {
        return value instanceof Value.Known(Object value1) && value1 != null
                && (heap.shuffled(value1) != null || heap.randomIndexSet(value1) != null || heap.removals(value1) > 0);
    }

    boolean isConcrete(Value value) {
        return value instanceof Value.Known || value instanceof Value.Closure closure && closure.captured().stream().allMatch(this::isConcrete)
                || value instanceof Value.Rng && mode == Mode.STUB;
    }

    static boolean isScalar(Value value) {
        return value instanceof Value.Known(Object value1) && (value1 == null || value1 instanceof Number || value1 instanceof String
                || value1 instanceof Boolean || value1 instanceof Character);
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
        if (expr instanceof NumberExpr.Const(double value1)) {
            return new Value.Known(switch (kind) {
                case INT -> (int) value1;
                case LONG -> (long) value1;
                case FLOAT -> (float) value1;
                case DOUBLE -> value1;
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
