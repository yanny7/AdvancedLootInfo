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
import net.mehvahdjukaar.sawmill.CarpenterTrades;
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
        registry.registerItemListing(CarpenterTrades.WoodToItemListing.class, SawmillCompat::getWoodToItemListingNode);
        registry.registerItemListing(CarpenterTrades.LogStrippingListing.class, SawmillCompat::getLogStrippingListingNode);
    }

    @NotNull
    private static IDataNode getWoodToItemListingNode(IServerUtils utils, CarpenterTrades.WoodToItemListing listing, TooltipNode condition) {
        Either<ItemStack, TagKey<? extends ItemLike>> wood = Either.left(getWoodStack(listing.childKey(), listing.woodPrice()));
        Either<ItemStack, TagKey<? extends ItemLike>> emeralds = Either.left(listing.emeralds());
        TooltipNode woodTooltip = getWoodTypeTooltip(listing.typeDependant());

        return new ItemsToItemsNode(
                utils,
                listing.buys() ? wood : emeralds,
                new RangeValue(listing.buys() ? listing.woodPrice() : listing.emeralds().getCount()),
                listing.buys() ? woodTooltip : TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                listing.buys() ? emeralds : wood,
                new RangeValue(listing.buys() ? listing.emeralds().getCount() : listing.woodPrice()),
                listing.buys() ? TooltipNode.empty() : woodTooltip,
                listing.maxTrades(),
                listing.xp(),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static IDataNode getLogStrippingListingNode(IServerUtils utils, CarpenterTrades.LogStrippingListing listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(getWoodStack(LOG_KEY, listing.amount())),
                new RangeValue(listing.amount()),
                getWoodTypeTooltip(true),
                Either.left(listing.price()),
                new RangeValue(Math.max(1, listing.price().getCount())),
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
