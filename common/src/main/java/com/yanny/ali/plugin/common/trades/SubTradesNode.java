package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class SubTradesNode<T> extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "sub_trades");

    private final ITooltipNode tooltip;

    public SubTradesNode(IServerUtils utils, T listing, ITooltipNode condition) {
        this.tooltip = condition;

        for (IDataNode subTrade : getSubTrades(utils, listing)) {
            addChildren(subTrade);
        }
    }

    public SubTradesNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.decodeNode(buf);
    }

    public List<IDataNode> getSubTrades(IServerUtils ignoredUtils, T ignoredListing) {
        throw new IllegalStateException("Must be overridden");
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        TooltipNode.encodeNode(tooltip, buf);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
