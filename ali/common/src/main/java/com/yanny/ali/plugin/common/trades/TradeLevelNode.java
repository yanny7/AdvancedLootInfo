package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.language.Lang;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import org.jetbrains.annotations.NotNull;

public class TradeLevelNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade_level");

    public final int level;
    public final RangeValue selectionCount;
    private final TooltipNode tooltip;

    // a trader adds every trade of the set instead of picking randomly once the set is no bigger than the number it picks
    public TradeLevelNode(IServerUtils utils, int level, TradeSet tradeSet) {
        this.level = level;
        this.selectionCount = utils.convertNumber(utils, tradeSet.amount).clamp(0, tradeSet.getTrades().size());

        for (Holder<VillagerTrade> trade : tradeSet.getTrades()) {
            addChildren(TradeUtils.getNode(utils, trade.value()));
        }

        tooltip = TooltipBuilder.branch((b) -> b
                .add(TooltipBuilder.value(this.level).build(Lang.Value.LEVEL))
                .add(TooltipBuilder.value(this.selectionCount.toIntString()).build(Lang.Description.RANDOM_TRADE_SELECTION))
        ).build();
    }

    public TradeLevelNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        level = buf.readInt();
        selectionCount = new RangeValue(buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeInt(level);
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
    public Identifier getId() {
        return ID;
    }
}
