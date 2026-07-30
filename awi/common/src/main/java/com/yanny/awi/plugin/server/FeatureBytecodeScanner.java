package com.yanny.awi.plugin.server;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IServerUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.objectweb.asm.ClassReader;
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
 * <p>Known PoC limitations: no inter-procedural receiver binding (a block placed as {@code this} inside a helper
 * such as {@code ChorusFlowerBlock#generatePlant} is missed); lambda bodies ({@code INVOKEDYNAMIC}) are not followed;
 * blocks selected from a tag through registry lookups are only caught when the tag key itself appears in the resolved
 * value chain.
 */
public final class FeatureBytecodeScanner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_METHODS = 400;
    private static final int MAX_RESOLVE_DEPTH = 12;

    private static final Map<Class<?>, Set<Block>> RESULT_CACHE = new ConcurrentHashMap<>();
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

    private FeatureBytecodeScanner() {}

    public static Set<Block> scan(IServerUtils utils, Feature<?> feature) {
        if (PLACE_NAME == null) {
            return Set.of();
        }

        return RESULT_CACHE.computeIfAbsent(feature.getClass(), (cls) -> {
            try {
                return new FeatureBytecodeScanner().doScan(utils, cls);
            } catch (Throwable t) {
                LOGGER.warn("Bytecode scan failed for {}", cls.getName(), t);
                return Set.of();
            }
        });
    }

    private Set<Block> doScan(IServerUtils utils, Class<?> featureClass) {
        ClassLoader cl = featureClass.getClassLoader();
        Registry<Block> blockRegistry = utils.getServerLevel().registryAccess().lookupOrThrow(Registries.BLOCK);
        String rootPkg = rootPackage(Type.getInternalName(featureClass));

        Set<Block> out = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Deque<MethodRef> worklist = new ArrayDeque<>();
        worklist.add(new MethodRef(Type.getInternalName(featureClass), PLACE_NAME, PLACE_DESC));

        while (!worklist.isEmpty() && visited.size() < MAX_METHODS) {
            MethodRef ref = worklist.poll();

            if (!visited.add(ref.key())) {
                continue;
            }

            ClassNode owner = classNode(cl, ref.owner);

            if (owner == null) {
                continue;
            }

            MethodNode method = findMethod(owner, ref.name, ref.desc);

            if (method == null || (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0
                    || method.instructions.size() == 0) {
                continue;
            }

            analyzeMethod(cl, owner, method, blockRegistry, rootPkg, out, worklist);
        }

        return out;
    }

    private void analyzeMethod(ClassLoader cl, ClassNode owner, MethodNode method, Registry<Block> blockRegistry,
                               String rootPkg, Set<Block> out, Deque<MethodRef> worklist) {
        Frame<SourceValue>[] frames;

        try {
            frames = new Analyzer<>(new SourceInterpreter()).analyze(owner.name, method);
        } catch (Throwable t) {
            LOGGER.debug("Analyzer failed for {}.{}{}", owner.name, method.name, method.desc);
            return;
        }

        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!(insn instanceof MethodInsnNode call) || insn.getOpcode() == Opcodes.INVOKEDYNAMIC) {
                continue;
            }

            // Transitive follow into feature/block/level-gen code and the mod's own package.
            if (shouldRecurse(call.owner, rootPkg)) {
                worklist.add(new MethodRef(call.owner, call.name, call.desc));
            }

            Class<?> callOwner = loadClass(cl, call.owner);

            if (callOwner == null || !(LevelWriter.class.isAssignableFrom(callOwner) || Feature.class.isAssignableFrom(callOwner))) {
                continue;
            }

            int index = method.instructions.indexOf(call);
            Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;

            if (frame == null) {
                continue;
            }

            Type[] args = Type.getArgumentTypes(call.desc);

            for (int i = 0; i < args.length; i++) {
                Class<?> argClass = loadClass(cl, args[i].getInternalName());

                if (argClass != null && BlockState.class.isAssignableFrom(argClass)) {
                    SourceValue value = argValue(frame, call, i);

                    if (value != null) {
                        resolve(value, frames, method, cl, blockRegistry, out, new HashSet<>(), 0);
                    }
                }
            }
        }
    }

    /** Resolves a stack value (producer instructions) to concrete blocks, following defaultBlockState()/setValue() chains. */
    private void resolve(SourceValue value, Frame<SourceValue>[] frames, MethodNode method, ClassLoader cl,
                         Registry<Block> blockRegistry, Set<Block> out, Set<AbstractInsnNode> guard, int depth) {
        if (depth > MAX_RESOLVE_DEPTH) {
            return;
        }

        for (AbstractInsnNode producer : value.insns) {
            if (!guard.add(producer)) {
                continue;
            }

            if (producer instanceof FieldInsnNode field && producer.getOpcode() == Opcodes.GETSTATIC) {
                addFieldValue(cl, field, blockRegistry, out);
            } else if (producer instanceof MethodInsnNode call && producer.getOpcode() != Opcodes.INVOKEDYNAMIC) {
                Class<?> returned = loadClass(cl, Type.getReturnType(call.desc).getInternalName());

                // defaultBlockState() / setValue() / rotate() ... : the concrete block lives in the receiver chain.
                if (returned != null && (BlockState.class.isAssignableFrom(returned) || Block.class.isAssignableFrom(returned))
                        && call.getOpcode() != Opcodes.INVOKESTATIC) {
                    int index = method.instructions.indexOf(call);
                    Frame<SourceValue> frame = index >= 0 && index < frames.length ? frames[index] : null;
                    SourceValue receiver = frame != null ? receiverValue(frame, call) : null;

                    if (receiver != null) {
                        resolve(receiver, frames, method, cl, blockRegistry, out, guard, depth + 1);
                    }
                }
            }
        }
    }

    private void addFieldValue(ClassLoader cl, FieldInsnNode field, Registry<Block> blockRegistry, Set<Block> out) {
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
            out.add(block);
        } else if (staticValue instanceof BlockState state) {
            out.add(state.getBlock());
        } else if (staticValue instanceof TagKey<?> tag) {
            expandBlockTag(tag, blockRegistry, out);
        }
    }

    @SuppressWarnings("unchecked")
    private void expandBlockTag(TagKey<?> tag, Registry<Block> blockRegistry, Set<Block> out) {
        if (!tag.registry().equals(Registries.BLOCK)) {
            return;
        }

        blockRegistry.getTagOrEmpty((TagKey<Block>) tag).forEach((holder) -> out.add(holder.value()));
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

    private static boolean shouldRecurse(String owner, String rootPkg) {
        return owner.startsWith("net/minecraft/world/level/levelgen/")
                || owner.startsWith("net/minecraft/world/level/block/")
                || (rootPkg != null && owner.startsWith(rootPkg));
    }

    private static String rootPackage(String internalName) {
        int first = internalName.indexOf('/');
        int second = first < 0 ? -1 : internalName.indexOf('/', first + 1);
        return second < 0 ? null : internalName.substring(0, second + 1);
    }

    private record MethodRef(String owner, String name, String desc) {
        String key() {
            return owner + '#' + name + desc;
        }
    }
}
