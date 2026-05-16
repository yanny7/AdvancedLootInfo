package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SubTradesNode<T> extends ListNode {
    public static final Identifier ID = Utils.modLoc("sub_trades");

    private final TooltipNode tooltip;

    public SubTradesNode(IServerUtils utils, T listing, TooltipNode condition) {
        this.tooltip = condition;

        for (IDataNode subTrade : getSubTrades(utils, listing)) {
            addChildren(subTrade);
        }
    }

    public SubTradesNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.decode(utils, buf);
    }

    public List<IDataNode> getSubTrades(IServerUtils ignoredUtils, T ignoredListing) {
        throw new IllegalStateException("Must be overridden");
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip.encode(utils, buf);
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
