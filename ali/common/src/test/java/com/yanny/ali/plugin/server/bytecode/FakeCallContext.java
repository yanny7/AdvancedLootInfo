package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

class FakeCallContext implements CallContext {
    final List<Invocation> invocations = new ArrayList<>();
    final List<Execution> executions = new ArrayList<>();
    final List<Call> concreteCalls = new ArrayList<>();
    final List<List<Value>> combined = new ArrayList<>();
    final List<NumberExpr> draws = new ArrayList<>();
    final Set<Object> owned = Collections.newSetFromMap(new IdentityHashMap<>());
    final Set<Object> tainted = Collections.newSetFromMap(new IdentityHashMap<>());
    final Set<Object> frozen = Collections.newSetFromMap(new IdentityHashMap<>());
    final Map<Object, Value.Stack> stackParts = new IdentityHashMap<>();
    final Map<ItemStack, Value.Stack> shadows = new IdentityHashMap<>();
    final Map<Object, CollState> colls = new IdentityHashMap<>();
    final List<Decision> decisions = new ArrayList<>();
    final Deque<Boolean> answers = new ArrayDeque<>();
    final List<Call> jdkCalls = new ArrayList<>();
    final Map<Object, Integer> levelKeys = new IdentityHashMap<>();
    final Map<String, Value> selfFields = new HashMap<>();
    final Set<Object> pools = Collections.newSetFromMap(new IdentityHashMap<>());
    final Map<Object, NumberExpr> drawCounts = new IdentityHashMap<>();
    final Map<Object, Object> listingSources = new IdentityHashMap<>();
    final Map<List<Object>, Integer> poolKeys = new HashMap<>();
    @Nullable
    Subject subject;
    @Nullable
    RegistryAccess registries;
    Interpreter.Mode mode = Interpreter.Mode.SYMBOLIC;
    int pick;
    @Nullable
    Value invokeResult;
    @Nullable
    Value executeResult;
    @Nullable
    Value combineResult;
    Function<Call, Value> concreteCall = (call) -> {
        throw new Fail("no concrete call");
    };
    Function<Call, Value> jdkCall = (call) -> {
        throw new Fail("no JDK call");
    };

    static Call call(int opcode, String owner, String name, String desc, Value... args) {
        return new Call(new MethodInsnNode(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE), List.of(args), FakeCallContext.class.getClassLoader(), 3);
    }

    static Value.Closure closure() {
        Handle impl = new Handle(Opcodes.H_INVOKESTATIC, "mod/Lambdas", "lambda$0", "()Ljava/lang/Object;", false);

        return new Value.Closure(Supplier.class, "get", "()Ljava/lang/Object;", impl, List.of(), FakeCallContext.class.getClassLoader());
    }

    @Override
    public boolean isConcrete(Value value) {
        return value instanceof Value.Known;
    }

    @Override
    public boolean isTainted(Value value) {
        return value instanceof Value.Known known && known.value() != null && tainted.contains(known.value());
    }

    @Override
    public Value wrap(@Nullable Object value) {
        return new Value.Known(value);
    }

    @Override
    public Value wrap(@Nullable Object value, Class<?> type) {
        return new Value.Known(value);
    }

    @Nullable
    @Override
    public Object toJava(Value value, Class<?> type) {
        return ((Value.Known) value).value();
    }

    @Nullable
    @Override
    public Value invoke(Value.Closure closure, List<Value> args, int depth) {
        invocations.add(new Invocation(closure, args, depth));
        return invokeResult;
    }

    @Nullable
    @Override
    public Value execute(Class<?> owner, MethodNode method, List<Value> args, int depth) {
        executions.add(new Execution(owner, method, args, depth));
        return executeResult;
    }

    @Nullable
    @Override
    public Value concreteCall(Call call, boolean ownedResult) {
        concreteCalls.add(call);
        return concreteCall.apply(call);
    }

    @Nullable
    @Override
    public Value combine(List<Value> results) {
        combined.add(results);
        return combineResult;
    }

    @Override
    public Object[] javaArgs(List<Value> args, Class<?>[] types, int offset) {
        return args.subList(offset, offset + types.length).stream().map((v) -> ((Value.Known) v).value()).toArray();
    }

    @Override
    public Interpreter.Mode mode() {
        return mode;
    }

    @Override
    public int pick() {
        return pick;
    }

    @Override
    public NumberExpr expr(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Number number) {
            return NumberExpr.constant(number.doubleValue());
        } else if (value instanceof Value.Num num) {
            return num.expr();
        }

        throw new Fail("not a number");
    }

    @Override
    public NumberExpr add(NumberExpr a, NumberExpr b) {
        return NumberExpr.add(a, b);
    }

    @Override
    public NumberExpr sub(NumberExpr a, NumberExpr b) {
        return NumberExpr.sub(a, b);
    }

    @Override
    public Value draw(NumberExpr expr, Value.Kind kind) {
        draws.add(expr);
        return new Value.Num(expr, kind);
    }

    @Override
    public void own(Object object) {
        owned.add(object);
    }

    @Override
    public void stackPart(Object part, Value.Stack stack) {
        stackParts.put(part, stack);
    }

    @Override
    public Value.Stack shadow(ItemStack real) {
        return shadows.computeIfAbsent(real, Interpreter::stackOf);
    }

    @Override
    public List<Value> shadowed(List<Value> args) {
        return args.stream().map((v) -> v instanceof Value.Known known && known.value() instanceof ItemStack real && !owned.contains(real) ? shadow(real) : v).toList();
    }

    @Override
    public boolean isOwned(@Nullable Object object) {
        return owned.contains(object);
    }

    @Override
    public boolean isFrozen(Object object) {
        return frozen.contains(object);
    }

    @Override
    public void taint(@Nullable Object object) {
        if (object != null) {
            tainted.add(object);
        }
    }

    @Override
    public CollState coll(Object object) {
        return colls.computeIfAbsent(object, (o) -> new CollState());
    }

    @Override
    public int removals(Object object) {
        return colls.containsKey(object) ? colls.get(object).removals : 0;
    }

    @Nullable
    @Override
    public Value.Draws shuffled(Object object) {
        return colls.containsKey(object) ? colls.get(object).shuffled : null;
    }

    @Nullable
    @Override
    public int[] randomIndexSet(Object object) {
        return colls.containsKey(object) ? colls.get(object).randomIndexSet : null;
    }

    @Override
    public boolean isPool(@Nullable Object draw) {
        return pools.contains(draw);
    }

    @Nullable
    @Override
    public NumberExpr drawCount(@Nullable Object draw) {
        return drawCounts.get(draw);
    }

    @Nullable
    @Override
    public Value.Offers offers(Object collection) {
        return colls.containsKey(collection) ? colls.get(collection).offers : null;
    }

    @Nullable
    @Override
    public Subject subject() {
        return subject;
    }

    @Nullable
    @Override
    public Value selfField(String field) {
        return selfFields.get(field);
    }

    @Override
    public void putSelfField(String field, Value value) {
        selfFields.put(field, value);
    }

    @Nullable
    @Override
    public Integer levelKey(Object listings) {
        return levelKeys.get(listings);
    }

    @Nullable
    @Override
    public Object listingSource(Object listing) {
        return listingSources.get(listing);
    }

    @Nullable
    @Override
    public Integer poolKey(List<Object> listings) {
        return poolKeys.get(listings);
    }

    @Override
    public BoundOffer boundOffer(Object element, MethodInsnNode call, List<Object> args) {
        return new BoundOffer(element, call.owner, call.name, call.desc, call.itf, args);
    }

    @Nullable
    @Override
    public RegistryAccess registries() {
        return registries;
    }

    @Override
    public Collection<TagKey<?>> tags() {
        return List.of();
    }

    @Override
    public List<Decision> decisions() {
        return List.copyOf(decisions);
    }

    @Override
    public boolean decide(Decision.Kind kind, @Nullable Object subject, @Nullable Object value, String reason) {
        boolean holds = !answers.isEmpty() && answers.pop();

        decisions.add(new Decision(kind, subject, value, holds));
        return holds;
    }

    @Nullable
    @Override
    public Value jdk(Call call, Class<?> declaring) {
        jdkCalls.add(call);
        return jdkCall.apply(call);
    }

    record Invocation(Value.Closure closure, List<Value> args, int depth) {
    }

    record Execution(Class<?> owner, MethodNode method, List<Value> args, int depth) {
    }
}
