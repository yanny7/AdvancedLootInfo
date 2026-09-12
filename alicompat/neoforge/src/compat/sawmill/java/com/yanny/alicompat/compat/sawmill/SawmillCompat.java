package com.yanny.alicompat.compat.sawmill;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.IModCompat;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.sawmill.trades.BiomeWoodToItemListing;
import net.mehvahdjukaar.sawmill.trades.LogStrippingListing;
import net.mehvahdjukaar.sawmill.trades.RandomWoodToItemListing;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class SawmillCompat implements IModCompat {
    private static final String LOG_KEY = "log";
    private static final String STRIPPED_LOG_KEY = "stripped_log";

    @NotNull
    @Override
    public String targetModId() {
        return SawmillLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemListing(BiomeWoodToItemListing.class, SawmillCompat::getBiomeWoodToItemListingNode);
        registry.registerItemListing(RandomWoodToItemListing.class, SawmillCompat::getRandomWoodToItemListingNode);
        registry.registerItemListing(LogStrippingListing.class, SawmillCompat::getLogStrippingListingNode);
    }

    @NotNull
    private static IDataNode getBiomeWoodToItemListingNode(IServerUtils utils, BiomeWoodToItemListing listing, TooltipNode condition) {
        return getWoodToItemListingNode(utils, condition, listing.buys(), listing.childKey(), listing.woodPrice(),
                listing.emeralds().itemStack(), listing.maxTrades(), listing.xp(), listing.priceMult(), true);
    }

    @NotNull
    private static IDataNode getRandomWoodToItemListingNode(IServerUtils utils, RandomWoodToItemListing listing, TooltipNode condition) {
        return getWoodToItemListingNode(utils, condition, listing.buys(), listing.childKey(), listing.woodPrice(),
                listing.emeralds().itemStack(), listing.maxTrades(), listing.xp(), listing.priceMult(), false);
    }

    @NotNull
    private static IDataNode getWoodToItemListingNode(IServerUtils utils, TooltipNode condition, boolean buys, String childKey, int woodPrice,
                                                      ItemStack emeraldStack, int maxTrades, int xp, float priceMult, boolean typeDependant) {
        Either<ItemStack, TagKey<? extends ItemLike>> wood = Either.left(getWoodStack(childKey, woodPrice));
        Either<ItemStack, TagKey<? extends ItemLike>> emeralds = Either.left(emeraldStack);
        TooltipNode woodTooltip = getWoodTypeTooltip(typeDependant);

        return new ItemsToItemsNode(
                utils,
                buys ? wood : emeralds,
                new RangeValue(buys ? woodPrice : emeraldStack.getCount()),
                buys ? woodTooltip : TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                buys ? emeralds : wood,
                new RangeValue(buys ? emeraldStack.getCount() : woodPrice),
                buys ? TooltipNode.empty() : woodTooltip,
                maxTrades,
                xp,
                priceMult,
                condition
        );
    }

    @NotNull
    private static IDataNode getLogStrippingListingNode(IServerUtils utils, LogStrippingListing listing, TooltipNode condition) {
        ItemStack price = listing.price().itemStack();

        return new ItemsToItemsNode(
                utils,
                Either.left(getWoodStack(LOG_KEY, listing.amount())),
                new RangeValue(listing.amount()),
                getWoodTypeTooltip(true),
                Either.left(price),
                new RangeValue(Math.max(1, price.getCount())),
                TooltipNode.empty(),
                Either.left(getWoodStack(STRIPPED_LOG_KEY, listing.amount())),
                new RangeValue(listing.amount()),
                getWoodTypeTooltip(true),
                listing.maxTrades(),
                listing.xp(),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static TooltipNode getWoodTypeTooltip(boolean typeDependant) {
        return TooltipBuilder.keyOnly(typeDependant ? SawmillLang.Value.VILLAGE_WOOD_TYPE : SawmillLang.Value.ANY_WOOD_TYPE).build();
    }

    @NotNull
    private static ItemStack getWoodStack(String childKey, int count) {
        Item item = WoodTypeRegistry.INSTANCE.getDefaultType().getItemOfThis(childKey);

        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }
}
