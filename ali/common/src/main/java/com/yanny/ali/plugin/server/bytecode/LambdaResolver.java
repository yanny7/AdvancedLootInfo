package com.yanny.ali.plugin.server.bytecode;

import com.yanny.ali.plugin.common.trades.TradeUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class LambdaResolver {
    private static final String LAMBDA_MARKER = "$$Lambda";
    private static final String LAMBDA_METAFACTORY = "java/lang/invoke/LambdaMetafactory";
    private static final int MAX_SCANNED_METHODS = 64;
    private static final int STUB_PICKS = 2;
    private static final Map<Class<?>, Optional<Resolved>> CACHE = new ConcurrentHashMap<>();
    private static final Set<Class<?>> IN_PROGRESS = ConcurrentHashMap.newKeySet();
    private static final Map<Class<?>, String> REASONS = new ConcurrentHashMap<>();
    private static final Map<StubKey, StubResult> STUB_RESULTS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Stored>> STORAGE = new ConcurrentHashMap<>();

    private LambdaResolver() {}

    static boolean isLambda(@Nullable Object value) {
        return value != null && value.getClass().isHidden() && value.getClass().getName().contains(LAMBDA_MARKER);
    }

    static void clearCaches() {
        CACHE.clear();
        REASONS.clear();
        STUB_RESULTS.clear();
        STORAGE.clear();
    }

    static String reason(Object lambda) {
        return REASONS.getOrDefault(lambda.getClass(), "unknown");
    }

    @Nullable
    static Target resolve(Object lambda) {
        Class<?> type = lambda.getClass();
        Optional<Resolved> resolved = CACHE.get(type);

        if (resolved == null) {
            if (!IN_PROGRESS.add(type)) {
                REASONS.put(type, "recursive resolution");
                return null;
            }

            try {
                resolved = find(lambda);
            } catch (Throwable e) {
                REASONS.put(type, "resolution threw " + e);
                resolved = Optional.empty();
            } finally {
                IN_PROGRESS.remove(type);
            }

            CACHE.put(type, resolved);
        }

        return resolved.map((r) -> new Target(r.owner(), r.impl(), captured(lambda, r.fields()))).orElse(null);
    }

    private static Optional<Resolved> find(Object lambda) throws ReflectiveOperationException {
        Class<?> type = lambda.getClass();
        String name = type.getName();
        Class<?> owner = Class.forName(name.substring(0, name.indexOf(LAMBDA_MARKER)), false, type.getClassLoader());
        ClassNode node = Resolution.classNode(owner);

        if (node == null) {
            REASONS.put(type, "no bytecode of " + owner.getName());
            return Optional.empty();
        }

        List<Field> fields = capturedFields(type);
        Set<String> interfaces = new HashSet<>();
        List<Handle> candidates = new ArrayList<>();
        Set<List<Type>> instantiated = new HashSet<>();

        for (Class<?> iface : type.getInterfaces()) {
            interfaces.add(Type.getInternalName(iface));
        }

        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                Handle handle = lambdaHandle(insn);

                if (handle != null && insn instanceof InvokeDynamicInsnNode indy && interfaces.contains(Type.getReturnType(indy.desc).getInternalName())
                        && capturedTypesMatch(Type.getArgumentTypes(indy.desc), fields) && !candidates.contains(handle)) {
                    candidates.add(handle);
                    instantiated.add(List.of(((Type) indy.bsmArgs[2]).getArgumentTypes()));
                }
            }
        }

        if (candidates.size() == 1) {
            return Optional.of(new Resolved(owner, candidates.get(0), fields));
        }
        if (candidates.isEmpty()) {
            REASONS.put(type, "no candidates");
            return Optional.empty();
        }

        if (instantiated.size() != 1) {
            REASONS.put(type, candidates.size() + " candidates with different instantiated parameter types");
            return Optional.empty();
        }

        Handle identified = fromStorage(lambda, owner, node, candidates);

        if (identified == null) {
            identified = identify(lambda, owner, candidates, fields, instantiated.iterator().next());
        }

        return identified != null ? Optional.of(new Resolved(owner, identified, fields)) : Optional.empty();
    }

    @Nullable
    private static Handle lambdaHandle(@Nullable AbstractInsnNode insn) {
        if (insn instanceof InvokeDynamicInsnNode indy && indy.bsm.getOwner().equals(LAMBDA_METAFACTORY) && indy.bsmArgs.length >= 3
                && indy.bsmArgs[1] instanceof Handle handle) {
            return handle;
        }

        return null;
    }

    @Nullable
    private static Handle fromStorage(Object lambda, Class<?> owner, ClassNode node, List<Handle> candidates) {
        for (Stored stored : STORAGE.computeIfAbsent(owner, (o) -> storage(node))) {
            if (candidates.contains(stored.handle()) && holds(owner, stored, lambda)) {
                return stored.handle();
            }
        }

        return null;
    }

    @NotNull
    private static List<Stored> storage(ClassNode node) {
        MethodNode clinit = Resolution.findMethod(node, "<clinit>", "()V");

        if (clinit == null) {
            return List.of();
        }

        Frame<SourceValue>[] frames;

        try {
            frames = new Analyzer<>(new SourceInterpreter()).analyze(node.name, clinit);
        } catch (AnalyzerException e) {
            return List.of();
        }

        List<Stored> stored = new ArrayList<>();
        Map<AbstractInsnNode, Map<Integer, Handle>> arrays = new HashMap<>();
        Map<String, Integer> adds = new HashMap<>();
        Set<String> shifted = new HashSet<>();

        for (int i = 0; i < clinit.instructions.size(); i++) {
            AbstractInsnNode insn = clinit.instructions.get(i);
            Frame<SourceValue> frame = frames[i];

            if (insn instanceof JumpInsnNode || insn instanceof TableSwitchInsnNode || insn instanceof LookupSwitchInsnNode) {
                break;
            }
            if (frame == null) {
                continue;
            }

            if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.PUTSTATIC && field.owner.equals(node.name)) {
                AbstractInsnNode value = origin(top(frame, 0), frames, clinit);
                Handle handle = lambdaHandle(value);

                if (handle != null) {
                    stored.add(new Stored(field.name, -1, handle, -1));
                } else if (arrays.containsKey(value)) {
                    arrays.get(value).forEach((index, element) -> stored.add(new Stored(field.name, index, element, -1)));
                }
            } else if (insn.getOpcode() == Opcodes.AASTORE) {
                AbstractInsnNode array = origin(top(frame, 2), frames, clinit);
                Integer index = constant(origin(top(frame, 1), frames, clinit));
                Handle handle = lambdaHandle(origin(top(frame, 0), frames, clinit));

                if (array != null && index != null && handle != null) {
                    arrays.computeIfAbsent(array, (a) -> new HashMap<>()).put(index, handle);
                }
            } else if (insn instanceof MethodInsnNode call) {
                int argCount = Type.getArgumentTypes(call.desc).length;

                for (int arg = 0; arg < argCount; arg++) {
                    String passed = ownStatic(origin(top(frame, arg), frames, clinit), node);

                    if (passed != null) {
                        shifted.add(passed);
                    }
                }

                String list = call.getOpcode() != Opcodes.INVOKESTATIC ? ownStatic(origin(top(frame, argCount), frames, clinit), node) : null;

                if (list != null) {
                    if (call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z")) {
                        int index = adds.merge(list, 1, Integer::sum) - 1;
                        Handle handle = lambdaHandle(origin(top(frame, 0), frames, clinit));

                        if (handle != null) {
                            stored.add(new Stored(list, index, handle, -1));
                        }
                    } else {
                        shifted.add(list);
                    }
                }
            }
        }

        stored.removeIf((s) -> s.index() >= 0 && shifted.contains(s.field()));
        return stored.stream().map((s) -> adds.containsKey(s.field()) ? new Stored(s.field(), s.index(), s.handle(), adds.get(s.field())) : s).toList();
    }

    private static SourceValue top(Frame<SourceValue> frame, int depth) {
        return frame.getStack(frame.getStackSize() - 1 - depth);
    }

    @Nullable
    private static AbstractInsnNode origin(SourceValue value, Frame<SourceValue>[] frames, MethodNode method) {
        if (value.insns.size() != 1) {
            return null;
        }

        AbstractInsnNode insn = value.insns.iterator().next();

        if (insn.getOpcode() == Opcodes.DUP) {
            Frame<SourceValue> frame = frames[method.instructions.indexOf(insn)];

            return frame != null ? origin(top(frame, 0), frames, method) : null;
        }

        return insn;
    }

    @Nullable
    private static String ownStatic(@Nullable AbstractInsnNode insn, ClassNode node) {
        if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC && field.owner.equals(node.name)) {
            return field.name;
        }

        return null;
    }

    @Nullable
    private static Integer constant(@Nullable AbstractInsnNode insn) {
        if (insn == null) {
            return null;
        }
        if (insn.getOpcode() >= Opcodes.ICONST_0 && insn.getOpcode() <= Opcodes.ICONST_5) {
            return insn.getOpcode() - Opcodes.ICONST_0;
        }
        if (insn instanceof IntInsnNode push && insn.getOpcode() != Opcodes.NEWARRAY) {
            return push.operand;
        }

        return insn instanceof LdcInsnNode ldc && ldc.cst instanceof Integer value ? value : null;
    }

    private static boolean holds(Class<?> owner, Stored stored, Object lambda) {
        try {
            Object value = Resolution.field(owner, stored.field()).get(null);

            if (stored.index() < 0) {
                return value == lambda;
            }
            if (value instanceof List<?> list) {
                return list.size() == stored.size() && stored.index() < list.size() && list.get(stored.index()) == lambda;
            }

            return value instanceof Object[] array && stored.index() < array.length && array[stored.index()] == lambda;
        } catch (Throwable e) {
            return false;
        }
    }

    @Nullable
    private static Handle identify(Object lambda, Class<?> owner, List<Handle> candidates, List<Field> fields, List<Type> params) {
        Method sam = samMethod(lambda.getClass());

        if (sam == null) {
            REASONS.put(lambda.getClass(), "no SAM method");
            return null;
        }

        List<Object> captured = captured(lambda, fields);
        List<Handle> matching = candidates.stream().filter((c) -> !readsStaticRandom(owner, c)).toList();

        if (matching.isEmpty()) {
            REASONS.put(lambda.getClass(), "every candidate reads a static random in " + owner.getName());
            return null;
        }

        for (int pick = 0; pick < STUB_PICKS && (pick == 0 || matching.size() > 1); pick++) {
            Object[] stubArgs = new Object[params.size()];
            List<Value> stubValues = new ArrayList<>();

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i).getSort() != Type.OBJECT) {
                    REASONS.put(lambda.getClass(), "primitive SAM parameter");
                    return null;
                }

                Class<?> param = Resolution.load(params.get(i).getInternalName(), owner.getClassLoader());

                if (param == RandomSource.class) {
                    stubArgs[i] = new StubRandom(pick);
                    stubValues.add(new Value.Rng(false));
                } else if (param == Random.class) {
                    stubArgs[i] = new StubRandom.Legacy(pick);
                    stubValues.add(new Value.Rng(true));
                } else {
                    stubValues.add(new Value.Known(null));
                }
            }

            Object first = runReal(lambda, sam, stubArgs);

            if (first instanceof Failed failed) {
                return fromStackTrace(failed.cause(), matching);
            }

            Object second = runReal(lambda, sam, freshStubs(stubArgs, pick));

            if (second instanceof Failed || !sameReal(first, second)) {
                if (!(second instanceof Failed)) {
                    REASONS.put(lambda.getClass(), "nondeterministic real run");
                }

                return null;
            }

            List<Handle> next = new ArrayList<>();
            Map<String, Integer> failures = new TreeMap<>();

            for (Handle candidate : matching) {
                StubKey key = captured.isEmpty() ? new StubKey(candidate, pick, params) : null;
                StubResult result = key != null ? STUB_RESULTS.get(key) : null;

                if (result == null) {
                    result = interpretStub(candidate, captured, stubValues, owner.getClassLoader(), pick);

                    if (key != null) {
                        STUB_RESULTS.put(key, result);
                    }
                }

                if (result.failure() != null) {
                    failures.merge(result.failure(), 1, Integer::sum);
                } else if (same(result.value(), first)) {
                    next.add(candidate);
                }
            }

            if (next.isEmpty()) {
                REASONS.put(lambda.getClass(), "no candidate matches the real run " + describe(first) + " (pick " + pick + ", "
                        + matching.size() + " candidates, failures " + failures + ")");
            }

            matching = next;
        }

        if (matching.size() > 1 && sameCode(matching, owner.getClassLoader())) {
            return matching.get(0);
        }
        if (matching.size() > 1) {
            REASONS.put(lambda.getClass(), matching.size() + " of " + candidates.size() + " candidates match: "
                    + matching.stream().map(Handle::getName).limit(5).toList());
        }

        return matching.size() == 1 ? matching.get(0) : null;
    }

    private static Object[] freshStubs(Object[] args, int pick) {
        Object[] copy = args.clone();

        for (int i = 0; i < copy.length; i++) {
            if (copy[i] instanceof StubRandom) {
                copy[i] = new StubRandom(pick);
            } else if (copy[i] instanceof StubRandom.Legacy) {
                copy[i] = new StubRandom.Legacy(pick);
            }
        }

        return copy;
    }

    @NotNull
    private static StubResult interpretStub(Handle candidate, List<Object> captured, List<Value> stubValues, ClassLoader loader, int pick) {
        try {
            Interpreter interpreter = new Interpreter(Interpreter.Mode.STUB, pick);
            List<Value> args = new ArrayList<>(captured.stream().map(interpreter.heap::wrap).toList());

            args.addAll(stubValues);
            return new StubResult(interpreter.invokeHandle(candidate, args, loader, 0), null);
        } catch (Throwable e) {
            return new StubResult(null, e instanceof Fail fail ? fail.describe() : e.toString());
        }
    }

    @Nullable
    private static Handle fromStackTrace(Throwable cause, List<Handle> candidates) {
        StackTraceElement[] trace = cause.getStackTrace();

        for (int i = trace.length - 1; i >= 0; i--) {
            StackTraceElement frame = trace[i];
            List<Handle> named = candidates.stream().filter((c) -> frame.getMethodName().equals(c.getName())
                    && frame.getClassName().equals(c.getOwner().replace('/', '.'))).toList();

            if (!named.isEmpty()) {
                return named.size() == 1 ? named.get(0) : null;
            }
        }

        return null;
    }

    private static boolean sameCode(List<Handle> handles, ClassLoader loader) {
        List<Object> first = null;

        for (Handle handle : handles) {
            List<Object> code;

            try {
                ClassNode node = Resolution.classNode(Resolution.load(handle.getOwner(), loader));
                MethodNode method = node != null ? Resolution.findMethod(node, handle.getName(), handle.getDesc()) : null;

                if (method == null || !method.tryCatchBlocks.isEmpty()) {
                    return false;
                }

                code = code(method);
            } catch (Fail fail) {
                return false;
            }

            if (first == null) {
                first = code;
            } else if (!first.equals(code)) {
                return false;
            }
        }

        return true;
    }

    @NotNull
    private static List<Object> code(MethodNode method) {
        Map<LabelNode, Integer> labels = new HashMap<>();
        List<Object> code = new ArrayList<>();

        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof LabelNode label) {
                labels.put(label, labels.size());
            }
        }

        for (AbstractInsnNode insn : method.instructions) {
            code.add(insn.getOpcode());

            if (insn instanceof LabelNode label) {
                code.add(labels.get(label));
            } else if (insn instanceof IntInsnNode node) {
                code.add(node.operand);
            } else if (insn instanceof VarInsnNode node) {
                code.add(node.var);
            } else if (insn instanceof TypeInsnNode node) {
                code.add(node.desc);
            } else if (insn instanceof FieldInsnNode node) {
                code.add(List.of(node.owner, node.name, node.desc));
            } else if (insn instanceof MethodInsnNode node) {
                code.add(List.of(node.owner, node.name, node.desc, node.itf));
            } else if (insn instanceof InvokeDynamicInsnNode node) {
                code.add(List.of(node.name, node.desc, node.bsm, Arrays.asList(node.bsmArgs)));
            } else if (insn instanceof JumpInsnNode node) {
                code.add(labels.get(node.label));
            } else if (insn instanceof LdcInsnNode node) {
                code.add(node.cst);
            } else if (insn instanceof IincInsnNode node) {
                code.add(List.of(node.var, node.incr));
            } else if (insn instanceof TableSwitchInsnNode node) {
                code.add(List.of(node.min, node.max, labels.get(node.dflt), node.labels.stream().map(labels::get).toList()));
            } else if (insn instanceof LookupSwitchInsnNode node) {
                code.add(List.of(node.keys, labels.get(node.dflt), node.labels.stream().map(labels::get).toList()));
            } else if (insn instanceof MultiANewArrayInsnNode node) {
                code.add(List.of(node.desc, node.dims));
            }
        }

        return code;
    }

    private static Object runReal(Object lambda, Method sam, Object[] args) {
        try {
            return sam.invoke(lambda, args);
        } catch (Throwable e) {
            Throwable cause = e instanceof InvocationTargetException target && target.getCause() != null ? target.getCause() : e;

            REASONS.put(lambda.getClass(), "real run threw " + cause);
            return new Failed(cause);
        }
    }

    private static boolean readsStaticRandom(Class<?> owner, Handle candidate) {
        Set<String> visited = new HashSet<>();
        Deque<Handle> queue = new ArrayDeque<>(List.of(candidate));
        ClassLoader loader = owner.getClassLoader();

        while (!queue.isEmpty() && visited.size() < MAX_SCANNED_METHODS) {
            Handle handle = queue.poll();

            if (!visited.add(handle.getOwner() + "." + handle.getName() + handle.getDesc())) {
                continue;
            }

            Class<?> type;

            try {
                type = Resolution.load(handle.getOwner(), loader);
            } catch (Fail fail) {
                continue;
            }

            if (Resolution.isJdk(type) || isPlatform(handle.getOwner())) {
                continue;
            }

            ClassNode node = Resolution.classNode(type);
            MethodNode method = node != null ? Resolution.findMethod(node, handle.getName(), handle.getDesc()) : null;

            if (method == null) {
                continue;
            }

            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC && isRandom(Type.getType(field.desc), loader)) {
                    return true;
                } else if (insn instanceof MethodInsnNode call) {
                    queue.add(new Handle(Opcodes.H_INVOKESTATIC, call.owner, call.name, call.desc, call.itf));
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    for (Object arg : indy.bsmArgs) {
                        if (arg instanceof Handle handleArg) {
                            queue.add(handleArg);
                        }
                    }
                }
            }
        }

        return false;
    }

    static boolean isPlatform(String internalName) {
        return internalName.startsWith("net/minecraft/") || internalName.startsWith("com/mojang/") || internalName.startsWith("net/minecraftforge/")
                || internalName.startsWith("net/neoforged/") || internalName.startsWith("net/fabricmc/");
    }

    private static boolean isRandom(Type type, ClassLoader loader) {
        if (type.getSort() != Type.OBJECT) {
            return false;
        }

        try {
            Class<?> cls = Resolution.load(type.getInternalName(), loader);

            return RandomSource.class.isAssignableFrom(cls) || Random.class.isAssignableFrom(cls);
        } catch (Fail fail) {
            return false;
        }
    }

    @Nullable
    private static Method samMethod(Class<?> type) {
        for (Class<?> iface : type.getInterfaces()) {
            for (Method method : iface.getMethods()) {
                if (Modifier.isAbstract(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !isObjectMethod(method)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

    private static boolean isObjectMethod(Method method) {
        try {
            Object.class.getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @NotNull
    private static List<Field> capturedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();

        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                fields.add(field);
            }
        }

        fields.sort(Comparator.comparingInt((f) -> captureIndex(f.getName())));
        return fields;
    }

    private static int captureIndex(String name) {
        int dollar = name.lastIndexOf('$');

        try {
            return Integer.parseInt(name.substring(dollar + 1));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static boolean capturedTypesMatch(Type[] types, List<Field> fields) {
        if (types.length != fields.size()) {
            return false;
        }

        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(fields.get(i).getType()).equals(types[i])) {
                return false;
            }
        }

        return true;
    }

    @NotNull
    private static List<Object> captured(Object lambda, List<Field> fields) {
        List<Object> values = new ArrayList<>();

        for (Field field : fields) {
            try {
                values.add(field.get(lambda));
            } catch (IllegalAccessException e) {
                throw new Fail("captured value");
            }
        }

        return values;
    }

    private static boolean same(@Nullable Value result, @Nullable Object real) {
        if (real == null) {
            return result instanceof Value.Known known && known.value() == null;
        }
        if (real instanceof ItemStack stack) {
            return sameStack(result, stack);
        }
        if (real instanceof MerchantOffer offer) {
            if (result instanceof Value.Known known && known.value() instanceof MerchantOffer other) {
                return sameOffer(other, offer);
            }

            return result instanceof Value.Offer o && sameStack(o.costA(), offer.getBaseCostA()) && sameStack(o.costB(), offer.getCostB())
                    && sameStack(o.result(), offer.getResult()) && o.maxUses() == offer.getMaxUses() && o.xp() == offer.getXp()
                    && Float.compare(o.priceMultiplier(), offer.getPriceMultiplier()) == 0;
        }

        return result instanceof Value.Known known && TradeUtils.valueKey(normalize(known.value())).equals(TradeUtils.valueKey(normalize(real)));
    }

    private static boolean sameStack(@Nullable Value value, ItemStack real) {
        if (value instanceof Value.Known known && known.value() instanceof ItemStack other) {
            return ItemStack.matches(other, real);
        }
        if (value instanceof Value.Stack stack && stack.options.size() == 1 && stack.count instanceof com.yanny.aci.api.NumberExpr.Const c) {
            ItemStack template = stack.options.get(0).copy();

            template.setCount((int) c.value());
            return ItemStack.matches(template, real);
        }

        return false;
    }

    private static boolean sameOffer(MerchantOffer a, MerchantOffer b) {
        return ItemStack.matches(a.getBaseCostA(), b.getBaseCostA()) && ItemStack.matches(a.getCostB(), b.getCostB())
                && ItemStack.matches(a.getResult(), b.getResult()) && a.getMaxUses() == b.getMaxUses() && a.getXp() == b.getXp()
                && Float.compare(a.getPriceMultiplier(), b.getPriceMultiplier()) == 0;
    }

    private static boolean sameReal(@Nullable Object a, @Nullable Object b) {
        if (a instanceof ItemStack x && b instanceof ItemStack y) {
            return ItemStack.matches(x, y);
        }
        if (a instanceof MerchantOffer x && b instanceof MerchantOffer y) {
            return sameOffer(x, y);
        }

        return Objects.equals(TradeUtils.valueKey(a), TradeUtils.valueKey(b));
    }

    private static String describe(@Nullable Object real) {
        if (real instanceof MerchantOffer offer) {
            return offer.getBaseCostA() + " + " + offer.getCostB() + " -> " + offer.getResult();
        }

        return String.valueOf(real);
    }

    @Nullable
    private static Object normalize(@Nullable Object value) {
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        } else if (value instanceof Character c) {
            return (int) c;
        } else if (value instanceof Byte b) {
            return (int) b;
        } else if (value instanceof Short s) {
            return (int) s;
        }

        return value;
    }

    private record Failed(Throwable cause) {
    }

    private record StubKey(Handle candidate, int pick, List<Type> params) {
    }

    private record StubResult(@Nullable Value value, @Nullable String failure) {
    }

    record Target(Class<?> owner, Handle impl, List<Object> captured) {
    }

    private record Resolved(Class<?> owner, Handle impl, List<Field> fields) {
    }

    private record Stored(String field, int index, Handle handle, int size) {
    }
}
