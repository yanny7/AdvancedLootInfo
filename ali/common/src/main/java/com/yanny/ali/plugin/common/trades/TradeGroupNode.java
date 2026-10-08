package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.language.Lang;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TradeGroupNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("trade_group");

    public final NumberExpr selectionCount;
    private final TooltipNode tooltip;

    public TradeGroupNode(NumberExpr offers, List<IDataNode> trades) {
        selectionCount = NumberExpr.min(offers, NumberExpr.constant(trades.size()));
        trades.forEach(this::addChildren);
        tooltip = TooltipBuilder.branch((b) -> b.add(TooltipBuilder.number(selectionCount).build(Lang.Description.RANDOM_TRADE_SELECTION))).build();
    }

    public TradeGroupNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        selectionCount = NumberExpr.decode(buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        selectionCount.encode(buf);
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
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
