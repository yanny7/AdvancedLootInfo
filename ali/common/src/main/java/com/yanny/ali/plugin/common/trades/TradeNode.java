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
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TradeNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade");

    public TradeNode(IServerUtils utils, VillagerProfession profession) {
        List<Int2ObjectMap.Entry<ResourceKey<TradeSet>>> entries = profession.tradeSetsByLevel().int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .toList();
        HolderLookup.RegistryLookup<TradeSet> lookup = utils.lookupProvider().lookup(Registries.TRADE_SET).orElseThrow();

        for (Int2ObjectMap.Entry<ResourceKey<TradeSet>> entry : entries) {
            Optional<Holder.Reference<TradeSet>> tradeSetReference = lookup.get(entry.getValue());

            tradeSetReference.ifPresent((tradeSet) -> addChildren(new TradeLevelNode(utils, entry.getIntKey(), tradeSet.value())));
        }
    }

    public TradeNode(IServerUtils utils, List<TradeSet> trades) {
        for (int i = 0; i < trades.size(); i++) {
            addChildren(new TradeLevelNode(utils, i, trades.get(i)));
        }
    }

    public TradeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {

    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return TooltipNode.EMPTY_INSTANCE;
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }
}
