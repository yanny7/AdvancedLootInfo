package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.List;

public class SubTradesNode<T extends VillagerTrades.ItemListing> extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "sub_trades");

    private final List<ITooltipNode> tooltip;

    public SubTradesNode(IServerUtils utils, T listing, List<ITooltipNode> conditions) {
        this.tooltip = conditions;

        for (IDataNode subTrade : getSubTrades(utils, listing)) {
            addChildren(subTrade);
        }
    }

    public SubTradesNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    public List<IDataNode> getSubTrades(IServerUtils ignoredUtils, T ignoredListing) {
        throw new IllegalStateException("Must be overridden");
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
