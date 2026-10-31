package com.yanny.ali.plugin.server.bytecode;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.*;

import static com.yanny.ali.plugin.server.bytecode.Resolution.Declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.declaration;
import static com.yanny.ali.plugin.server.bytecode.Resolution.field;
import static com.yanny.ali.plugin.server.bytecode.Resolution.load;

final class CallSites {
    static final int DEPTH = 3;

    private final Class<?> self;
    private final Sink sink;
    private final Map<MethodNode, Optional<Frame<SourceValue>[]>> frames = new IdentityHashMap<>();
    private final Map<Reach, Boolean> reaching = new HashMap<>();
    private final Set<String> visited = new HashSet<>();
    private final List<Site> sites = new ArrayList<>();

    private CallSites(Class<?> self, Sink sink) {
        this.self = self;
        this.sink = sink;
    }

    static List<Site> find(Class<?> self, Class<?> owner, MethodNode method, Sink sink) {
        CallSites search = new CallSites(self, sink);
        Type[] params = Type.getArgumentTypes(method.desc);
        Arg[] args = new Arg[params.length];

        for (int i = 0; i < params.length; i++) {
            args[i] = new EntryParam(i);
        }

        search.walk(new Context(new Declaration(owner, method), (method.access & Opcodes.ACC_STATIC) == 0 ? Self.INSTANCE : null, args), 0);
        return search.sites;
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

            if (isSink(call, owner.getClassLoader())) {
                sites.add(site(source(context, analyzed, frame.getStack(first + sink.argument())), context));
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

        boolean onSelf = (method.access & Opcodes.ACC_STATIC) == 0 && owner.isAssignableFrom(self);
        boolean found = false;

        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call) {
                if (isSink(call, owner.getClassLoader())) {
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

    private boolean isSink(MethodInsnNode call, ClassLoader loader) {
        if (!RuntimeNames.is(sink.method(), call)) {
            return false;
        }

        try {
            return sink.owner().isAssignableFrom(load(call.owner, loader));
        } catch (Fail fail) {
            return false;
        }
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
        MethodNode method = context.holder().method();
        Class<?> owner = context.holder().owner();

        if (value.insns.size() != 1) {
            return new Computed(context);
        }

        AbstractInsnNode insn = value.insns.iterator().next();
        Frame<SourceValue> at = analyzed[method.instructions.indexOf(insn)];

        if (at == null) {
            return new Computed(context);
        }
        if (insn.getOpcode() == Opcodes.DUP || insn.getOpcode() == Opcodes.CHECKCAST) {
            return source(context, analyzed, at.getStack(at.getStackSize() - 1));
        }
        if (insn.getOpcode() == Opcodes.ALOAD) {
            int var = ((VarInsnNode) insn).var;
            SourceValue local = at.getLocal(var);

            if (local.insns.isEmpty()) {
                return parameter(context, var);
            }
            if (local.insns.size() == 1 && local.insns.iterator().next() instanceof VarInsnNode store && store.getOpcode() == Opcodes.ASTORE) {
                Frame<SourceValue> stored = analyzed[method.instructions.indexOf(store)];

                return stored != null ? source(context, analyzed, stored.getStack(stored.getStackSize() - 1)) : new Computed(context);
            }

            return new Computed(context);
        }
        if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC && Type.getType(field.desc).getSort() == Type.OBJECT) {
            try {
                return new Constant(field(load(field.owner, owner.getClassLoader()), field.name).get(null));
            } catch (Throwable e) {
                return new Computed(context);
            }
        }
        if (insn instanceof MethodInsnNode call && call.getOpcode() != Opcodes.INVOKESTATIC
                && (RuntimeNames.is(RuntimeNames.GET_DEFAULT_LOOT_TABLE, call) || RuntimeNames.is(RuntimeNames.MOB_GET_LOOT_TABLE, call))
                && source(context, analyzed, at.getStack(at.getStackSize() - 1)) instanceof Self) {
            return OwnDefault.INSTANCE;
        }

        return new Computed(context);
    }

    private static Arg parameter(Context context, int var) {
        MethodNode method = context.holder().method();
        int slot = 0;

        if ((method.access & Opcodes.ACC_STATIC) == 0) {
            if (var == 0) {
                return context.receiver() != null ? context.receiver() : new Computed(context);
            }

            slot = 1;
        }

        Type[] params = Type.getArgumentTypes(method.desc);

        for (int i = 0; i < params.length; i++) {
            if (slot == var) {
                return context.params()[i];
            }

            slot += params[i].getSize();
        }

        return new Computed(context);
    }

    private static Site site(Arg arg, Context context) {
        if (arg instanceof Constant constant) {
            return new Site(constant, context.holder(), false, Set.of());
        }
        if (arg instanceof EntryParam param) {
            return new Site(new Param(param.index()), context.holder(), false, Set.of());
        }
        if (arg instanceof OwnDefault) {
            return new Site(OwnDefault.INSTANCE, context.holder(), false, Set.of());
        }

        Context holder = arg instanceof Computed computed ? computed.context() : context;
        Set<Integer> selfParams = new LinkedHashSet<>();

        for (int i = 0; i < holder.params().length; i++) {
            if (holder.params()[i] instanceof Self) {
                selfParams.add(i);
            }
        }

        return new Site(Other.INSTANCE, holder.holder(), holder.receiver() instanceof Self, selfParams);
    }

    @Nullable
    private Frame<SourceValue>[] frames(Class<?> owner, MethodNode method) {
        return frames.computeIfAbsent(method, (m) -> {
            try {
                return Optional.of(new Analyzer<>(new SourceInterpreter()).analyze(Type.getInternalName(owner), m));
            } catch (AnalyzerException e) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    record Sink(RuntimeNames.Ref method, Class<?> owner, int argument) {
    }

    record Site(Source source, Declaration holder, boolean selfReceiver, Set<Integer> selfParams) {
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

    private sealed interface Arg permits Constant, OwnDefault, Self, EntryParam, Computed {
    }

    private enum Self implements Arg {
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
