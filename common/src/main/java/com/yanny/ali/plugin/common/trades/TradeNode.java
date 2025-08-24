package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class TradeNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "trade");

    public TradeNode(IServerUtils utils, Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap) {
        List<Int2ObjectMap.Entry<VillagerTrades.ItemListing[]>> entries = itemListingMap.int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .toList();

        for (Int2ObjectMap.Entry<VillagerTrades.ItemListing[]> entry : entries) {
            if (entry.getValue().length > 0) {
                addChildren(new TradeLevelNode(utils, entry.getIntKey(), entry.getValue()));
            }
        }
    }

    public TradeNode(IServerUtils utils, List<Pair<VillagerTrades.ItemListing[], Integer>> itemListingList) {
        this(utils, convert(itemListingList));
    }

    public TradeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {

    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return List.of();
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> convert(List<Pair<VillagerTrades.ItemListing[], Integer>> itemListingList) {
        List<Pair<VillagerTrades.ItemListing[], Integer>> list = itemListingList.stream().sorted(Comparator.comparing(Pair::getRight)).toList();
        Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap = new Int2ObjectArrayMap<>();

        for (Pair<VillagerTrades.ItemListing[], Integer> pair : list) {
            itemListingMap.compute(pair.getRight(), (i, listing) -> {
                if (listing == null) {
                    return pair.getLeft();
                } else {
                    return ArrayUtils.addAll(listing, pair.getLeft());
                }
            });
        }

        return itemListingMap;
    }
}
