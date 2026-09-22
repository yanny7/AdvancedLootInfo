package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.api.TradeLevel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class TradeNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade");

    private final TooltipNode tooltip;

    public TradeNode(IServerUtils utils, Int2ObjectMap<TradeLevel> levels) {
        levels.int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .forEach((entry) -> addChildren(new TradeLevelNode(utils, entry.getIntKey(), entry.getValue())));

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
