package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

final class RuntimeNames {
    static final boolean AVAILABLE;
    static final Ref NEXT_INT;
    static final Ref NEXT_INT_BOUND;
    static final Ref NEXT_INT_BETWEEN;
    static final Ref NEXT_INT_ORIGIN;
    static final Ref NEXT_LONG;
    static final Ref NEXT_BOOLEAN;
    static final Ref NEXT_FLOAT;
    static final Ref NEXT_DOUBLE;
    static final Ref NEXT_GAUSSIAN;
    static final Ref FORK;
    static final Ref SET_SEED;
    static final Ref CONSUME_COUNT;
    static final Ref STACK_SET_COUNT;
    static final Ref STACK_GROW;
    static final Ref STACK_SHRINK;
    static final Ref STACK_GET_COUNT;
    static final Ref STACK_GET_ITEM;
    static final Ref STACK_COPY;
    static final Ref STACK_COPY_WITH_COUNT;
    static final Ref STACK_IS_EMPTY;
    static final Ref GET_OFFER;
    static final Ref LEVEL_IS_CLIENT_SIDE;
    static final Ref FIND_MAP_STRUCTURE;
    static final Ref MAP_CREATE;
    static final Ref MAP_RENDER_BIOME_PREVIEW;
    static final Ref MAP_ADD_TARGET_DECORATION;

    static {
        Map<String, Ref> refs = read();

        NEXT_INT = refs.get("nextInt");
        NEXT_INT_BOUND = refs.get("nextIntBound");
        NEXT_INT_BETWEEN = refs.get("nextIntBetween");
        NEXT_INT_ORIGIN = refs.get("nextIntOrigin");
        NEXT_LONG = refs.get("nextLong");
        NEXT_BOOLEAN = refs.get("nextBoolean");
        NEXT_FLOAT = refs.get("nextFloat");
        NEXT_DOUBLE = refs.get("nextDouble");
        NEXT_GAUSSIAN = refs.get("nextGaussian");
        FORK = refs.get("fork");
        SET_SEED = refs.get("setSeed");
        CONSUME_COUNT = refs.get("consumeCount");
        STACK_SET_COUNT = refs.get("stackSetCount");
        STACK_GROW = refs.get("stackGrow");
        STACK_SHRINK = refs.get("stackShrink");
        STACK_GET_COUNT = refs.get("stackGetCount");
        STACK_GET_ITEM = refs.get("stackGetItem");
        STACK_COPY = refs.get("stackCopy");
        STACK_COPY_WITH_COUNT = refs.get("stackCopyWithCount");
        STACK_IS_EMPTY = refs.get("stackIsEmpty");
        GET_OFFER = refs.get("getOffer");
        LEVEL_IS_CLIENT_SIDE = refs.get("levelIsClientSide");
        FIND_MAP_STRUCTURE = refs.get("findMapStructure");
        MAP_CREATE = refs.get("mapCreate");
        MAP_RENDER_BIOME_PREVIEW = refs.get("mapRenderBiomePreview");
        MAP_ADD_TARGET_DECORATION = refs.get("mapAddTargetDecoration");
        AVAILABLE = refs.size() == 26;
    }

    private RuntimeNames() {}

    static boolean is(@Nullable Ref ref, MethodInsnNode call) {
        return ref != null && ref.name.equals(call.name) && ref.desc.equals(call.desc);
    }

    static boolean is(@Nullable Ref ref, FieldInsnNode field) {
        return ref != null && ref.name.equals(field.name) && ref.desc.equals(field.desc);
    }

    private static Map<String, Ref> read() {
        Map<String, Ref> refs = new HashMap<>();

        try (InputStream is = RuntimeNames.class.getResourceAsStream(RuntimeNames.class.getSimpleName() + ".class")) {
            if (is == null) {
                return refs;
            }

            ClassNode node = new ClassNode();

            new ClassReader(is).accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);

            for (MethodNode method : node.methods) {
                for (AbstractInsnNode insn : method.instructions) {
                    if (insn instanceof MethodInsnNode call) {
                        refs.putIfAbsent(method.name, new Ref(call.name, call.desc));
                        break;
                    }
                    if (insn instanceof FieldInsnNode field) {
                        refs.putIfAbsent(method.name, new Ref(field.name, field.desc));
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {}

        refs.keySet().retainAll(java.util.Set.of("nextInt", "nextIntBound", "nextIntBetween", "nextIntOrigin", "nextLong", "nextBoolean",
                "nextFloat", "nextDouble", "nextGaussian", "fork", "setSeed", "consumeCount", "stackSetCount", "stackGrow", "stackShrink",
                "stackGetCount", "stackGetItem", "stackCopy", "stackCopyWithCount", "stackIsEmpty", "getOffer", "levelIsClientSide", "findMapStructure",
                "mapCreate", "mapRenderBiomePreview", "mapAddTargetDecoration"));
        return refs;
    }

    record Ref(String name, String desc) {
    }

    // The methods below are never called; read() takes the runtime name and descriptor of the call or field each uses.

    private static int nextInt(RandomSource random) {
        return random.nextInt();
    }

    private static int nextIntBound(RandomSource random) {
        return random.nextInt(1);
    }

    private static int nextIntBetween(RandomSource random) {
        return random.nextIntBetweenInclusive(0, 1);
    }

    private static int nextIntOrigin(RandomSource random) {
        return random.nextInt(0, 1);
    }

    private static long nextLong(RandomSource random) {
        return random.nextLong();
    }

    private static boolean nextBoolean(RandomSource random) {
        return random.nextBoolean();
    }

    private static float nextFloat(RandomSource random) {
        return random.nextFloat();
    }

    private static double nextDouble(RandomSource random) {
        return random.nextDouble();
    }

    private static double nextGaussian(RandomSource random) {
        return random.nextGaussian();
    }

    private static RandomSource fork(RandomSource random) {
        return random.fork();
    }

    private static void setSeed(RandomSource random) {
        random.setSeed(0);
    }

    private static void consumeCount(RandomSource random) {
        random.consumeCount(0);
    }

    private static void stackSetCount(ItemStack stack) {
        stack.setCount(0);
    }

    private static void stackGrow(ItemStack stack) {
        stack.grow(0);
    }

    private static void stackShrink(ItemStack stack) {
        stack.shrink(0);
    }

    private static int stackGetCount(ItemStack stack) {
        return stack.getCount();
    }

    private static Object stackGetItem(ItemStack stack) {
        return stack.getItem();
    }

    private static ItemStack stackCopy(ItemStack stack) {
        return stack.copy();
    }

    private static ItemStack stackCopyWithCount(ItemStack stack) {
        return stack.copyWithCount(0);
    }

    private static boolean stackIsEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    private static MerchantOffer getOffer(VillagerTrades.ItemListing listing) {
        return listing.getOffer(null, null);
    }

    private static boolean levelIsClientSide(Level level) {
        return level.isClientSide;
    }

    private static BlockPos findMapStructure(ServerLevel level) {
        return level.findNearestMapStructure(null, null, 0, false);
    }

    private static ItemStack mapCreate() {
        return MapItem.create(null, 0, 0, (byte) 0, false, false);
    }

    private static void mapRenderBiomePreview() {
        MapItem.renderBiomePreviewMap(null, null);
    }

    private static void mapAddTargetDecoration() {
        MapItemSavedData.addTargetDecoration(null, null, null, null);
    }
}
