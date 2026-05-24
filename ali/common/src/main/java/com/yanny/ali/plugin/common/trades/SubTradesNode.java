package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SubTradesNode<T> extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("sub_trades");

    private final TooltipNode tooltip;

    public SubTradesNode(IServerUtils utils, T listing, TooltipNode condition) {
        this.tooltip = condition;

        for (IDataNode subTrade : getSubTrades(utils, listing)) {
            addChildren(subTrade);
        }
    }

    public SubTradesNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.CACHE.getNodeById(buf.readVarInt());
    }

    public List<IDataNode> getSubTrades(IServerUtils ignoredUtils, T ignoredListing) {
        throw new IllegalStateException("Must be overridden");
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeVarInt(TooltipNode.CACHE.getNodeId(tooltip));
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
