package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.GroupNode;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TradeUtils {
    private static final int MAX_KEY_DEPTH = 4;

    public static Object[] distinctListings(Object[] listings) {
        Set<Object> seen = new HashSet<>();
        List<Object> distinct = new ArrayList<>(listings.length);

        for (Object listing : listings) {
            if (listing == null || seen.add(valueKey(listing))) {
                distinct.add(listing);
            }
        }

        return distinct.size() == listings.length ? listings : distinct.toArray();
    }

    public static Object valueKey(@Nullable Object value) {
        try {
            Object key = structuralKey(value, new IdentityHashMap<>(), 0);

            // hashed here so a foreign hashCode that throws is caught, not inside the caller's HashSet
            Objects.hashCode(key);
            return key;
        } catch (Throwable e) {
            return new Ref(value);
        }
    }

    @Nullable
    private static Object structuralKey(@Nullable Object value, Map<Object, Boolean> visiting, int depth) {
        if (value == null) {
            return null;
        }
        if (value instanceof ItemStack stack) {
            return Arrays.asList(ItemStack.class, stack.getItem(), stack.getCount(), stack.getComponentsPatch());
        }

        Class<?> type = value.getClass();
        boolean container = type.isArray() || value instanceof Collection<?> || value instanceof Map<?, ?>;

        if (!container && !isStructural(type)) {
            return value;
        }
        if (depth >= MAX_KEY_DEPTH || visiting.containsKey(value)) {
            return new Ref(value);
        }

        visiting.put(value, true);

        try {
            if (type.isArray()) {
                List<Object> elements = new ArrayList<>();

                elements.add(type);

                for (int i = 0; i < Array.getLength(value); i++) {
                    elements.add(structuralKey(Array.get(value, i), visiting, depth + 1));
                }

                return elements;
            }
            if (value instanceof Set<?> set) {
                Set<Object> elements = new HashSet<>();

                set.forEach((e) -> elements.add(structuralKey(e, visiting, depth + 1)));
                return Arrays.asList(Set.class, elements);
            }
            if (value instanceof Collection<?> collection) {
                List<Object> elements = new ArrayList<>();

                elements.add(List.class);
                collection.forEach((e) -> elements.add(structuralKey(e, visiting, depth + 1)));
                return elements;
            }
            if (value instanceof Map<?, ?> map) {
                Map<Object, Object> entries = new HashMap<>();

                map.forEach((k, v) -> entries.put(structuralKey(k, visiting, depth + 1), structuralKey(v, visiting, depth + 1)));
                return Arrays.asList(Map.class, entries);
            }

            List<Object> fields = new ArrayList<>();

            fields.add(type);

            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        fields.add(structuralKey(field.get(value), visiting, depth + 1));
                    }
                }
            }

            return fields;
        } catch (Throwable e) {
            return new Ref(value);
        } finally {
            visiting.remove(value);
        }
    }

    private static boolean isStructural(Class<?> type) {
        String name = type.getName();

        if (type.isEnum() || name.startsWith("java.")) {
            return false;
        }
        if (type.isRecord()) {
            return true;
        }
        if (name.startsWith("net.minecraft.") || name.startsWith("com.mojang.")) {
            return false;
        }

        try {
            return type.getMethod("equals", Object.class).getDeclaringClass() == Object.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @NotNull
    public static IDataNode getSlotNode(IServerUtils utils, LootPoolEntryContainer entry) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr sumWeight = NodeUtils.getTotalWeight(utils, List.of(entry), conditions);

        return NodeUtils.getChildren(utils, List.of(entry), NumberExpr.constant(1), sumWeight, conditions, List.of(), List.of()).get(0);
    }

    @NotNull
    public static IDataNode getSlotNode(List<IDataNode> options) {
        return options.size() == 1 ? options.get(0) : new GroupNode(options, TooltipNode.empty());
    }

    @NotNull
    public static IDataNode getItemSlotNode(List<ItemStack> stacks, NumberExpr count, TooltipNode tooltip) {
        return getSlotNode(stacks.stream().<IDataNode>map((s) -> new ItemNode(1, count, s, tooltip, List.of(), List.of())).toList());
    }

    @NotNull
    public static IDataNode getEmptySlotNode() {
        return new ItemNode(1, NumberExpr.constant(1), ItemStack.EMPTY, TooltipNode.empty(), List.of(), List.of());
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, MerchantOffer offer, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(offer.getBaseCostA()),
                NumberExpr.constant(offer.getBaseCostA().getCount()),
                TooltipNode.empty(),
                Either.left(offer.getCostB()),
                NumberExpr.constant(offer.getCostB().getCount()),
                TooltipNode.empty(),
                Either.left(offer.getResult()),
                NumberExpr.constant(offer.getResult().getCount()),
                TooltipNode.empty(),
                NumberExpr.constant(offer.getMaxUses()),
                NumberExpr.constant(offer.getXp()),
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrade trade) {
        return tradeNode(utils, trade, utils.getValueTooltip(utils, trade.doubleTradePriceEnchantments).build(Lang.Branch.DOUBLE_TRADE_PRICE_ENCHANTMENTS));
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrade trade, TooltipNode condition) {
        return tradeNode(utils, trade, TooltipBuilder.array((b) -> b
                .add(condition)
                .add(utils.getValueTooltip(utils, trade.doubleTradePriceEnchantments).build(Lang.Branch.DOUBLE_TRADE_PRICE_ENCHANTMENTS))
        ).build());
    }

    @NotNull
    private static ItemsToItemsNode tradeNode(IServerUtils utils, VillagerTrade trade, TooltipNode tooltip) {
        return new ItemsToItemsNode(
                utils,
                Either.left(trade.wants.item().value().getDefaultInstance()),
                TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, trade.wants.count(), c)),
                utils.getValueTooltip(utils, trade.wants.components()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.additionalWants.map((t) -> t.item().value().getDefaultInstance()).orElse(ItemStack.EMPTY)),
                trade.additionalWants.map((t) -> TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, t.count(), c))).orElse(LootCount.of(NumberExpr.constant(1))),
                trade.additionalWants.map((t) -> utils.getValueTooltip(utils, t.components())).orElse(TooltipBuilder.empty()).build(Lang.Branch.EXPECTED_COMPONENTS),
                Either.left(trade.gives.create()),
                NumberExpr.constant(trade.gives.count()),
                utils.getValueTooltip(utils, NodeUtils.unwrapFunctions(utils, trade.givenItemModifier)).build(),
                TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, trade.maxUses, c)),
                TooltipUtils.collectConditions((c) -> utils.convertContextInt(utils, trade.xp, c)),
                tooltip
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrade trade) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();

        inputs.add(trade.wants.item().value());
        trade.additionalWants.ifPresent((tradeCost) -> inputs.add(tradeCost.item().value()));
        outputs.add(trade.gives.item().value());

        return new Pair<>(inputs, outputs);
    }

    private record Ref(@Nullable Object value) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Ref ref && ref.value == value;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(value);
        }
    }
}
