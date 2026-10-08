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
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.sawmill.CarpenterTrades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerType;
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
import java.util.function.Supplier;

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
        registry.registerItemListing(CarpenterTrades.WoodToItemListing.class, SawmillCompat::getWoodToItemListingNode);
        registry.registerItemListing(CarpenterTrades.LogStrippingListing.class, SawmillCompat::getLogStrippingListingNode);
    }

    @NotNull
    private static IDataNode getWoodToItemListingNode(IServerUtils utils, CarpenterTrades.WoodToItemListing listing, TooltipNode condition) {
        List<WoodType> types = listing.typeDependant() ? getVillageWoodTypes() : List.copyOf(WoodTypeRegistry.getTypes());
        TooltipNode woodTooltip = getWoodTypeTooltip(listing.typeDependant());
        IDataNode wood = TradeUtils.getItemSlotNode(getWoodStacks(types, (t) -> t.getItemOfThis(listing.childKey())), NumberExpr.constant(listing.woodPrice()), woodTooltip);
        IDataNode emeralds = TradeUtils.getItemSlotNode(List.of(listing.emeralds()), NumberExpr.constant(listing.emeralds().getCount()), TooltipNode.empty());

        return new ItemsToItemsNode(
                utils,
                listing.buys() ? wood : emeralds,
                TradeUtils.getEmptySlotNode(),
                listing.buys() ? emeralds : wood,
                listing.maxTrades(),
                listing.xp(),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static IDataNode getLogStrippingListingNode(IServerUtils utils, CarpenterTrades.LogStrippingListing listing, TooltipNode condition) {
        List<WoodType> types = getVillageWoodTypes().stream().filter((t) -> t.getItemOfThis(STRIPPED_LOG_KEY) != null).toList();
        NumberExpr amount = NumberExpr.constant(listing.amount());
        TooltipNode woodTooltip = getWoodTypeTooltip(true);

        return new ItemsToItemsNode(
                utils,
                TradeUtils.getItemSlotNode(getWoodStacks(types, (t) -> t.log.asItem()), amount, woodTooltip),
                TradeUtils.getItemSlotNode(List.of(listing.price()), NumberExpr.constant(Math.max(1, listing.price().getCount())), TooltipNode.empty()),
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
    private static List<WoodType> getVillageWoodTypes() {
        Map<VillagerType, List<WoodType>> typeMap = getTypeMap();
        Set<WoodType> types = new LinkedHashSet<>();

        BuiltInRegistries.VILLAGER_TYPE.forEach((t) -> types.addAll(typeMap.getOrDefault(t, List.of(WoodTypeRegistry.OAK_TYPE))));
        return List.copyOf(types);
    }

    @NotNull
    private static Map<VillagerType, List<WoodType>> getTypeMap() {
        try {
            Field field = CarpenterTrades.class.getDeclaredField("TYPE_MAP");

            field.setAccessible(true);
            //noinspection unchecked
            return ((Supplier<Map<VillagerType, List<WoodType>>>) field.get(null)).get();
        } catch (Throwable e) {
            LOGGER.warn("Failed to read Sawmill village wood types: {}", e.getMessage(), e);
            return Map.of();
        }
    }
}
