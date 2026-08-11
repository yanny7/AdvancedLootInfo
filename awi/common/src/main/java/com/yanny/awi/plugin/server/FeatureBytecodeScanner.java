package com.yanny.awi.plugin.server;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.slf4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proof of concept: statically discovers the blocks a {@link Feature} places directly in its {@code place()} method
 * (category C - blocks hardcoded in bytecode, absent from the {@code FeatureConfiguration}), without ever running
 * world generation.
 *
 * <p>Approach: ASM data-flow (sink tracking). Only {@link BlockState}/{@link Block} values that flow as an argument
 * into a block-placing sink ({@code LevelWriter#setBlock} / {@code Feature#setBlock}) are collected, so blocks that
 * are merely read/checked (e.g. an {@code END_STONE} support check) are NOT reported.
 *
 * <p>Mapping-agnostic by design: nothing is matched by method/field/class NAME (those are remapped in production).
 * Sinks and value types are identified via assignability of real {@link Class} objects, and concrete blocks are
 * resolved by reflectively reading the referenced static field's value.
 *
 * <p>The value flowing into a sink is followed backwards across four hops, each of which is load-bearing for real
 * vanilla features - without them only a block written inline in the {@code setBlock} argument would ever be seen:
 * <ul>
 *     <li>local variables, back to the stores that reach them ({@link #throughLocal});</li>
 *     <li>method parameters, back to what call sites pass in ({@link #bindArguments} / {@link #addParameterCandidates});</li>
 *     <li>{@code CHECKCAST}, and the receiver plus block-carrying arguments of the call that produced the value, which
 *     is what exposes a tag-driven pick such as {@code BLOCK.getTag(BlockTags.CORAL_BLOCKS)} through an {@code Optional}
 *     chain;</li>
 *     <li>lambda and method-reference bodies, through the {@code INVOKEDYNAMIC} bootstrap handle, including the
 *     parameters the lambda receives from the call it is handed to rather than from a capture
 *     ({@link #bindLambdaParameters}).</li>
 * </ul>
 *
 * <p>Method lookup walks the class hierarchy in both directions. Up, because {@code place()} is frequently declared
 * on an abstract base ({@code CoralFeature}, {@code AbstractHugeMushroomFeature}) rather than on the registered
 * subclass. Down, because such a base then calls back into an abstract method the subclass implements
 * ({@code CoralFeature#place} -> abstract {@code placeFeature}); stopping at the abstract declaration would lose
 * every block placed below it.
 *
 * <p>Branches guarded by a {@code static final boolean} flag that reads {@code false} are pruned before any of that
 * happens - see {@link #reachableInstructions}.
 *
 * <p>Known limitations: a lambda parameter is only bound when the receiver it is applied to resolves to blocks, and
 * an abstract call is only dispatched to the class being scanned, not to arbitrary implementors.
 */
public final class FeatureBytecodeScanner {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Runaway guard, not a tuning knob: lowering it silently drops blocks rather than failing. Vanilla's widest
    // feature (fossil) analyzes 422 methods and no other reaches 200, so this sits at ~2x the measured worst case.
    private static final int MAX_METHODS = 1000;
    private static final int MAX_RESOLVE_DEPTH = 12;

    private static final Map<Class<?>, ScanResult> RESULT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ClassNode> CLASS_NODE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    // Runtime name+descriptor of Feature#place(FeaturePlaceContext), resolved reflectively (mapping-agnostic).
    private static final String PLACE_NAME;
    private static final String PLACE_DESC;

    static {
        Method place = null;

        for (Method m : Feature.class.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers()) && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == FeaturePlaceContext.class && m.getReturnType() == boolean.class) {
                place = m;
                break;
            }
        }

        PLACE_NAME = place != null ? place.getName() : null;
        PLACE_DESC = place != null ? Type.getMethodDescriptor(place) : null;
    }

    /** Callee method key -> parameter position -> what its call sites were seen passing in. One scan's worth. */
    private final Map<String, Map<Integer, Candidates>> parameterCandidates = new HashMap<>();

    /** Key of the method being analyzed, so a parameter can be matched against {@link #parameterCandidates}. */
    private String currentMethodKey;

    /** Instructions of the method being analyzed that survive {@link #reachableInstructions}. */
    private Set<AbstractInsnNode> currentReachable;

    private FeatureBytecodeScanner() {}

    /**
     * Scans one feature class for the blocks and block tags it places. Needs no registry and no running server: a tag
     * is reported as the tag itself, and resolving it into members is the display's job
     * ({@link com.yanny.awi.plugin.common.nodes.BlockNode}).
     */
    public static ScanResult scan(Class<?> featureClass) {
        if (PLACE_NAME == null) {
            return ScanResult.EMPTY;
        }

        return RESULT_CACHE.computeIfAbsent(featureClass, (cls) -> {
            try {
                return new FeatureBytecodeScanner().doScan(cls);
            } catch (Throwable t) {
                LOGGER.warn("Bytecode scan failed for {}", cls.getName(), t);
                return ScanResult.EMPTY;
            }
        });
    }

    /**
     * Drops every cached scan result and the intermediate ASM/class caches. Must be called once the worldgen scan is
     * done: the caches are only useful within a single scan and they retain the {@link ClassNode} graph of every
     * visited class.
     */
    public static void clearCaches() {
        RESULT_CACHE.clear();
        CLASS_NODE_CACHE.clear();
        CLASS_CACHE.clear();
    }

    private ScanResult doScan(Class<?> featureClass) {
        ClassLoader cl = featureClass.getClassLoader();
        String rootPkg = rootPackage(Type.getInternalName(featureClass));

        Candidates out = Candidates.create();
        Set<String> visited = new HashSet<>();
        Deque<MethodRef> worklist = new ArrayDeque<>();
        int analyzed = 0;

        worklist.add(new MethodRef(Type.getInternalName(featureClass), PLACE_NAME, PLACE_DESC));

        while (!worklist.isEmpty() && analyzed < MAX_METHODS) {
            MethodRef ref = worklist.poll();

            if (!visited.add(ref.key())) {
                continue;
            }

            Declaration declaration = declaration(cl, ref.owner, ref.name, ref.desc);

            // An abstract declaration is dispatched at runtime. A feature calling itself (CoralFeature#place ->
            // abstract placeFeature) has the scanned class as its receiver, so the body to analyze is the override
            // there - stopping at the abstract declaration loses everything below it.
            if (declaration != null && declaration.isAbstract() && isSubclass(cl, featureClass, ref.owner)) {
                Declaration override = declaration(cl, Type.getInternalName(featureClass), ref.name, ref.desc);

                declaration = override != null && !override.isAbstract() ? override : declaration;
            }

            if (declaration == null || declaration.isAbstract()
                    || (declaration.method().access & Opcodes.ACC_NATIVE) != 0
                    || declaration.method().instructions.size() == 0) {
                continue;
            }

            analyzed++;
            // Keyed by the reference, not the declaring class, so it matches what a call site records for it.
            currentMethodKey = ref.key();
            analyzeMethod(cl, declaration.owner(), declaration.method(), rootPkg, out, worklist, visited);
        }

        // Only a still-pending, not-yet-visited method means the budget actually cut the walk short.
        boolean truncated = worklist.stream().anyMatch((ref) -> !visited.contains(ref.key()));

        return new ScanResult(out.blocks(), out.tags(), analyzed, truncated);
    }

    private void analyzeMethod(ClassLoader cl, ClassNode owner, MethodNode method, String rootPkg, Candidates out,
                               Deque<MethodRef> worklist, Set<String> visited) {
        Frame<SourceValue>[] frames;

        try {
            frames = new Analyzer<>(new SourceInterpreter()).analyze(owner.name, method);
        } catch (Throwable t) {
            LOGGER.debug("Analyzer failed for {}.{}{}", owner.name, method.name, method.desc);
            return;
        }

        currentReachable = reachableInstructions(cl, method);

        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!currentReachable.contains(insn)) {
                continue;
            }

            // A lambda/method reference body is a separate (usually synthetic) method reachable only through the
            // bootstrap handle - features routinely place their blocks in one (e.g. UnderwaterMagmaFeature's forEach).
            if (insn instanceof InvokeDynamicInsnNode indy) {
                queueBootstrapTargets(indy, rootPkg, worklist);
                continue;
            }

            if (!(insn instanceof MethodInsnNode call)) {
                continue;
            }

            int index = method.instructions.indexOf(call);
            Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;

            if (frame == null) {
                continue;
            }

            // Transitive follow into feature/block/level-gen code and the mod's own package.
            if (shouldRecurse(call.owner, rootPkg)) {
                worklist.add(new MethodRef(call.owner, call.name, call.desc));
                bindArguments(cl, call, frame, frames, method, worklist, visited);
            }

            // Not gated on shouldRecurse: the call handing a lambda its values is usually a JDK one (Optional#ifPresent).
            bindLambdaParameters(cl, call, frame, frames, method, rootPkg, worklist, visited);

            Class<?> callOwner = loadClass(cl, call.owner);

            if (callOwner == null || !(LevelWriter.class.isAssignableFrom(callOwner) || Feature.class.isAssignableFrom(callOwner))) {
                continue;
            }

            Type[] args = Type.getArgumentTypes(call.desc);

            for (int i = 0; i < args.length; i++) {
                Class<?> argClass = loadClass(cl, args[i].getInternalName());

                if (argClass != null && BlockState.class.isAssignableFrom(argClass)) {
                    SourceValue value = argValue(frame, call, i);

                    if (value != null) {
                        resolve(value, frames, method, cl, out, new HashSet<>(), 0);
                    }
                }
            }
        }
    }

    /**
     * Records, per callee parameter, what a call site passes in - the caller half of the interprocedural link.
     * A feature that hands the block to a helper ({@code placeFeature(level, random, pos, state)} in {@code
     * CoralFeature}) places it through a parameter, which has no producer instruction of its own; the callee half
     * ({@link #addParameterCandidates}) picks these up when that parameter reaches a placement sink.
     *
     * <p>Recording is unconditional but harmless: a block merely handed to a predicate is never materialized, because
     * only a parameter that actually flows into a sink is ever looked up.
     *
     * <p>A callee analyzed before this call site was reached never saw these blocks, so it is dropped from
     * {@code visited} and re-queued. The candidate sets only ever grow, so the re-queueing terminates.
     */
    private void bindArguments(ClassLoader cl, MethodInsnNode call, Frame<SourceValue> frame, Frame<SourceValue>[] frames,
                               MethodNode method, Deque<MethodRef> worklist, Set<String> visited) {
        Type[] args = Type.getArgumentTypes(call.desc);

        for (int i = 0; i < args.length; i++) {
            Class<?> argClass = loadClass(cl, args[i].getInternalName());

            if (argClass == null || !carriesBlocks(argClass)) {
                continue;
            }

            Candidates resolved = Candidates.create();

            resolve(argValue(frame, call, i), frames, method, cl, resolved, new HashSet<>(), 0);

            if (resolved.isEmpty()) {
                continue;
            }

            recordCandidates(new MethodRef(call.owner, call.name, call.desc), i, resolved, worklist, visited);
        }
    }

    /**
     * Binds the parameters a lambda receives from the call it is handed to, rather than from a capture. In
     * {@code BLOCK.getTag(BlockTags.CORALS)...ifPresent(block -> level.setBlock(pos, block.defaultBlockState(), 2))}
     * the placed block is the {@code Consumer}'s own argument, so it has no producer anywhere in the enclosing method
     * - it comes out of the receiver the lambda is being applied to. Resolving that receiver and binding it to the
     * implementation method's trailing parameters is what recovers the coral fans.
     *
     * <p>The receiver is only resolved once a lambda is actually found among the arguments, and the result is bound
     * only to parameters that can carry a block, so a lambda over positions or random sources binds nothing.
     */
    private void bindLambdaParameters(ClassLoader cl, MethodInsnNode call, Frame<SourceValue> frame, Frame<SourceValue>[] frames,
                                      MethodNode method, String rootPkg, Deque<MethodRef> worklist, Set<String> visited) {
        if (call.getOpcode() == Opcodes.INVOKESTATIC) {
            return;
        }

        Type[] args = Type.getArgumentTypes(call.desc);
        Candidates fromReceiver = null;

        for (int i = 0; i < args.length; i++) {
            SourceValue value = argValue(frame, call, i);

            if (value == null) {
                continue;
            }

            for (AbstractInsnNode producer : value.insns) {
                if (!(producer instanceof InvokeDynamicInsnNode indy)) {
                    continue;
                }

                if (fromReceiver == null) {
                    fromReceiver = Candidates.create();
                    resolve(receiverValue(frame, call), frames, method, cl, fromReceiver, new HashSet<>(), 0);
                }

                if (!fromReceiver.isEmpty()) {
                    bindFunctionalParameters(cl, indy, fromReceiver, rootPkg, worklist, visited);
                }
            }
        }
    }

    /**
     * Binds {@code candidates} to the parameters of a lambda body that come from the functional interface. An
     * {@code INVOKEDYNAMIC} consumes exactly the captured values off the stack, so the implementation method's
     * parameters start with the captures and the functional arguments make up the rest.
     */
    private void bindFunctionalParameters(ClassLoader cl, InvokeDynamicInsnNode indy, Candidates candidates, String rootPkg,
                                          Deque<MethodRef> worklist, Set<String> visited) {
        int captured = Type.getArgumentTypes(indy.desc).length;

        for (Object arg : indy.bsmArgs) {
            if (!(arg instanceof Handle handle) || handle.getTag() < Opcodes.H_INVOKEVIRTUAL
                    || !shouldRecurse(handle.getOwner(), rootPkg)) {
                continue;
            }

            // A non-static implementation takes its receiver from the first captured value, so it consumes one
            // capture without spending a parameter.
            Type[] implArgs = Type.getArgumentTypes(handle.getDesc());
            int first = Math.max(captured - (handle.getTag() == Opcodes.H_INVOKESTATIC ? 0 : 1), 0);

            for (int i = first; i < implArgs.length; i++) {
                Class<?> type = loadClass(cl, implArgs[i].getInternalName());

                if (type != null && carriesBlocks(type)) {
                    recordCandidates(new MethodRef(handle.getOwner(), handle.getName(), handle.getDesc()), i, candidates,
                            worklist, visited);
                }
            }
        }
    }

    /** Adds call-site values to a callee parameter, re-queueing the callee if it was already analyzed without them. */
    private void recordCandidates(MethodRef callee, int parameter, Candidates candidates, Deque<MethodRef> worklist, Set<String> visited) {
        Candidates known = parameterCandidates.computeIfAbsent(callee.key(), (key) -> new HashMap<>())
                .computeIfAbsent(parameter, (index) -> Candidates.create());

        if (known.addAll(candidates) && visited.remove(callee.key())) {
            worklist.add(callee);
        }
    }

    /** Callee half of the interprocedural link: a parameter resolves to whatever its call sites were seen passing in. */
    private void addParameterCandidates(VarInsnNode local, MethodNode method, Candidates out) {
        Map<Integer, Candidates> known = currentMethodKey != null ? parameterCandidates.get(currentMethodKey) : null;

        if (local.getOpcode() != Opcodes.ALOAD || known == null) {
            return;
        }

        Candidates candidates = known.get(parameterIndex(method, local.var));

        if (candidates != null) {
            out.addAll(candidates);
        }
    }

    /** Maps a local variable slot back to its parameter position, or {@code -1} if the slot is not a parameter. */
    private static int parameterIndex(MethodNode method, int slot) {
        Type[] args = Type.getArgumentTypes(method.desc);
        int current = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;

        for (int i = 0; i < args.length; i++) {
            if (current == slot) {
                return i;
            }

            current += args[i].getSize();
        }

        return -1;
    }

    /** Types that can carry a concrete block into a placement, and are therefore worth following. */
    private static boolean carriesBlocks(Class<?> type) {
        return Block.class.isAssignableFrom(type) || BlockState.class.isAssignableFrom(type)
                || TagKey.class.isAssignableFrom(type);
    }

    /**
     * Queues the implementation methods referenced by an {@code INVOKEDYNAMIC}'s bootstrap arguments. Covers
     * {@code LambdaMetafactory} without depending on it: any method handle among the arguments is a body worth
     * analyzing, and non-lambda bootstraps (string concatenation, record {@code ObjectMethods}) either carry no handle
     * or carry accessors that place nothing.
     */
    private static void queueBootstrapTargets(InvokeDynamicInsnNode indy, String rootPkg, Deque<MethodRef> worklist) {
        for (Object arg : indy.bsmArgs) {
            // Tags below H_INVOKEVIRTUAL are field handles, whose descriptor would never match a method.
            if (arg instanceof Handle handle && handle.getTag() >= Opcodes.H_INVOKEVIRTUAL
                    && shouldRecurse(handle.getOwner(), rootPkg)) {
                worklist.add(new MethodRef(handle.getOwner(), handle.getName(), handle.getDesc()));
            }
        }
    }

    /**
     * Resolves a stack value (producer instructions) to concrete blocks and block tags, following
     * defaultBlockState()/setValue() chains.
     */
    private void resolve(SourceValue value, Frame<SourceValue>[] frames, MethodNode method, ClassLoader cl,
                         Candidates out, Set<AbstractInsnNode> guard, int depth) {
        if (value == null || depth > MAX_RESOLVE_DEPTH) {
            return;
        }

        for (AbstractInsnNode producer : value.insns) {
            // The analyzer merges both branches of a dead flag test into one stack value, so the dead half has to be
            // dropped here too - otherwise LargeDripstoneFeature's DEBUG ? GLASS : DRIPSTONE_BLOCK reports glass.
            if (currentReachable != null && !currentReachable.contains(producer)) {
                continue;
            }

            if (!guard.add(producer)) {
                continue;
            }

            if (producer instanceof FieldInsnNode field && producer.getOpcode() == Opcodes.GETSTATIC) {
                addFieldValue(cl, field, out);
            } else if (producer instanceof VarInsnNode local) {
                SourceValue stored = throughLocal(local, frames, method);

                if (stored != null && !stored.insns.isEmpty()) {
                    resolve(stored, frames, method, cl, out, guard, depth + 1);
                } else {
                    addParameterCandidates(local, method, out);
                }
            } else if (producer instanceof TypeInsnNode cast && producer.getOpcode() == Opcodes.CHECKCAST) {
                // Optional#get() and friends return Object; the cast is the only thing standing between the call and
                // the block it produced.
                resolve(stackTop(cast, frames, method), frames, method, cl, out, guard, depth + 1);
            } else if (producer instanceof MethodInsnNode call) {
                int index = method.instructions.indexOf(call);
                Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;

                if (frame == null) {
                    continue;
                }

                // The produced value was computed from the receiver and the arguments, so both are genuine
                // dependencies: defaultBlockState()/setValue() live in the receiver chain, while a tag-driven pick
                // (BuiltInRegistries.BLOCK.getTag(BlockTags.CORAL_BLOCKS)...) only exposes its TagKey as an argument.
                if (call.getOpcode() != Opcodes.INVOKESTATIC) {
                    resolve(receiverValue(frame, call), frames, method, cl, out, guard, depth + 1);
                }

                Type[] args = Type.getArgumentTypes(call.desc);

                for (int i = 0; i < args.length; i++) {
                    Class<?> argClass = loadClass(cl, args[i].getInternalName());

                    if (argClass != null && carriesBlocks(argClass)) {
                        resolve(argValue(frame, call, i), frames, method, cl, out, guard, depth + 1);
                    }
                }
            }
        }
    }

    /**
     * Hops one step across a local variable, which {@link SourceInterpreter} treats as an opaque producer: a load's
     * producer is the load itself, not the value that was stored. A load resolves to whatever the slot holds at that
     * point (the stores reaching it), and a store resolves to the value on top of the stack when it ran. A block
     * assigned to a local before being placed - {@code BlockState state = flag ? TALL_SEAGRASS... : SEAGRASS...;} then
     * {@code setBlock(pos, state, 2)} - is only reachable this way; the two branches of the ternary merge on the stack
     * before the store, so both arrive.
     *
     * <p>Returns {@code null} for a slot that holds a method parameter, which has no producer instruction to follow.
     */
    private static SourceValue throughLocal(VarInsnNode local, Frame<SourceValue>[] frames, MethodNode method) {
        int index = method.instructions.indexOf(local);
        Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;

        if (frame == null) {
            return null;
        }

        if (local.getOpcode() == Opcodes.ALOAD) {
            return local.var < frame.getLocals() ? frame.getLocal(local.var) : null;
        }

        if (local.getOpcode() == Opcodes.ASTORE) {
            return frame.getStackSize() > 0 ? frame.getStack(frame.getStackSize() - 1) : null;
        }

        return null;
    }

    /** The value an instruction consumed from the top of the stack, for single-operand pass-through nodes. */
    private static SourceValue stackTop(AbstractInsnNode insn, Frame<SourceValue>[] frames, MethodNode method) {
        int index = method.instructions.indexOf(insn);
        Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;

        return frame != null && frame.getStackSize() > 0 ? frame.getStack(frame.getStackSize() - 1) : null;
    }

    @SuppressWarnings("unchecked")
    private void addFieldValue(ClassLoader cl, FieldInsnNode field, Candidates out) {
        Type type = Type.getType(field.desc);

        if (type.getSort() != Type.OBJECT) {
            return;
        }

        Class<?> fieldType = loadClass(cl, type.getInternalName());

        if (fieldType == null) {
            return;
        }

        boolean isBlock = Block.class.isAssignableFrom(fieldType);
        boolean isState = BlockState.class.isAssignableFrom(fieldType);
        boolean isTag = TagKey.class.isAssignableFrom(fieldType);

        if (!isBlock && !isState && !isTag) {
            return;
        }

        Object staticValue = staticFieldValue(cl, field.owner, field.name);

        if (staticValue instanceof Block block) {
            out.blocks().add(block);
        } else if (staticValue instanceof BlockState state) {
            out.blocks().add(state.getBlock());
        } else if (staticValue instanceof TagKey<?> tag && tag.registry().equals(Registries.BLOCK)) {
            out.tags().add((TagKey<Block>) tag);
        }
    }

    // -- reachability ----------------------------------------------------------------------------------------------

    /**
     * Forward control-flow sweep that folds tests on {@code static final boolean} flags, so a branch that can never run
     * places nothing. Vanilla's debug markers are the reason this exists: up to 1.21.8 {@code SharedConstants.DEBUG_*}
     * were compile-time constants and javac stripped the branches guarded by them, but they are now initialized from a
     * method call, so the bytecode carries them and a plain walk reports {@code LargeDripstoneFeature}'s diamond block,
     * gold block and creeper head as blocks the feature places.
     *
     * <p>Only {@code IFEQ}/{@code IFNE} directly on a {@code GETSTATIC} of a final boolean is folded, and only when the
     * value can be read reflectively; anything else keeps both successors, so this can never prune live code.
     */
    private static Set<AbstractInsnNode> reachableInstructions(ClassLoader cl, MethodNode method) {
        Set<AbstractInsnNode> reachable = new HashSet<>();
        Deque<AbstractInsnNode> worklist = new ArrayDeque<>();

        if (method.instructions.getFirst() != null) {
            worklist.add(method.instructions.getFirst());
        }

        // A handler is entered by the JVM, not by an edge from the guarded region, so it has no predecessor to reach it.
        method.tryCatchBlocks.forEach((handler) -> worklist.add(handler.handler));

        while (!worklist.isEmpty()) {
            AbstractInsnNode insn = worklist.poll();

            if (insn == null || !reachable.add(insn)) {
                continue;
            }

            int opcode = insn.getOpcode();

            if (insn instanceof JumpInsnNode jump) {
                Boolean taken = jump.getOpcode() == Opcodes.GOTO ? Boolean.TRUE : constantBranch(cl, jump);

                if (taken == null || taken) {
                    worklist.add(jump.label);
                }

                if (taken == null || !taken) {
                    worklist.add(jump.getNext());
                }
            } else if (insn instanceof TableSwitchInsnNode tableSwitch) {
                worklist.add(tableSwitch.dflt);
                worklist.addAll(tableSwitch.labels);
            } else if (insn instanceof LookupSwitchInsnNode lookupSwitch) {
                worklist.add(lookupSwitch.dflt);
                worklist.addAll(lookupSwitch.labels);
            } else if (!(opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) && opcode != Opcodes.ATHROW) {
                worklist.add(insn.getNext());
            }
        }

        return reachable;
    }

    /** Whether a conditional jump is statically always/never taken, or {@code null} if that is not decidable. */
    private static Boolean constantBranch(ClassLoader cl, JumpInsnNode jump) {
        if (jump.getOpcode() != Opcodes.IFEQ && jump.getOpcode() != Opcodes.IFNE) {
            return null;
        }

        AbstractInsnNode previous = jump.getPrevious();

        while (previous != null && previous.getOpcode() < 0) {
            previous = previous.getPrevious();
        }

        if (!(previous instanceof FieldInsnNode field) || previous.getOpcode() != Opcodes.GETSTATIC
                || !Type.BOOLEAN_TYPE.getDescriptor().equals(field.desc)) {
            return null;
        }

        Boolean value = constantFlag(cl, field.owner, field.name);

        return value == null ? null : jump.getOpcode() == Opcodes.IFEQ ? !value : value;
    }

    /** Reads a {@code static final boolean} field's value, or {@code null} if it is not one or cannot be read. */
    private static Boolean constantFlag(ClassLoader cl, String ownerInternal, String name) {
        try {
            Class<?> owner = loadClass(cl, ownerInternal);

            if (owner == null) {
                return null;
            }

            Field field = owner.getDeclaredField(name);

            if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
                return null;
            }

            field.setAccessible(true);
            return field.get(null) instanceof Boolean flag ? flag : null;
        } catch (Throwable t) {
            return null;
        }
    }

    // -- ASM stack helpers -----------------------------------------------------------------------------------------

    private static SourceValue receiverValue(Frame<SourceValue> frame, MethodInsnNode call) {
        int base = frame.getStackSize() - consumedEntries(call);
        return base >= 0 ? frame.getStack(base) : null;
    }

    private static SourceValue argValue(Frame<SourceValue> frame, MethodInsnNode call, int argIndex) {
        boolean isStatic = call.getOpcode() == Opcodes.INVOKESTATIC;
        // In the ASM analysis framework the operand stack holds ONE entry per value (long/double are not double-counted).
        int offset = frame.getStackSize() - consumedEntries(call) + (isStatic ? 0 : 1) + argIndex;
        return offset >= 0 && offset < frame.getStackSize() ? frame.getStack(offset) : null;
    }

    private static int consumedEntries(MethodInsnNode call) {
        int entries = call.getOpcode() == Opcodes.INVOKESTATIC ? 0 : 1;
        return entries + Type.getArgumentTypes(call.desc).length;
    }

    // -- loading / reflection --------------------------------------------------------------------------------------

    private static ClassNode classNode(ClassLoader cl, String internalName) {
        return CLASS_NODE_CACHE.computeIfAbsent(internalName, (name) -> {
            try (InputStream is = cl.getResourceAsStream(name + ".class")) {
                if (is == null) {
                    return null;
                }

                ClassNode node = new ClassNode();
                new ClassReader(is).accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                return node;
            } catch (Throwable t) {
                return null;
            }
        });
    }

    /** Resolves a method reference to the class that actually declares it, walking up the superclass chain. */
    private static Declaration declaration(ClassLoader cl, String internalName, String name, String desc) {
        ClassNode owner = classNode(cl, internalName);

        while (owner != null) {
            MethodNode method = findMethod(owner, name, desc);

            if (method != null) {
                return new Declaration(owner, method);
            }

            owner = owner.superName != null ? classNode(cl, owner.superName) : null;
        }

        return null;
    }

    private static boolean isSubclass(ClassLoader cl, Class<?> type, String internalName) {
        Class<?> superType = loadClass(cl, internalName);

        return superType != null && superType != type && superType.isAssignableFrom(type);
    }

    private static MethodNode findMethod(ClassNode owner, String name, String desc) {
        for (MethodNode method : owner.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }

        return null;
    }

    private static Class<?> loadClass(ClassLoader cl, String internalName) {
        return CLASS_CACHE.computeIfAbsent(internalName, (name) -> {
            try {
                return Class.forName(Type.getObjectType(name).getClassName(), false, cl);
            } catch (Throwable t) {
                return null;
            }
        });
    }

    private static Object staticFieldValue(ClassLoader cl, String ownerInternal, String name) {
        try {
            Class<?> owner = loadClass(cl, ownerInternal);

            if (owner == null) {
                return null;
            }

            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * <b>The {@code rootPkg} clause is load-bearing, not a fallback - do not narrow it.</b> For a vanilla feature
     * {@code rootPkg} degrades to {@code net/minecraft/}, which looks like it makes the two explicit prefixes
     * redundant. It is the other way round: under Fabric's production (intermediary) mappings every Minecraft class is
     * flat {@code net/minecraft/class_NNNN}, so the two package prefixes below match <i>nothing</i> there and the
     * {@code rootPkg} clause is the only thing keeping the walk alive. Restricting it to the two prefixes cuts the
     * walk by ~70% in dev (3464 -> 1051 methods over vanilla's features) without losing a single block, which is
     * exactly what makes it tempting - and it would reduce the scan to {@code place()} alone in a Fabric production
     * jar. Tests run against named mappings, so they cannot catch that regression.
     *
     * <p>The two explicit prefixes only ever apply on loaders that keep Mojang package names (Forge from 1.17 on).
     */
    private static boolean shouldRecurse(String owner, String rootPkg) {
        return owner.startsWith("net/minecraft/world/level/levelgen/")
                || owner.startsWith("net/minecraft/world/level/block/")
                || (rootPkg != null && owner.startsWith(rootPkg));
    }

    /** Two leading segments of a class's package - the mod's own namespace, or {@code net/minecraft/} for vanilla. */
    private static String rootPackage(String internalName) {
        int first = internalName.indexOf('/');
        int second = first < 0 ? -1 : internalName.indexOf('/', first + 1);
        return second < 0 ? null : internalName.substring(0, second + 1);
    }

    /**
     * @param blocks             blocks that flow into a placement sink
     * @param tags               block tags that flow into a placement sink, left unexpanded so a caller can either
     *                           display the tag itself or resolve its current members
     * @param visitedMethods     methods analyzed, out of the {@link #MAX_METHODS} budget
     * @param methodLimitReached whether the budget cut the walk short, so the results may be incomplete
     */
    public record ScanResult(Set<Block> blocks, Set<TagKey<Block>> tags, int visitedMethods, boolean methodLimitReached) {
        public static final ScanResult EMPTY = new ScanResult(Set.of(), Set.of(), 0, false);
    }

    /** What a value resolved to: concrete blocks, plus the block tags it was reached through. */
    private record Candidates(Set<Block> blocks, Set<TagKey<Block>> tags) {
        static Candidates create() {
            return new Candidates(new HashSet<>(), new HashSet<>());
        }

        boolean isEmpty() {
            return blocks.isEmpty() && tags.isEmpty();
        }

        boolean addAll(Candidates other) {
            boolean grown = blocks.addAll(other.blocks);

            return tags.addAll(other.tags) || grown;
        }
    }

    private record Declaration(ClassNode owner, MethodNode method) {
        boolean isAbstract() {
            return (method.access & Opcodes.ACC_ABSTRACT) != 0;
        }
    }

    private record MethodRef(String owner, String name, String desc) {
        static String key(String owner, String name, String desc) {
            return owner + '#' + name + desc;
        }

        String key() {
            return key(owner, name, desc);
        }
    }
}
