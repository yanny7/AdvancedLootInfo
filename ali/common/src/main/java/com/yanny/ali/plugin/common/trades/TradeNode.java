package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class TradeNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade");

    private final TooltipNode tooltip;

    public TradeNode(IServerUtils utils, Int2ObjectMap<VillagerTrades.ItemListing[]> itemListingMap, boolean isWanderingTrader) {
        List<Int2ObjectMap.Entry<VillagerTrades.ItemListing[]>> entries = itemListingMap.int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .toList();

        for (Int2ObjectMap.Entry<VillagerTrades.ItemListing[]> entry : entries) {
            if (entry.getValue().length > 0) {
                addChildren(new TradeLevelNode(utils, entry.getIntKey(), entry.getValue(), isWanderingTrader));
            }
        }

        tooltip = TooltipNode.empty();
    }

    public TradeNode(IServerUtils utils, List<Pair<VillagerTrades.ItemListing[], Integer>> itemListingList, boolean isWanderingTrader) {
        this(utils, convert(itemListingList), isWanderingTrader);
    }

    public TradeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public Identifier getId() {
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
