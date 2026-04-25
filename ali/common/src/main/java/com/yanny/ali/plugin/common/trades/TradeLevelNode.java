package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import org.jetbrains.annotations.NotNull;

public class TradeLevelNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade_level");

    public final int level;

    public TradeLevelNode(IServerUtils utils, int level, TradeSet tradeSet) {
        this.level = level;

        for (Holder<VillagerTrade> trade : tradeSet.getTrades()) {
            addChildren(TradeUtils.getNode(utils, trade.value()));
        }
    }

    public TradeLevelNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        level = buf.readInt();
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeInt(level);
    }

    @NotNull
    @Override
    public ITooltipNode getTooltip() {
        return ValueTooltipNode.value(level).build("ali.property.value.level");
    }

    @NotNull
    @Override
    public Identifier getId() {
        return ID;
    }
}
