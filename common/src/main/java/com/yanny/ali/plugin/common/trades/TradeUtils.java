package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeUtils {
    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, MerchantOffer offer, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(offer.getBaseCostA()),
                new RangeValue(offer.getBaseCostA().getCount()),
                Either.left(offer.getCostB()),
                new RangeValue(offer.getCostB().getCount()),
                Either.left(offer.getResult()),
                new RangeValue(offer.getResult().getCount()),
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier(),
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.DyedArmorForEmeralds listing, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.value),
                EmptyTooltipNode.EMPTY,
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(listing.item.getDefaultInstance()),
                new RangeValue(),
                LiteralTooltipNode.translatable("ali.type.function.dyed_randomly"),
                listing.maxUses,
                listing.villagerXp,
                0.2F,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EmeraldForItems listing, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.itemStack),
                new RangeValue(listing.itemStack.getCount()),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EnchantBookForEmeralds listing, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(5, 64),
                EmptyTooltipNode.EMPTY,
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(Items.ENCHANTED_BOOK.getDefaultInstance()),
                new RangeValue(),
                LiteralTooltipNode.translatable("ali.type.function.enchant_randomly"),
                12,
                listing.villagerXp,
                0.2F,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EnchantedItemForEmeralds listing, ITooltipNode condition) {
        ITooltipNode tooltip = BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, UniformGenerator.between(5, 19)).build("ali.property.value.levels"))
                .add(utils.getValueTooltip(utils, false).build("ali.property.value.treasure"))
                .build("ali.type.function.enchant_with_levels");

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.baseEmeraldCost + 5, listing.baseEmeraldCost + 19),
                EmptyTooltipNode.EMPTY,
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(listing.itemStack),
                new RangeValue(),
                tooltip,
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.ItemsAndEmeraldsToItems listing, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.fromItem),
                new RangeValue(listing.fromItem.getCount()),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.emeraldCost),
                Either.left(listing.toItem),
                new RangeValue(listing.toItem.getCount()),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.ItemsForEmeralds listing, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.emeraldCost),
                Either.left(listing.itemStack),
                new RangeValue(listing.itemStack.getCount()),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.SuspiciousStewForEmerald listing, ITooltipNode condition) {
        ItemStack stew = Items.SUSPICIOUS_STEW.getDefaultInstance();

        SuspiciousStewItem.saveMobEffects(stew, listing.effects);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(stew),
                new RangeValue(),
                GenericTooltipUtils.getCollectionTooltip(utils, listing.effects, (u, effect) -> utils.getValueTooltip(utils, effect.effect())
                        .add(utils.getValueTooltip(utils, effect.duration()).build("ali.property.value.duration"))
                        .build("ali.property.value.effect")).build("ali.type.function.set_stew_effect"),
                12,
                listing.xp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.TippedArrowForItemsAndEmeralds listing, ITooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.fromItem.getDefaultInstance()),
                new RangeValue(listing.fromCount),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.emeraldCost),
                Either.left(listing.toItem),
                new RangeValue(listing.toCount),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                condition
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.TreasureMapForEmeralds listing, ITooltipNode condition) {
        ItemStack map = Items.MAP.getDefaultInstance();

        map.setHoverName(Component.translatable(listing.displayName));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.emeraldCost),
                EmptyTooltipNode.EMPTY,
                Either.left(Items.COMPASS.getDefaultInstance()),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(map),
                new RangeValue(),
                ArrayTooltipNode.array()
                        .add(utils.getValueTooltip(utils, listing.destination).build("ali.property.value.destination"))
                        .add(utils.getValueTooltip(utils, listing.destinationType).build("ali.property.value.map_decoration"))
                        .build(),
                listing.maxUses,
                listing.villagerXp,
                0.2F,
                condition
        );
    }

    @NotNull
    public static SubTradesNode<VillagerTrades.TypeSpecificTrade> getNode(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing, ITooltipNode condition) {
        return new SubTradesNode<>(utils, listing, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Map.Entry<VillagerType, VillagerTrades.ItemListing> entry : listing.trades().entrySet()) {
                    VillagerType type = entry.getKey();
                    ITooltipNode cond = utils.getValueTooltip(utils, type.toString()).build("ali.property.value.villager_type");

                    nodes.add(utils.getItemListing(utils, entry.getValue(), cond));
                }

                return nodes;
            }
        };
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, MerchantOffer offer) {
        return new Pair<>(
                List.of(offer.getBaseCostA().getItem(), offer.getCostB().getItem()),
                List.of(offer.getResult().getItem())
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.DyedArmorForEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(listing.item)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EmeraldForItems listing) {
        return new Pair<>(
                List.of(listing.itemStack.getItem()),
                List.of(Items.EMERALD)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EnchantBookForEmeralds ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.ENCHANTED_BOOK)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EnchantedItemForEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(listing.itemStack.getItem())
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.ItemsAndEmeraldsToItems listing) {
        return new Pair<>(
                List.of(Items.EMERALD, listing.fromItem.getItem()),
                List.of(listing.toItem.getItem())
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.ItemsForEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(listing.itemStack.getItem())
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.SuspiciousStewForEmerald ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.SUSPICIOUS_STEW)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.TippedArrowForItemsAndEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD, listing.fromItem),
                List.of(Items.TIPPED_ARROW)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.TreasureMapForEmeralds ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD, Items.COMPASS),
                List.of(Items.MAP)
        );
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing) {
        Pair<List<Item>, List<Item>> result = new Pair<>(new ArrayList<>(), new ArrayList<>());

        for (VillagerTrades.ItemListing itemListing : listing.trades().values()) {
            Pair<List<Item>, List<Item>> pair = utils.collectItems(utils, itemListing);

            result.getA().addAll(pair.getA());
            result.getB().addAll(pair.getB());
        }

        return result;
    }
}
