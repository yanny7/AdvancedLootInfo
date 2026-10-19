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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TradeUtils {
    private static final int MAX_KEY_DEPTH = 4;

    public static VillagerTrades.ItemListing[] distinctListings(VillagerTrades.ItemListing[] listings) {
        Set<Object> seen = new HashSet<>();
        List<VillagerTrades.ItemListing> distinct = new ArrayList<>(listings.length);

        for (VillagerTrades.ItemListing listing : listings) {
            if (listing == null || seen.add(listingKey(listing))) {
                distinct.add(listing);
            }
        }

        return distinct.size() == listings.length ? listings : distinct.toArray(new VillagerTrades.ItemListing[0]);
    }

    public static Object listingKey(@Nullable VillagerTrades.ItemListing listing) {
        return structuralKey(listing, new IdentityHashMap<>(), 0);
    }

    @Nullable
    private static Object structuralKey(@Nullable Object value, Map<Object, Boolean> visiting, int depth) {
        if (value == null) {
            return null;
        }
        if (value instanceof ItemStack stack) {
            return Arrays.asList(ItemStack.class, stack.getItem(), stack.getCount(), stack.getTag());
        }

        Class<?> type = value.getClass();

        if (type.isArray()) {
            List<Object> elements = new ArrayList<>();

            elements.add(type);

            for (int i = 0; i < Array.getLength(value); i++) {
                elements.add(structuralKey(Array.get(value, i), visiting, depth + 1));
            }

            return elements;
        }
        if (value instanceof List<?> list) {
            List<Object> elements = new ArrayList<>();

            elements.add(List.class);
            list.forEach((e) -> elements.add(structuralKey(e, visiting, depth + 1)));
            return elements;
        }
        if (depth >= MAX_KEY_DEPTH || visiting.containsKey(value) || !isStructural(type)) {
            return value;
        }

        List<Object> fields = new ArrayList<>();

        fields.add(type);
        visiting.put(value, true);

        try {
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        fields.add(structuralKey(field.get(value), visiting, depth + 1));
                    }
                }
            }
        } catch (Throwable e) {
            return value;
        } finally {
            visiting.remove(value);
        }

        return fields;
    }

    private static boolean isStructural(Class<?> type) {
        String name = type.getName();

        if (type.isEnum() || name.startsWith("java.") || name.startsWith("net.minecraft.") || name.startsWith("com.mojang.")) {
            return false;
        }

        try {
            return type.isRecord() || type.getMethod("equals", Object.class).getDeclaringClass() == Object.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    @NotNull
    public static IDataNode getSlotNode(IServerUtils utils, LootPoolEntryContainer entry) {
        List<TooltipNode> conditions = new ArrayList<>();
        NumberExpr sumWeight = NodeUtils.getTotalWeight(utils, List.of(entry), conditions);

        return NodeUtils.getChildren(utils, new LootPoolEntryContainer[]{entry}, NumberExpr.constant(1), sumWeight, conditions, List.of(), List.of()).get(0);
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
                Either.left(offer.getCostB()),
                NumberExpr.constant(offer.getCostB().getCount()),
                Either.left(offer.getResult()),
                NumberExpr.constant(offer.getResult().getCount()),
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier(),
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.DyedArmorForEmeralds listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(listing.value),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(listing.item.getDefaultInstance()),
                NumberExpr.constant(1),
                TooltipBuilder.keyOnly(Lang.Functions.DYED_RANDOMLY).build(),
                listing.maxUses,
                listing.villagerXp,
                0.2F,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EmeraldForItems listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.item.getDefaultInstance()),
                NumberExpr.constant(listing.cost),
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(1),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EnchantBookForEmeralds listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.range(5, 64),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(Items.ENCHANTED_BOOK.getDefaultInstance()),
                NumberExpr.constant(1),
                TooltipBuilder.keyOnly(Lang.Functions.ENCHANT_RANDOMLY).build(),
                12,
                listing.villagerXp,
                0.2F,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EnchantedItemForEmeralds listing, TooltipNode condition) {
        TooltipNode tooltip = TooltipBuilder.branch((b) -> b
                        .add(TooltipBuilder.number(NumberExpr.uniformInt(5, 19)).build(Lang.Value.LEVELS))
                        .add(utils.getValueTooltip(utils, false).build(Lang.Value.TREASURE))
                )
                .build(Lang.Functions.ENCHANT_WITH_LEVELS);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.min(NumberExpr.add(NumberExpr.constant(listing.baseEmeraldCost), NumberExpr.uniformInt(5, 19)), NumberExpr.constant(64)),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(listing.itemStack),
                NumberExpr.constant(1),
                tooltip,
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.ItemsAndEmeraldsToItems listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.fromItem),
                NumberExpr.constant(listing.fromCount),
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(listing.emeraldCost),
                Either.left(listing.toItem),
                NumberExpr.constant(listing.toCount),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.ItemsForEmeralds listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(listing.emeraldCost),
                Either.left(listing.itemStack),
                NumberExpr.constant(listing.numberOfItems),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.SuspiciousStewForEmerald listing, TooltipNode condition) {
        ItemStack stew = Items.SUSPICIOUS_STEW.getDefaultInstance();

        SuspiciousStewItem.saveMobEffect(stew, listing.effect, listing.duration);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(stew),
                NumberExpr.constant(1),
                TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, listing.effect).build(Lang.Value.EFFECT))
                                .add(utils.getValueTooltip(utils, listing.duration).build(Lang.Value.DURATION))
                        )
                        .build(),
                12,
                listing.xp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.TippedArrowForItemsAndEmeralds listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.fromItem.getDefaultInstance()),
                NumberExpr.constant(listing.fromCount),
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(listing.emeraldCost),
                Either.left(listing.toItem),
                NumberExpr.constant(listing.toCount),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.TreasureMapForEmeralds listing, TooltipNode condition) {
        ItemStack map = Items.MAP.getDefaultInstance();

        map.setHoverName(Component.translatable(listing.displayName));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(listing.emeraldCost),
                TooltipNode.empty(),
                Either.left(Items.COMPASS.getDefaultInstance()),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(map),
                NumberExpr.constant(1),
                TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, listing.destination).build(Lang.Value.DESTINATION))
                                .add(utils.getValueTooltip(utils, listing.destinationType).build(Lang.Value.MAP_DECORATION))
                        )
                        .build(),
                listing.maxUses,
                listing.villagerXp,
                0.2F,
                condition
        );
    }
}
