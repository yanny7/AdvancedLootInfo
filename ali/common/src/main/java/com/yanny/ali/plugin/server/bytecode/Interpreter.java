package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberInterval;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class Interpreter implements Opcodes {
    private static final int MAX_STEPS = 200_000;
    private static final int MAX_DEPTH = 12;
    private static final int MAX_OPTIONS = 1024;
    private static final int MAX_DECISIONS = 8;
    private static final String CONSTRUCTOR = "<init>";
    private static final String ITEM_STACK = Type.getInternalName(ItemStack.class);
    private static final String ITEM_COST = Type.getInternalName(ItemCost.class);
    private static final String MERCHANT_OFFER = Type.getInternalName(MerchantOffer.class);
    private static final String OPTIONAL = Type.getInternalName(Optional.class);
    private static final Set<String> TRADE_TYPES = Set.of(ITEM_STACK, ITEM_COST, MERCHANT_OFFER, Type.getInternalName(MerchantOffers.class));
    private static final String LAMBDA_METAFACTORY = "java/lang/invoke/LambdaMetafactory";
    private static final String STRING_CONCAT_FACTORY = "java/lang/invoke/StringConcatFactory";
    private static final Value.Known NULL = new Value.Known(null);
    private static final Set<String> STATIC_SAFE = Set.of("java/lang/Math", "java/lang/StrictMath", "java/lang/Integer", "java/lang/Long",
            "java/lang/Float", "java/lang/Double", "java/lang/Boolean", "java/lang/Character", "java/lang/Short", "java/lang/Byte",
            "java/lang/String", "java/lang/Enum", "java/util/Objects", "java/util/List", "java/util/Set", "java/util/Map",
            "java/util/Collections", "java/util/Arrays", "java/util/Optional", "java/util/Comparator", "java/util/stream/Stream",
            "java/util/stream/IntStream", "java/util/stream/Collectors");
    private static final Set<String> STATIC_RANDOM = Set.of("random", "shuffle");
    private static final Set<String> READ_ONLY = Set.of("get", "getOrDefault", "size", "isEmpty", "contains", "containsKey",
            "containsValue", "indexOf", "lastIndexOf", "keySet", "values", "entrySet", "stream", "iterator", "listIterator",
            "spliterator", "toArray", "equals", "hashCode", "toString", "length", "charAt", "substring", "startsWith", "endsWith",
            "trim", "toLowerCase", "toUpperCase", "concat", "getKey", "getValue", "intValue", "longValue", "floatValue",
            "doubleValue", "booleanValue", "byteValue", "shortValue", "charValue", "compareTo", "ordinal", "name", "getClass",
            "isPresent", "orElse", "orElseGet", "orElseThrow", "map", "filter", "subList", "first", "firstKey", "lastKey");
    private static final Set<String> LOOKUPS = Set.of("get", "getOrDefault", "containsKey");
    private static final Set<String> ORDER_ONLY = Set.of("reverse", "shuffle", "sort");
    private static final Set<String> FRESH_RESULTS = Set.of("stream", "iterator", "listIterator", "spliterator", "toArray");
    private static final Map<String, Optional<ClassNode>> CLASS_NODES = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Method>> METHODS = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Field>> FIELDS = new ConcurrentHashMap<>();
    private static final boolean TRACE = Boolean.getBoolean("ali.debug.dispatch");
    private static final Map<String, LongAdder> TRACED = new ConcurrentHashMap<>();

    private final Mode mode;
    private final int pick;
    private final Map<Object, Integer> owned = new IdentityHashMap<>();
    private final Map<Object, Value.Stack> shadows = new IdentityHashMap<>();
    private final List<Frame> frames = new ArrayList<>();
    private int frozenBelow;
    private int steps;
    @Nullable
    private final List<Boolean> script;
    private final List<Decision> decisions = new ArrayList<>();
    @Nullable
    private Class<?> merchant;
    @Nullable
    private Value.Offers merchantOffers;
    private final Map<Object, Integer> listingKeys = new IdentityHashMap<>();
    private final Map<Object, List<BoundOffer>> boundOffers = new IdentityHashMap<>();
    private final Map<String, Value> merchantFields = new HashMap<>();
    private final Map<String, Value> statics = new HashMap<>();
    private final Set<Object> tainted = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Object, Value.Stack> stackParts = new IdentityHashMap<>();
    private final Map<String, Integer> heavyWorldCalls = new HashMap<>();
    private final Map<Object, Integer> removed = new IdentityHashMap<>();
    private final Map<Object, int[]> randomSets = new IdentityHashMap<>();
    private final Map<Object, Value.Draws> shuffled = new IdentityHashMap<>();
    private final Map<Object, NumberExpr> drawCounts = new IdentityHashMap<>();
    private final Map<Object, Object> elementSources = new IdentityHashMap<>();
    private final Set<Object> pools = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Object, Value.Offers> offerLists = new IdentityHashMap<>();
    private final Map<NumberExpr, Drawn> drawn = new IdentityHashMap<>();
    private boolean ownGetOffers;
    @Nullable
    private final RegistryAccess registries;
    private final Set<String> opaque;
    private boolean retry;

    Interpreter(Mode mode, int pick) {
        this(mode, pick, null, null, new HashSet<>());
    }

    Interpreter(Mode mode, int pick, @Nullable List<Boolean> script, @Nullable RegistryAccess registries, Set<String> opaque) {
        this.mode = mode;
        this.pick = pick;
        this.script = script;
        this.registries = registries;
        this.opaque = opaque;
    }

    List<Decision> decisions() {
        return decisions;
    }

    private boolean decide(Decision.Kind kind, @Nullable Object subject, @Nullable Object value, String reason) {
        if (script == null) {
            throw new Fail(reason);
        }

        Decision decision = new Decision(kind, subject, value, false);

        for (Decision earlier : decisions) {
            if (earlier.sameTest(decision)) {
                return earlier.holds();
            }
            if (kind == Decision.Kind.EQUALS && earlier.kind() == kind && earlier.holds() && subject != null && subject.equals(earlier.subject())) {
                return false;
            }
        }

        boolean holds = decisions.size() < script.size() && script.get(decisions.size());

        if ((kind != Decision.Kind.EQUALS || holds) && decisions.stream().filter((d) -> d.kind() != Decision.Kind.EQUALS || d.holds()).count() >= MAX_DECISIONS) {
            heavyWorldCalls.entrySet().stream().filter((e) -> e.getValue() > 1 && !opaque.contains(e.getKey())).max(Map.Entry.comparingByValue())
                    .ifPresent((e) -> retry = opaque.add(e.getKey()));
            throw new Fail("too many branches");
        }

        decisions.add(holds ? decision.negated() : decision);
        return holds;
    }

    static void clearCaches() {
        CLASS_NODES.clear();
        METHODS.clear();
        FIELDS.clear();
    }

    static List<String> dispatchTrace() {
        List<String> lines = TRACED.entrySet().stream().sorted(Map.Entry.comparingByKey()).map((e) -> e.getKey() + " " + e.getValue().sum()).toList();

        TRACED.clear();
        return lines;
    }

    private static void trace(String label, MethodInsnNode call) {
        if (TRACE) {
            TRACED.computeIfAbsent(label + " " + call.owner + "." + call.name + call.desc, (k) -> new LongAdder()).increment();
        }
    }

    private static void trace(String label, String type, String desc) {
        if (TRACE) {
            TRACED.computeIfAbsent(label + " " + type + "." + CONSTRUCTOR + desc, (k) -> new LongAdder()).increment();
        }
    }

    @Nullable
    Value call(Object receiver, Class<?> iface, String name, String desc, List<Value> args) {
        return call(receiver, new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(iface), name, desc, true), args);
    }

    Value call(Object receiver, MethodInsnNode call, List<Value> args) {
        List<Value> all = new ArrayList<>();

        all.add(wrap(receiver));
        all.addAll(args);
        return invoke(call, all, receiver.getClass().getClassLoader(), 0);
    }

    Value.Offers trades(Class<?> merchant) {
        Value self = Value.Unknown.of(Type.getType(merchant), Origin.TRADER);
        Declaration getOffers = ownDeclaration(merchant, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), false);

        this.merchant = merchant;
        merchantOffers = new Value.Offers();
        ownGetOffers = getOffers != null;

        if (getOffers != null) {
            if (execute(getOffers.owner(), getOffers.method(), List.of(self), 0) instanceof Value.Offers offers) {
                return offers;
            }

            throw new Fail("offers of the trader");
        }

        Declaration updateTrades = ownDeclaration(merchant, RuntimeNames.UPDATE_TRADES.name(), RuntimeNames.UPDATE_TRADES.desc(), false);

        if (updateTrades == null) {
            throw new Fail("no trades of the trader");
        }

        execute(updateTrades.owner(), updateTrades.method(), List.of(self), 0);
        return merchantOffers;
    }

    Value.Offers writtenTrades(Class<?> merchant) {
        Declaration writer = offersWriter(merchant);

        if (writer == null) {
            throw new Fail("no method writing the offers of the trader");
        }

        List<Value> args = new ArrayList<>();

        args.add(Value.Unknown.of(Type.getType(merchant), Origin.TRADER));
        Arrays.stream(Type.getArgumentTypes(writer.method().desc)).forEach((type) -> args.add(Value.Unknown.of(type, null)));
        this.merchant = merchant;
        merchantOffers = new Value.Offers();
        ownGetOffers = ownDeclaration(merchant, RuntimeNames.GET_OFFERS.name(), RuntimeNames.GET_OFFERS.desc(), false) != null;
        execute(writer.owner(), writer.method(), args, 0);
        return merchantOffers;
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

                Declaration own = ownDeclaration(merchant, method.name, method.desc, false);

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
                if (insn.getOpcode() == PUTFIELD && isVillagerOffers((FieldInsnNode) insn, loader)) {
                    return true;
                }
            } catch (Fail ignored) {}
        }

        return false;
    }

    private static boolean isPlainOffers(Class<?> type) {
        return MerchantOffers.class.isAssignableFrom(type) && declaration(type, "add", "(Ljava/lang/Object;)Z", false) instanceof JdkDeclaration;
    }

    private static boolean isVillagerOffers(FieldInsnNode insn, ClassLoader loader) {
        return RuntimeNames.is(RuntimeNames.OFFERS, insn) && field(load(insn.owner, loader), insn.name).getDeclaringClass() == AbstractVillager.class;
    }

    private boolean isSelf(@Nullable Value value) {
        return merchant != null && value instanceof Value.Unknown unknown && Origin.TRADER.equals(unknown.origin());
    }

    @Nullable
    private static Declaration ownDeclaration(Class<?> start, String name, String desc, boolean exact) {
        if (!(declaration(start, name, desc, exact) instanceof Declaration declaration) || LambdaResolver.isPlatform(Type.getInternalName(declaration.owner()))) {
            return null;
        }

        return declaration;
    }

    @Nullable
    Value invokeHandle(Handle handle, List<Value> args, ClassLoader loader, int depth) {
        return switch (handle.getTag()) {
            case H_INVOKESTATIC -> invoke(new MethodInsnNode(INVOKESTATIC, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface()), args, loader, depth);
            case H_INVOKEVIRTUAL -> invoke(new MethodInsnNode(INVOKEVIRTUAL, handle.getOwner(), handle.getName(), handle.getDesc(), false), args, loader, depth);
            case H_INVOKEINTERFACE -> invoke(new MethodInsnNode(INVOKEINTERFACE, handle.getOwner(), handle.getName(), handle.getDesc(), true), args, loader, depth);
            case H_INVOKESPECIAL -> invoke(new MethodInsnNode(INVOKESPECIAL, handle.getOwner(), handle.getName(), handle.getDesc(), handle.isInterface()), args, loader, depth);
            case H_NEWINVOKESPECIAL -> construct(handle.getOwner(), handle.getDesc(), args, loader);
            default -> throw new Fail("unsupported method handle");
        };
    }

    @Nullable
    private Value execute(Class<?> owner, MethodNode method, List<Value> args, int depth) {
        if (depth > MAX_DEPTH) {
            throw new Fail("call depth");
        }
        if ((method.access & (ACC_ABSTRACT | ACC_NATIVE)) != 0) {
            throw new Fail("no body");
        }
        if (MixinChanges.isChanged(owner, method)) {
            throw new Fail("changed by a mixin: " + owner.getName() + "." + method.name);
        }

        Frame frame = new Frame(Math.max(method.maxLocals, 1));
        int slot = 0;
        int index = 0;

        if ((method.access & ACC_STATIC) == 0) {
            frame.locals[slot++] = args.get(index++);
        }

        for (Type type : Type.getArgumentTypes(method.desc)) {
            Value arg = args.get(index++);

            frame.locals[slot] = arg instanceof Value.Known known ? wrap(known.value(), javaType(type)) : arg;
            slot += type.getSize();
        }

        frames.add(frame);

        try {
            return run(frame, owner.getClassLoader(), method, depth);
        } catch (Fail fail) {
            throw fail.at(owner.getName() + "." + method.name);
        } finally {
            frames.remove(frames.size() - 1);
        }
    }

    @Nullable
    private Value run(Frame f, ClassLoader loader, MethodNode method, int depth) {
        AbstractInsnNode insn = method.instructions.getFirst();

        while (insn != null) {
            if (++steps > MAX_STEPS) {
                throw new Fail("step budget");
            }

            int op = insn.getOpcode();
            AbstractInsnNode next = insn.getNext();

            if (op < 0) {
                insn = next;
                continue;
            }

            switch (op) {
                case NOP -> {}
                case ACONST_NULL -> f.push(NULL);
                case ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5 -> f.push(new Value.Known(op - ICONST_0));
                case LCONST_0, LCONST_1 -> f.push(new Value.Known((long) (op - LCONST_0)));
                case FCONST_0, FCONST_1, FCONST_2 -> f.push(new Value.Known((float) (op - FCONST_0)));
                case DCONST_0, DCONST_1 -> f.push(new Value.Known((double) (op - DCONST_0)));
                case BIPUSH, SIPUSH -> f.push(new Value.Known(((IntInsnNode) insn).operand));
                case LDC -> f.push(ldc(((LdcInsnNode) insn).cst, loader));
                case ILOAD, LLOAD, FLOAD, DLOAD, ALOAD -> f.push(f.load(((VarInsnNode) insn).var));
                case ISTORE, FSTORE, ASTORE -> f.locals[((VarInsnNode) insn).var] = f.pop();
                case LSTORE, DSTORE -> {
                    int var = ((VarInsnNode) insn).var;

                    f.locals[var] = f.pop();
                    f.locals[var + 1] = null;
                }
                case IINC -> {
                    IincInsnNode iinc = (IincInsnNode) insn;

                    f.locals[iinc.var] = binary(IADD, f.load(iinc.var), new Value.Known(iinc.incr));
                }
                case IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD -> {
                    Value idx = f.pop();

                    f.push(arrayLoad(f.pop(), idx));
                }
                case IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE -> {
                    Value value = f.pop();
                    Value idx = f.pop();

                    arrayStore(op, f.pop(), idx, value);
                }
                case POP, MONITORENTER, MONITOREXIT -> f.pop();
                case POP2 -> {
                    if (!f.pop().isWide()) {
                        f.pop();
                    }
                }
                case DUP -> f.push(f.peek());
                case DUP_X1 -> {
                    Value v1 = f.pop();
                    Value v2 = f.pop();

                    f.push(v1);
                    f.push(v2);
                    f.push(v1);
                }
                case DUP_X2 -> {
                    Value v1 = f.pop();
                    Value v2 = f.pop();

                    if (v2.isWide()) {
                        f.push(v1);
                        f.push(v2);
                    } else {
                        Value v3 = f.pop();

                        f.push(v1);
                        f.push(v3);
                        f.push(v2);
                    }

                    f.push(v1);
                }
                case DUP2 -> {
                    Value v1 = f.pop();

                    if (v1.isWide()) {
                        f.push(v1);
                        f.push(v1);
                    } else {
                        Value v2 = f.pop();

                        f.push(v2);
                        f.push(v1);
                        f.push(v2);
                        f.push(v1);
                    }
                }
                case DUP2_X1 -> {
                    Value v1 = f.pop();

                    if (v1.isWide()) {
                        Value v2 = f.pop();

                        f.push(v1);
                        f.push(v2);
                        f.push(v1);
                    } else {
                        Value v2 = f.pop();
                        Value v3 = f.pop();

                        f.push(v2);
                        f.push(v1);
                        f.push(v3);
                        f.push(v2);
                        f.push(v1);
                    }
                }
                case DUP2_X2 -> dup2x2(f);
                case SWAP -> {
                    Value v1 = f.pop();
                    Value v2 = f.pop();

                    f.push(v1);
                    f.push(v2);
                }
                case IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM,
                     ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR -> {
                    Value b = f.pop();

                    f.push(binary(op, f.pop(), b));
                }
                case INEG, LNEG, FNEG, DNEG -> f.push(negate(op, f.pop()));
                case I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C, I2S -> f.push(convert(op, f.pop()));
                case LCMP, FCMPL, FCMPG, DCMPL, DCMPG -> {
                    Value b = f.pop();

                    f.push(compare(op, f.pop(), b));
                }
                case IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE -> {
                    if (branch(op, f.pop(), new Value.Known(0))) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE -> {
                    Value b = f.pop();

                    if (branch(op - IF_ICMPEQ + IFEQ, f.pop(), b)) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case IF_ACMPEQ, IF_ACMPNE -> {
                    Value b = f.pop();
                    boolean same = sameReference(f.pop(), b);

                    if (same == (op == IF_ACMPEQ)) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case IFNULL, IFNONNULL -> {
                    if (isNull(f.pop()) == (op == IFNULL)) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case GOTO -> next = ((JumpInsnNode) insn).label;
                case TABLESWITCH -> {
                    TableSwitchInsnNode sw = (TableSwitchInsnNode) insn;
                    int at = switchCase(f.pop(), IntStream.rangeClosed(sw.min, sw.max).boxed().toList());

                    next = at >= 0 ? sw.labels.get(at) : sw.dflt;
                }
                case LOOKUPSWITCH -> {
                    LookupSwitchInsnNode sw = (LookupSwitchInsnNode) insn;
                    int at = switchCase(f.pop(), sw.keys);

                    next = at >= 0 ? sw.labels.get(at) : sw.dflt;
                }
                case IRETURN, LRETURN, FRETURN, DRETURN, ARETURN -> {
                    return f.pop();
                }
                case RETURN -> {
                    return null;
                }
                case GETSTATIC -> f.push(getStatic((FieldInsnNode) insn, loader));
                case PUTSTATIC -> {
                    FieldInsnNode field = (FieldInsnNode) insn;

                    statics.put(field.owner + "." + field.name, f.pop());
                }
                case GETFIELD -> f.push(getField((FieldInsnNode) insn, f.pop(), loader));
                case PUTFIELD -> {
                    Value value = f.pop();

                    putField((FieldInsnNode) insn, f.pop(), value, loader);
                }
                case INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE -> {
                    MethodInsnNode call = (MethodInsnNode) insn;
                    Value[] args = new Value[Type.getArgumentTypes(call.desc).length + (op == INVOKESTATIC ? 0 : 1)];

                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = f.pop();
                    }

                    Value result = invoke(call, Arrays.asList(args), loader, depth);
                    Type returnType = Type.getReturnType(call.desc);

                    if (returnType.getSort() != Type.VOID) {
                        if (result == null) {
                            throw new Fail("missing return value");
                        }

                        f.push(result instanceof Value.Unknown unknown && unknown.type() == null ? Value.Unknown.of(returnType, unknown.origin()) : result);
                    }
                }
                case INVOKEDYNAMIC -> f.push(invokeDynamic((InvokeDynamicInsnNode) insn, f, loader));
                case NEW -> f.push(new Value.Uninit(((TypeInsnNode) insn).desc));
                case NEWARRAY -> f.push(newArray(primitiveArrayType(((IntInsnNode) insn).operand), f.pop()));
                case ANEWARRAY -> f.push(newArray(arrayComponent(((TypeInsnNode) insn).desc, loader), f.pop()));
                case ARRAYLENGTH -> {
                    if (!(f.pop() instanceof Value.Known known) || known.value() == null || !known.value().getClass().isArray()) {
                        throw new Fail("array length");
                    }

                    f.push(new Value.Known(Array.getLength(known.value())));
                }
                case CHECKCAST -> f.push(checkCast(f.pop(), ((TypeInsnNode) insn).desc, loader));
                case INSTANCEOF -> f.push(new Value.Known(instanceOf(f.pop(), ((TypeInsnNode) insn).desc, loader) ? 1 : 0));
                default -> throw new Fail("opcode " + op);
            }

            insn = next;
        }

        throw new Fail("end of method");
    }

    private static void dup2x2(Frame f) {
        Value v1 = f.pop();
        Value v2 = f.pop();

        if (v1.isWide() && v2.isWide()) {
            f.push(v1);
            f.push(v2);
            f.push(v1);
        } else if (v1.isWide()) {
            Value v3 = f.pop();

            f.push(v1);
            f.push(v3);
            f.push(v2);
            f.push(v1);
        } else {
            Value v3 = f.pop();

            if (v3.isWide()) {
                f.push(v2);
                f.push(v1);
                f.push(v3);
            } else {
                Value v4 = f.pop();

                f.push(v2);
                f.push(v1);
                f.push(v4);
                f.push(v3);
            }

            f.push(v2);
            f.push(v1);
        }
    }

    @Nullable
    private Value invoke(MethodInsnNode call, List<Value> args, ClassLoader loader, int depth) {
        Value result = dispatch(call, args, loader, depth);

        if (result instanceof Value.Known known && isData(known.value()) && args.stream().anyMatch(this::isTainted)) {
            trace("after.taint", call);
            taint(known.value());
        }

        if (call.getOpcode() != INVOKESTATIC && call.name.equals("get") && args.size() == 2 && args.get(0) instanceof Value.Known map && map.value() instanceof Map<?, ?>
                && args.get(1) instanceof Value.Known key && result instanceof Value.Known listings && isKeyedList(listings.value())) {
            if (key.value() instanceof Integer level) {
                trace("after.levelKey", call);
                listingKeys.put(listings.value(), level);
            } else if (key.value() instanceof Enum<?> constant) {
                trace("after.levelKey", call);
                listingKeys.put(listings.value(), constant.ordinal() + 1);
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

            replace(uninit, construct(uninit.type, call.desc, args.subList(1, args.size()), loader));
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

        if (merchant != null && RuntimeNames.is(RuntimeNames.GET_OFFER, call)) {
            if (args.get(0) instanceof Value.Choice choice && (choice.draw() != null && pools.contains(choice.draw()) || isUniformPick(choice))
                    && choice.options().stream().allMatch(VillagerTrades.ItemListing.class::isInstance)) {
                trace("undistributed.getOffer.choice", call);
                return new Value.Pick(choice.options().stream().map(VillagerTrades.ItemListing.class::cast).toList(), choice.draw(), poolKey(choice.options()));
            }
            if (args.get(0) instanceof Value.Known known && known.value() instanceof VillagerTrades.ItemListing listing) {
                Object source = elementSources.containsKey(listing) ? elementSources.get(listing) : keyedSource(listing);

                trace("undistributed.getOffer.known", call);
                return new Value.Pick(List.of(listing), source != null ? source : listing, source != null ? listingKeys.get(source) : null);
            }
        }
        if (merchant != null && !isStatic && args.get(0) instanceof Value.Choice choice && (choice.draw() != null && pools.contains(choice.draw()) || isUniformPick(choice))
                && choice.options().stream().noneMatch(MerchantOffer.class::isInstance) && returnsOffer(call, loader)) {
            List<Object> bound = bound(args.subList(1, args.size()));

            if (bound != null) {
                List<VillagerTrades.ItemListing> listings = choice.options().stream().<VillagerTrades.ItemListing>map((element) -> boundOffer(element, call, bound)).toList();

                trace("undistributed.boundOffer", call);
                return new Value.Pick(listings, choice.draw(), poolKey(choice.options()));
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
            return optionalCall(call, stack, args, depth);
        }
        if (!isStatic && call.owner.equals(OPTIONAL) && args.get(0) instanceof Value.Known known && known.value() instanceof Optional<?> optional
                && args.stream().skip(1).anyMatch((arg) -> arg instanceof Value.Closure || !isConcrete(arg))) {
            trace("undistributed.optionalConcrete", call);
            return optionalCall(call, optional.isPresent() ? wrap(optional.get()) : null, args, depth);
        }

        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && owned.containsKey(list)
                && call.name.equals("remove") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Choice choice
                && choice.options().size() == list.size() - removed.getOrDefault(list, 0) && new HashSet<Object>(list).containsAll(choice.options())) {
            removed.merge(list, 1, Integer::sum);
            trace("undistributed.removeChoice", call);
            return new Value.Known(1);
        }
        Value.Offers collected = !isStatic && call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && isSymbolicOffer(args.get(1))
                ? offerList(args.get(0), true) : null;

        if (collected != null) {
            trace("undistributed.addOffer", call);
            return args.get(1) instanceof Value.Choice choice ? addPicked(collected, choice) : offersCall(call, collected, args, depth);
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && offerLists.containsKey(known.value())) {
            trace("undistributed.collectedOffers", call);
            return offersCall(call, offerLists.get(known.value()), args, depth);
        }

        int[] choices = choiceIndices(args);

        if (choices.length > 0) {
            trace("distribute", call);
            return distribute(call, args, choices, loader, depth);
        }

        Value receiver = isStatic ? null : args.get(0);

        if (receiver instanceof Value.Rng rng) {
            trace("receiver.rng", call);
            return random(call, rng, args, depth);
        }
        if (receiver instanceof Value.Stack stack && call.owner.equals(ITEM_COST)) {
            trace("receiver.cost", call);
            return costCall(call, stack, args, loader);
        }
        if (receiver instanceof Value.Stack stack) {
            trace("receiver.stack", call);
            return stackCall(call, stack, args, loader);
        }
        if (receiver instanceof Value.Known known && known.value() instanceof ItemStack real) {
            trace("receiver.realStack", call);
            return knownStackCall(call, real, args, loader);
        }
        if (receiver instanceof Value.Num num) {
            trace("receiver.num", call);
            return unbox(call, num);
        }
        if (receiver instanceof Value.Closure closure) {
            if (!call.name.equals(closure.samName())) {
                trace("receiver.closure.fail", call);
                throw new Fail("closure default method");
            }

            List<Value> all = new ArrayList<>(closure.captured());

            all.addAll(args.subList(1, args.size()));
            trace("receiver.closure", call);
            return invokeHandle(closure.impl(), all, closure.loader(), depth + 1);
        }
        if (isSelf(receiver)) {
            if (!ownGetOffers && RuntimeNames.is(RuntimeNames.GET_OFFERS, call)) {
                trace("receiver.trader.getOffers", call);
                return merchantOffers;
            }
            if (RuntimeNames.is(RuntimeNames.ADD_OFFERS_FROM_ITEM_LISTINGS, call)) {
                if (!(args.get(1) instanceof Value.Offers offers) || !(args.get(2) instanceof Value.Known known)
                        || !(known.value() instanceof VillagerTrades.ItemListing[] listings)) {
                    trace("receiver.trader.addOffers.fail", call);
                    throw new Fail("offers from item listings");
                }

                offers.entries.add(new Value.Offers.Listings(listings, args.get(3), listingKeys.get(listings)));
                trace("receiver.trader.addOffers", call);
                return null;
            }

            boolean special = call.getOpcode() == INVOKESPECIAL;
            Class<?> start = special ? load(call.owner, loader) : merchant;

            if (start != null) {
                Declaration own = ownDeclaration(start, call.name, call.desc, special);

                if (own != null) {
                    trace("receiver.trader.own", call);
                    return execute(own.owner(), own.method(), args, depth + 1);
                }
            }
        }
        if (receiver instanceof Value.Offers offers) {
            trace("receiver.offers", call);
            return offersCall(call, offers, args, depth);
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
        if (receiver instanceof Value.Unknown && registries != null && (RuntimeNames.is(RuntimeNames.REGISTRY_ACCESS, call) || RuntimeNames.is(RuntimeNames.ENTITY_REGISTRY_ACCESS, call))) {
            trace("receiver.registryAccess", call);
            return new Value.Known(registries);
        }
        if (receiver instanceof Value.Unknown) {
            Type returnType = Type.getReturnType(call.desc);

            if (returnType.getSort() == Type.OBJECT && isRandomType(returnType, loader)) {
                trace("receiver.unknown.random", call);
                return new Value.Rng(isLegacyRandom(returnType, loader));
            }

            taintOwned(args);
            trace("receiver.unknown", call);
            return Value.Unknown.of(returnType, Origin.of(receiver, call.name + call.desc, args.subList(1, args.size())));
        }
        if (receiver instanceof Value.Offer || receiver instanceof Value.Uninit || receiver instanceof Value.Cmp) {
            trace("receiver.unsupported.fail", call);
            throw new Fail("unsupported receiver");
        }
        if (!isStatic && receiver instanceof Value.Known known && known.value() instanceof Map<?, ?> map && LOOKUPS.contains(call.name)
                && args.get(1) instanceof Value.Unknown key && key.origin() != null && args.subList(2, args.size()).stream().allMatch(this::isConcrete)) {
            trace("receiver.lookup", call);
            return lookup(call, map.getClass(), map, key.origin(), args, loader);
        }
        if (receiver instanceof Value.Known known && known.value() == null) {
            trace("receiver.null.fail", call);
            throw new Fail("null receiver");
        }
        if (receiver instanceof Value.Known known && LambdaResolver.isLambda(known.value())) {
            trace("receiver.lambda", call);
            return lambdaCall(known.value(), args, depth);
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
            return jdkCall(call, jdk.owner(), args, loader);
        }

        if (isStatic && isJdk(start)) {
            return jdkCall(call, start, args, loader);
        }

        if (args.stream().anyMatch(this::holdsStack)) {
            trace("arguments.stacks", call);
            return stackArgumentCall(call, args, loader);
        }

        if (declared != null && opaque.contains(call.owner + "." + call.name + call.desc) && isWorldCall(call, args, loader)) {
            trace("arguments.opaqueWorldCall", call);
            return worldResult(call, args);
        }
        if (!isStatic && receiver instanceof Value.Known known && args.stream().skip(1).anyMatch(this::isTainted) && !returnsTradeData(call)) {
            trace("arguments.tainted", call);
            return uncertainChange(call, known.value());
        }

        Value result;

        if (declared instanceof Declaration declaration) {
            int before = decisions.size();

            try {
                trace("execution.interpret", call);
                result = execute(declaration.owner(), declaration.method(), args, depth + 1);
            } catch (Fail fail) {
                result = fallbackCall(call, args, loader, fail);
            }
            if (decisions.size() - before > 1 && isWorldCall(call, args, loader)) {
                heavyWorldCalls.merge(call.owner + "." + call.name + call.desc, decisions.size() - before, Math::max);
            }
        } else {
            trace("execution.noBytecode", call);
            result = fallbackCall(call, args, loader, new Fail("no bytecode for " + call.owner + "." + call.name));
        }

        return result;
    }

    @Nullable
    private Value optionalCall(MethodInsnNode call, @Nullable Value present, List<Value> args, int depth) {
        switch (call.name) {
            case "isPresent" -> {
                return new Value.Known(present != null ? 1 : 0);
            }
            case "isEmpty" -> {
                return new Value.Known(present == null ? 1 : 0);
            }
            case "get", "orElseThrow" -> {
                if (present == null) {
                    throw new Fail("value of an empty Optional");
                }

                return present;
            }
            case "orElse" -> {
                return present != null ? present : args.get(1);
            }
            case "orElseGet" -> {
                return present != null ? present : applyClosure(args.get(1), List.of(), depth);
            }
            case "ifPresent" -> {
                if (present != null) {
                    applyClosure(args.get(1), List.of(present), depth);
                }

                return null;
            }
            case "map" -> {
                if (present == null) {
                    return new Value.Known(Optional.empty());
                }

                Value mapped = applyClosure(args.get(1), List.of(present), depth);

                if (mapped instanceof Value.Stack stack) {
                    return stack;
                }
                if (mapped == null || mapped instanceof Value.Known) {
                    return new Value.Known(Optional.ofNullable(mapped instanceof Value.Known known ? known.value() : null));
                }

                throw new Fail("symbolic result of Optional.map");
            }
            default -> throw new Fail("symbolic Optional." + call.name);
        }
    }

    @Nullable
    private Value applyClosure(Value function, List<Value> args, int depth) {
        if (!(function instanceof Value.Closure closure)) {
            throw new Fail("symbolic Optional function");
        }

        List<Value> all = new ArrayList<>(closure.captured());

        all.addAll(args);
        return invokeHandle(closure.impl(), all, closure.loader(), depth + 1);
    }

    @Nullable
    private Value lambdaCall(Object lambda, List<Value> args, int depth) {
        LambdaResolver.Target target = LambdaResolver.resolve(lambda);

        if (target == null) {
            throw new Fail("unresolved lambda (" + LambdaResolver.reason(lambda) + ")");
        }

        List<Value> all = new ArrayList<>(target.captured().stream().map(this::wrap).toList());

        all.addAll(args.subList(1, args.size()));
        return invokeHandle(target.impl(), all, target.owner().getClassLoader(), depth + 1);
    }

    @Nullable
    private Value fallbackCall(MethodInsnNode call, List<Value> args, ClassLoader loader, Fail fail) {
        if (args.stream().anyMatch(this::holdsStack)) {
            trace("fallback.stacks", call);
            return stackArgumentCall(call, args, loader);
        }
        if (args.stream().allMatch(this::isConcrete)) {
            trace("fallback.concrete", call);
            return concreteCall(call, args, loader, false);
        }
        if (isWorldCall(call, args, loader)) {
            if (fail.isBudget()) {
                trace("fallback.opaqueBudget", call);
                retry = retry || opaque.add(call.owner + "." + call.name + call.desc);
                throw fail;
            }

            trace("fallback.worldResult", call);
            return worldResult(call, args);
        }
        if (!fail.isBudget() && call.getOpcode() != INVOKESTATIC && args.get(0) instanceof Value.Known known && known.value() != null
                && owned.containsKey(known.value()) && args.stream().skip(1).allMatch((v) -> isConcrete(v) || v instanceof Value.Num || v instanceof Value.Unknown)
                && !returnsTradeData(call)) {
            trace("fallback.uncertain", call);
            return uncertainChange(call, known.value());
        }

        trace("fallback.rethrow", call);
        throw fail;
    }

    private static boolean returnsTradeData(MethodInsnNode call) {
        Type returnType = Type.getReturnType(call.desc);

        return returnType.getSort() == Type.OBJECT && TRADE_TYPES.contains(returnType.getInternalName());
    }

    @Nullable
    private Value uncertainChange(MethodInsnNode call, @Nullable Object changed) {
        Type returnType = Type.getReturnType(call.desc);

        taint(changed);
        return returnType.getSort() == Type.VOID ? null : Value.Unknown.of(returnType, null);
    }

    private boolean isTainted(Value value) {
        return value instanceof Value.Known known && known.value() != null && tainted.contains(known.value());
    }

    private void taintOwned(List<Value> args) {
        args.forEach((v) -> taint(v instanceof Value.Known known && known.value() != null && owned.containsKey(known.value()) ? known.value() : null));
    }

    private static boolean isData(@Nullable Object value) {
        return value instanceof Collection<?> || value instanceof Map<?, ?> || value instanceof Tag || value instanceof DataComponentMap || value instanceof DataComponentPatch
                || value != null && value.getClass().isArray();
    }

    private void taint(@Nullable Object object) {
        if (object != null && tainted.add(object) && stackParts.containsKey(object)) {
            stackParts.get(object).randomTag = true;
        }
    }

    private boolean isWorldCall(MethodInsnNode call, List<Value> args, ClassLoader loader) {
        Type returnType = Type.getReturnType(call.desc);

        if (returnsTradeData(call) || returnType.getSort() == Type.OBJECT && isRandomType(returnType, loader)) {
            return false;
        }

        return args.stream().anyMatch((v) -> v instanceof Value.Unknown || v instanceof Value.Rng)
                && args.stream().allMatch((v) -> v instanceof Value.Unknown || v instanceof Value.Rng || v instanceof Value.Known);
    }

    @Nullable
    private Value worldResult(MethodInsnNode call, List<Value> args) {
        Type returnType = Type.getReturnType(call.desc);

        taintOwned(args);

        if (returnType.getSort() == Type.VOID) {
            return null;
        }
        if (call.getOpcode() == INVOKESTATIC) {
            return Value.Unknown.of(returnType, Origin.of(null, call.owner + "." + call.name + call.desc, args));
        }

        return Value.Unknown.of(returnType, Origin.of(args.get(0), call.name + call.desc, args.subList(1, args.size())));
    }

    boolean retry() {
        return retry;
    }

    @Nullable
    private Value distribute(MethodInsnNode call, List<Value> args, int[] choices, ClassLoader loader, int depth) {
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
        int outer = frozenBelow;

        frozenBelow = owned.size() + 1;

        try {
            for (int option = 0; option < first.options().size(); option++) {
                List<Value> copy = select(base, choices, option);

                if (original != null) {
                    Value.Stack branch = original.copy();

                    copy.set(stackIndex, branch);
                    branches.add(branch);
                }

                results.add(invoke(call, copy, loader, depth));
            }
        } finally {
            frozenBelow = outer;
        }

        if (original != null) {
            merge(original, branches);
        }

        return sameDraw(combine(results), first);
    }

    private List<Value> select(List<Value> args, int[] choices, int option) {
        List<Value> copy = new ArrayList<>(args);

        for (int index : choices) {
            copy.set(index, wrap(((Value.Choice) args.get(index)).options().get(option)));
        }

        return copy;
    }

    @Nullable
    private static Value sameDraw(@Nullable Value result, Value.Choice source) {
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
    private Value combine(List<Value> results) {
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

            return new Value.Num(NumberExpr.weighted(results.stream().map((r) -> new NumberExpr.WeightedEntry(1, expr(r))).toList()), kind);
        }

        throw new Fail("results cannot be combined");
    }

    @Nullable
    private Value random(MethodInsnNode call, Value.Rng rng, List<Value> args, int depth) {
        Semantic semantic = rng.legacy() ? legacySemantic(call) : sourceSemantic(call);

        if (semantic == null) {
            if (!rng.legacy() && declaration(RandomSource.class, call.name, call.desc, false) instanceof Declaration declaration) {
                return execute(declaration.owner(), declaration.method(), args, depth + 1);
            }

            throw new Fail("random method " + call.name);
        }

        boolean stub = mode == Mode.STUB;

        if (stub) {
            return stubRandom(semantic, rng, args);
        }

        return switch (semantic) {
            case BOUND -> draw(NumberExpr.uniformInt(NumberExpr.constant(0), sub(expr(args.get(1)), NumberExpr.constant(1))), Value.Kind.INT);
            case BETWEEN -> draw(uniformInt(expr(args.get(1)), expr(args.get(2))), Value.Kind.INT);
            case ORIGIN -> draw(uniformInt(expr(args.get(1)), sub(expr(args.get(2)), NumberExpr.constant(1))), Value.Kind.INT);
            case FLOAT -> draw(NumberExpr.uniformFloat(0, 1), Value.Kind.FLOAT);
            case FLOAT_BOUND -> draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, NumberExpr.constant(0), expr(args.get(1))), Value.Kind.FLOAT);
            case FLOAT_ORIGIN -> draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, expr(args.get(1)), expr(args.get(2))), Value.Kind.FLOAT);
            case DOUBLE -> draw(NumberExpr.uniformFloat(0, 1), Value.Kind.DOUBLE);
            case DOUBLE_BOUND -> draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, NumberExpr.constant(0), expr(args.get(1))), Value.Kind.DOUBLE);
            case DOUBLE_ORIGIN -> draw(NumberExpr.fn(NumberFunctions.UNIFORM_FLOAT, expr(args.get(1)), expr(args.get(2))), Value.Kind.DOUBLE);
            case BOOLEAN -> draw(NumberExpr.uniformInt(0, 1), Value.Kind.INT);
            case INT -> throw new Fail("unbounded random int");
            case LONG -> throw new Fail("random long");
            case GAUSSIAN -> throw new Fail("random gaussian");
            case FORK -> rng;
            case IGNORED -> null;
        };
    }

    private Value draw(NumberExpr expr, Value.Kind kind) {
        if (expr instanceof NumberExpr.Fn fn && fn.args().size() == 2 && fn.args().get(0) instanceof NumberExpr.Const lo && fn.args().get(1) instanceof NumberExpr.Const hi) {
            if (fn.id().equals(NumberFunctions.UNIFORM_INT) && hi.value() - lo.value() < MAX_OPTIONS) {
                drawn.put(expr, new Drawn(Draw.discrete((int) lo.value(), (int) hi.value()), 0));
            } else if (fn.id().equals(NumberFunctions.UNIFORM_FLOAT) && hi.value() > lo.value()) {
                drawn.put(expr, new Drawn(Draw.continuous(lo.value(), hi.value()), 0));
            }
        }

        return num(expr, kind);
    }

    @Nullable
    private Value stubRandom(Semantic semantic, Value.Rng rng, List<Value> args) {
        float fraction = StubRandom.fraction(pick);

        return switch (semantic) {
            case BOUND -> new Value.Known(StubRandom.bound(pick, intOf(args.get(1))));
            case BETWEEN -> new Value.Known(intOf(args.get(1)) + StubRandom.bound(pick, intOf(args.get(2)) - intOf(args.get(1)) + 1));
            case ORIGIN -> new Value.Known(intOf(args.get(1)) + StubRandom.bound(pick, intOf(args.get(2)) - intOf(args.get(1))));
            case FLOAT -> new Value.Known(fraction);
            case FLOAT_BOUND -> new Value.Known(fraction * floatOf(args.get(1)));
            case FLOAT_ORIGIN -> new Value.Known(floatOf(args.get(1)) + fraction * (floatOf(args.get(2)) - floatOf(args.get(1))));
            case DOUBLE -> new Value.Known((double) fraction);
            case DOUBLE_BOUND -> new Value.Known(fraction * doubleOf(args.get(1)));
            case DOUBLE_ORIGIN -> new Value.Known(doubleOf(args.get(1)) + fraction * (doubleOf(args.get(2)) - doubleOf(args.get(1))));
            case BOOLEAN -> new Value.Known(pick != 0 ? 1 : 0);
            case INT -> new Value.Known(pick);
            case LONG -> new Value.Known((long) pick);
            case GAUSSIAN -> new Value.Known(0D);
            case FORK -> rng;
            case IGNORED -> null;
        };
    }

    @Nullable
    private static Semantic sourceSemantic(MethodInsnNode call) {
        if (RuntimeNames.is(RuntimeNames.NEXT_INT_BOUND, call)) {
            return Semantic.BOUND;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_INT_BETWEEN, call)) {
            return Semantic.BETWEEN;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_INT_ORIGIN, call)) {
            return Semantic.ORIGIN;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_FLOAT, call)) {
            return Semantic.FLOAT;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_DOUBLE, call)) {
            return Semantic.DOUBLE;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_BOOLEAN, call)) {
            return Semantic.BOOLEAN;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_INT, call)) {
            return Semantic.INT;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_LONG, call)) {
            return Semantic.LONG;
        } else if (RuntimeNames.is(RuntimeNames.NEXT_GAUSSIAN, call)) {
            return Semantic.GAUSSIAN;
        } else if (RuntimeNames.is(RuntimeNames.FORK, call)) {
            return Semantic.FORK;
        } else if (RuntimeNames.is(RuntimeNames.SET_SEED, call) || RuntimeNames.is(RuntimeNames.CONSUME_COUNT, call)) {
            return Semantic.IGNORED;
        }

        return null;
    }

    @Nullable
    private static Semantic legacySemantic(MethodInsnNode call) {
        return switch (call.name + call.desc) {
            case "nextInt(I)I" -> Semantic.BOUND;
            case "nextInt(II)I" -> Semantic.ORIGIN;
            case "nextInt()I" -> Semantic.INT;
            case "nextFloat()F" -> Semantic.FLOAT;
            case "nextFloat(F)F" -> Semantic.FLOAT_BOUND;
            case "nextFloat(FF)F" -> Semantic.FLOAT_ORIGIN;
            case "nextDouble()D" -> Semantic.DOUBLE;
            case "nextDouble(D)D" -> Semantic.DOUBLE_BOUND;
            case "nextDouble(DD)D" -> Semantic.DOUBLE_ORIGIN;
            case "nextBoolean()Z" -> Semantic.BOOLEAN;
            case "nextLong()J" -> Semantic.LONG;
            case "nextGaussian()D" -> Semantic.GAUSSIAN;
            case "setSeed(J)V" -> Semantic.IGNORED;
            default -> null;
        };
    }

    private static boolean isRandomType(Type type, ClassLoader loader) {
        try {
            Class<?> cls = load(type.getInternalName(), loader);

            return RandomSource.class.isAssignableFrom(cls) || Random.class.isAssignableFrom(cls);
        } catch (Fail fail) {
            return false;
        }
    }

    private static boolean isLegacyRandom(Type type, ClassLoader loader) {
        return Random.class.isAssignableFrom(load(type.getInternalName(), loader));
    }

    @Nullable
    private Value stackCall(MethodInsnNode call, Value.Stack stack, List<Value> args, ClassLoader loader) {
        if (RuntimeNames.is(RuntimeNames.STACK_SET_COUNT, call)) {
            stack.count = expr(args.get(1));
            return null;
        } else if (RuntimeNames.is(RuntimeNames.STACK_GROW, call)) {
            stack.count = add(stack.count, expr(args.get(1)));
            return null;
        } else if (RuntimeNames.is(RuntimeNames.STACK_SHRINK, call)) {
            stack.count = sub(stack.count, expr(args.get(1)));
            return null;
        } else if (RuntimeNames.is(RuntimeNames.STACK_GET_COUNT, call)) {
            return num(stack.count, Value.Kind.INT);
        } else if (RuntimeNames.is(RuntimeNames.STACK_GET_ITEM, call)) {
            List<Object> items = stack.options.stream().<Object>map(ItemStack::getItem).distinct().toList();

            return items.size() == 1 ? new Value.Known(items.get(0)) : new Value.Choice(items);
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY, call)) {
            return stack.copy();
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY_WITH_COUNT, call)) {
            Value.Stack copy = stack.copy();

            copy.count = expr(args.get(1));
            return copy;
        } else if (RuntimeNames.is(RuntimeNames.STACK_IS_EMPTY, call)) {
            NumberInterval bounds = stack.count.bounds();

            if (stack.options.stream().noneMatch(ItemStack::isEmpty) && !bounds.isUnknown() && bounds.lo() >= 1) {
                return new Value.Known(0);
            }
            if (stack.count instanceof NumberExpr.Const c && c.value() <= 0) {
                return new Value.Known(1);
            }

            throw new Fail("stack emptiness");
        }

        return onTemplates(call, stack, args, 0, loader);
    }

    @Nullable
    private Value costCall(MethodInsnNode call, Value.Stack stack, List<Value> args, ClassLoader loader) {
        if (RuntimeNames.is(RuntimeNames.COST_ITEM_STACK, call)) {
            return stack;
        } else if (RuntimeNames.is(RuntimeNames.COST_COUNT, call)) {
            return num(stack.count, Value.Kind.INT);
        } else if (!isObject(Type.getReturnType(call.desc), ITEM_COST)) {
            throw new Fail("item cost call " + call.name);
        } else if (!args.stream().skip(1).allMatch(this::isConcrete) || args.stream().anyMatch(this::isTainted)) {
            return unknownStackChange(call, stack, "item cost call with symbolic arguments");
        }

        List<ItemStack> templates = new ArrayList<>();

        for (ItemStack template : stack.options) {
            List<Value> copy = new ArrayList<>(args);

            copy.set(0, new Value.Known(new ItemCost(template.getItemHolder(), 1, DataComponentPredicate.EMPTY)));

            if (!(concreteCall(call, copy, loader, true) instanceof Value.Known known) || !(known.value() instanceof ItemCost cost)) {
                throw new Fail("item cost call " + call.name);
            }

            templates.add(cost.itemStack());
        }

        Value.Stack result = new Value.Stack(templates, stack.count, stack.randomTag);

        result.destination = stack.destination;
        result.decoration = stack.decoration;
        return result;
    }

    @Nullable
    private Value knownStackCall(MethodInsnNode call, ItemStack real, List<Value> args, ClassLoader loader) {
        if (RuntimeNames.is(RuntimeNames.STACK_GET_COUNT, call)) {
            return new Value.Known(real.getCount());
        } else if (RuntimeNames.is(RuntimeNames.STACK_GET_ITEM, call)) {
            return wrap(real.getItem());
        } else if (RuntimeNames.is(RuntimeNames.STACK_IS_EMPTY, call)) {
            return new Value.Known(real.isEmpty() ? 1 : 0);
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY, call)) {
            return stackOf(real);
        } else if (RuntimeNames.is(RuntimeNames.STACK_COPY_WITH_COUNT, call)) {
            Value.Stack copy = stackOf(real);

            copy.count = expr(args.get(1));
            return copy;
        }

        return stackCall(call, shadow(real), shadowed(args), loader);
    }

    @Nullable
    private Value stackArgumentCall(MethodInsnNode call, List<Value> args, ClassLoader loader) {
        List<Value> shadowedArgs = shadowed(args);
        List<Value.Stack> stacks = shadowedArgs.stream().filter(Value.Stack.class::isInstance).map(Value.Stack.class::cast).toList();
        Map<ItemStack, ItemStack> fixed = new IdentityHashMap<>();

        if (stacks.size() > 1) {
            Value.Stack kept = stacks.stream().filter((s) -> !isFixed(s)).findFirst().orElse(stacks.get(0));

            shadowedArgs = shadowedArgs.stream().map((v) -> v instanceof Value.Stack s && s != kept && isFixed(s) ? fixedStack(s, fixed) : v).toList();
        }

        Value result = singleStackArgumentCall(call, shadowedArgs, loader);

        if (fixed.entrySet().stream().anyMatch((e) -> !ItemStack.matches(e.getKey(), e.getValue()))) {
            throw new Fail("stack argument changed");
        }

        return result;
    }

    private static boolean isFixed(Value.Stack stack) {
        return stack.options.size() == 1 && stack.count instanceof NumberExpr.Const && !stack.randomTag && stack.destination == null && stack.decoration == null;
    }

    private Value fixedStack(Value.Stack stack, Map<ItemStack, ItemStack> fixed) {
        ItemStack real = stack.options.get(0).copy();

        real.setCount((int) ((NumberExpr.Const) stack.count).value());
        own(real);
        fixed.put(real, real.copy());
        return new Value.Known(real);
    }

    @Nullable
    private Value singleStackArgumentCall(MethodInsnNode call, List<Value> shadowedArgs, ClassLoader loader) {
        int index = -1;

        for (int i = 0; i < shadowedArgs.size(); i++) {
            if (shadowedArgs.get(i) instanceof Value.Stack) {
                if (index >= 0) {
                    throw new Fail("several stack arguments");
                }

                index = i;
            }
        }

        Value.Stack stack = (Value.Stack) shadowedArgs.get(index);

        if (shadowedArgs.stream().filter((v) -> v != stack).allMatch(this::isConcrete)) {
            return onTemplates(call, stack, shadowedArgs, index, loader);
        }

        return unknownStackChange(call, stack, "stack with symbolic arguments");
    }

    @Nullable
    private Value unknownStackChange(MethodInsnNode call, Value.Stack stack, String reason) {
        Type returnType = Type.getReturnType(call.desc);

        if (returnType.getSort() == Type.VOID) {
            stack.randomTag = true;
            return null;
        }
        if (isObject(returnType, ITEM_STACK)) {
            stack.randomTag = true;
            return stack.copy();
        }
        if (isObject(returnType, ITEM_COST)) {
            Value.Stack copy = stack.copy();

            copy.randomTag = true;
            return copy;
        }
        if (isComponentWrite(call)) {
            stack.randomTag = true;
            return Value.Unknown.of(returnType, null);
        }

        throw new Fail(reason);
    }

    private static boolean isComponentWrite(MethodInsnNode call) {
        if (call.owner.equals(ITEM_STACK) && RuntimeNames.STACK_SET != null && RuntimeNames.STACK_UPDATE != null) {
            return call.name.equals(RuntimeNames.STACK_SET.name()) || call.name.equals(RuntimeNames.STACK_UPDATE.name());
        }

        return RuntimeNames.is(RuntimeNames.UPDATE_ENCHANTMENTS, call);
    }

    @Nullable
    private Value onTemplates(MethodInsnNode call, Value.Stack stack, List<Value> args, int index, ClassLoader loader) {
        if (!args.stream().filter((v) -> v != stack).allMatch(this::isConcrete)) {
            return unknownStackChange(call, stack, "stack call with symbolic arguments");
        }

        if (args.stream().anyMatch(this::isTainted)) {
            return unknownStackChange(call, stack, "stack call with uncertain arguments");
        }

        NumberExpr.Const constant = stack.count instanceof NumberExpr.Const c ? c : null;
        List<Object> results = new ArrayList<>();
        boolean returnsTemplate = true;

        for (ItemStack template : stack.options) {
            template.setCount(constant != null ? (int) constant.value() : 1);

            List<Value> copy = new ArrayList<>(args);

            copy.set(index, new Value.Known(template));

            Value result = concreteCall(call, copy, loader, true);

            if (constant == null && template.getCount() != 1) {
                throw new Fail("stack count changed by an unknown call");
            }
            if (result instanceof Value.Known known && known.value() != template) {
                returnsTemplate = false;
                results.add(known.value());

                if (known.value() != null && !(known.value() instanceof ItemStack)) {
                    stackParts.put(known.value(), stack);
                }
            } else if (result != null && !(result instanceof Value.Known)) {
                return unknownStackChange(call, stack, "symbolic result of a stack call");
            }
        }

        if (constant != null && !stack.options.isEmpty()) {
            stack.count = NumberExpr.constant(stack.options.get(0).getCount());
        }
        if (Type.getReturnType(call.desc).getSort() == Type.VOID) {
            return null;
        }
        if (returnsTemplate) {
            return stack;
        }

        results.stream().filter(Objects::nonNull).forEach(this::own);

        if (results.stream().allMatch(ItemStack.class::isInstance)) {
            return combine(results.stream().map(this::wrap).toList());
        }

        return results.size() == 1 ? wrap(results.get(0)) : new Value.Choice(results);
    }

    private void own(Object object) {
        owned.putIfAbsent(object, owned.size() + 1);
    }

    private boolean isFrozen(Object object) {
        Integer order = owned.get(object);

        return order != null && order < frozenBelow;
    }

    private Value.Stack shadow(ItemStack real) {
        Value.Stack shadow = shadows.get(real);

        if (shadow == null) {
            shadow = stackOf(real);
            shadows.put(real, shadow);

            for (Frame frame : frames) {
                frame.replace((v) -> v instanceof Value.Known known && known.value() == real, shadows.get(real));
            }
        }

        return shadow;
    }

    private List<Value> shadowed(List<Value> args) {
        return args.stream().map((v) -> v instanceof Value.Known known && known.value() instanceof ItemStack real && !owned.containsKey(real) ? shadow(real) : v).toList();
    }

    private boolean holdsStack(Value value) {
        return value instanceof Value.Stack;
    }

    private Value.Stack stackOf(ItemStack real) {
        return new Value.Stack(List.of(real.copy()), NumberExpr.constant(real.getCount()), false);
    }

    private Value limit(Value.Stack stack) {
        if (stack.options.size() > MAX_OPTIONS) {
            throw new Fail("too many options");
        }

        return stack;
    }

    @Nullable
    private Object keyedSource(Object listing) {
        for (Object keyed : listingKeys.keySet()) {
            List<?> elements = keyed instanceof Object[] array ? Arrays.asList(array) : (List<?>) keyed;

            if (elements.stream().anyMatch((e) -> e == listing)) {
                return keyed;
            }
        }

        return null;
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
    private List<Object> bound(List<Value> args) {
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

    private BoundOffer boundOffer(Object element, MethodInsnNode call, List<Object> args) {
        BoundOffer offer = new BoundOffer(element, call.owner, call.name, call.desc, call.itf, args);

        return boundOffers.computeIfAbsent(element, (e) -> new ArrayList<>()).stream().filter(offer::equals).findFirst().orElseGet(() -> {
            boundOffers.get(element).add(offer);
            return offer;
        });
    }

    private static boolean isKeyedList(@Nullable Object value) {
        return value instanceof Object[] array && array.length > 0 || value instanceof List<?> list && !list.isEmpty();
    }

    @Nullable
    private Integer poolKey(List<Object> listings) {
        for (Map.Entry<Object, Integer> entry : listingKeys.entrySet()) {
            List<?> keyed = entry.getKey() instanceof Object[] array ? Arrays.asList(array) : (List<?>) entry.getKey();

            if (keyed.equals(listings)) {
                return entry.getValue();
            }
        }

        return null;
    }

    @Nullable
    private Value offersCall(MethodInsnNode call, Value.Offers offers, List<Value> args, int depth) {
        if (call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Pick pickValue) {
            Integer index = offers.picks.get(pickValue.pool());

            NumberExpr drawCount = drawCounts.get(pickValue.pool());

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
                && known.value() instanceof Collection<?> collection && !tainted.contains(collection)) {
            collection.forEach((offer) -> offers.entries.add(new Value.Offers.Single(wrap(offer))));
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

    private Value addPicked(Value.Offers offers, Value.Choice choice) {
        Integer index = offers.picks.get(choice.draw());
        NumberExpr drawCount = drawCounts.get(choice.draw());

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

    private boolean isSymbolicOffer(Value value) {
        return value instanceof Value.Pick || value instanceof Value.Offer || value instanceof Value.Choice choice && choice.draw() != null
                && pools.contains(choice.draw()) && !choice.options().isEmpty() && choice.options().stream().allMatch(MerchantOffer.class::isInstance);
    }

    @Nullable
    private Value.Offers offerList(Value value, boolean create) {
        if (value instanceof Value.Offers offers) {
            return offers;
        }
        if (!(value instanceof Value.Known known) || !(known.value() instanceof Collection<?> collection) || !owned.containsKey(collection)) {
            return null;
        }
        if (offerLists.containsKey(collection) || !create) {
            return offerLists.get(collection);
        }
        if (isFrozen(collection) || !collection.stream().allMatch(MerchantOffer.class::isInstance)) {
            throw new Fail("offers in a collection");
        }

        Value.Offers offers = new Value.Offers();

        collection.forEach((offer) -> offers.entries.add(new Value.Offers.Single(wrap(offer))));
        collection.clear();
        offerLists.put(collection, offers);
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

    private Value construct(String type, String desc, List<Value> args, ClassLoader loader) {
        if (desc.equals("()V") && isPlainOffers(load(type, loader))) {
            trace("constructor.offers", type, desc);
            return new Value.Offers();
        }
        if (type.equals(ITEM_STACK)) {
            trace("constructor.stack", type, desc);
            return constructStack(ItemStack.class, desc, args);
        }
        if (type.equals(ITEM_COST)) {
            trace("constructor.itemCost", type, desc);
            return constructStack(ItemCost.class, desc, args);
        }
        if (type.equals(MERCHANT_OFFER) || !args.stream().allMatch(this::isConcrete) && isForwardingOffer(load(type, loader), desc)) {
            trace("constructor.offer", type, desc);
            return constructOffer(desc, args);
        }
        List<Value> drawn = args.stream().map(Interpreter::drawnInt).toList();
        int[] choices = choiceIndices(drawn);

        if (choices.length > 0) {
            Value.Choice first = (Value.Choice) drawn.get(choices[0]);
            List<Value> results = new ArrayList<>();

            for (int option = 0; option < first.options().size(); option++) {
                results.add(construct(type, desc, select(drawn, choices, option), loader));
            }

            trace("constructor.perChoice", type, desc);
            return Objects.requireNonNull(sameDraw(combine(results), first));
        }
        if (args.stream().anyMatch(Value.Unknown.class::isInstance) && args.stream().allMatch((v) -> v instanceof Value.Unknown || isConcrete(v))) {
            taintOwned(args);
            trace("constructor.unknown", type, desc);
            return Value.Unknown.of(Type.getObjectType(type), Origin.of(null, type + "." + CONSTRUCTOR + desc, args));
        }
        if (!args.stream().allMatch(this::isConcrete)) {
            trace("constructor.symbolic.fail", type, desc);
            throw new Fail("symbolic constructor arguments");
        }
        if (args.stream().anyMatch(this::isSelection)) {
            trace("constructor.guard.fail", type, desc);
            throw new Fail("random selection passed to a call");
        }

        Class<?> owner = load(type, loader);
        Constructor<?> constructor = constructor(owner, desc);

        try {
            constructor.setAccessible(true);

            Object created = constructor.newInstance(javaArgs(args, constructor.getParameterTypes(), 0));

            own(created);

            if (args.stream().anyMatch(this::isTainted)) {
                taint(created);
            }

            trace("constructor.real", type, desc);
            return wrap(created);
        } catch (Throwable e) {
            trace("constructor.real.fail", type, desc);
            throw new Fail("constructor failed");
        }
    }

    @NotNull
    private Value constructStack(Class<?> type, String desc, List<Value> args) {
        Type[] params = Type.getArgumentTypes(desc);

        if (type == ItemCost.class && params.length == 4 && args.get(3) instanceof Value.Stack shown) {
            Value.Stack cost = shown.copy();

            cost.count = expr(args.get(1));
            return cost;
        }

        NumberExpr count = params.length >= 2 && params[1].getSort() == Type.INT ? expr(args.get(1)) : NumberExpr.constant(1);
        List<Object> items;

        if (args.get(0) instanceof Value.Choice choice) {
            items = choice.options();
        } else {
            Object item = concrete(args.get(0));

            if (item == null) {
                throw new Fail("null item");
            }

            items = List.of(item);
        }
        Constructor<?> constructor = constructor(type, desc);
        List<ItemStack> templates = new ArrayList<>();

        for (Object item : items) {
            List<Value> copy = new ArrayList<>(args);

            copy.set(0, new Value.Known(item));

            if (params.length >= 2 && params[1].getSort() == Type.INT) {
                copy.set(1, new Value.Known(count instanceof NumberExpr.Const c ? (int) c.value() : 1));
            }
            if (!copy.stream().allMatch(this::isConcrete)) {
                throw new Fail("symbolic stack argument");
            }

            try {
                constructor.setAccessible(true);
                Object created = constructor.newInstance(javaArgs(copy, constructor.getParameterTypes(), 0));

                templates.add(created instanceof ItemCost cost ? cost.itemStack() : (ItemStack) created);
            } catch (Throwable e) {
                throw new Fail("stack constructor failed");
            }
        }

        return limit(new Value.Stack(templates, count, false));
    }

    private Value constructOffer(String desc, List<Value> args) {
        Type[] params = Type.getArgumentTypes(desc);

        if (params.length >= 5 && isObject(params[0], ITEM_COST)) {
            boolean hasCostB = isObject(params[1], OPTIONAL);
            int resultIndex = hasCostB ? 2 : 1;
            List<Value> numbers = args.subList(resultIndex + 1, args.size());
            Value costA = cost(args.get(0));
            Value costB = hasCostB ? optionalCost(args.get(1)) : new Value.Known(ItemStack.EMPTY);
            Value result = args.get(resultIndex);

            if (isObject(params[resultIndex], ITEM_STACK) && numbers.size() == 3) {
                return new Value.Offer(costA, costB, result, intOf(numbers.get(0)), intOf(numbers.get(1)), floatOf(numbers.get(2)));
            }
            if (isObject(params[resultIndex], ITEM_STACK) && numbers.size() >= 4) {
                return new Value.Offer(costA, costB, result, intOf(numbers.get(1)), intOf(numbers.get(2)), floatOf(numbers.get(3)));
            }
        }
        if (args.stream().allMatch(this::isConcrete)) {
            Constructor<?> constructor = constructor(MerchantOffer.class, desc);

            try {
                return wrap(constructor.newInstance(javaArgs(args, constructor.getParameterTypes(), 0)));
            } catch (Throwable e) {
                throw new Fail("offer constructor failed");
            }
        }

        throw new Fail("offer constructor");
    }

    private static boolean isForwardingOffer(Class<?> type, String desc) {
        ClassNode node = type != MerchantOffer.class && MerchantOffer.class.isAssignableFrom(type) ? classNode(type) : null;
        MethodNode init = node != null ? findMethod(node, CONSTRUCTOR, desc) : null;

        if (init == null) {
            return false;
        }

        List<AbstractInsnNode> insns = Arrays.stream(init.instructions.toArray()).filter((i) -> i.getOpcode() >= 0).toList();
        Type[] params = Type.getArgumentTypes(desc);

        if (insns.size() != params.length + 3 || !(insns.get(0) instanceof VarInsnNode self) || self.getOpcode() != ALOAD || self.var != 0) {
            return false;
        }

        int slot = 1;

        for (int i = 0; i < params.length; i++) {
            if (!(insns.get(i + 1) instanceof VarInsnNode load) || load.getOpcode() != params[i].getOpcode(ILOAD) || load.var != slot) {
                return false;
            }

            slot += params[i].getSize();
        }

        return insns.get(params.length + 1) instanceof MethodInsnNode call && call.getOpcode() == INVOKESPECIAL && call.owner.equals(MERCHANT_OFFER)
                && call.name.equals(CONSTRUCTOR) && call.desc.equals(desc) && insns.get(params.length + 2).getOpcode() == RETURN;
    }

    private static boolean isObject(Type type, String internalName) {
        return type.getSort() == Type.OBJECT && type.getInternalName().equals(internalName);
    }

    private static Value cost(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof ItemCost cost) {
            return new Value.Known(cost.itemStack());
        }
        if (value instanceof Value.Choice choice && choice.options().stream().allMatch(ItemCost.class::isInstance)) {
            return new Value.Choice(choice.options().stream().<Object>map((o) -> ((ItemCost) o).itemStack()).toList(), choice.draw());
        }

        return value;
    }

    private static Value optionalCost(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Optional<?> optional) {
            return optional.isPresent() ? cost(new Value.Known(optional.get())) : new Value.Known(ItemStack.EMPTY);
        }

        return cost(value);
    }

    private void replace(Value.Uninit uninit, Value created) {
        for (Frame frame : frames) {
            frame.replace((v) -> v == uninit, created);
        }
    }

    private Value invokeDynamic(InvokeDynamicInsnNode indy, Frame f, ClassLoader loader) {
        Value[] captured = new Value[Type.getArgumentTypes(indy.desc).length];

        for (int i = captured.length - 1; i >= 0; i--) {
            captured[i] = f.pop();
        }

        if (indy.bsm.getOwner().equals(LAMBDA_METAFACTORY) && indy.bsmArgs.length >= 3 && indy.bsmArgs[1] instanceof Handle impl) {
            Class<?> iface = load(Type.getReturnType(indy.desc).getInternalName(), loader);

            return new Value.Closure(iface, indy.name, ((Type) indy.bsmArgs[0]).getDescriptor(), impl, Arrays.asList(captured), loader);
        }
        if (indy.bsm.getOwner().equals(STRING_CONCAT_FACTORY) && indy.bsm.getName().equals("makeConcatWithConstants")) {
            if (!Arrays.stream(captured).allMatch((v) -> v instanceof Value.Known)) {
                return Value.UNKNOWN;
            }

            String recipe = (String) indy.bsmArgs[0];
            Type[] types = Type.getArgumentTypes(indy.desc);
            StringBuilder builder = new StringBuilder();
            int arg = 0;
            int constant = 1;

            for (char c : recipe.toCharArray()) {
                if (c == '\u0001') {
                    builder.append(toJava(captured[arg], javaType(types[arg])));
                    arg++;
                } else if (c == '\u0002') {
                    builder.append(indy.bsmArgs[constant++]);
                } else {
                    builder.append(c);
                }
            }

            return new Value.Known(builder.toString());
        }

        throw new Fail("invokedynamic " + indy.bsm.getOwner());
    }

    @NotNull
    private Object materialize(Value.Closure closure) {
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

            all.add(toJava(captured.get(i), param >= 0 && param < params.length ? javaType(params[param]) : Object.class));
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

    private static Class<?> javaType(Type type) {
        return switch (type.getSort()) {
            case Type.BOOLEAN -> boolean.class;
            case Type.CHAR -> char.class;
            case Type.BYTE -> byte.class;
            case Type.SHORT -> short.class;
            case Type.INT -> int.class;
            case Type.LONG -> long.class;
            case Type.FLOAT -> float.class;
            case Type.DOUBLE -> double.class;
            default -> Object.class;
        };
    }

    @Nullable
    private Value jdkCall(MethodInsnNode call, Class<?> declaring, List<Value> args, ClassLoader loader) {
        boolean isStatic = call.getOpcode() == INVOKESTATIC;
        Type returnType = Type.getReturnType(call.desc);

        if (isStatic && call.owner.equals("java/util/Objects") && call.name.startsWith("requireNonNull")) {
            if (isNull(args.get(0))) {
                trace("jdk.requireNonNull.fail", call);
                throw new Fail("requireNonNull");
            }

            trace("jdk.requireNonNull", call);
            return args.get(0);
        }
        if (isStatic && call.owner.equals("java/util/Objects") && (call.name.equals("isNull") || call.name.equals("nonNull"))) {
            trace("jdk.isNull", call);
            return new Value.Known(isNull(args.get(0)) == call.name.equals("isNull") ? 1 : 0);
        }
        if (args.stream().skip(isStatic ? 0 : 1).anyMatch(this::isSelection)) {
            trace("jdk.guard.fail", call);
            throw new Fail("random selection passed to a call");
        }
        if (isStatic && call.owner.equals("java/util/concurrent/ThreadLocalRandom") && call.name.equals("current")) {
            trace("jdk.threadLocalRandom", call);
            return new Value.Rng(true);
        }
        if (isStatic && (call.owner.equals("java/lang/Math") || call.owner.equals("java/lang/StrictMath")) && args.stream().anyMatch((v) -> v instanceof Value.Num)) {
            trace("jdk.math", call);
            return math(call, args, returnType);
        }
        if (isStatic && call.name.equals("valueOf") && args.size() == 1 && args.get(0) instanceof Value.Num num) {
            trace("jdk.valueOf", call);
            return call.owner.equals("java/lang/String") ? Value.UNKNOWN : num;
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && call.name.equals("get")
                && call.desc.equals("(I)Ljava/lang/Object;") && args.get(1) instanceof Value.Num num) {
            trace("jdk.listPick", call);
            return pick(list, num);
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof List<?> list && owned.containsKey(list)
                && call.name.equals("remove") && call.desc.equals("(I)Ljava/lang/Object;")
                && (args.get(1) instanceof Value.Num || removed.containsKey(list) || merchant != null && list.size() == 1)) {
            trace("jdk.removeRandom", call);
            return removeRandom(list, args.get(1));
        }
        if (isStatic && call.owner.equals("java/util/Collections") && ORDER_ONLY.contains(call.name) && args.get(0) instanceof Value.Offers) {
            trace("jdk.orderOffers", call);
            return null;
        }
        if (isStatic && call.owner.equals("java/util/Collections") && call.name.equals("shuffle") && args.get(0) instanceof Value.Known known
                && known.value() instanceof List<?> list && owned.containsKey(list) && list.size() <= MAX_OPTIONS) {
            if (!list.isEmpty()) {
                shuffled.put(list, new Value.Draws(list, new ArrayList<>(list), list.size()));
                pools.add(list);
            }

            trace("jdk.shuffle", call);
            return null;
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && shuffled.containsKey(known.value())) {
            Value.Draws draws = shuffled.get(known.value());

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
                        NumberExpr to = expr(args.get(2));
                        Object view = new ArrayList<>();

                        if (args.get(2) instanceof Value.Num) {
                            drawCounts.put(draws.pool, withoutCap(to, draws.left));
                        }

                        own(view);
                        shuffled.put(view, new Value.Draws(draws.pool, draws.options, Math.min(draws.left, (int) Math.floor(to.bounds().hi()))));
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
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() instanceof Set<?> set && owned.containsKey(set)
                && call.name.equals("add") && call.desc.equals("(Ljava/lang/Object;)Z") && args.get(1) instanceof Value.Num num) {
            trace("jdk.addRandom", call);
            return addRandom(set, num);
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && randomSets.containsKey(known.value())) {
            int[] draws = randomSets.get(known.value());

            trace("jdk.randomSet", call);
            return switch (call.name + call.desc) {
                case "size()I" -> new Value.Known(draws[1]);
                case "iterator()Ljava/util/Iterator;" -> new Value.Draws(known.value(), IntStream.range(0, draws[0]).boxed().map(Object.class::cast).toList(), draws[1]);
                default -> throw new Fail("change under a random choice");
            };
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && removed.containsKey(known.value())) {
            int remaining = ((List<?>) known.value()).size() - removed.get(known.value());

            trace("jdk.removed", call);
            return switch (call.name + call.desc) {
                case "size()I" -> new Value.Known(remaining);
                case "isEmpty()Z" -> new Value.Known(remaining == 0 ? 1 : 0);
                default -> throw new Fail("change under a random choice");
            };
        }
        if (!isStatic && args.get(0) instanceof Value.Known known && known.value() != null && owned.containsKey(known.value())
                && args.stream().skip(1).anyMatch((v) -> v instanceof Value.Unknown || v instanceof Value.Num)
                && args.stream().skip(1).allMatch((v) -> isConcrete(v) || v instanceof Value.Unknown || v instanceof Value.Num) && !returnsTradeData(call)) {
            trace("jdk.uncertain", call);
            return uncertainChange(call, READ_ONLY.contains(call.name) ? null : known.value());
        }
        if (isStatic && STATIC_SAFE.contains(call.owner) && !STATIC_RANDOM.contains(call.name) && returnType.getSort() != Type.VOID
                && args.stream().anyMatch(Value.Unknown.class::isInstance) && args.stream().allMatch((v) -> v instanceof Value.Unknown || isScalar(v))) {
            trace("jdk.staticUnknown", call);
            return Value.Unknown.of(returnType, null);
        }
        if (!args.stream().allMatch(this::isConcrete)) {
            trace("jdk.symbolic.fail", call);
            throw new Fail("symbolic argument of " + call.owner + "." + call.name);
        }
        if (isStatic) {
            if (!STATIC_SAFE.contains(call.owner) || STATIC_RANDOM.contains(call.name)) {
                trace("jdk.static.fail", call);
                throw new Fail("JDK call " + call.owner + "." + call.name);
            }

            trace("jdk.static", call);
            return concreteCall(call, args, loader, false);
        }

        Object receiver = ((Value.Known) args.get(0)).value();
        boolean ownedReceiver = owned.containsKey(receiver) || receiver instanceof BaseStream<?, ?>;

        if (READ_ONLY.contains(call.name) && receiver != null && tainted.contains(receiver)) {
            trace("jdk.taintedRead.fail", call);
            throw new Fail("uncertain contents of " + declaring.getName());
        }

        if (!ownedReceiver && !READ_ONLY.contains(call.name)) {
            trace("jdk.notOwned.fail", call);
            throw new Fail("JDK call " + declaring.getName() + "." + call.name);
        }
        if (!READ_ONLY.contains(call.name) && receiver != null && isFrozen(receiver)) {
            trace("jdk.frozen.fail", call);
            throw new Fail("change under a random choice");
        }

        trace("jdk.real", call);
        Value result = concreteCall(call, args, loader, false);

        if (result instanceof Value.Known known && known.value() != null && (ownedReceiver || FRESH_RESULTS.contains(call.name))) {
            own(known.value());
        }
        if (!READ_ONLY.contains(call.name) && args.stream().skip(1).anyMatch(this::isTainted)) {
            taint(receiver);
        }

        return result;
    }

    @Nullable
    private Value lookup(MethodInsnNode call, Class<?> declaring, Map<?, ?> map, Origin key, List<Value> args, ClassLoader loader) {
        List<Object> candidates = new ArrayList<>(map.keySet());

        candidates.sort(Comparator.comparing(String::valueOf));

        for (Object candidate : candidates) {
            if (candidate != null && decide(Decision.Kind.EQUALS, key, candidate, "map lookup by a value from the trader or world")) {
                List<Value> concrete = new ArrayList<>(args);

                concrete.set(1, new Value.Known(candidate));
                return jdkCall(call, declaring, concrete, loader);
            }
        }

        return switch (call.name) {
            case "get" -> NULL;
            case "getOrDefault" -> args.get(2);
            default -> new Value.Known(0);
        };
    }

    private Value math(MethodInsnNode call, List<Value> args, Type returnType) {
        Value.Kind kind = kindOf(returnType);

        return switch (call.name) {
            case "min" -> num(NumberExpr.min(expr(args.get(0)), expr(args.get(1))), kind);
            case "max" -> num(NumberExpr.max(expr(args.get(0)), expr(args.get(1))), kind);
            case "abs" -> num(NumberExpr.fn(NumberFunctions.ABS, expr(args.get(0))), kind);
            case "floor" -> num(NumberExpr.fn(NumberFunctions.FLOOR, expr(args.get(0))), kind);
            case "ceil" -> num(NumberExpr.fn(NumberFunctions.CEIL, expr(args.get(0))), kind);
            case "round" -> num(NumberExpr.fn(NumberFunctions.ROUND, expr(args.get(0))), kind);
            case "sqrt" -> num(NumberExpr.fn(NumberFunctions.SQRT, expr(args.get(0))), kind);
            case "pow" -> num(NumberExpr.fn(NumberFunctions.POW, expr(args.get(0)), expr(args.get(1))), kind);
            default -> throw new Fail("Math." + call.name);
        };
    }

    private Value pick(List<?> list, Value.Num index) {
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

    private Value removeRandom(List<?> list, Value index) {
        int remaining = list.size() - removed.getOrDefault(list, 0);
        NumberInterval bounds = expr(index).bounds();

        if (remaining <= 0 || bounds.lo() != 0 || bounds.hi() != remaining - 1 || list.size() > MAX_OPTIONS) {
            throw new Fail("removal of a random element");
        }

        removed.merge(list, 1, Integer::sum);
        pools.add(list);
        return new Value.Choice(new ArrayList<>(list), list);
    }

    private static NumberExpr withoutCap(NumberExpr count, int size) {
        if (count instanceof NumberExpr.Fn fn && fn.id().equals(NumberFunctions.MIN)) {
            List<NumberExpr> args = fn.args().stream().filter((a) -> !(a instanceof NumberExpr.Const c && c.value() >= size)).map((a) -> withoutCap(a, size)).toList();

            return args.size() == 1 ? args.get(0) : NumberExpr.fn(NumberFunctions.MIN, args.toArray(new NumberExpr[0]));
        }

        return count;
    }

    private Value addRandom(Set<?> set, Value.Num index) {
        NumberInterval bounds = index.expr().bounds();
        int bound = (int) bounds.hi() + 1;
        int[] draws = randomSets.computeIfAbsent(set, (s) -> new int[]{bound, 0});

        if (!set.isEmpty() || bounds.lo() != 0 || bounds.hi() != Math.rint(bounds.hi()) || draws[0] != bound || bound > MAX_OPTIONS) {
            throw new Fail("random set element");
        }

        draws[1] = Math.min(draws[1] + 1, bound);
        pools.add(set);
        return new Value.Known(1);
    }

    private static Value drawnInt(Value value) {
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

    private Value unbox(MethodInsnNode call, Value.Num num) {
        return switch (call.name) {
            case "intValue", "shortValue", "byteValue" -> new Value.Num(num.expr(), Value.Kind.INT);
            case "longValue" -> new Value.Num(num.expr(), Value.Kind.LONG);
            case "floatValue" -> new Value.Num(num.expr(), Value.Kind.FLOAT);
            case "doubleValue" -> new Value.Num(num.expr(), Value.Kind.DOUBLE);
            default -> throw new Fail("number method " + call.name);
        };
    }

    @Nullable
    private Value concreteCall(MethodInsnNode call, List<Value> args, ClassLoader loader, boolean ownedResult) {
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
            own(result);
        }

        return wrap(result, method.getReturnType());
    }

    private Object[] javaArgs(List<Value> args, Class<?>[] types, int offset) {
        Object[] result = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            result[i] = toJava(args.get(i + offset), types[i]);
        }

        return result;
    }

    @Nullable
    private Object toJava(Value value, Class<?> type) {
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
    private static Object concrete(Value value) {
        if (value instanceof Value.Known known) {
            return known.value();
        }

        throw new Fail("symbolic value");
    }

    private static boolean isLogging(String owner) {
        return owner.startsWith("org/slf4j/") || owner.startsWith("org/apache/logging/log4j/") || owner.startsWith("java/util/logging/");
    }

    private boolean isSelection(Value value) {
        return value instanceof Value.Known known && known.value() != null
                && (shuffled.containsKey(known.value()) || randomSets.containsKey(known.value()) || removed.containsKey(known.value()));
    }

    private boolean isConcrete(Value value) {
        return value instanceof Value.Known || value instanceof Value.Closure closure && closure.captured().stream().allMatch(this::isConcrete)
                || value instanceof Value.Rng && mode == Mode.STUB;
    }

    private static boolean isScalar(Value value) {
        return value instanceof Value.Known known && (known.value() == null || known.value() instanceof Number || known.value() instanceof String
                || known.value() instanceof Boolean || known.value() instanceof Character);
    }

    private Value wrap(@Nullable Object value, Class<?> type) {
        if (!type.isPrimitive()) {
            return wrap(value);
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

    Value wrap(@Nullable Object value) {
        if (value instanceof RandomSource) {
            return new Value.Rng(false);
        } else if (value instanceof Random) {
            return new Value.Rng(true);
        } else if (value != null && shadows.containsKey(value)) {
            return shadows.get(value);
        }

        return new Value.Known(value);
    }

    private Value getStatic(FieldInsnNode insn, ClassLoader loader) {
        if (statics.containsKey(insn.owner + "." + insn.name)) {
            return statics.get(insn.owner + "." + insn.name);
        }

        Field field = field(load(insn.owner, loader, true), insn.name);

        try {
            return wrap(field.get(null), field.getType());
        } catch (Throwable e) {
            throw new Fail("static field " + insn.owner + "." + insn.name);
        }
    }

    private Value getField(FieldInsnNode insn, Value receiver, ClassLoader loader) {
        if (receiver instanceof Value.Unknown && RuntimeNames.is(RuntimeNames.LEVEL_IS_CLIENT_SIDE, insn)
                && Level.class.isAssignableFrom(load(insn.owner, loader))) {
            return new Value.Known(0);
        }
        if (isSelf(receiver) && !ownGetOffers && isVillagerOffers(insn, loader)) {
            return merchantOffers;
        }
        if (isSelf(receiver) && merchantFields.containsKey(insn.owner + "." + insn.name)) {
            return merchantFields.get(insn.owner + "." + insn.name);
        }
        if (isSelf(receiver) && insn.desc.equals(Type.getDescriptor(MerchantOffers.class))) {
            return NULL;
        }
        if (receiver instanceof Value.Unknown && Type.getType(insn.desc).getSort() == Type.OBJECT && isRandomType(Type.getType(insn.desc), loader)) {
            return new Value.Rng(isLegacyRandom(Type.getType(insn.desc), loader));
        }
        if (receiver instanceof Value.Unknown) {
            return Value.Unknown.of(Type.getType(insn.desc), Origin.of(receiver, insn.name, List.of()));
        }
        if (receiver instanceof Value.Choice choice) {
            List<Value> results = new ArrayList<>();

            for (Object option : choice.options()) {
                results.add(getField(insn, wrap(option), loader));
            }

            return Objects.requireNonNull(sameDraw(combine(results), choice));
        }
        if (!(receiver instanceof Value.Known known) || known.value() == null) {
            throw new Fail("field of a symbolic value");
        }

        Field field = field(load(insn.owner, loader), insn.name);

        try {
            Object value = field.get(known.value());

            if (tainted.contains(known.value()) && isData(value)) {
                taint(value);
            }

            return wrap(value, field.getType());
        } catch (Throwable e) {
            throw new Fail("field " + insn.owner + "." + insn.name);
        }
    }

    private void putField(FieldInsnNode insn, Value receiver, Value value, ClassLoader loader) {
        if (isSelf(receiver) && isVillagerOffers(insn, loader) && value instanceof Value.Offers offers) {
            merchantOffers = offers;

            if (!ownGetOffers) {
                return;
            }
        }
        if (isSelf(receiver)) {
            merchantFields.put(insn.owner + "." + insn.name, value);
            return;
        }
        if (!(receiver instanceof Value.Known known) || !owned.containsKey(known.value()) || !(value instanceof Value.Known known2)) {
            throw new Fail("write to a field");
        }
        if (known.value() != null && isFrozen(known.value())) {
            throw new Fail("change under a random choice");
        }

        Field field = field(load(insn.owner, loader), insn.name);

        try {
            field.set(known.value(), toJava(known2, field.getType()));
        } catch (Throwable e) {
            throw new Fail("field write " + insn.owner + "." + insn.name);
        }
    }

    private Value arrayLoad(Value array, Value index) {
        if (!(array instanceof Value.Known known) || known.value() == null || !known.value().getClass().isArray()) {
            throw new Fail("array load");
        }

        Object arr = known.value();

        if (tainted.contains(arr)) {
            throw new Fail("uncertain array element");
        }
        if (index instanceof Value.Known k) {
            int i = (Integer) k.value();

            if (i < 0 || i >= Array.getLength(arr)) {
                throw new Fail("array index");
            }

            Object element = Array.get(arr, i);

            if (element instanceof VillagerTrades.ItemListing) {
                elementSources.put(element, arr);
            }

            return wrap(element, arr.getClass().getComponentType());
        }
        if (index instanceof Value.Choice choice && choice.options().stream().allMatch((o) -> o instanceof Integer i && i >= 0 && i < Array.getLength(arr))) {
            List<Object> elements = choice.options().stream().map((o) -> Array.get(arr, (Integer) o)).toList();

            if (arr.getClass().getComponentType().isPrimitive()) {
                return sameDraw(combine(elements.stream().map((o) -> wrap(o, arr.getClass().getComponentType())).toList()), choice);
            }

            elements.stream().filter(VillagerTrades.ItemListing.class::isInstance).forEach((e) -> elementSources.put(e, arr));
            return new Value.Choice(elements, choice.draw());
        }
        if (index instanceof Value.Num num) {
            List<Object> elements = new ArrayList<>();

            for (int i = 0; i < Array.getLength(arr); i++) {
                elements.add(Array.get(arr, i));
            }

            Value.Choice choice = (Value.Choice) pick(elements, num);

            if (arr.getClass().getComponentType().isPrimitive()) {
                return sameDraw(combine(choice.options().stream().map((o) -> wrap(o, arr.getClass().getComponentType())).toList()), choice);
            }

            return choice;
        }

        throw new Fail("array index");
    }

    private void arrayStore(int op, Value array, Value index, Value value) {
        if (!(array instanceof Value.Known known) || !owned.containsKey(known.value()) || !(index instanceof Value.Known k)) {
            throw new Fail("array store");
        }
        if (known.value() != null && isFrozen(known.value())) {
            throw new Fail("change under a random choice");
        }

        Object arr = known.value();
        int i = (Integer) k.value();

        if (value instanceof Value.Num || value instanceof Value.Choice || value instanceof Value.Unknown) {
            taint(arr);
            return;
        }

        Object element = value instanceof Value.Closure closure ? materialize(closure) : concrete(value);

        switch (op) {
            case BASTORE -> {
                if (arr instanceof boolean[] booleans) {
                    booleans[i] = (Integer) element != 0;
                } else {
                    ((byte[]) arr)[i] = (byte) (int) (Integer) element;
                }
            }
            case CASTORE -> ((char[]) arr)[i] = (char) (int) (Integer) element;
            case SASTORE -> ((short[]) arr)[i] = (short) (int) (Integer) element;
            default -> Array.set(arr, i, element);
        }
    }

    private Value newArray(Class<?> component, Value size) {
        if (!(size instanceof Value.Known known)) {
            throw new Fail("symbolic array size");
        }

        Object array = Array.newInstance(component, (Integer) known.value());

        own(array);
        return new Value.Known(array);
    }

    private static Class<?> primitiveArrayType(int type) {
        return switch (type) {
            case T_BOOLEAN -> boolean.class;
            case T_CHAR -> char.class;
            case T_FLOAT -> float.class;
            case T_DOUBLE -> double.class;
            case T_BYTE -> byte.class;
            case T_SHORT -> short.class;
            case T_INT -> int.class;
            case T_LONG -> long.class;
            default -> throw new Fail("array type");
        };
    }

    private static Class<?> arrayComponent(String desc, ClassLoader loader) {
        if (desc.startsWith("[")) {
            try {
                return Class.forName(desc.replace('/', '.'), false, loader);
            } catch (Throwable e) {
                throw new Fail("array class");
            }
        }

        return load(desc, loader);
    }

    private Value checkCast(Value value, String desc, ClassLoader loader) {
        if (value instanceof Value.Known known && known.value() != null && !arrayComponent(desc, loader).isInstance(known.value())) {
            throw new Fail("class cast");
        }
        if (value instanceof Value.Unknown unknown && !desc.startsWith("[")) {
            return unknown.withType(desc);
        }

        return value;
    }

    private static Class<?> onServer(Class<?> type) {
        return type.isAssignableFrom(ServerLevel.class) && Level.class.isAssignableFrom(type) ? ServerLevel.class : type;
    }

    private boolean instanceOf(Value value, String desc, ClassLoader loader) {
        Class<?> type = arrayComponent(desc, loader);

        if (value instanceof Value.Known known) {
            return type.isInstance(known.value());
        } else if (value instanceof Value.Stack) {
            return type.isAssignableFrom(ItemStack.class);
        } else if (value instanceof Value.Offer) {
            return type.isAssignableFrom(MerchantOffer.class);
        } else if (value instanceof Value.Closure closure) {
            return type.isAssignableFrom(closure.iface());
        } else if (value instanceof Value.Rng rng) {
            return type.isAssignableFrom(rng.legacy() ? Random.class : RandomSource.class);
        } else if (value instanceof Value.Choice choice) {
            return decided(choice, type::isInstance);
        } else if (value instanceof Value.Unknown unknown && unknown.type() != null && type.isAssignableFrom(onServer(load(unknown.type(), loader)))) {
            return true;
        } else if (value instanceof Value.Unknown unknown) {
            return decide(Decision.Kind.UNKNOWN, unknown.origin(), desc, "instanceof of an unknown value");
        }

        throw new Fail("instanceof of an unknown value");
    }

    private boolean isNull(Value value) {
        if (value instanceof Value.Known known) {
            return known.value() == null;
        }
        if (value instanceof Value.Choice choice) {
            return decided(choice, Objects::isNull);
        }
        if (value instanceof Value.Unknown unknown && unknown.found() != null && unknown.origin() != null) {
            return !decide(Decision.Kind.FOUND, unknown.origin(), unknown.found(), "null check of an unknown value");
        }
        if (value instanceof Value.Unknown unknown) {
            return !decide(Decision.Kind.UNKNOWN, unknown.origin(), NULL, "null check of an unknown value");
        }
        if (value instanceof Value.Uninit) {
            throw new Fail("null check of an unknown value");
        }

        return false;
    }

    private boolean sameReference(Value a, Value b) {
        if (a instanceof Value.Known ka && b instanceof Value.Known kb) {
            return ka.value() == kb.value();
        }
        if (a instanceof Value.Choice choice && b instanceof Value.Known known) {
            return decided(choice, (o) -> o == known.value());
        }
        if (b instanceof Value.Choice choice && a instanceof Value.Known known) {
            return decided(choice, (o) -> o == known.value());
        }
        if (a instanceof Value.Unknown unknown && b instanceof Value.Known known) {
            return equality(unknown, known.value());
        }
        if (b instanceof Value.Unknown unknown && a instanceof Value.Known known) {
            return equality(unknown, known.value());
        }
        if (a instanceof Value.Unknown || b instanceof Value.Unknown) {
            return decide(Decision.Kind.UNKNOWN, null, null, "reference comparison of an unknown value");
        }
        if (a instanceof Value.Known ka && ka.value() == null || b instanceof Value.Known kb && kb.value() == null) {
            return false;
        }
        if (a instanceof Value.Known || b instanceof Value.Known) {
            throw new Fail("reference comparison of a symbolic value");
        }

        return a == b;
    }

    private boolean equality(Value.Unknown unknown, @Nullable Object value) {
        if (value == null) {
            return isNull(unknown);
        }
        if (unknown.origin() == null) {
            return decide(Decision.Kind.UNKNOWN, null, null, "reference comparison of an unknown value");
        }

        return decide(Decision.Kind.EQUALS, unknown.origin(), value, "reference comparison of an unknown value");
    }

    private boolean decided(Value.Choice choice, java.util.function.Predicate<Object> test) {
        if (choice.options().stream().allMatch(test)) {
            return true;
        }
        if (choice.options().stream().noneMatch(test)) {
            return false;
        }

        boolean holds = decide(Decision.Kind.RANDOM, decisions.size(), share(choice, test), "reference comparison of a random choice");

        narrow(choice, (o) -> test.test(o) == holds);
        return holds;
    }

    @Nullable
    private static Double share(Value.Choice choice, java.util.function.Predicate<Object> test) {
        return choice.draw() != null ? (double) choice.options().stream().filter(test).count() / choice.options().size() : null;
    }

    @Nullable
    private Value.Choice narrow(Value.Choice choice, java.util.function.Predicate<Object> keep) {
        List<Integer> kept = IntStream.range(0, choice.options().size()).filter((i) -> keep.test(choice.options().get(i))).boxed().toList();
        Map<Value, Value> narrowed = new IdentityHashMap<>();
        java.util.function.Predicate<Value> match = (v) -> v instanceof Value.Choice other && (other == choice || choice.draw() != null
                && other.draw() == choice.draw() && other.options().size() == choice.options().size());
        java.util.function.UnaryOperator<Value> with = (v) -> narrowed.computeIfAbsent(v, (o) -> narrowed((Value.Choice) o, kept));

        for (Frame frame : frames) {
            frame.replace(match, with);
        }

        statics.replaceAll((k, v) -> match.test(v) ? with.apply(v) : v);
        merchantFields.replaceAll((k, v) -> match.test(v) ? with.apply(v) : v);

        return kept.size() > 1 ? (Value.Choice) narrowed(choice, kept) : null;
    }

    private Value narrowed(Value.Choice choice, List<Integer> kept) {
        List<Object> options = kept.stream().map(choice.options()::get).toList();

        return options.size() == 1 ? wrap(options.get(0)) : new Value.Choice(options, choice.draw());
    }

    private int switchCase(Value value, List<Integer> keys) {
        if (value instanceof Value.Known known) {
            return keys.indexOf((Integer) known.value());
        }
        if (value instanceof Value.Choice choice && choice.options().stream().allMatch(Integer.class::isInstance)) {
            Value.Choice rest = choice;

            for (int i = 0; i < keys.size(); i++) {
                Integer key = keys.get(i);
                Value.Choice current = rest;

                if (current.options().stream().allMatch(key::equals)) {
                    return i;
                }
                if (current.options().stream().anyMatch(key::equals)) {
                    if (decide(Decision.Kind.RANDOM, decisions.size(), share(current, key::equals), "switch on a random choice")) {
                        narrow(current, key::equals);
                        return i;
                    }

                    rest = narrow(current, (o) -> !key.equals(o));

                    if (rest == null) {
                        return keys.indexOf(current.options().stream().filter((o) -> !key.equals(o)).findFirst().map(Integer.class::cast).orElseThrow());
                    }
                }
            }

            return -1;
        }
        if (value instanceof Value.Num num && num.expr().bounds().isPoint()) {
            return keys.indexOf((int) num.expr().bounds().lo());
        }
        Drawn source = value instanceof Value.Num num ? drawn.get(num.expr()) : null;

        if (source != null && source.draw().values != null) {
            for (int i = 0; i < keys.size(); i++) {
                double key = keys.get(i) - source.offset();
                double probability = source.draw().probability(IFEQ, key);

                if (probability >= 1 || probability > 0 && decide(Decision.Kind.RANDOM, decisions.size(), probability, "switch on a random value")) {
                    source.draw().narrow(IFEQ, key);
                    narrowed(source.draw());
                    return i;
                }

                source.draw().narrow(IFNE, key);
            }

            narrowed(source.draw());
            return -1;
        }
        if (value instanceof Value.Num num) {
            NumberInterval bounds = num.expr().bounds();
            List<Integer> possible = IntStream.range(0, keys.size()).filter((i) -> keys.get(i) >= bounds.lo() && keys.get(i) <= bounds.hi()).boxed().toList();
            boolean covered = possible.size() == Math.floor(bounds.hi()) - Math.ceil(bounds.lo()) + 1;
            int subject = decisions.size();

            for (int i = 0; i < possible.size(); i++) {
                boolean last = covered && i == possible.size() - 1;

                if (last || decide(Decision.Kind.RANDOM, subject, keys.get(possible.get(i)), "switch on a random value")) {
                    return possible.get(i);
                }
            }

            return -1;
        }
        if (value instanceof Value.Unknown unknown) {
            for (int i = 0; i < keys.size(); i++) {
                boolean holds;

                if (unknown.origin() != null) {
                    holds = decide(Decision.Kind.EQUALS, unknown.origin(), keys.get(i), "switch on a value from the trader or world");
                } else {
                    holds = decide(Decision.Kind.UNKNOWN, null, null, "switch on a value from the trader or world");
                }

                if (holds) {
                    return i;
                }
            }

            return -1;
        }

        throw new Fail("symbolic switch");
    }

    @NotNull
    private static Value ldc(Object cst, ClassLoader loader) {
        if (cst instanceof Integer || cst instanceof Float || cst instanceof Long || cst instanceof Double || cst instanceof String) {
            return new Value.Known(cst);
        }
        if (cst instanceof Type type && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY)) {
            return new Value.Known(type.getSort() == Type.OBJECT ? load(type.getInternalName(), loader) : arrayComponent(type.getDescriptor(), loader));
        }

        throw new Fail("constant " + cst);
    }

    private Value binary(int op, Value a, Value b) {
        if (a instanceof Value.Known ka && b instanceof Value.Known kb && ka.value() != null && kb.value() != null) {
            return new Value.Known(Arithmetic.compute(op, ka.value(), kb.value()));
        }
        if (a instanceof Value.Unknown || b instanceof Value.Unknown) {
            return unknownNumber(Arithmetic.kindOf(op));
        }

        Value.Kind kind = Arithmetic.kindOf(op);
        NumberExpr x = expr(a);
        NumberExpr y = expr(b);

        return switch (op) {
            case IADD, LADD, FADD, DADD -> num(add(x, y), kind);
            case ISUB, LSUB, FSUB, DSUB -> num(sub(x, y), kind);
            case IMUL, LMUL, FMUL, DMUL -> num(NumberExpr.mul(x, y), kind);
            case IDIV, LDIV -> num(NumberExpr.fn(NumberFunctions.TRUNC, NumberExpr.div(x, y)), kind);
            case FDIV, DDIV -> num(NumberExpr.div(x, y), kind);
            case IREM, LREM, FREM, DREM -> num(NumberExpr.fn(NumberFunctions.MOD, x, y), kind);
            default -> throw new Fail("bitwise operation on a symbolic value");
        };
    }

    private Value negate(int op, Value a) {
        if (a instanceof Value.Known known && known.value() != null) {
            return new Value.Known(Arithmetic.negate(known.value()));
        }
        if (a instanceof Value.Unknown) {
            return unknownNumber(Arithmetic.kindOf(op - INEG + IADD));
        }

        return num(NumberExpr.fn(NumberFunctions.NEG, expr(a)), Arithmetic.kindOf(op - INEG + IADD));
    }

    private Value convert(int op, Value a) {
        if (a instanceof Value.Known known && known.value() != null) {
            return new Value.Known(Arithmetic.convert(op, known.value()));
        }
        if (a instanceof Value.Unknown) {
            return unknownNumber(switch (op) {
                case I2L, F2L, D2L -> Value.Kind.LONG;
                case I2D, L2D, F2D -> Value.Kind.DOUBLE;
                case I2F, L2F, D2F -> Value.Kind.FLOAT;
                default -> Value.Kind.INT;
            });
        }

        NumberExpr x = expr(a);

        return switch (op) {
            case I2L, F2L, D2L -> num(op == I2L ? x : NumberExpr.fn(NumberFunctions.TRUNC, x), Value.Kind.LONG);
            case I2F, L2F, D2F -> num(x, Value.Kind.FLOAT);
            case I2D, L2D, F2D -> num(x, Value.Kind.DOUBLE);
            case F2I, D2I -> num(NumberExpr.fn(NumberFunctions.TRUNC, x), Value.Kind.INT);
            default -> num(x, Value.Kind.INT);
        };
    }

    @NotNull
    private Value compare(int op, Value a, Value b) {
        if (a instanceof Value.Known ka && b instanceof Value.Known kb && ka.value() != null && kb.value() != null) {
            return new Value.Known(Arithmetic.compare(op, ka.value(), kb.value()));
        }
        if (a instanceof Value.Unknown || b instanceof Value.Unknown) {
            return unknownNumber(Value.Kind.INT);
        }

        return new Value.Cmp(expr(a), expr(b));
    }

    @NotNull
    private static Value unknownNumber(Value.Kind kind) {
        return new Value.Unknown(kind == Value.Kind.LONG || kind == Value.Kind.DOUBLE, null, null, null);
    }

    private boolean branch(int op, Value a, Value b) {
        if ((op == IFEQ || op == IFNE) && a instanceof Value.Unknown unknown && unknown.origin() != null && unknown.origin().isBoolean()
                && b instanceof Value.Known known && Integer.valueOf(0).equals(known.value())) {
            return test(unknown.origin()) == (op == IFNE);
        }
        if (a instanceof Value.Unknown || b instanceof Value.Unknown) {
            return decide(Decision.Kind.UNKNOWN, null, null, "value from the trader or world");
        }
        if (a instanceof Value.Known ka && b instanceof Value.Known kb) {
            int x = (Integer) ka.value();
            int y = (Integer) kb.value();

            return switch (op) {
                case IFEQ -> x == y;
                case IFNE -> x != y;
                case IFLT -> x < y;
                case IFGE -> x >= y;
                case IFGT -> x > y;
                default -> x <= y;
            };
        }

        Boolean onDraw = drawnBranch(op, a, b);

        if (onDraw != null) {
            return onDraw;
        }

        NumberInterval difference;

        if (a instanceof Value.Cmp cmp) {
            difference = cmp.left().bounds().minus(cmp.right().bounds());
        } else {
            difference = expr(a).bounds().minus(expr(b).bounds());
        }

        Boolean decided = Arithmetic.decide(op, difference);

        if (decided == null) {
            return decide(Decision.Kind.RANDOM, decisions.size(), probability(op, a, b), "branch on a random value");
        }

        return decided;
    }

    @Nullable
    private Boolean drawnBranch(int op, Value a, Value b) {
        NumberExpr left = a instanceof Value.Cmp cmp ? cmp.left() : expr(a);
        NumberExpr right = a instanceof Value.Cmp cmp ? cmp.right() : expr(b);

        if (!drawn.containsKey(left) && drawn.containsKey(right)) {
            return drawnBranch(mirror(op), right, left);
        }

        return drawnBranch(op, left, right);
    }

    @Nullable
    private Boolean drawnBranch(int op, NumberExpr left, NumberExpr right) {
        Drawn source = drawn.get(left);

        if (source == null || !(right instanceof NumberExpr.Const constant)) {
            return null;
        }

        Draw draw = source.draw();
        double bound = constant.value() - source.offset();
        double probability = draw.probability(op, bound);

        if (probability <= 0) {
            return false;
        }
        if (probability >= 1) {
            return true;
        }

        boolean holds = decide(Decision.Kind.RANDOM, decisions.size(), probability, "branch on a random value");

        draw.narrow(holds ? op : negation(op), bound);
        narrowed(draw);
        return holds;
    }

    private void narrowed(Draw draw) {
        Map<NumberExpr, NumberExpr> replacements = new IdentityHashMap<>();

        drawn.forEach((expr, source) -> {
            if (source.draw() == draw) {
                replacements.put(expr, draw.expr(source.offset()));
            }
        });
        replacements.forEach((from, to) -> {
            if (!(to instanceof NumberExpr.Const)) {
                drawn.put(to, drawn.get(from));
            }
        });

        for (Frame frame : frames) {
            frame.replace((v) -> v instanceof Value.Num || v instanceof Value.Stack, (v) -> {
                if (v instanceof Value.Stack stack) {
                    stack.count = substitute(stack.count, replacements);
                    return stack;
                }

                Value.Num num = (Value.Num) v;
                NumberExpr expr = substitute(num.expr(), replacements);

                return expr == num.expr() ? num : num(expr, num.kind());
            });
        }
    }

    private static NumberExpr substitute(NumberExpr expr, Map<NumberExpr, NumberExpr> replacements) {
        if (replacements.containsKey(expr)) {
            return replacements.get(expr);
        }
        if (expr instanceof NumberExpr.Fn fn) {
            List<NumberExpr> args = fn.args().stream().map((a) -> substitute(a, replacements)).toList();

            return IntStream.range(0, args.size()).allMatch((i) -> args.get(i) == fn.args().get(i)) ? expr : NumberExpr.fn(fn.id(), args);
        }
        if (expr instanceof NumberExpr.Weighted weighted) {
            List<NumberExpr.WeightedEntry> entries = weighted.entries().stream()
                    .map((e) -> new NumberExpr.WeightedEntry(e.weight(), substitute(e.value(), replacements))).toList();

            return IntStream.range(0, entries.size()).allMatch((i) -> entries.get(i).value() == weighted.entries().get(i).value()) ? expr : NumberExpr.weighted(entries);
        }

        return expr;
    }

    private static int negation(int op) {
        return switch (op) {
            case IFEQ -> IFNE;
            case IFNE -> IFEQ;
            case IFLT -> IFGE;
            case IFGE -> IFLT;
            case IFGT -> IFLE;
            default -> IFGT;
        };
    }

    @Nullable
    private Double probability(int op, Value a, Value b) {
        NumberExpr left = a instanceof Value.Cmp cmp ? cmp.left() : expr(a);
        NumberExpr right = a instanceof Value.Cmp cmp ? cmp.right() : expr(b);
        Double probability = uniformProbability(op, left, right);

        return probability != null ? probability : uniformProbability(mirror(op), right, left);
    }

    @Nullable
    private static Double uniformProbability(int op, NumberExpr draw, NumberExpr bound) {
        if (!(bound instanceof NumberExpr.Const constant) || !(draw instanceof NumberExpr.Fn fn) || fn.args().size() != 2
                || !(fn.args().get(0) instanceof NumberExpr.Const lo) || !(fn.args().get(1) instanceof NumberExpr.Const hi)) {
            return null;
        }

        double value = constant.value();

        if (fn.id().equals(NumberFunctions.UNIFORM_FLOAT) && hi.value() > lo.value()) {
            double below = Math.max(0, Math.min(1, (value - lo.value()) / (hi.value() - lo.value())));

            return switch (op) {
                case IFLT, IFLE -> below;
                case IFGT, IFGE -> 1 - below;
                case IFEQ -> 0.0;
                default -> 1.0;
            };
        }
        if (fn.id().equals(NumberFunctions.UNIFORM_INT) && hi.value() >= lo.value()) {
            double total = hi.value() - lo.value() + 1;
            double below = Math.max(0, Math.min(hi.value(), Math.ceil(value) - 1) - lo.value() + 1);
            double atMost = Math.max(0, Math.min(hi.value(), Math.floor(value)) - lo.value() + 1);
            double count = switch (op) {
                case IFLT -> below;
                case IFLE -> atMost;
                case IFGT -> total - atMost;
                case IFGE -> total - below;
                case IFEQ -> atMost - below;
                default -> total - (atMost - below);
            };

            return count / total;
        }

        return null;
    }

    private static int mirror(int op) {
        return switch (op) {
            case IFLT -> IFGT;
            case IFGT -> IFLT;
            case IFLE -> IFGE;
            case IFGE -> IFLE;
            default -> op;
        };
    }

    private boolean test(Origin origin) {
        if (origin.receiver() instanceof Origin subject && origin.args().size() == 1 && origin.args().get(0) != null && !(origin.args().get(0) instanceof Origin)) {
            return decide(origin.isEquality() ? Decision.Kind.EQUALS : Decision.Kind.TEST, subject, origin.args().get(0), "value from the trader or world");
        }

        return decide(Decision.Kind.UNKNOWN, origin, null, "value from the trader or world");
    }

    private NumberExpr expr(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Number number) {
            return NumberExpr.constant(number.doubleValue());
        } else if (value instanceof Value.Num num) {
            return num.expr();
        } else if (value instanceof Value.Choice choice && choice.options().stream().allMatch(Number.class::isInstance)) {
            return NumberExpr.weighted(choice.options().stream().map((o) -> new NumberExpr.WeightedEntry(1, NumberExpr.constant(((Number) o).doubleValue()))).toList());
        }

        throw new Fail(value instanceof Value.Unknown ? "value from the trader or world" : "not a number");
    }

    @NotNull
    private static Value num(NumberExpr expr, Value.Kind kind) {
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

    @NotNull
    private NumberExpr add(NumberExpr a, NumberExpr b) {
        NumberExpr shifted = null;

        if (b instanceof NumberExpr.Const c) {
            shifted = shift(a, c.value());
        } else if (a instanceof NumberExpr.Const c) {
            shifted = shift(b, c.value());
        }

        return shifted != null ? shifted : NumberExpr.add(a, b);
    }

    @NotNull
    private NumberExpr sub(NumberExpr a, NumberExpr b) {
        NumberExpr shifted = b instanceof NumberExpr.Const c ? shift(a, -c.value()) : null;

        return shifted != null ? shifted : NumberExpr.sub(a, b);
    }

    @NotNull
    private static NumberExpr uniformInt(NumberExpr min, NumberExpr max) {
        return NumberExpr.uniformInt(min, max);
    }

    @Nullable
    private NumberExpr shift(NumberExpr value, double by) {
        if (value instanceof NumberExpr.Fn fn && (fn.id().equals(NumberFunctions.UNIFORM_INT) || fn.id().equals(NumberFunctions.UNIFORM_FLOAT))
                && fn.args().get(0) instanceof NumberExpr.Const lo && fn.args().get(1) instanceof NumberExpr.Const hi) {
            NumberExpr shifted = NumberExpr.fn(fn.id(), NumberExpr.constant(lo.value() + by), NumberExpr.constant(hi.value() + by));
            Drawn source = drawn.get(value);

            if (source != null) {
                drawn.put(shifted, new Drawn(source.draw(), source.offset() + by));
            }

            return shifted;
        }

        return null;
    }

    private static Value.Kind kindOf(Type type) {
        return switch (type.getSort()) {
            case Type.LONG -> Value.Kind.LONG;
            case Type.FLOAT -> Value.Kind.FLOAT;
            case Type.DOUBLE -> Value.Kind.DOUBLE;
            default -> Value.Kind.INT;
        };
    }

    private int intOf(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Integer i) {
            return i;
        }

        throw new Fail("symbolic trade number");
    }

    private float floatOf(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Float f) {
            return f;
        }

        throw new Fail("symbolic float");
    }

    private double doubleOf(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Double d) {
            return d;
        }

        throw new Fail("symbolic double");
    }

    private static int[] choiceIndices(List<Value> args) {
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

    @NotNull
    static Class<?> load(String internalName, ClassLoader loader) {
        return load(internalName, loader, false);
    }

    @NotNull
    static Class<?> load(String internalName, @Nullable ClassLoader loader, boolean initialize) {
        try {
            return Class.forName(internalName.replace('/', '.'), initialize, loader);
        } catch (Throwable e) {
            throw new Fail("class " + internalName);
        }
    }

    static boolean isJdk(Class<?> type) {
        String name = type.getName();

        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.") || name.startsWith("sun.") || name.startsWith("com.sun.");
    }

    @Nullable
    static ClassNode classNode(Class<?> type) {
        if (type.isHidden() || type.isArray() || type.isPrimitive()) {
            return null;
        }

        String internalName = Type.getInternalName(type);

        return CLASS_NODES.computeIfAbsent(internalName, (name) -> {
            ClassLoader loader = type.getClassLoader() != null ? type.getClassLoader() : ClassLoader.getSystemClassLoader();

            try (InputStream is = loader.getResourceAsStream(name + ".class")) {
                if (is == null) {
                    return Optional.empty();
                }

                ClassNode node = new ClassNode();

                new ClassReader(is).accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                return Optional.of(node);
            } catch (Throwable e) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    @Nullable
    static MethodNode findMethod(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }

        return null;
    }

    @Nullable
    private static Declared declaration(Class<?> start, String name, String desc, boolean exact) {
        for (Class<?> type = start; type != null; type = exact ? null : type.getSuperclass()) {
            if (isJdk(type)) {
                if (method(type, name, desc) != null) {
                    return new JdkDeclaration(type);
                }

                break;
            }

            ClassNode node = classNode(type);
            MethodNode method = node != null ? findMethod(node, name, desc) : null;

            if (method != null && (method.access & ACC_ABSTRACT) == 0) {
                return new Declaration(type, method);
            }
        }

        Deque<Class<?>> interfaces = new ArrayDeque<>();

        for (Class<?> type = start; type != null; type = type.getSuperclass()) {
            interfaces.addAll(Arrays.asList(type.getInterfaces()));
        }

        while (!interfaces.isEmpty()) {
            Class<?> type = interfaces.poll();

            if (isJdk(type)) {
                continue;
            }

            ClassNode node = classNode(type);
            MethodNode method = node != null ? findMethod(node, name, desc) : null;

            if (method != null && (method.access & ACC_ABSTRACT) == 0) {
                return new Declaration(type, method);
            }

            interfaces.addAll(Arrays.asList(type.getInterfaces()));
        }

        return null;
    }

    @Nullable
    static Method method(Class<?> owner, String name, String desc) {
        return METHODS.computeIfAbsent(owner.getName() + '.' + name + desc, (key) -> {
            Deque<Class<?>> types = new ArrayDeque<>();

            types.add(owner);

            while (!types.isEmpty()) {
                Class<?> type = types.poll();

                for (Method method : type.getDeclaredMethods()) {
                    if (method.getName().equals(name) && Type.getMethodDescriptor(method).equals(desc)) {
                        return Optional.of(method);
                    }
                }

                if (type.getSuperclass() != null) {
                    types.add(type.getSuperclass());
                }

                types.addAll(Arrays.asList(type.getInterfaces()));
            }

            return Optional.empty();
        }).orElse(null);
    }

    private static Constructor<?> constructor(Class<?> owner, String desc) {
        for (Constructor<?> constructor : owner.getDeclaredConstructors()) {
            if (Type.getConstructorDescriptor(constructor).equals(desc)) {
                return constructor;
            }
        }

        throw new Fail("no constructor " + owner.getName());
    }

    @NotNull
    static Field field(Class<?> owner, String name) {
        return FIELDS.computeIfAbsent(owner.getName() + '.' + name, (key) -> {
            Deque<Class<?>> types = new ArrayDeque<>();

            types.add(owner);

            while (!types.isEmpty()) {
                Class<?> type = types.poll();

                for (Field field : type.getDeclaredFields()) {
                    if (field.getName().equals(name)) {
                        try {
                            field.setAccessible(true);
                            return Optional.of(field);
                        } catch (Throwable e) {
                            return Optional.empty();
                        }
                    }
                }

                if (type.getSuperclass() != null) {
                    types.add(type.getSuperclass());
                }

                types.addAll(Arrays.asList(type.getInterfaces()));
            }

            return Optional.empty();
        }).orElseThrow(() -> new Fail("field " + owner.getName() + "." + name));
    }

    enum Mode {
        SYMBOLIC,
        STUB,
    }

    private enum Semantic {
        BOUND,
        BETWEEN,
        ORIGIN,
        FLOAT,
        FLOAT_BOUND,
        FLOAT_ORIGIN,
        DOUBLE,
        DOUBLE_BOUND,
        DOUBLE_ORIGIN,
        BOOLEAN,
        INT,
        LONG,
        GAUSSIAN,
        FORK,
        IGNORED,
    }

    private record Drawn(Draw draw, double offset) {
    }

    private static final class Draw {
        @Nullable
        final List<Integer> values;
        double lo;
        double hi;

        private Draw(@Nullable List<Integer> values, double lo, double hi) {
            this.values = values;
            this.lo = lo;
            this.hi = hi;
        }

        @NotNull
        static Draw discrete(int lo, int hi) {
            return new Draw(IntStream.rangeClosed(lo, hi).boxed().collect(Collectors.toCollection(ArrayList::new)), lo, hi);
        }

        @NotNull
        static Draw continuous(double lo, double hi) {
            return new Draw(null, lo, hi);
        }

        double probability(int op, double bound) {
            if (values != null) {
                return values.isEmpty() ? 0 : (double) values.stream().filter((v) -> compares(op, v, bound)).count() / values.size();
            }
            if (hi <= lo) {
                return 0;
            }

            double below = Math.max(0, Math.min(1, (bound - lo) / (hi - lo)));

            return switch (op) {
                case IFLT, IFLE -> below;
                case IFGT, IFGE -> 1 - below;
                case IFEQ -> 0;
                default -> 1;
            };
        }

        NumberExpr expr(double offset) {
            if (values == null) {
                return NumberExpr.uniformFloat(lo + offset, hi + offset);
            }
            if (values.size() == 1) {
                return NumberExpr.constant(values.get(0) + offset);
            }

            int min = Collections.min(values);
            int max = Collections.max(values);

            if (max - min + 1 == values.size()) {
                return NumberExpr.uniformInt(min + offset, max + offset);
            }

            return NumberExpr.weighted(values.stream().map((v) -> new NumberExpr.WeightedEntry(1, NumberExpr.constant(v + offset))).toList());
        }

        void narrow(int op, double bound) {
            if (values != null) {
                values.removeIf((v) -> !compares(op, v, bound));
            } else if (op == IFLT || op == IFLE) {
                hi = Math.min(hi, bound);
            } else if (op == IFGT || op == IFGE) {
                lo = Math.max(lo, bound);
            }
        }

        private static boolean compares(int op, double value, double bound) {
            return switch (op) {
                case IFEQ -> value == bound;
                case IFNE -> value != bound;
                case IFLT -> value < bound;
                case IFGE -> value >= bound;
                case IFGT -> value > bound;
                default -> value <= bound;
            };
        }
    }

    private sealed interface Declared permits Declaration, JdkDeclaration {
        Class<?> owner();
    }

    private record Declaration(Class<?> owner, MethodNode method) implements Declared {
    }

    private record JdkDeclaration(Class<?> owner) implements Declared {
    }

    static final class Fail extends RuntimeException {
        private static final Set<String> BUDGETS = Set.of("too many branches", "step budget", "call depth");

        @Nullable
        private String where;

        Fail(String message) {
            super(message, null, false, false);
        }

        Fail at(String method) {
            if (where == null) {
                where = method;
            }

            return this;
        }

        String describe() {
            return where != null ? getMessage() + " in " + where : getMessage();
        }

        boolean isBudget() {
            return BUDGETS.contains(getMessage());
        }
    }

    private static final class Frame {
        final Value[] locals;
        final List<Value> stack = new ArrayList<>();

        Frame(int maxLocals) {
            locals = new Value[maxLocals];
        }

        void push(Value value) {
            stack.add(value);
        }

        Value pop() {
            if (stack.isEmpty()) {
                throw new Fail("empty stack");
            }

            return stack.remove(stack.size() - 1);
        }

        Value peek() {
            if (stack.isEmpty()) {
                throw new Fail("empty stack");
            }

            return stack.get(stack.size() - 1);
        }

        @NotNull
        Value load(int var) {
            Value value = locals[var];

            if (value == null) {
                throw new Fail("unset local");
            }

            return value;
        }

        void replace(java.util.function.Predicate<Value> match, Value with) {
            replace(match, (v) -> with);
        }

        void replace(java.util.function.Predicate<Value> match, java.util.function.UnaryOperator<Value> with) {
            for (int i = 0; i < locals.length; i++) {
                if (locals[i] != null && match.test(locals[i])) {
                    locals[i] = with.apply(locals[i]);
                }
            }

            stack.replaceAll((v) -> match.test(v) ? with.apply(v) : v);
        }
    }
}
