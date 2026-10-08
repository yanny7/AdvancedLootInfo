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
import com.yanny.ali.plugin.server.DataComponentTooltipUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class TradeUtils {
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
                Either.left(listing.itemStack.itemStack()),
                NumberExpr.constant(listing.itemStack.count()),
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
                Either.left(listing.fromItem.itemStack()),
                NumberExpr.constant(listing.fromItem.count()),
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.constant(listing.emeraldCost),
                Either.left(listing.toItem),
                NumberExpr.constant(listing.toItem.getCount()),
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
                NumberExpr.constant(listing.itemStack.getCount()),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.SuspiciousStewForEmerald listing, TooltipNode condition) {
        ItemStack stew = Items.SUSPICIOUS_STEW.getDefaultInstance();

        stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, listing.effects);

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
                DataComponentTooltipUtils.getSuspiciousStewEffectsTooltip(utils, listing.effects).build(),
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

        map.set(DataComponents.ITEM_NAME, Component.translatable(listing.displayName));

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

    @NotNull
    public static SubTradesNode<VillagerTrades.TypeSpecificTrade> getNode(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing, TooltipNode condition) {
        return new SubTradesNode<>(utils, listing, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Map.Entry<VillagerType, VillagerTrades.ItemListing> entry : listing.trades().entrySet()) {
                    VillagerType type = entry.getKey();
                    TooltipNode cond = utils.getValueTooltip(utils, ResourceKey.create(Registries.VILLAGER_TYPE, BuiltInRegistries.VILLAGER_TYPE.getKey(type))).build(Lang.Value.VILLAGER_TYPE);

                    nodes.add(utils.getItemListing(utils, entry.getValue(), cond));
                }

                return nodes;
            }
        };
    }
}
