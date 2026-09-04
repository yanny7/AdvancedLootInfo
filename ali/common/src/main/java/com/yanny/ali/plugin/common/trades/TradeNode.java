package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TradeNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade");

    private final TooltipNode tooltip;

    public TradeNode(IServerUtils utils, Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel) {
        List<Int2ObjectMap.Entry<ResourceKey<TradeSet>>> entries = tradeSetsByLevel.int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .toList();
        HolderLookup.RegistryLookup<TradeSet> lookup = utils.lookupProvider().lookup(Registries.TRADE_SET).orElseThrow();

        for (Int2ObjectMap.Entry<ResourceKey<TradeSet>> entry : entries) {
            Optional<Holder.Reference<TradeSet>> tradeSetReference = lookup.get(entry.getValue());

            tradeSetReference.ifPresent((tradeSet) -> addChildren(new TradeLevelNode(utils, entry.getIntKey(), tradeSet.value())));
        }

        tooltip = TooltipNode.empty();
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
}
