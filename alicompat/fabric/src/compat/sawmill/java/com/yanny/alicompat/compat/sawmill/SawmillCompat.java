package com.yanny.alicompat.compat.sawmill;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.sawmill.trades.BiomeToWoodList;
import net.mehvahdjukaar.sawmill.trades.BiomeWoodToItemListing;
import net.mehvahdjukaar.sawmill.trades.LogStrippingListing;
import net.mehvahdjukaar.sawmill.trades.RandomWoodToItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SawmillCompat implements IModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
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
        return getWoodToItemListingNode(utils, condition, listing.buys(), listing.childKey(), listing.woodPrice(), listing.emeralds().itemStack(),
                listing.maxTrades(), listing.xp(), listing.priceMult(), getVillageWoodTypes(listing.biomeWoods()), true);
    }

    @NotNull
    private static IDataNode getRandomWoodToItemListingNode(IServerUtils utils, RandomWoodToItemListing listing, TooltipNode condition) {
        List<WoodType> types = WoodTypeRegistry.INSTANCE.getValues().stream().filter((t) -> !listing.blacklist().contains(t)).toList();

        return getWoodToItemListingNode(utils, condition, listing.buys(), listing.childKey(), listing.woodPrice(), listing.emeralds().itemStack(),
                listing.maxTrades(), listing.xp(), listing.priceMult(), types, false);
    }

    @NotNull
    private static IDataNode getWoodToItemListingNode(IServerUtils utils, TooltipNode condition, boolean buys, String childKey, int woodPrice,
                                                      ItemStack emeraldStack, int maxTrades, int xp, float priceMult, List<WoodType> types, boolean typeDependant) {
        TooltipNode woodTooltip = getWoodTypeTooltip(typeDependant);
        IDataNode wood = TradeUtils.getItemSlotNode(getWoodStacks(types, (t) -> t.getItemOfThis(childKey)), NumberExpr.constant(woodPrice), woodTooltip);
        IDataNode emeralds = TradeUtils.getItemSlotNode(List.of(emeraldStack), NumberExpr.constant(emeraldStack.getCount()), TooltipNode.empty());

        return new ItemsToItemsNode(
                utils,
                buys ? wood : emeralds,
                TradeUtils.getEmptySlotNode(),
                buys ? emeralds : wood,
                maxTrades,
                xp,
                priceMult,
                condition
        );
    }

    @NotNull
    private static IDataNode getLogStrippingListingNode(IServerUtils utils, LogStrippingListing listing, TooltipNode condition) {
        List<WoodType> types = getVillageWoodTypes(listing.biomeWoods()).stream().filter((t) -> t.getItemOfThis(STRIPPED_LOG_KEY) != null).toList();
        ItemStack price = listing.price().itemStack();
        NumberExpr amount = NumberExpr.constant(listing.amount());
        TooltipNode woodTooltip = getWoodTypeTooltip(true);

        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(getWoodStacks(types, (t) -> t.log.asItem()), amount, woodTooltip),
                TradeUtils.getItemSlotNode(List.of(price), NumberExpr.constant(Math.max(1, price.getCount())), TooltipNode.empty()),
                TradeUtils.getItemSlotNode(getWoodStacks(types, (t) -> t.getItemOfThis(STRIPPED_LOG_KEY)), amount, woodTooltip),
                listing.maxTrades(),
                listing.xp(),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static List<ItemStack> getWoodStacks(List<WoodType> types, Function<WoodType, @Nullable Item> getter) {
        List<ItemStack> stacks = new ArrayList<>();

        for (WoodType type : types) {
            Item item = getter.apply(type);

            if (item != null && item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }

        return stacks;
    }

    @NotNull
    private static TooltipNode getWoodTypeTooltip(boolean typeDependant) {
        return TooltipBuilder.keyOnly(typeDependant ? SawmillLang.Value.VILLAGE_WOOD_TYPE : SawmillLang.Value.ANY_WOOD_TYPE).build();
    }

    @NotNull
    private static List<WoodType> getVillageWoodTypes(BiomeToWoodList biomeWoods) {
        Set<WoodType> types = new LinkedHashSet<>();

        getByBiome(biomeWoods).values().forEach(types::addAll);
        types.add(VanillaWoodTypes.OAK);
        return List.copyOf(types);
    }

    @NotNull
    private static Map<?, List<WoodType>> getByBiome(BiomeToWoodList biomeWoods) {
        try {
            Field field = BiomeToWoodList.class.getDeclaredField("byBiome");

            field.setAccessible(true);
            //noinspection unchecked
            return (Map<?, List<WoodType>>) field.get(biomeWoods);
        } catch (Throwable e) {
            LOGGER.warn("Failed to read Sawmill village wood types: {}", e.getMessage(), e);
            return Map.of();
        }
    }
}
