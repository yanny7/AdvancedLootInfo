package com.yanny.awi.plugin.server;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.slf4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

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
 * <p>The value flowing into a sink is followed backwards across seven hops, each of which is load-bearing for real
 * vanilla features - without them only a block written inline in the {@code setBlock} argument would ever be seen:
 * <ul>
 *     <li>local variables, back to the stores that reach them ({@link #throughLocal});</li>
 *     <li>method parameters, back to what call sites pass in ({@link #bindArguments} / {@link #addParameterCandidates});</li>
 *     <li>{@code CHECKCAST}, and the receiver plus block-carrying arguments of the call that produced the value, which
 *     is what exposes a tag-driven pick such as {@code BLOCK.getTag(BlockTags.CORAL_BLOCKS)} through an {@code Optional}
 *     chain;</li>
 *     <li>lambda and method-reference bodies, through the {@code INVOKEDYNAMIC} bootstrap handle, including the
 *     parameters the lambda receives from the call it is handed to rather than from a capture
 *     ({@link #bindLambdaParameters});</li>
 *     <li>a lambda handed to a method as an <i>argument</i>, back to the values that method later passes into it
 *     ({@link #recordFunctionalArguments} / {@link #bindFunctionalCandidates});</li>
 *     <li>the return value of a called helper, resolved out of its own {@code ARETURN}s ({@link #returnedCandidates});</li>
 *     <li>a {@code final} instance field, resolved out of the {@code PUTFIELD}s in its owner's constructors
 *     ({@link #addInstanceFieldValue}).</li>
 * </ul>
 *
 * <p>Method lookup walks the class hierarchy in both directions. Up, because {@code place()} is frequently declared
 * on an abstract base ({@code CoralFeature}, {@code AbstractHugeMushroomFeature}) rather than on the registered
 * subclass. Down, because such a base then calls back into an abstract method the subclass implements
 * ({@code CoralFeature#place} -> abstract {@code placeFeature}); stopping at the abstract declaration would lose
 * every block placed below it. An abstract call that is neither is dispatched to the block classes implementing it
 * ({@link #queueImplementors}).
 *
 * <p>Branches guarded by a {@code static final boolean} flag that reads {@code false} are pruned before any of that
 * happens - see {@link #reachableInstructions}.
 *
 * <p>Known limitations, in the "block not found" direction: a lambda parameter is only bound when the receiver it is
 * applied to resolves to blocks or the enclosing method was handed the lambda directly, and abstract dispatch only
 * reaches implementors that are registered blocks - not an inner class or an anonymous implementation. That second
 * limit is why a sculk patch reports the sculk it grows but not its veins: those are placed through
 * {@code MultifaceSpreader.SpreadConfig#getStateForPlacement}, whose implementations are inner classes.
 *
 * <h2>False positives</h2>
 *
 * <p>Two kinds exist, and they have different causes - do not treat them as one problem.
 *
 * <p><b>A block placed behind a test on the configuration.</b> The scan is keyed by {@link Feature} class, so it cannot
 * know which {@code FeatureConfiguration} the block is being reported for, and it reports every branch -
 * {@code LakeFeature}'s {@code ICE} is only placed when {@code configuration.fluid()} yields water, and 1.20.1 ships
 * exactly one lake ({@code lake_lava}). Those blocks are separated out into
 * {@link ScanResult#configConditionalBlocks()} by {@link #configGuardedInstructions}, and
 * {@code FeatureConfigurationCollectorUtils} drops them unless {@code AwiConfig.showConfigConditionalBlocks} says
 * otherwise. An unrecognized guard shape leaves the block reported unconditionally, so this only ever hides what it
 * positively proved conditional.
 *
 * <p>A guard encoded as a <i>number</i> is out of reach by construction: {@code HugeFungusFeature} grows weeping vines
 * only for crimson, but the test reaches one of its two call sites as a probability
 * ({@code placeHatBlock(..., bl2 ? 0.1F : 0.0F)}, then {@code randomSource.nextFloat() < h}). Proving that
 * {@code h == 0.0F} makes the comparison unsatisfiable is value reasoning, not the identity-test detection this does.
 *
 * <p><b>A block placed by a library the feature calls into.</b> {@code FossilFeature} reports {@code BARRIER}, which
 * comes from {@code StructureTemplate#placeInWorld}, not from the feature - and the blocks the fossil really places live
 * in NBT structures that no static analysis can reach. Reads of the world are already excluded for this reason (see
 * {@link #resolveReturnValue}); template placement is not, because there is no equally crisp type to exclude it by.
 */
public final class FeatureBytecodeScanner {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Runaway guard, not a tuning knob: lowering it silently drops blocks rather than failing. Vanilla's widest
    // feature (fossil) analyzes 402 methods and no other reaches 250, so this sits at ~2.5x the measured worst case.
    private static final int MAX_METHODS = 1000;
    private static final int MAX_RESOLVE_DEPTH = 12;
    // Keeps abstract dispatch off the wide base types: BlockBehaviour's own methods are implemented by every block in
    // the game, and queueing all of them would spend the whole method budget on code no feature reaches.
    private static final int MAX_IMPLEMENTORS = 4;

    private static final String CONSTRUCTOR = "<init>";

    private static final Map<Class<?>, ScanResult> RESULT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ClassNode> CLASS_NODE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> IMPLEMENTOR_CACHE = new ConcurrentHashMap<>();

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

    /** Callee method key -> parameter position -> the lambdas its call sites were seen passing in. */
    private final Map<String, Map<Integer, Set<Functional>>> functionalArguments = new HashMap<>();

    /** Method key -> blocks its return value can be, so a helper's body is only walked once. */
    private final Map<String, Candidates> returnedValues = new HashMap<>();

    /** Field key -> blocks its constructors assign to it. */
    private final Map<String, Candidates> instanceFieldValues = new HashMap<>();

    /** Return values and fields currently being resolved, so a cyclic helper cannot recurse forever. */
    private final Set<String> inProgress = new HashSet<>();

    /** Callee method key -> boolean parameter positions its call sites were seen passing a config-derived value into. */
    private final Map<String, Set<Integer>> configTaintedParameters = new HashMap<>();

    /** Method keys reached from a call site that only runs when a config-dependent test passed. */
    private final Set<String> reachedGuarded = new HashSet<>();

    /** Method keys reached from a call site that runs regardless of any config-dependent test. */
    private final Set<String> reachedUnguarded = new HashSet<>();

    /** Two leading package segments of the scanned class, see {@link #shouldRecurse}. */
    private String rootPkg;

    /** Key of the method being analyzed, so a parameter can be matched against {@link #parameterCandidates}. */
    private String currentMethodKey;

    /** Instructions of the method being analyzed that survive {@link #reachableInstructions}. */
    private Set<AbstractInsnNode> currentReachable;

    /** Instructions of the method being analyzed that only run when a config-dependent test passed. */
    private Set<AbstractInsnNode> currentConfigGuarded;

    /** Whether the method being analyzed was itself reached only through a config-dependent test. */
    private boolean currentEnteredGuarded;

    /** Whether the method being analyzed is also reached on a path with no config-dependent test. */
    private boolean currentEnteredUnguarded;

    /** Helper bodies walked for their return value, out of the same {@link #MAX_METHODS} budget shape as the main walk. */
    private int analyzedReturns;

    private FeatureBytecodeScanner() {}

    /**
     * Scans one feature class for the blocks and block tags it places. Needs no running server, and a tag is reported
     * as the tag itself - resolving it into members is the display's job
     * ({@link com.yanny.awi.plugin.common.nodes.BlockNode}). The block registry is read for one purpose only, as the
     * implementor index behind {@link #queueImplementors}.
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
        IMPLEMENTOR_CACHE.clear();
    }

    /**
     * Walks the feature twice. The interprocedural facts - which parameters carry blocks, which carry the result of a
     * config-selective test - are only complete once the whole walk has run, and how a method was reached is recorded
     * monotonically and never retracted. A method analyzed before its caller's taint was known therefore records itself
     * as unguarded permanently, which is what hid {@code HugeFungusFeature}'s weeping vines: {@code placeHatDropBlock}
     * is reached before the crimson test that guards it is known. The second walk starts from cleared reach flags and
     * the facts the first one learned, and only its attribution is used.
     */
    private ScanResult doScan(Class<?> featureClass) {
        ClassLoader cl = featureClass.getClassLoader();
        MethodRef root = new MethodRef(Type.getInternalName(featureClass), PLACE_NAME, PLACE_DESC);

        rootPkg = rootPackage(Type.getInternalName(featureClass));

        walk(cl, featureClass, root);
        reachedGuarded.clear();
        reachedUnguarded.clear();

        return walk(cl, featureClass, root);
    }

    private ScanResult walk(ClassLoader cl, Class<?> featureClass, MethodRef root) {
        Candidates guarded = Candidates.create();
        Candidates unguarded = Candidates.create();
        Set<String> visited = new HashSet<>();
        Deque<MethodRef> worklist = new ArrayDeque<>();
        int analyzed = 0;

        reachedUnguarded.add(root.key());
        worklist.add(root);

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

            // Not a self-call, so the receiver is some other object entirely - the only implementors we can enumerate
            // are the registered blocks (SculkBehaviour, which is how a sculk patch spreads).
            if (declaration != null && declaration.isAbstract()) {
                queueImplementors(cl, ref, worklist, visited);
            }

            if (declaration == null || declaration.isAbstract()
                    || (declaration.method().access & Opcodes.ACC_NATIVE) != 0
                    || declaration.method().instructions.size() == 0) {
                continue;
            }

            analyzed++;
            // Keyed by the reference, not the declaring class, so it matches what a call site records for it.
            currentMethodKey = ref.key();
            currentEnteredGuarded = reachedGuarded.contains(ref.key());
            // Absent information counts as unguarded, so a block is only ever reported as conditional on positive
            // evidence that every path to it passed a config-dependent test.
            currentEnteredUnguarded = reachedUnguarded.contains(ref.key()) || !currentEnteredGuarded;
            analyzeMethod(cl, declaration.owner(), declaration.method(), guarded, unguarded, worklist, visited);
        }

        // Only a still-pending, not-yet-visited method means the budget actually cut the walk short.
        boolean truncated = worklist.stream().anyMatch((pending) -> !visited.contains(pending.key()));

        Set<Block> blocks = new HashSet<>(unguarded.blocks());
        Set<TagKey<Block>> tags = new HashSet<>(unguarded.tags());
        Set<Block> conditionalBlocks = new HashSet<>(guarded.blocks());

        blocks.addAll(guarded.blocks());
        tags.addAll(guarded.tags());
        conditionalBlocks.removeAll(unguarded.blocks());

        return new ScanResult(blocks, conditionalBlocks, tags, analyzed, truncated);
    }

    private void analyzeMethod(ClassLoader cl, ClassNode owner, MethodNode method, Candidates guarded, Candidates unguarded,
                               Deque<MethodRef> worklist, Set<String> visited) {
        Frame<SourceValue>[] frames = frames(owner, method);

        if (frames == null) {
            return;
        }

        currentReachable = reachableInstructions(cl, method);
        currentConfigGuarded = configGuardedInstructions(cl, method, frames);

        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!currentReachable.contains(insn)) {
                continue;
            }

            boolean siteGuarded = isConfigGuarded(insn);
            boolean siteUnguarded = !currentConfigGuarded.contains(insn) && currentEnteredUnguarded;

            // A lambda/method reference body is a separate (usually synthetic) method reachable only through the
            // bootstrap handle - features routinely place their blocks in one (e.g. UnderwaterMagmaFeature's forEach).
            if (insn instanceof InvokeDynamicInsnNode indy) {
                queueBootstrapTargets(indy, siteGuarded, siteUnguarded, worklist, visited);
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
            if (shouldRecurse(call.owner)) {
                markReached(new MethodRef(call.owner, call.name, call.desc), siteGuarded, siteUnguarded, worklist, visited);
                bindArguments(cl, call, frame, frames, method, worklist, visited);
                recordFunctionalArguments(call, frame, worklist, visited);
                recordConfigTaint(cl, call, frame, frames, method, worklist, visited);
            }

            // Not gated on shouldRecurse: the call handing a lambda its values is usually a JDK one (Optional#ifPresent).
            bindLambdaParameters(cl, call, frame, frames, method, worklist, visited);
            bindFunctionalCandidates(cl, call, frame, frames, method, worklist, visited);

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
                        // Resolved per sink rather than straight into one accumulator, so the blocks this placement
                        // contributes can be attributed to whether the placement itself is config-dependent.
                        Candidates placed = Candidates.create();

                        resolve(value, frames, method, cl, placed, new HashSet<>(), 0);

                        if (siteGuarded) {
                            guarded.addAll(placed);
                        }

                        if (siteUnguarded) {
                            unguarded.addAll(placed);
                        }
                    }
                }
            }
        }
    }

    /** Whether an instruction only runs when a config-dependent test passed, locally or on the way into this method. */
    private boolean isConfigGuarded(AbstractInsnNode insn) {
        return currentConfigGuarded.contains(insn) || currentEnteredGuarded;
    }

    /**
     * Records how a callee was reached, re-queueing it when that grows. A method first seen behind a config-dependent
     * test and later reached without one places its blocks unconditionally after all, so it has to be revisited - the
     * same monotone growth argument as {@link #recordCandidates}.
     */
    private void markReached(MethodRef callee, boolean siteGuarded, boolean siteUnguarded, Deque<MethodRef> worklist,
                             Set<String> visited) {
        boolean grown = siteGuarded && reachedGuarded.add(callee.key());

        grown |= siteUnguarded && reachedUnguarded.add(callee.key());

        if (!visited.contains(callee.key())) {
            worklist.add(callee);
        } else if (grown) {
            visited.remove(callee.key());
            worklist.add(callee);
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
                                      MethodNode method, Deque<MethodRef> worklist, Set<String> visited) {
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
                    bindFunctionalParameters(cl, indy, fromReceiver, worklist, visited);
                }
            }
        }
    }

    /**
     * Records which lambdas a call site hands to which callee parameter. This is the caller half of the second lambda
     * link: a lambda passed <i>as an argument</i> gets its values from inside the callee rather than from the receiver
     * {@link #bindLambdaParameters} looks at, and the callee is frequently static, so that hop never fires. Dripstone
     * is the case that needs it - {@code DripstoneUtils#growPointedDripstone} hands
     * {@code state -> level.setBlock(pos, state, 2)} to the static {@code buildBaseToTipColumn}, which is the only place
     * {@code POINTED_DRIPSTONE} is ever produced.
     *
     * <p>Re-queued on growth for the same reason {@link #recordCandidates} is: a callee already analyzed did not see
     * the lambda, so its use of that parameter bound nothing.
     */
    private void recordFunctionalArguments(MethodInsnNode call, Frame<SourceValue> frame, Deque<MethodRef> worklist,
                                           Set<String> visited) {
        Type[] args = Type.getArgumentTypes(call.desc);
        MethodRef callee = new MethodRef(call.owner, call.name, call.desc);

        for (int i = 0; i < args.length; i++) {
            SourceValue value = argValue(frame, call, i);

            if (value == null) {
                continue;
            }

            for (AbstractInsnNode producer : value.insns) {
                if (!(producer instanceof InvokeDynamicInsnNode indy)) {
                    continue;
                }

                int captured = Type.getArgumentTypes(indy.desc).length;

                for (Object arg : indy.bsmArgs) {
                    if (!(arg instanceof Handle handle) || handle.getTag() < Opcodes.H_INVOKEVIRTUAL
                            || !shouldRecurse(handle.getOwner())) {
                        continue;
                    }

                    Set<Functional> known = functionalArguments.computeIfAbsent(callee.key(), (key) -> new HashMap<>())
                            .computeIfAbsent(i, (index) -> new HashSet<>());

                    if (known.add(new Functional(handle, captured)) && visited.remove(callee.key())) {
                        worklist.add(callee);
                    }
                }
            }
        }
    }

    /**
     * Callee half of the lambda-argument link: a call on a parameter that holds a lambda feeds the lambda's body. The
     * binding is positional - the {@code n}-th argument of {@code consumer.accept(...)} is the {@code n}-th value the
     * body receives from the functional interface.
     *
     * <p>Argument types are not filtered by {@link #carriesBlocks} here, unlike everywhere else: a functional
     * interface's descriptor is erased, so {@code Consumer#accept} declares {@code Object}. The implementation
     * method's own parameter type is what decides whether the value is kept.
     */
    private void bindFunctionalCandidates(ClassLoader cl, MethodInsnNode call, Frame<SourceValue> frame, Frame<SourceValue>[] frames,
                                          MethodNode method, Deque<MethodRef> worklist, Set<String> visited) {
        Map<Integer, Set<Functional>> known = currentMethodKey != null ? functionalArguments.get(currentMethodKey) : null;

        if (known == null || call.getOpcode() == Opcodes.INVOKESTATIC) {
            return;
        }

        SourceValue receiver = receiverValue(frame, call);
        Set<Functional> functionals = new HashSet<>();

        if (receiver != null) {
            for (AbstractInsnNode producer : receiver.insns) {
                if (producer instanceof VarInsnNode local && local.getOpcode() == Opcodes.ALOAD) {
                    functionals.addAll(known.getOrDefault(parameterIndex(method, local.var), Set.of()));
                }
            }
        }

        if (functionals.isEmpty()) {
            return;
        }

        Type[] args = Type.getArgumentTypes(call.desc);

        for (int i = 0; i < args.length; i++) {
            Candidates resolved = Candidates.create();

            resolve(argValue(frame, call, i), frames, method, cl, resolved, new HashSet<>(), 0);

            if (resolved.isEmpty()) {
                continue;
            }

            for (Functional functional : functionals) {
                bindFunctionalArgument(cl, functional, i, resolved, worklist, visited);
            }
        }
    }

    /** Binds one positional functional argument to the matching parameter of a lambda body. */
    private void bindFunctionalArgument(ClassLoader cl, Functional functional, int argIndex, Candidates candidates,
                                        Deque<MethodRef> worklist, Set<String> visited) {
        Handle handle = functional.handle();
        Type[] implArgs = Type.getArgumentTypes(handle.getDesc());
        int index = functional.firstFunctionalParameter() + argIndex;

        if (index >= implArgs.length) {
            return;
        }

        Class<?> type = loadClass(cl, implArgs[index].getInternalName());

        if (type != null && carriesBlocks(type)) {
            recordCandidates(new MethodRef(handle.getOwner(), handle.getName(), handle.getDesc()), index, candidates,
                    worklist, visited);
        }
    }

    /**
     * Binds {@code candidates} to the parameters of a lambda body that come from the functional interface. An
     * {@code INVOKEDYNAMIC} consumes exactly the captured values off the stack, so the implementation method's
     * parameters start with the captures and the functional arguments make up the rest.
     */
    private void bindFunctionalParameters(ClassLoader cl, InvokeDynamicInsnNode indy, Candidates candidates,
                                          Deque<MethodRef> worklist, Set<String> visited) {
        int captured = Type.getArgumentTypes(indy.desc).length;

        for (Object arg : indy.bsmArgs) {
            if (!(arg instanceof Handle handle) || handle.getTag() < Opcodes.H_INVOKEVIRTUAL
                    || !shouldRecurse(handle.getOwner())) {
                continue;
            }

            Type[] implArgs = Type.getArgumentTypes(handle.getDesc());
            int first = new Functional(handle, captured).firstFunctionalParameter();

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
    private void queueBootstrapTargets(InvokeDynamicInsnNode indy, boolean siteGuarded, boolean siteUnguarded,
                                       Deque<MethodRef> worklist, Set<String> visited) {
        for (Object arg : indy.bsmArgs) {
            // Tags below H_INVOKEVIRTUAL are field handles, whose descriptor would never match a method.
            if (arg instanceof Handle handle && handle.getTag() >= Opcodes.H_INVOKEVIRTUAL
                    && shouldRecurse(handle.getOwner())) {
                // Guardedness is taken from where the lambda is created, not from where it is invoked - the invocation
                // is usually inside a JDK method this walk never enters.
                markReached(new MethodRef(handle.getOwner(), handle.getName(), handle.getDesc()), siteGuarded,
                        siteUnguarded, worklist, visited);
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
            } else if (producer instanceof FieldInsnNode field && producer.getOpcode() == Opcodes.GETFIELD) {
                addInstanceFieldValue(cl, field, out, depth);
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

                resolveReturnValue(cl, call, out, depth);
            }
        }
    }

    /**
     * Follows a call into the helper that produced the value, when neither its receiver nor its arguments carry the
     * block. {@code DripstoneUtils#createPointedDripstone(direction, thickness)} is the shape this exists for: a static
     * factory whose whole input is enum values, so without reading its {@code ARETURN} the pointed dripstone is
     * invisible - the block only appears inside the callee.
     *
     * <p>Reads of the world itself are excluded, which is load-bearing rather than an optimization: a feature that
     * copies a state it read back into the world ({@code setBlock(pos, level.getBlockState(other), 2)}) would otherwise
     * report the sentinel states a reader returns out of bounds - {@code ProtoChunk#getBlockState} answers
     * {@code VOID_AIR}, so fossil and end gateway both picked up blocks no feature places.
     */
    private void resolveReturnValue(ClassLoader cl, MethodInsnNode call, Candidates out, int depth) {
        Class<?> callOwner = loadClass(cl, call.owner);

        if (!shouldRecurse(call.owner) || callOwner == null || BlockGetter.class.isAssignableFrom(callOwner)) {
            return;
        }

        Type returnType = Type.getReturnType(call.desc);
        Class<?> returnClass = returnType.getSort() == Type.OBJECT ? loadClass(cl, returnType.getInternalName()) : null;

        if (returnClass != null && carriesBlocks(returnClass)) {
            out.addAll(returnedCandidates(cl, new MethodRef(call.owner, call.name, call.desc), depth));
        }
    }

    /** Blocks a method's return value can be, resolved once per method and reused. */
    private Candidates returnedCandidates(ClassLoader cl, MethodRef ref, int depth) {
        Candidates known = returnedValues.get(ref.key());

        if (known != null) {
            return known;
        }

        Candidates collected = Candidates.create();

        if (depth > MAX_RESOLVE_DEPTH || analyzedReturns >= MAX_METHODS || !inProgress.add(ref.key())) {
            return collected;
        }

        try {
            Declaration declaration = declaration(cl, ref.owner, ref.name, ref.desc);

            if (declaration == null || declaration.isAbstract() || declaration.method().instructions.size() == 0) {
                return collected;
            }

            Frame<SourceValue>[] frames = frames(declaration.owner(), declaration.method());

            if (frames == null) {
                return collected;
            }

            analyzedReturns++;
            collectStackTops(cl, declaration.owner().name, declaration.method(), frames,
                    (insn) -> insn.getOpcode() == Opcodes.ARETURN, collected, depth);
            returnedValues.put(ref.key(), collected);
        } finally {
            inProgress.remove(ref.key());
        }

        return collected;
    }

    /**
     * Blocks a {@code final} instance field can hold, read out of the {@code PUTFIELD}s in its owner's constructors.
     * A {@code GETFIELD} has no value to read reflectively - {@link #scan} is handed a {@link Class}, never an
     * instance - so a feature that caches its states in fields ({@code DesertWellFeature}: sand, sandstone, sandstone
     * slab, water) otherwise reaches its sinks carrying nothing.
     *
     * <p>Restricted to {@code final} fields: anything else can be reassigned from outside the constructor, and the
     * constructor's value would then be a guess rather than the field's content.
     */
    private void addInstanceFieldValue(ClassLoader cl, FieldInsnNode field, Candidates out, int depth) {
        String key = field.owner + '#' + field.name + ':' + field.desc;
        Candidates known = instanceFieldValues.get(key);

        if (known != null) {
            out.addAll(known);
            return;
        }

        if (!shouldRecurse(field.owner) || !isFinalBlockField(cl, field) || depth > MAX_RESOLVE_DEPTH || !inProgress.add(key)) {
            return;
        }

        try {
            ClassNode owner = classNode(cl, field.owner);

            if (owner == null) {
                return;
            }

            Candidates collected = Candidates.create();

            for (MethodNode method : owner.methods) {
                Frame<SourceValue>[] frames = CONSTRUCTOR.equals(method.name) ? frames(owner, method) : null;

                if (frames != null) {
                    collectStackTops(cl, owner.name, method, frames, (insn) -> insn.getOpcode() == Opcodes.PUTFIELD
                            && insn instanceof FieldInsnNode store && store.name.equals(field.name)
                            && store.desc.equals(field.desc), collected, depth);
                }
            }

            instanceFieldValues.put(key, collected);
            out.addAll(collected);
        } finally {
            inProgress.remove(key);
        }
    }

    /** Whether a field is declared {@code final} and typed to carry a block, checked on the bytecode, not reflectively. */
    private static boolean isFinalBlockField(ClassLoader cl, FieldInsnNode field) {
        ClassNode owner = classNode(cl, field.owner);
        Type type = Type.getType(field.desc);

        if (owner == null || type.getSort() != Type.OBJECT) {
            return false;
        }

        Class<?> fieldType = loadClass(cl, type.getInternalName());

        if (fieldType == null || !carriesBlocks(fieldType)) {
            return false;
        }

        return owner.fields.stream().anyMatch((node) -> node.name.equals(field.name) && node.desc.equals(field.desc)
                && (node.access & Opcodes.ACC_FINAL) != 0);
    }

    /**
     * Resolves the stack top at every reachable instruction the selector accepts, inside a method other than the one
     * being walked. {@link #resolve} reads the current method out of {@link #currentMethodKey}/{@link #currentReachable},
     * so both have to be swapped for the duration and put back afterwards.
     */
    private void collectStackTops(ClassLoader cl, String ownerName, MethodNode method, Frame<SourceValue>[] frames,
                                  Predicate<AbstractInsnNode> selector, Candidates out, int depth) {
        String previousKey = currentMethodKey;
        Set<AbstractInsnNode> previousReachable = currentReachable;

        currentMethodKey = MethodRef.key(ownerName, method.name, method.desc);
        currentReachable = reachableInstructions(cl, method);

        try {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (currentReachable.contains(insn) && selector.test(insn)) {
                    resolve(stackTop(insn, frames, method), frames, method, cl, out, new HashSet<>(), depth + 1);
                }
            }
        } finally {
            currentMethodKey = previousKey;
            currentReachable = previousReachable;
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

        // ILOAD/ISTORE are here for guard detection, which follows boolean locals; resolve only ever reaches this with
        // object-typed values, so accepting the wider set changes nothing for it.
        if (local.getOpcode() == Opcodes.ALOAD || local.getOpcode() == Opcodes.ILOAD) {
            return local.var < frame.getLocals() ? frame.getLocal(local.var) : null;
        }

        if (local.getOpcode() == Opcodes.ASTORE || local.getOpcode() == Opcodes.ISTORE) {
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

    // -- config-dependent guards -----------------------------------------------------------------------------------

    /**
     * Instructions that only run when a test on the {@code FeatureConfiguration} passed. This is what separates a block
     * a feature always places from one it places only for some configurations - {@code LakeFeature}'s {@code ICE} is
     * only reached when {@code configuration.fluid()} yields water, and {@code HugeFungusFeature}'s weeping vines only
     * when its {@code hatState} is crimson, so both are reported for configurations that never place them.
     *
     * <p>Control dependence, per conditional jump: what is reachable from one successor but not from the other runs only
     * when that branch was taken. Nested regions need no extra work, since an outer region already contains them.
     */
    private Set<AbstractInsnNode> configGuardedInstructions(ClassLoader cl, MethodNode method, Frame<SourceValue>[] frames) {
        Set<AbstractInsnNode> guarded = new HashSet<>();

        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!(insn instanceof JumpInsnNode jump) || jump.getOpcode() == Opcodes.GOTO
                    || !currentReachable.contains(insn) || !isConfigCondition(cl, jump, frames, method)) {
                continue;
            }

            Set<AbstractInsnNode> fromTaken = reachableFrom(jump.label);
            Set<AbstractInsnNode> fromNext = reachableFrom(jump.getNext());

            fromTaken.stream().filter((node) -> !fromNext.contains(node)).forEach(guarded::add);
            fromNext.stream().filter((node) -> !fromTaken.contains(node)).forEach(guarded::add);
        }

        return guarded;
    }

    /** Forward walk from one CFG node, restricted to the instructions that survived {@link #reachableInstructions}. */
    private Set<AbstractInsnNode> reachableFrom(AbstractInsnNode start) {
        Set<AbstractInsnNode> reached = new HashSet<>();
        Deque<AbstractInsnNode> worklist = new ArrayDeque<>();

        worklist.add(start);

        while (!worklist.isEmpty()) {
            AbstractInsnNode insn = worklist.poll();

            if (insn == null || !currentReachable.contains(insn) || !reached.add(insn)) {
                continue;
            }

            int opcode = insn.getOpcode();

            if (insn instanceof JumpInsnNode jump) {
                worklist.add(jump.label);

                if (jump.getOpcode() != Opcodes.GOTO) {
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

        return reached;
    }

    /** Whether a conditional jump tests a value that came out of the configuration. */
    private boolean isConfigCondition(ClassLoader cl, JumpInsnNode jump, Frame<SourceValue>[] frames, MethodNode method) {
        int index = method.instructions.indexOf(jump);
        Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;
        int opcode = jump.getOpcode();
        int operands = opcode >= Opcodes.IF_ICMPEQ && opcode <= Opcodes.IF_ACMPNE ? 2 : 1;

        if (frame == null || frame.getStackSize() < operands) {
            return false;
        }

        for (int i = 0; i < operands; i++) {
            if (isConfigSelectiveTest(frame.getStack(frame.getStackSize() - 1 - i), frames, method, cl, new HashSet<>(), 0)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether a boolean value is an identity test of a block, state or fluid that came out of the configuration -
     * {@code configuration.hatState.is(Blocks.NETHER_WART_BLOCK)}, or {@code configuration.fluid()}'s state tested
     * against {@code FluidTags.WATER}.
     *
     * <p><b>Being config-derived is deliberately not enough</b>, and this is the whole reason this method exists rather
     * than {@link #isConfigDerived} being used directly. A loop bound (`{@code l < configuration.tries()}`) and an
     * iteration over a configuration's list (`{@code for (Spike spike : configuration.spikes())}`) are config-derived
     * too, yet they always run - and since a feature's whole body typically sits inside one, treating them as guards
     * marked nearly every feature's own blocks as conditional (measured: 8 of 61 features, including
     * {@code underwater_magma}'s magma and {@code end_spike}'s obsidian). An identity test is the shape that actually
     * <i>selects</i>, so only that is counted.
     */
    private boolean isConfigSelectiveTest(SourceValue value, Frame<SourceValue>[] frames, MethodNode method, ClassLoader cl,
                                          Set<AbstractInsnNode> guard, int depth) {
        if (value == null || depth > MAX_RESOLVE_DEPTH) {
            return false;
        }

        for (AbstractInsnNode producer : value.insns) {
            if (!currentReachable.contains(producer) || !guard.add(producer)) {
                continue;
            }

            if (producer instanceof MethodInsnNode call) {
                int index = method.instructions.indexOf(call);
                Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;

                if (frame != null && isStateIdentityTest(cl, call)
                        && isConfigDerived(receiverValue(frame, call), frames, method, cl, new HashSet<>(), 0)) {
                    return true;
                }
            } else if (producer instanceof VarInsnNode local) {
                SourceValue stored = throughLocal(local, frames, method);

                if (stored != null && !stored.insns.isEmpty()) {
                    if (isConfigSelectiveTest(stored, frames, method, cl, guard, depth + 1)) {
                        return true;
                    }
                } else if (isTaintedParameter(local, method)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Whether a call is a boolean identity test on a block, state or fluid, whichever end of the hierarchy declares it. */
    private static boolean isStateIdentityTest(ClassLoader cl, MethodInsnNode call) {
        if (call.getOpcode() == Opcodes.INVOKESTATIC || !Type.BOOLEAN_TYPE.equals(Type.getReturnType(call.desc))) {
            return false;
        }

        Class<?> owner = loadClass(cl, call.owner);

        if (owner == null) {
            return false;
        }

        // Either direction: BlockState#is is declared on BlockBehaviour.BlockStateBase, which BlockState extends.
        return isRelated(owner, BlockState.class) || isRelated(owner, Block.class) || isRelated(owner, FluidState.class);
    }

    private static boolean isRelated(Class<?> owner, Class<?> type) {
        return owner.isAssignableFrom(type) || type.isAssignableFrom(owner);
    }

    /**
     * Whether a stack value was computed from the {@code FeatureConfiguration}. Backwards over the same producer graph
     * {@link #resolve} walks, but collecting a yes/no instead of blocks. A configuration is recognized by type, never by
     * name: reading a field <i>of</i> one, calling a method <i>on</i> one (a record accessor), or receiving one as a
     * declared parameter or return value ({@code FeaturePlaceContext#config()}) all count.
     */
    private boolean isConfigDerived(SourceValue value, Frame<SourceValue>[] frames, MethodNode method, ClassLoader cl,
                                    Set<AbstractInsnNode> guard, int depth) {
        if (value == null || depth > MAX_RESOLVE_DEPTH) {
            return false;
        }

        for (AbstractInsnNode producer : value.insns) {
            if (!currentReachable.contains(producer) || !guard.add(producer)) {
                continue;
            }

            if (producer instanceof FieldInsnNode field) {
                if (isConfiguration(cl, Type.getObjectType(field.owner)) || isConfiguration(cl, Type.getType(field.desc))) {
                    return true;
                }

                if (producer.getOpcode() == Opcodes.GETFIELD
                        && isConfigDerived(stackTop(producer, frames, method), frames, method, cl, guard, depth + 1)) {
                    return true;
                }
            } else if (producer instanceof MethodInsnNode call) {
                if (isConfiguration(cl, Type.getObjectType(call.owner)) || isConfiguration(cl, Type.getReturnType(call.desc))) {
                    return true;
                }

                int index = method.instructions.indexOf(call);
                Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;

                if (frame == null) {
                    continue;
                }

                if (call.getOpcode() != Opcodes.INVOKESTATIC
                        && isConfigDerived(receiverValue(frame, call), frames, method, cl, guard, depth + 1)) {
                    return true;
                }

                for (int i = 0; i < Type.getArgumentTypes(call.desc).length; i++) {
                    if (isConfigDerived(argValue(frame, call, i), frames, method, cl, guard, depth + 1)) {
                        return true;
                    }
                }
            } else if (producer instanceof TypeInsnNode cast && producer.getOpcode() == Opcodes.CHECKCAST) {
                if (isConfigDerived(stackTop(cast, frames, method), frames, method, cl, guard, depth + 1)) {
                    return true;
                }
            } else if (producer instanceof VarInsnNode local) {
                SourceValue stored = throughLocal(local, frames, method);

                if (stored != null && !stored.insns.isEmpty()) {
                    if (isConfigDerived(stored, frames, method, cl, guard, depth + 1)) {
                        return true;
                    }
                } else if (isConfigParameter(cl, local, method)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Whether a slot holds a parameter declared as a configuration. */
    private static boolean isConfigParameter(ClassLoader cl, VarInsnNode local, MethodNode method) {
        int index = parameterIndex(method, local.var);

        return index >= 0 && isConfiguration(cl, Type.getArgumentTypes(method.desc)[index]);
    }

    /** Whether a slot holds a {@code boolean} parameter that call sites fill with a config-selective test's result. */
    private boolean isTaintedParameter(VarInsnNode local, MethodNode method) {
        int index = parameterIndex(method, local.var);

        return index >= 0 && currentMethodKey != null
                && configTaintedParameters.getOrDefault(currentMethodKey, Set.of()).contains(index);
    }

    /**
     * Records that a call site fills a {@code boolean} parameter with a config-derived value, the interprocedural half
     * of guard detection. {@code HugeFungusFeature} needs it: the crimson test is evaluated once in {@code placeHat} and
     * travels two calls down as a plain flag, so the method that actually places the vines has no config value in sight.
     *
     * <p>Only {@code boolean} parameters are tracked, and only when the value is a config-<i>selective</i> test
     * ({@link #isConfigSelectiveTest}), not merely config-derived. A configuration passed by reference is already
     * recognized from its declared type, and anything else (an int, an enum) is not a decided test - leaving it
     * unrecognized keeps the block reported unconditionally, which is the safe direction.
     */
    private void recordConfigTaint(ClassLoader cl, MethodInsnNode call, Frame<SourceValue> frame, Frame<SourceValue>[] frames,
                                   MethodNode method, Deque<MethodRef> worklist, Set<String> visited) {
        Type[] args = Type.getArgumentTypes(call.desc);
        MethodRef callee = new MethodRef(call.owner, call.name, call.desc);

        for (int i = 0; i < args.length; i++) {
            if (!Type.BOOLEAN_TYPE.equals(args[i])
                    || !isConfigSelectiveTest(argValue(frame, call, i), frames, method, cl, new HashSet<>(), 0)) {
                continue;
            }

            if (configTaintedParameters.computeIfAbsent(callee.key(), (key) -> new HashSet<>()).add(i)
                    && visited.remove(callee.key())) {
                worklist.add(callee);
            }
        }
    }

    private static boolean isConfiguration(ClassLoader cl, Type type) {
        if (type.getSort() != Type.OBJECT) {
            return false;
        }

        Class<?> candidate = loadClass(cl, type.getInternalName());

        return candidate != null && FeatureConfiguration.class.isAssignableFrom(candidate);
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

    private static Frame<SourceValue>[] frames(ClassNode owner, MethodNode method) {
        try {
            return new Analyzer<>(new SourceInterpreter()).analyze(owner.name, method);
        } catch (Throwable t) {
            LOGGER.debug("Analyzer failed for {}.{}{}", owner.name, method.name, method.desc);
            return null;
        }
    }

    /**
     * Queues the implementations of an abstract method on the classes of registered blocks. Vanilla needs this for
     * sculk: a patch spreads through {@code SculkSpreader}, which calls {@code SculkBehaviour#attemptSpreadVein} on
     * whatever block it found in the world, so the blocks it places live in {@code SculkBlock}/{@code SculkVeinBlock}
     * and no static receiver type leads there.
     *
     * <p>The block registry is the only implementor index available, which also bounds the blow-up: a method declared
     * on a wide base type ({@code BlockBehaviour}) resolves to hundreds of classes and is dropped by
     * {@link #MAX_IMPLEMENTORS} rather than dispatched.
     */
    private void queueImplementors(ClassLoader cl, MethodRef ref, Deque<MethodRef> worklist, Set<String> visited) {
        Class<?> declaring = loadClass(cl, ref.owner);

        if (declaring == null) {
            return;
        }

        // The dispatch is not a call site of its own, so an implementation inherits how the abstract reference itself
        // was reached.
        boolean siteGuarded = reachedGuarded.contains(ref.key());
        boolean siteUnguarded = reachedUnguarded.contains(ref.key()) || !siteGuarded;

        for (String implementor : IMPLEMENTOR_CACHE.computeIfAbsent(ref.owner, (name) -> implementors(declaring))) {
            markReached(new MethodRef(implementor, ref.name, ref.desc), siteGuarded, siteUnguarded, worklist, visited);
        }
    }

    private static Set<String> implementors(Class<?> declaring) {
        Set<String> found = new HashSet<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            if (declaring.isAssignableFrom(block.getClass())) {
                found.add(Type.getInternalName(block.getClass()));

                if (found.size() > MAX_IMPLEMENTORS) {
                    return Set.of();
                }
            }
        }

        return found;
    }

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
    private boolean shouldRecurse(String owner) {
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
     * @param blocks                  blocks that flow into a placement sink
     * @param configConditionalBlocks the subset of {@code blocks} whose every placement is reached only through a test
     *                                on the {@code FeatureConfiguration} - so whether they are placed at all depends on
     *                                which configuration the feature is used with
     *                                (see {@link #configGuardedInstructions})
     * @param tags                    block tags that flow into a placement sink, left unexpanded so a caller can either
     *                                display the tag itself or resolve its current members
     * @param visitedMethods          methods analyzed, out of the {@link #MAX_METHODS} budget
     * @param methodLimitReached      whether the budget cut the walk short, so the results may be incomplete
     */
    public record ScanResult(Set<Block> blocks, Set<Block> configConditionalBlocks, Set<TagKey<Block>> tags,
                             int visitedMethods, boolean methodLimitReached) {
        public static final ScanResult EMPTY = new ScanResult(Set.of(), Set.of(), Set.of(), 0, false);
    }

    /**
     * A lambda body plus how many values the {@code INVOKEDYNAMIC} captured, which is what separates the captured
     * parameters of the implementation method from the ones the functional interface supplies.
     */
    private record Functional(Handle handle, int captured) {
        /**
         * Index of the implementation method's first parameter that comes from the functional interface. A non-static
         * implementation takes its receiver from the first captured value, so it consumes one capture without spending
         * a parameter.
         */
        int firstFunctionalParameter() {
            return Math.max(captured - (handle.getTag() == Opcodes.H_INVOKESTATIC ? 0 : 1), 0);
        }
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
