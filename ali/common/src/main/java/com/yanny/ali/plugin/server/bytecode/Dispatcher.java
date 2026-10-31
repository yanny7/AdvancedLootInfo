package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.BaseStream;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.*;
import static com.yanny.ali.plugin.server.bytecode.MethodModel.NO_OPINION;
import static com.yanny.ali.plugin.server.bytecode.Resolution.*;

final class Dispatcher implements Opcodes, CallContext {
    static final MethodModel NEW_PER_CHOICE = construction(Dispatcher::newPerChoice);
    static final MethodModel NEW_UNKNOWN = construction(Dispatcher::newUnknown);
    static final MethodModel EQUALS_UNKNOWN = branch(Dispatcher::equalsUnknown);
    static final MethodModel INT_VALUE = branch(Dispatcher::intValue);
    static final MethodModel CLOSURE = branch(Dispatcher::closure);
    static final MethodModel DRAWS = branch(Dispatcher::draws);
    static final MethodModel UNKNOWN = branch(Dispatcher::unknown);
    static final MethodModel UNSUPPORTED = branch(Dispatcher::unsupported);
    static final MethodModel NULL_RECEIVER = branch(Dispatcher::nullReceiver);
    static final MethodModel LAMBDA = branch(Dispatcher::lambda);
    static final MethodModel REQUIRE_NON_NULL = branch(Dispatcher::requireNonNull);
    static final MethodModel IS_NULL = branch(Dispatcher::isNull);
    static final MethodModel SELECTION_GUARD = branch(Dispatcher::selectionGuard);
    static final MethodModel THREAD_LOCAL_RANDOM = branch(Dispatcher::threadLocalRandom);
    static final MethodModel UNCERTAIN = branch(Dispatcher::uncertain);
    static final MethodModel STATIC_UNKNOWN = branch(Dispatcher::staticUnknown);

    private final Interpreter interpreter;

    Dispatcher(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Nullable
    Value invoke(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        Value result = dispatch(call, args, loader, depth);

        if (result instanceof Value.Known(Object value1) && TradeTypes.isData(value1) && args.stream().anyMatch(interpreter::isTainted)) {
            interpreter.heap.taint(value1);
        }

        if (call.getOpcode() != INVOKESTATIC && call.name.equals("get") && args.size() == 2 && args.getFirst() instanceof Value.Known(Map<?, ?> ignored)
                && args.get(1) instanceof Value.Known(Object key) && result instanceof Value.Known(Object listings) && isKeyedList(listings)) {
            if (key instanceof Integer level) {
                interpreter.pickSources.putLevel(listings, level);
            } else if (key instanceof Enum<?> constant) {
                interpreter.pickSources.putLevel(listings, constant.ordinal() + 1);
            }
        }

        return result;
    }

    Value construct(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        Value answer = apply(Phase.CONSTRUCTOR, new Call(new MethodInsnNode(INVOKESPECIAL, type, CONSTRUCTOR, desc, false), args, loader, depth));

        return answer != NO_OPINION ? answer : realConstructor(type, desc, args, loader);
    }

    @Override
    public boolean isConcrete(Value value) {
        return interpreter.isConcrete(value);
    }

    @Override
    public boolean isTainted(Value value) {
        return interpreter.isTainted(value);
    }

    @Override
    public Value wrap(@Nullable Object value) {
        return interpreter.heap.wrap(value);
    }

    @Override
    public Value wrap(@Nullable Object value, Class<?> type) {
        return interpreter.wrap(value, type);
    }

    @Nullable
    @Override
    public Object toJava(Value value, Class<?> type) {
        return interpreter.toJava(value, type);
    }

    @Nullable
    @Override
    public Value invoke(Value.Closure closure, List<Value> args, int depth) {
        List<Value> all = new ArrayList<>(closure.captured());

        all.addAll(args);
        return interpreter.invokeHandle(closure.impl(), all, closure.loader(), depth + 1);
    }

    @Nullable
    @Override
    public Value execute(Class<?> owner, MethodNode method, List<Value> args, int depth) {
        return interpreter.machine.execute(owner, method, args, depth + 1);
    }

    @Nullable
    @Override
    public Value concreteCall(Call call, boolean ownedResult) {
        return interpreter.concreteCall(call.insn(), call.args(), call.loader(), ownedResult);
    }

    @Nullable
    @Override
    public Value combine(List<Value> results) {
        return interpreter.combine(results);
    }

    @Override
    public Object[] javaArgs(List<Value> args, Class<?>[] types, int offset) {
        return interpreter.javaArgs(args, types, offset);
    }

    @Override
    public Interpreter.Mode mode() {
        return interpreter.mode;
    }

    @Override
    public int pick() {
        return interpreter.pick;
    }

    @Override
    public NumberExpr expr(Value value) {
        return NumberDomain.expr(value);
    }

    @Override
    public NumberExpr add(NumberExpr a, NumberExpr b) {
        return interpreter.numbers.add(a, b);
    }

    @Override
    public NumberExpr sub(NumberExpr a, NumberExpr b) {
        return interpreter.numbers.sub(a, b);
    }

    @Override
    public Value draw(NumberExpr expr, Value.Kind kind) {
        return interpreter.numbers.draw(expr, kind);
    }

    @Override
    public void own(Object object) {
        interpreter.heap.own(object);
    }

    @Override
    public void stackPart(Object part, Value.Stack stack) {
        interpreter.heap.stackPart(part, stack);
    }

    @Override
    public Value.Stack shadow(ItemStack real) {
        return interpreter.heap.shadow(real);
    }

    @Override
    public List<Value> shadowed(List<Value> args) {
        return interpreter.shadowed(args);
    }

    @Override
    public boolean isOwned(@Nullable Object object) {
        return interpreter.heap.isOwned(object);
    }

    @Override
    public boolean isFrozen(Object object) {
        return interpreter.heap.isFrozen(object);
    }

    @Override
    public void taint(@Nullable Object object) {
        interpreter.heap.taint(object);
    }

    @Override
    public CollState coll(Object object) {
        return interpreter.heap.coll(object);
    }

    @Override
    public int removals(Object object) {
        return interpreter.heap.removals(object);
    }

    @Nullable
    @Override
    public Value.Draws shuffled(Object object) {
        return interpreter.heap.shuffled(object);
    }

    @Override
    public int[] randomIndexSet(Object object) {
        return interpreter.heap.randomIndexSet(object);
    }

    @Override
    public boolean isPool(@Nullable Object draw) {
        return interpreter.heap.isPool(draw);
    }

    @Nullable
    @Override
    public NumberExpr drawCount(@Nullable Object draw) {
        return interpreter.heap.drawCount(draw);
    }

    @Nullable
    @Override
    public Value.Offers offers(Object collection) {
        return interpreter.heap.offers(collection);
    }

    @Nullable
    @Override
    public Subject subject() {
        return interpreter.subject;
    }

    @Nullable
    @Override
    public Value selfField(String field) {
        return interpreter.store.hasSelfField(field) ? interpreter.store.getSelfField(field) : null;
    }

    @Override
    public void putSelfField(String field, Value value) {
        interpreter.store.putSelfField(field, value);
    }

    @Nullable
    @Override
    public Integer levelKey(Object listings) {
        return interpreter.pickSources.levelKey(listings);
    }

    @Nullable
    @Override
    public Object listingSource(Object listing) {
        return interpreter.pickSources.source(listing);
    }

    @Nullable
    @Override
    public Integer poolKey(List<Object> listings) {
        return interpreter.pickSources.poolKey(listings);
    }

    @Override
    public BoundOffer boundOffer(Object element, MethodInsnNode call, List<Object> args) {
        return interpreter.pickSources.boundOffer(element, call, args);
    }

    @Nullable
    @Override
    public RegistryAccess registries() {
        return interpreter.registries;
    }

    @Override
    public Collection<TagKey<?>> tags() {
        return interpreter.heap.tags();
    }

    @Override
    public List<Decision> decisions() {
        return List.copyOf(interpreter.decisions.taken());
    }

    @Override
    public boolean decide(Decision.Kind kind, @Nullable Object subject, @Nullable Object value, String reason) {
        return interpreter.decisions.decide(kind, subject, value, reason);
    }

    @Nullable
    @Override
    public Value jdk(Call call, Class<?> declaring) {
        Value answer = apply(Phase.JDK, call);

        return answer != NO_OPINION ? answer : jdkReal(call.insn(), declaring, call.args(), call.loader());
    }

    @Nullable
    private Value apply(Phase phase, Call call) {
        for (MethodModel model : interpreter.profile.models(phase)) {
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
            if (!(args.getFirst() instanceof Value.Uninit uninit)) {
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
            return interpreter.distribute(call, args, choices, loader, depth);
        }

        answer = apply(Phase.RECEIVER, invocation);

        if (answer != NO_OPINION) {
            return answer;
        }

        Value receiver = isStatic ? null : args.getFirst();

        Class<?> start;

        if (isStatic || call.getOpcode() == INVOKESPECIAL) {
            start = load(call.owner, loader);
        } else if (receiver instanceof Value.Known(Object value1)) {
            start = value1.getClass();
        } else {
            throw new Fail("unsupported receiver");
        }

        Declared declared = declaration(start, call.name, call.desc, call.getOpcode() == INVOKESPECIAL);

        if (declared instanceof JdkDeclaration(Class<?> owner)) {
            return jdk(invocation, owner);
        }

        if (isStatic && isJdk(start)) {
            return jdk(invocation, start);
        }

        if (args.stream().anyMatch(this::holdsStack)) {
            return ItemStackModel.argumentCall(this, invocation);
        }

        if (declared != null && interpreter.opaque.isOpaque(call) && isWorldCall(call, args, loader)) {
            return worldResult(call, args);
        }
        if (!isStatic && receiver instanceof Value.Known(Object value2) && args.stream().skip(1).anyMatch(interpreter::isTainted) && !TradeTypes.returnsTradeData(call)) {
            return interpreter.uncertainChange(call, value2);
        }

        Value result;

        if (declared instanceof Declaration(Class<?> owner, MethodNode method)) {
            int before = interpreter.decisions.taken().size();

            try {
                result = interpreter.machine.execute(owner, method, args, depth + 1);
            } catch (Fail fail) {
                result = fallbackCall(call, args, loader, fail);
            }
            if (interpreter.decisions.taken().size() - before > 1 && isWorldCall(call, args, loader)) {
                interpreter.opaque.count(call, interpreter.decisions.taken().size() - before);
            }
        } else {
            result = fallbackCall(call, args, loader, new Fail("no bytecode for " + call.owner + "." + call.name));
        }

        return result;
    }

    private Value realConstructor(String type, String desc, List<Value> args, ClassLoader loader) {
        if (!args.stream().allMatch(interpreter::isConcrete)) {
            throw new Fail("symbolic constructor arguments");
        }
        if (args.stream().anyMatch(interpreter::isSelection)) {
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

            return interpreter.heap.wrap(created);
        } catch (Throwable e) {
            throw new Fail("constructor failed");
        }
    }

    @Nullable
    private Value jdkReal(MethodInsnNode call, Class<?> declaring, List<Value> args, ClassLoader loader) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!args.stream().allMatch(interpreter::isConcrete)) {
            throw new Fail("symbolic argument of " + call.owner + "." + call.name);
        }
        if (isStatic) {
            if (!STATIC_SAFE.contains(call.owner) || STATIC_RANDOM.contains(call.name)) {
                throw new Fail("JDK call " + call.owner + "." + call.name);
            }

            return interpreter.concreteCall(call, args, loader, false);
        }

        Object receiver = ((Value.Known) args.getFirst()).value();
        boolean ownedReceiver = interpreter.heap.isOwned(receiver) || receiver instanceof BaseStream<?, ?>;

        if (READ_ONLY.contains(call.name) && receiver != null && interpreter.heap.isTainted(receiver)) {
            throw new Fail("uncertain contents of " + declaring.getName());
        }

        if (!ownedReceiver && !READ_ONLY.contains(call.name)) {
            throw new Fail("JDK call " + declaring.getName() + "." + call.name);
        }
        if (!READ_ONLY.contains(call.name) && receiver != null && interpreter.heap.isFrozen(receiver)) {
            throw new Fail("change under a random choice");
        }

        Value result = interpreter.concreteCall(call, args, loader, false);

        if (result instanceof Value.Known(Object value1) && value1 != null && (ownedReceiver || FRESH_RESULTS.contains(call.name))) {
            interpreter.heap.own(value1);
        }
        if (!READ_ONLY.contains(call.name) && args.stream().skip(1).anyMatch(interpreter::isTainted)) {
            interpreter.heap.taint(receiver);
        }

        return result;
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

            return Objects.requireNonNull(sameDraw(interpreter.combine(results), first));
        }

        return NO_OPINION;
    }

    private Value newUnknown(String type, String desc, List<Value> args, ClassLoader loader, int depth) {
        if (args.stream().anyMatch(Value.Unknown.class::isInstance) && args.stream().allMatch((v) -> v instanceof Value.Unknown || interpreter.isConcrete(v))) {
            interpreter.taintOwned(args);
            return Value.Unknown.of(Type.getObjectType(type), Origin.of(null, type + "." + CONSTRUCTOR + desc, args));
        }

        return NO_OPINION;
    }

    private Value equalsUnknown(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && call.name.equals("equals") && call.desc.equals("(Ljava/lang/Object;)Z") && args.getFirst() instanceof Value.Known(Object value1)
                && args.get(1) instanceof Value.Unknown unknown) {
            return new Value.Unknown(false, null, null, Origin.equality(unknown, value1));
        }

        return NO_OPINION;
    }

    private Value intValue(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        if (call.name.equals("intValue") && call.desc.equals("()I") && args.getFirst() instanceof Value.Choice choice && choice.options().stream().allMatch(Integer.class::isInstance)) {
            return choice;
        }

        return NO_OPINION;
    }

    @Nullable
    private Value closure(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.getFirst();

        if (receiver instanceof Value.Closure closure) {
            if (!call.name.equals(closure.samName())) {
                throw new Fail("closure default method");
            }

            List<Value> all = new ArrayList<>(closure.captured());

            all.addAll(args.subList(1, args.size()));
            return interpreter.invokeHandle(closure.impl(), all, closure.loader(), depth + 1);
        }

        return NO_OPINION;
    }

    private Value draws(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.getFirst();

        if (receiver instanceof Value.Draws draws) {
            if (call.name.equals("hasNext") && call.desc.equals("()Z")) {
                return new Value.Known(draws.left > 0 ? 1 : 0);
            }
            if (call.name.equals("next") && call.desc.equals("()Ljava/lang/Object;") && draws.left > 0) {
                draws.left--;
                return new Value.Choice(draws.options, draws.pool);
            }

            throw new Fail("iteration over random draws");
        }

        return NO_OPINION;
    }

    private Value unknown(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.getFirst();

        if (receiver instanceof Value.Unknown) {
            Type returnType = Type.getReturnType(call.desc);

            if (returnType.getSort() == Type.OBJECT && isRandomType(returnType, loader)) {
                return new Value.Rng(isLegacyRandom(returnType, loader));
            }

            interpreter.taintOwned(args);
            return Value.Unknown.of(returnType, Origin.of(receiver, call.name + call.desc, args.subList(1, args.size())));
        }

        return NO_OPINION;
    }

    private Value unsupported(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.getFirst();

        if (receiver instanceof Value.Offer || receiver instanceof Value.Uninit || receiver instanceof Value.Cmp) {
            throw new Fail("unsupported receiver");
        }

        return NO_OPINION;
    }

    private Value nullReceiver(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.getFirst();

        if (receiver instanceof Value.Known(Object value1) && value1 == null) {
            throw new Fail("null receiver");
        }

        return NO_OPINION;
    }

    @Nullable
    private Value lambda(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Value receiver = isStatic ? null : args.getFirst();

        if (receiver instanceof Value.Known(Object value1) && LambdaResolver.isLambda(value1)) {
            return interpreter.lambdaCall(value1, args, depth);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value requireNonNull(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/Objects") && call.name.startsWith("requireNonNull")) {
            if (interpreter.decisions.isNull(args.getFirst())) {
                throw new Fail("requireNonNull");
            }

            return args.getFirst();
        }

        return NO_OPINION;
    }

    private Value isNull(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/Objects") && (call.name.equals("isNull") || call.name.equals("nonNull"))) {
            return new Value.Known(interpreter.decisions.isNull(args.getFirst()) == call.name.equals("isNull") ? 1 : 0);
        }

        return NO_OPINION;
    }

    private Value selectionGuard(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (args.stream().skip(isStatic ? 0 : 1).anyMatch(interpreter::isSelection)) {
            throw new Fail("random selection passed to a call");
        }

        return NO_OPINION;
    }

    private Value threadLocalRandom(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (isStatic && call.owner.equals("java/util/concurrent/ThreadLocalRandom") && call.name.equals("current")) {
            return new Value.Rng(true);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value uncertain(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;

        if (!isStatic && args.getFirst() instanceof Value.Known(Object value1) && value1 != null && interpreter.heap.isOwned(value1)
                && args.stream().skip(1).anyMatch((v) -> v instanceof Value.Unknown || v instanceof Value.Num)
                && args.stream().skip(1).allMatch((v) -> interpreter.isConcrete(v) || v instanceof Value.Unknown || v instanceof Value.Num) && !TradeTypes.returnsTradeData(call)) {
            return interpreter.uncertainChange(call, READ_ONLY.contains(call.name) ? null : value1);
        }

        return NO_OPINION;
    }

    private Value staticUnknown(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Type returnType = Type.getReturnType(call.desc);

        if (isStatic && STATIC_SAFE.contains(call.owner) && !STATIC_RANDOM.contains(call.name) && returnType.getSort() != Type.VOID
                && args.stream().anyMatch(Value.Unknown.class::isInstance) && args.stream().allMatch((v) -> v instanceof Value.Unknown || isScalar(v))) {
            return Value.Unknown.of(returnType, null);
        }

        return NO_OPINION;
    }

    @Nullable
    private Value fallbackCall(MethodInsnNode call, List<Value> args, ClassLoader loader, Fail fail) {
        if (args.stream().allMatch(interpreter::isConcrete)) {
            return interpreter.concreteCall(call, args, loader, false);
        }
        if (isWorldCall(call, args, loader)) {
            if (fail.isBudget()) {
                interpreter.opaque.mark(call);
                throw fail;
            }

            return worldResult(call, args);
        }
        if (!fail.isBudget() && call.getOpcode() != INVOKESTATIC && args.getFirst() instanceof Value.Known(Object value1) && value1 != null
                && interpreter.heap.isOwned(value1) && args.stream().skip(1).allMatch((v) -> interpreter.isConcrete(v) || v instanceof Value.Num || v instanceof Value.Unknown)
                && !TradeTypes.returnsTradeData(call)) {
            return interpreter.uncertainChange(call, value1);
        }

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

        return Value.Unknown.of(returnType, Origin.of(args.getFirst(), call.name + call.desc, args.subList(1, args.size())));
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
