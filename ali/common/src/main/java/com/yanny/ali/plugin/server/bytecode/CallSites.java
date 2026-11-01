package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.yanny.ali.plugin.server.bytecode.Resolution.*;

final class CallSites {
    static final int DEPTH = 3;
    private static final int RECEIVER = -1;
    private static final int NONE = -2;

    private final Class<?> self;
    private final Sink sink;
    private final Predicate<Declaration> mayReach;
    private final Map<MethodNode, Optional<Frame<SourceValue>[]>> frames = new IdentityHashMap<>();
    private final Map<Reach, Boolean> reaching = new HashMap<>();
    private final Set<String> visited = new HashSet<>();
    private List<Site> sites = new ArrayList<>();

    CallSites(Class<?> self, Sink sink, Predicate<Declaration> mayReach) {
        this.self = self;
        this.sink = sink;
        this.mayReach = mayReach;
    }

    List<Site> find(Class<?> owner, MethodNode method) {
        Type[] params = Type.getArgumentTypes(method.desc);
        Arg[] args = new Arg[params.length];

        for (int i = 0; i < params.length; i++) {
            args[i] = new EntryParam(i);
        }

        visited.clear();
        sites = new ArrayList<>();
        walk(new Context(new Declaration(owner, method), (method.access & Opcodes.ACC_STATIC) == 0 ? Self.INSTANCE : null, args), 0);
        return sites;
    }

    static Source argument(String owner, MethodNode method, MethodInsnNode call, int argument, ClassLoader loader) {
        Frame<SourceValue>[] analyzed = analyze(owner, method);
        Frame<SourceValue> frame = analyzed != null ? analyzed[method.instructions.indexOf(call)] : null;

        if (frame == null) {
            return Other.INSTANCE;
        }

        int first = frame.getStackSize() - Type.getArgumentTypes(call.desc).length;
        Arg arg = trace(method, analyzed, frame.getStack(first + argument), loader, (var) -> {
            int index = parameterIndex(method, var);

            return index >= 0 ? new EntryParam(index) : Untraced.INSTANCE;
        }, () -> Untraced.INSTANCE);

        if (arg instanceof Constant constant) {
            return constant;
        }
        if (arg instanceof EntryParam(int index)) {
            return new Param(index);
        }

        return Other.INSTANCE;
    }

    private void walk(Context context, int depth) {
        MethodNode method = context.holder().method();
        Class<?> owner = context.holder().owner();

        if (!visited.add(owner.getName() + "." + method.name + method.desc) || !reaches(owner, method, depth)) {
            return;
        }

        Frame<SourceValue>[] analyzed = frames(owner, method);

        if (analyzed == null) {
            return;
        }

        for (int i = 0; i < method.instructions.size(); i++) {
            if (!(method.instructions.get(i) instanceof MethodInsnNode call) || analyzed[i] == null) {
                continue;
            }

            Frame<SourceValue> frame = analyzed[i];
            Type[] params = Type.getArgumentTypes(call.desc);
            int first = frame.getStackSize() - params.length;

            if (sink.matches(call, owner.getClassLoader())) {
                sites.add(withLive(site(source(context, analyzed, frame.getStack(first + sink.argument())), context)));
            } else if (depth < DEPTH) {
                Arg receiver = call.getOpcode() == Opcodes.INVOKESTATIC ? null : source(context, analyzed, frame.getStack(first - 1));
                Declaration callee = callee(call, owner.getClassLoader(), receiver instanceof Self);

                if (callee != null) {
                    Arg[] args = new Arg[params.length];

                    for (int p = 0; p < params.length; p++) {
                        args[p] = source(context, analyzed, frame.getStack(first + p));
                    }

                    walk(new Context(callee, receiver, args), depth + 1);
                }
            }
        }
    }

    private boolean reaches(Class<?> owner, MethodNode method, int depth) {
        Reach key = new Reach(method, depth);
        Boolean known = reaching.get(key);

        if (known != null) {
            return known;
        }

        reaching.put(key, false);

        if (!mayReach.test(new Declaration(owner, method))) {
            return false;
        }

        boolean onSelf = (method.access & Opcodes.ACC_STATIC) == 0 && owner.isAssignableFrom(self);
        boolean found = false;

        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call) {
                if (sink.matches(call, owner.getClassLoader())) {
                    found = true;
                    break;
                }

                Declaration callee = depth < DEPTH ? callee(call, owner.getClassLoader(), onSelf) : null;

                if (callee != null && reaches(callee.owner(), callee.method(), depth + 1)) {
                    found = true;
                    break;
                }
            }
        }

        reaching.put(key, found);
        return found;
    }

    @Nullable
    private Declaration callee(MethodInsnNode call, ClassLoader loader, boolean onSelf) {
        if (LambdaResolver.isPlatform(call.owner) || call.name.startsWith("<")) {
            return null;
        }

        try {
            boolean special = call.getOpcode() == Opcodes.INVOKESPECIAL;
            Class<?> declared = load(call.owner, loader);
            Class<?> start = onSelf && !special && declared.isAssignableFrom(self) ? self : declared;

            if (declaration(start, call.name, call.desc, special) instanceof Declaration found && !LambdaResolver.isPlatform(Type.getInternalName(found.owner()))) {
                return found;
            }
        } catch (Fail ignored) {}

        return null;
    }

    private Arg source(Context context, Frame<SourceValue>[] analyzed, SourceValue value) {
        return trace(context.holder().method(), analyzed, value, context.holder().owner().getClassLoader(), (var) -> parameter(context, var), () -> new Computed(context));
    }

    private static Arg trace(MethodNode method, Frame<SourceValue>[] analyzed, SourceValue value, ClassLoader loader, IntFunction<Arg> parameter, Supplier<Arg> computed) {
        if (value.insns.size() != 1) {
            return computed.get();
        }

        AbstractInsnNode insn = value.insns.iterator().next();
        Frame<SourceValue> at = analyzed[method.instructions.indexOf(insn)];

        if (at == null) {
            return computed.get();
        }
        if (insn.getOpcode() == Opcodes.DUP || insn.getOpcode() == Opcodes.CHECKCAST) {
            return trace(method, analyzed, at.getStack(at.getStackSize() - 1), loader, parameter, computed);
        }
        if (insn.getOpcode() == Opcodes.ALOAD) {
            int var = ((VarInsnNode) insn).var;
            SourceValue local = at.getLocal(var);

            if (local.insns.isEmpty()) {
                return parameter.apply(var);
            }
            if (local.insns.size() == 1 && local.insns.iterator().next() instanceof VarInsnNode store && store.getOpcode() == Opcodes.ASTORE) {
                Frame<SourceValue> stored = analyzed[method.instructions.indexOf(store)];

                return stored != null ? trace(method, analyzed, stored.getStack(stored.getStackSize() - 1), loader, parameter, computed) : computed.get();
            }

            return computed.get();
        }
        if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC && Type.getType(field.desc).getSort() == Type.OBJECT) {
            try {
                return new Constant(field(load(field.owner, loader), field.name).get(null));
            } catch (Throwable e) {
                return computed.get();
            }
        }
        if (insn instanceof MethodInsnNode call && call.getOpcode() != Opcodes.INVOKESTATIC
                && (RuntimeNames.is(RuntimeNames.GET_DEFAULT_LOOT_TABLE, call) || RuntimeNames.is(RuntimeNames.MOB_GET_LOOT_TABLE, call))
                && trace(method, analyzed, at.getStack(at.getStackSize() - 1), loader, parameter, computed) instanceof Self) {
            return OwnDefault.INSTANCE;
        }

        return computed.get();
    }

    private static Arg parameter(Context context, int var) {
        int index = parameterIndex(context.holder().method(), var);

        if (index == RECEIVER) {
            return context.receiver() != null ? context.receiver() : new Computed(context);
        }

        return index >= 0 ? context.params()[index] : new Computed(context);
    }

    private static int parameterIndex(MethodNode method, int var) {
        int slot = 0;

        if ((method.access & Opcodes.ACC_STATIC) == 0) {
            if (var == 0) {
                return RECEIVER;
            }

            slot = 1;
        }

        Type[] params = Type.getArgumentTypes(method.desc);

        for (int i = 0; i < params.length; i++) {
            if (slot == var) {
                return i;
            }

            slot += params[i].getSize();
        }

        return NONE;
    }

    @NotNull
    private Site withLive(Site site) {
        return site.source() == Other.INSTANCE ? new Site(site.source(), site.holder(), site.selfReceiver(), site.selfParams(), live(site.holder())) : site;
    }

    @Nullable
    private BitSet live(Declaration holder) {
        MethodNode method = holder.method();
        Class<?> owner = holder.owner();
        List<List<Integer>> predecessors = new ArrayList<>();

        for (int i = 0; i < method.instructions.size(); i++) {
            predecessors.add(new ArrayList<>());
        }

        try {
            new Analyzer<>(new SourceInterpreter()) {
                @Override
                protected void newControlFlowEdge(int insn, int successor) {
                    predecessors.get(successor).add(insn);
                }

                @Override
                protected boolean newControlFlowExceptionEdge(int insn, int successor) {
                    predecessors.get(successor).add(insn);
                    return true;
                }
            }.analyze(Type.getInternalName(owner), method);
        } catch (AnalyzerException e) {
            return null;
        }

        boolean onSelf = (method.access & Opcodes.ACC_STATIC) == 0 && owner.isAssignableFrom(self);
        BitSet live = new BitSet(method.instructions.size());
        Deque<Integer> queue = new ArrayDeque<>();

        for (int i = 0; i < method.instructions.size(); i++) {
            if (method.instructions.get(i) instanceof MethodInsnNode call && leadsToSink(call, owner.getClassLoader(), onSelf)) {
                live.set(i);
                queue.add(i);
            }
        }

        while (!queue.isEmpty()) {
            for (int predecessor : predecessors.get(queue.poll())) {
                if (!live.get(predecessor)) {
                    live.set(predecessor);
                    queue.add(predecessor);
                }
            }
        }

        return live;
    }

    private boolean leadsToSink(MethodInsnNode call, ClassLoader loader, boolean onSelf) {
        if (sink.matches(call, loader)) {
            return true;
        }

        Declaration callee = callee(call, loader, onSelf);

        return callee != null && reaches(callee.owner(), callee.method(), 1);
    }

    @NotNull
    private static Site site(Arg arg, Context context) {
        switch (arg) {
            case Constant constant -> {
                return new Site(constant, context.holder(), false, Set.of(), null);
            }
            case EntryParam(int index) -> {
                return new Site(new Param(index), context.holder(), false, Set.of(), null);
            }
            case OwnDefault ignored -> {
                return new Site(OwnDefault.INSTANCE, context.holder(), false, Set.of(), null);
            }
            default -> {}
        }

        Context holder = arg instanceof Computed(Context computed) ? computed : context;
        Set<Integer> selfParams = new LinkedHashSet<>();

        for (int i = 0; i < holder.params().length; i++) {
            if (holder.params()[i] instanceof Self) {
                selfParams.add(i);
            }
        }

        return new Site(Other.INSTANCE, holder.holder(), holder.receiver() instanceof Self, selfParams, null);
    }

    @Nullable
    private Frame<SourceValue>[] frames(Class<?> owner, MethodNode method) {
        return frames.computeIfAbsent(method, (m) -> Optional.ofNullable(analyze(Type.getInternalName(owner), m))).orElse(null);
    }

    private static Frame<SourceValue>[] analyze(String owner, MethodNode method) {
        try {
            return new Analyzer<>(new SourceInterpreter()).analyze(owner, method);
        } catch (AnalyzerException e) {
            return null;
        }
    }

    record Sink(RuntimeNames.Ref method, Class<?> owner, int argument) {
        boolean matches(MethodInsnNode call, ClassLoader loader) {
            if (!RuntimeNames.is(method, call)) {
                return false;
            }

            try {
                return owner.isAssignableFrom(load(call.owner, loader));
            } catch (Fail fail) {
                return false;
            }
        }
    }

    record Site(Source source, Declaration holder, boolean selfReceiver, Set<Integer> selfParams, @Nullable BitSet live) {
    }

    sealed interface Source permits Constant, Param, OwnDefault, Other {
    }

    record Constant(@Nullable Object value) implements Source, Arg {
    }

    record Param(int index) implements Source {
    }

    enum OwnDefault implements Source, Arg {
        INSTANCE
    }

    enum Other implements Source {
        INSTANCE
    }

    private sealed interface Arg permits Constant, OwnDefault, Self, Untraced, EntryParam, Computed {
    }

    private enum Self implements Arg {
        INSTANCE
    }

    private enum Untraced implements Arg {
        INSTANCE
    }

    private record EntryParam(int index) implements Arg {
    }

    private record Computed(Context context) implements Arg {
    }

    private record Context(Declaration holder, @Nullable Arg receiver, Arg[] params) {
    }

    private record Reach(MethodNode method, int depth) {
    }
}
