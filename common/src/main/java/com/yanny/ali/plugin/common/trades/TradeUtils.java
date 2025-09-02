package com.yanny.ali.plugin.common.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.Collections;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class TradeUtils {
    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, MerchantOffer offer, List<ITooltipNode> conditions) {
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
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.DyedArmorForEmeralds listing, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.value),
                Collections.emptyList(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(listing.item.getDefaultInstance()),
                new RangeValue(),
                Collections.singletonList(new TooltipNode(translatable("ali.type.function.dyed_randomly"))),
                listing.maxUses,
                listing.villagerXp,
                0.2F,
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EmeraldForItems listing, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.item.getDefaultInstance()),
                new RangeValue(listing.cost),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EnchantBookForEmeralds listing, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(5, 64),
                Collections.emptyList(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(Items.ENCHANTED_BOOK.getDefaultInstance()),
                new RangeValue(),
                Collections.singletonList(new TooltipNode(translatable("ali.type.function.enchant_randomly"))),
                12,
                listing.villagerXp,
                0.2F,
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.EnchantedItemForEmeralds listing, List<ITooltipNode> conditions) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.function.enchant_with_levels"));

        tooltip.add(getNumberProviderTooltip(utils, "ali.property.value.levels", UniformGenerator.between(5, 19)));
        tooltip.add(getBooleanTooltip(utils, "ali.property.value.treasure", false));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.baseEmeraldCost + 5, listing.baseEmeraldCost + 19),
                Collections.emptyList(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(listing.itemStack),
                new RangeValue(),
                Collections.singletonList(tooltip),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.ItemsAndEmeraldsToItems listing, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.fromItem),
                new RangeValue(listing.fromCount),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.emeraldCost),
                Either.left(listing.toItem),
                new RangeValue(listing.toCount),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.ItemsForEmeralds listing, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.emeraldCost),
                Either.left(listing.itemStack),
                new RangeValue(listing.numberOfItems),
                listing.maxUses,
                listing.villagerXp,
                listing.priceMultiplier,
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.SuspiciousStewForEmerald listing, List<ITooltipNode> conditions) {
        ItemStack stew = Items.SUSPICIOUS_STEW.getDefaultInstance();

        SuspiciousStewItem.saveMobEffect(stew, listing.effect, listing.duration);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(stew),
                new RangeValue(),
                List.of(
                        RegistriesTooltipUtils.getMobEffectTooltip(utils, "ali.property.value.effect", listing.effect),
                        GenericTooltipUtils.getIntegerTooltip(utils, "ali.property.value.duration", listing.duration)
                ),
                12,
                listing.xp,
                listing.priceMultiplier,
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.TippedArrowForItemsAndEmeralds listing, List<ITooltipNode> conditions) {
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
                conditions
        );
    }

    @NotNull
    public static ItemsToItemsNode getNode(IServerUtils utils, VillagerTrades.TreasureMapForEmeralds listing, List<ITooltipNode> conditions) {
        ItemStack map = Items.MAP.getDefaultInstance();

        map.setHoverName(Component.translatable(listing.displayName));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(listing.emeraldCost),
                Collections.emptyList(),
                Either.left(Items.COMPASS.getDefaultInstance()),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(map),
                new RangeValue(),
                List.of(
                        getTagKeyTooltip(utils, "ali.property.value.destination", listing.destination),
                        getEnumTooltip(utils, "ali.property.value.map_decoration", listing.destinationType)
                ),
                listing.maxUses,
                listing.villagerXp,
                0.2F,
                conditions
        );
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
                List.of(listing.item),
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
}
