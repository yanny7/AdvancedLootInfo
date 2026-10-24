package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.IntStream;

import static com.yanny.ali.plugin.server.bytecode.Interpreter.concrete;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.num;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.pick;
import static com.yanny.ali.plugin.server.bytecode.Interpreter.sameDraw;
import static com.yanny.ali.plugin.server.bytecode.Resolution.*;

final class Machine implements Opcodes {
    private static final int MAX_STEPS = 200_000;
    private static final int MAX_DEPTH = 12;

    private final Interpreter interpreter;
    private int steps;

    Machine(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Nullable
    Value execute(Class<?> owner, MethodNode method, List<Value> args, int depth) {
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

            frame.locals[slot] = arg instanceof Value.Known known ? interpreter.wrap(known.value(), javaType(type)) : arg;
            slot += type.getSize();
        }

        interpreter.store.enter(frame);

        try {
            return run(frame, owner.getClassLoader(), method, depth);
        } catch (Fail fail) {
            throw fail.at(owner.getName() + "." + method.name);
        } finally {
            interpreter.store.leave();
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
                case ACONST_NULL -> f.push(Interpreter.NULL);
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
                    if (interpreter.decisions.branch(op, f.pop(), new Value.Known(0))) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE -> {
                    Value b = f.pop();

                    if (interpreter.decisions.branch(op - IF_ICMPEQ + IFEQ, f.pop(), b)) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case IF_ACMPEQ, IF_ACMPNE -> {
                    Value b = f.pop();
                    boolean same = interpreter.decisions.sameReference(f.pop(), b);

                    if (same == (op == IF_ACMPEQ)) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case IFNULL, IFNONNULL -> {
                    if (interpreter.decisions.isNull(f.pop()) == (op == IFNULL)) {
                        next = ((JumpInsnNode) insn).label;
                    }
                }
                case GOTO -> next = ((JumpInsnNode) insn).label;
                case TABLESWITCH -> {
                    TableSwitchInsnNode sw = (TableSwitchInsnNode) insn;
                    int at = interpreter.decisions.switchCase(f.pop(), IntStream.rangeClosed(sw.min, sw.max).boxed().toList());

                    next = at >= 0 ? sw.labels.get(at) : sw.dflt;
                }
                case LOOKUPSWITCH -> {
                    LookupSwitchInsnNode sw = (LookupSwitchInsnNode) insn;
                    int at = interpreter.decisions.switchCase(f.pop(), sw.keys);

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

                    interpreter.store.putStatic(field.owner + "." + field.name, f.pop());
                }
                case GETFIELD -> f.push(FieldModel.get(interpreter.dispatcher, (FieldInsnNode) insn, f.pop(), loader));
                case PUTFIELD -> {
                    Value value = f.pop();

                    FieldModel.put(interpreter.dispatcher, (FieldInsnNode) insn, f.pop(), value, loader);
                }
                case INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE -> {
                    MethodInsnNode call = (MethodInsnNode) insn;
                    Value[] args = new Value[Type.getArgumentTypes(call.desc).length + (op == INVOKESTATIC ? 0 : 1)];

                    for (int i = args.length - 1; i >= 0; i--) {
                        args[i] = f.pop();
                    }

                    Value result = interpreter.dispatcher.invoke(call, Arrays.asList(args), loader, depth);
                    Type returnType = Type.getReturnType(call.desc);

                    if (returnType.getSort() != Type.VOID) {
                        if (result == null) {
                            throw new Fail("missing return value");
                        }

                        f.push(result instanceof Value.Unknown unknown && unknown.type() == null ? Value.Unknown.of(returnType, unknown.origin()) : result);
                    }
                }
                case INVOKEDYNAMIC -> {
                    InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;
                    Value[] captured = new Value[Type.getArgumentTypes(indy.desc).length];

                    for (int i = captured.length - 1; i >= 0; i--) {
                        captured[i] = f.pop();
                    }

                    f.push(Indy.apply(interpreter.dispatcher, indy, Arrays.asList(captured), loader));
                }
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

    private Value getStatic(FieldInsnNode insn, ClassLoader loader) {
        if (interpreter.store.hasStatic(insn.owner + "." + insn.name)) {
            return interpreter.store.getStatic(insn.owner + "." + insn.name);
        }

        Field field = field(load(insn.owner, loader, true), insn.name);

        try {
            return interpreter.wrap(field.get(null), field.getType());
        } catch (Throwable e) {
            throw new Fail("static field " + insn.owner + "." + insn.name);
        }
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
        NumberExpr x = NumberDomain.expr(a);
        NumberExpr y = NumberDomain.expr(b);

        return switch (op) {
            case IADD, LADD, FADD, DADD -> num(interpreter.numbers.add(x, y), kind);
            case ISUB, LSUB, FSUB, DSUB -> num(interpreter.numbers.sub(x, y), kind);
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

        return num(NumberExpr.fn(NumberFunctions.NEG, NumberDomain.expr(a)), Arithmetic.kindOf(op - INEG + IADD));
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

        NumberExpr x = NumberDomain.expr(a);

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

        return new Value.Cmp(NumberDomain.expr(a), NumberDomain.expr(b));
    }

    @NotNull
    private static Value unknownNumber(Value.Kind kind) {
        return new Value.Unknown(kind == Value.Kind.LONG || kind == Value.Kind.DOUBLE, null, null, null);
    }

    private Value arrayLoad(Value array, Value index) {
        if (!(array instanceof Value.Known known) || known.value() == null || !known.value().getClass().isArray()) {
            throw new Fail("array load");
        }

        Object arr = known.value();

        if (interpreter.heap.isTainted(arr)) {
            throw new Fail("uncertain array element");
        }
        if (index instanceof Value.Known k) {
            int i = (Integer) k.value();

            if (i < 0 || i >= Array.getLength(arr)) {
                throw new Fail("array index");
            }

            Object element = Array.get(arr, i);

            if (element instanceof VillagerTrades.ItemListing) {
                interpreter.pickSources.putSource(element, arr);
            }

            return interpreter.wrap(element, arr.getClass().getComponentType());
        }
        if (index instanceof Value.Choice choice && choice.options().stream().allMatch((o) -> o instanceof Integer i && i >= 0 && i < Array.getLength(arr))) {
            List<Object> elements = choice.options().stream().map((o) -> Array.get(arr, (Integer) o)).toList();

            if (arr.getClass().getComponentType().isPrimitive()) {
                return sameDraw(interpreter.combine(elements.stream().map((o) -> interpreter.wrap(o, arr.getClass().getComponentType())).toList()), choice);
            }

            elements.stream().filter(VillagerTrades.ItemListing.class::isInstance).forEach((e) -> interpreter.pickSources.putSource(e, arr));
            return new Value.Choice(elements, choice.draw());
        }
        if (index instanceof Value.Num num) {
            List<Object> elements = new ArrayList<>();

            for (int i = 0; i < Array.getLength(arr); i++) {
                elements.add(Array.get(arr, i));
            }

            Value.Choice choice = (Value.Choice) pick(elements, num);

            if (arr.getClass().getComponentType().isPrimitive()) {
                return sameDraw(interpreter.combine(choice.options().stream().map((o) -> interpreter.wrap(o, arr.getClass().getComponentType())).toList()), choice);
            }

            return choice;
        }

        throw new Fail("array index");
    }

    private void arrayStore(int op, Value array, Value index, Value value) {
        if (!(array instanceof Value.Known known) || !interpreter.heap.isOwned(known.value()) || !(index instanceof Value.Known k)) {
            throw new Fail("array store");
        }
        if (known.value() != null && interpreter.heap.isFrozen(known.value())) {
            throw new Fail("change under a random choice");
        }

        Object arr = known.value();
        int i = (Integer) k.value();

        if (value instanceof Value.Num || value instanceof Value.Choice || value instanceof Value.Unknown) {
            interpreter.heap.taint(arr);
            return;
        }

        Object element = value instanceof Value.Closure closure ? interpreter.materialize(closure) : concrete(value);

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

        interpreter.heap.own(array);
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
            return interpreter.decisions.decided(choice, type::isInstance);
        } else if (value instanceof Value.Unknown unknown && unknown.type() != null && type.isAssignableFrom(onServer(load(unknown.type(), loader)))) {
            return true;
        } else if (value instanceof Value.Unknown unknown) {
            return interpreter.decisions.decide(Decision.Kind.UNKNOWN, unknown.origin(), desc, "instanceof of an unknown value");
        }

        throw new Fail("instanceof of an unknown value");
    }

    static Class<?> javaType(Type type) {
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

    static final class Frame {
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
