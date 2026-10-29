package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.CommonLogUtils;
import com.yanny.ali.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

final class RuntimeNames {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
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
    static final Ref STACK_SET;
    static final Ref STACK_UPDATE;
    static final Ref UPDATE_ENCHANTMENTS;
    static final Ref COST_ITEM_STACK;
    static final Ref COST_COUNT;
    static final Ref GET_OFFER;
    static final Ref LEVEL_IS_CLIENT_SIDE;
    static final Ref FIND_MAP_STRUCTURE;
    static final Ref FIND_MAP_STRUCTURE_SET;
    static final Ref REGISTRY_ACCESS;
    static final Ref ENTITY_REGISTRY_ACCESS;
    static final Ref MAP_CREATE;
    static final Ref MAP_RENDER_BIOME_PREVIEW;
    static final Ref MAP_ADD_TARGET_DECORATION;
    static final Ref GET_OFFERS;
    static final Ref UPDATE_TRADES;
    static final Ref ADD_OFFERS_FROM_TRADE_SET;
    static final Ref OFFERS;

    static {
        Map<String, Ref> refs = new HashMap<>();
        List<String> missing = new ArrayList<>();

        read(Probes.class, refs);
        read(Probes.VillagerNames.class, refs);

        NEXT_INT = ref(refs, "nextInt", missing);
        NEXT_INT_BOUND = ref(refs, "nextIntBound", missing);
        NEXT_INT_BETWEEN = ref(refs, "nextIntBetween", missing);
        NEXT_INT_ORIGIN = ref(refs, "nextIntOrigin", missing);
        NEXT_LONG = ref(refs, "nextLong", missing);
        NEXT_BOOLEAN = ref(refs, "nextBoolean", missing);
        NEXT_FLOAT = ref(refs, "nextFloat", missing);
        NEXT_DOUBLE = ref(refs, "nextDouble", missing);
        NEXT_GAUSSIAN = ref(refs, "nextGaussian", missing);
        FORK = ref(refs, "fork", missing);
        SET_SEED = ref(refs, "setSeed", missing);
        CONSUME_COUNT = ref(refs, "consumeCount", missing);
        STACK_SET_COUNT = ref(refs, "stackSetCount", missing);
        STACK_GROW = ref(refs, "stackGrow", missing);
        STACK_SHRINK = ref(refs, "stackShrink", missing);
        STACK_GET_COUNT = ref(refs, "stackGetCount", missing);
        STACK_GET_ITEM = ref(refs, "stackGetItem", missing);
        STACK_COPY = ref(refs, "stackCopy", missing);
        STACK_COPY_WITH_COUNT = ref(refs, "stackCopyWithCount", missing);
        STACK_IS_EMPTY = ref(refs, "stackIsEmpty", missing);
        STACK_SET = ref(refs, "stackSet", missing);
        STACK_UPDATE = ref(refs, "stackUpdate", missing);
        UPDATE_ENCHANTMENTS = ref(refs, "updateEnchantments", missing);
        COST_ITEM_STACK = ref(refs, "costItemStack", missing);
        COST_COUNT = ref(refs, "costCount", missing);
        GET_OFFER = ref(refs, "getOffer", missing);
        LEVEL_IS_CLIENT_SIDE = ref(refs, "levelIsClientSide", missing);
        FIND_MAP_STRUCTURE = ref(refs, "findMapStructure", missing);
        FIND_MAP_STRUCTURE_SET = ref(refs, "findMapStructureSet", missing);
        REGISTRY_ACCESS = ref(refs, "registryAccess", missing);
        ENTITY_REGISTRY_ACCESS = ref(refs, "entityRegistryAccess", missing);
        MAP_CREATE = ref(refs, "mapCreate", missing);
        MAP_RENDER_BIOME_PREVIEW = ref(refs, "mapRenderBiomePreview", missing);
        MAP_ADD_TARGET_DECORATION = ref(refs, "mapAddTargetDecoration", missing);
        GET_OFFERS = ref(refs, "getOffers", missing);
        UPDATE_TRADES = ref(refs, "villagerUpdateTrades", missing);
        ADD_OFFERS_FROM_TRADE_SET = ref(refs, "villagerAddOffers", missing);
        OFFERS = ref(refs, "villagerOffers", missing);
        AVAILABLE = missing.isEmpty();

        if (!AVAILABLE) {
            LOGGER.warn("Auto-detection of trades from bytecode is disabled, runtime names not found: {}", missing);
        }
    }

    private RuntimeNames() {}

    static boolean is(@Nullable Ref ref, MethodInsnNode call) {
        return ref != null && ref.name.equals(call.name) && ref.desc.equals(call.desc);
    }

    static boolean is(@Nullable Ref ref, FieldInsnNode field) {
        return ref != null && ref.name.equals(field.name) && ref.desc.equals(field.desc);
    }

    @Nullable
    private static Ref ref(Map<String, Ref> refs, String name, List<String> missing) {
        Ref ref = refs.get(name);

        if (ref == null) {
            missing.add(name);
        }

        return ref;
    }

    private static void read(Class<?> type, Map<String, Ref> refs) {
        String name = type.getName();

        try (InputStream is = type.getResourceAsStream(name.substring(name.lastIndexOf('.') + 1) + ".class")) {
            if (is == null) {
                return;
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
    }

    record Ref(String name, String desc) {
    }

    // The methods below are never called; read() takes the runtime name and descriptor of the call or field each uses.
    @SuppressWarnings({"unused", "DataFlowIssue"})
    private static final class Probes {
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

        private static Object stackSet(ItemStack stack) {
            return stack.set((DataComponentType<Object>) null, null);
        }

        private static Object stackUpdate(ItemStack stack) {
            return stack.update((DataComponentType<Object>) null, null, (UnaryOperator<Object>) null);
        }

        private static Object updateEnchantments() {
            return EnchantmentHelper.updateEnchantments(null, null);
        }

        private static ItemStack costItemStack(ItemCost cost) {
            return cost.itemStack();
        }

        private static int costCount(ItemCost cost) {
            return cost.count();
        }

        private static MerchantOffer getOffer(VillagerTrade trade) {
            return trade.getOffer(null);
        }

        private static boolean levelIsClientSide(Level level) {
            return level.isClientSide();
        }

        private static BlockPos findMapStructure(ServerLevel level) {
            return level.findNearestMapStructure((TagKey<Structure>) null, null, 0, false);
        }

        private static BlockPos findMapStructureSet(ServerLevel level) {
            return level.findNearestMapStructure((HolderSet<Structure>) null, null, 0, false);
        }

        private static Object registryAccess(Level level) {
            return level.registryAccess();
        }

        private static Object entityRegistryAccess(Entity entity) {
            return entity.registryAccess();
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

        private static MerchantOffers getOffers(Merchant merchant) {
            return merchant.getOffers();
        }

        private abstract static class VillagerNames extends AbstractVillager {
            private VillagerNames(EntityType<? extends AbstractVillager> type) {
                super(type, null);
            }

            private void villagerUpdateTrades() {
                updateTrades(null);
            }

            private void villagerAddOffers() {
                addOffersFromTradeSet(null, null, null);
            }

            private MerchantOffers villagerOffers() {
                return offers;
            }
        }
    }
}
