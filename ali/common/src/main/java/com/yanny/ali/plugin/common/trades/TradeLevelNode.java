package com.yanny.ali.plugin.common.trades;

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
    public final int selectionCount;
    private final TooltipNode tooltip;

    // Vanilla picks a fixed number of trades at random from this level's pool at villager-spawn/level-up time
    // (Villager#updateTrades / AbstractVillager#addOffersFromItemListings passes a literal `2`; the Wandering
    // Trader's two pools use `5` and `1` respectively, see WanderingTrader#updateTrades) - clamped to the pool
    // size since vanilla adds every entry instead of picking randomly when the pool is that small or smaller.
    public TradeLevelNode(IServerUtils utils, int level, TradeSet tradeSet, boolean isWanderingTrader) {
        this.level = level;
        this.selectionCount = isWanderingTrader
                ? (level == 2 ? 1 : Math.min(5, tradeSet.getTrades().size()))
                : Math.min(2, tradeSet.getTrades().size());

        for (Holder<VillagerTrade> trade : tradeSet.getTrades()) {
            addChildren(TradeUtils.getNode(utils, trade.value()));
        }

        tooltip = TooltipBuilder.branch((b) -> b
                .add(TooltipBuilder.value(this.level).build(Lang.Value.LEVEL))
                .add(TooltipBuilder.value(this.selectionCount).build(Lang.Description.RANDOM_TRADE_SELECTION))
        ).build();
    }

    public TradeLevelNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        level = buf.readInt();
        selectionCount = buf.readInt();
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeInt(level);
        buf.writeInt(selectionCount);
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
