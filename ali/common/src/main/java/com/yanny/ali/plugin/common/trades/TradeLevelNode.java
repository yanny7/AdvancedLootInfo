package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TradeLevelNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade_level");

    public final int level;
    public final NumberExpr selectionCount;
    private final TooltipNode tooltip;

    // a trader adds every trade of the set instead of picking randomly once the set is no bigger than the number it picks
    public TradeLevelNode(IServerUtils utils, int level, TradeSet tradeSet) {
        this.level = level;
        LootCount selection = TooltipUtils.collectConditions(utils, () -> NumberExpr.min(utils.convertContextInt(utils, tradeSet.amount()), NumberExpr.constant(tradeSet.trades().size())));

        this.selectionCount = selection.value();

        for (Holder<VillagerTrade> trade : tradeSet.trades()) {
            addChildren(TradeUtils.getNode(utils, trade.value()));
        }

        tooltip = getTooltip(level, selection, 1.0f);
    }

    public TradeLevelNode(int level, TradeLevelInfo levelInfo, List<IDataNode> trades) {
        this.level = level;
        this.selectionCount = NumberExpr.min(levelInfo.offers(), NumberExpr.constant(trades.size()));
        trades.forEach(this::addChildren);
        tooltip = getTooltip(level, LootCount.of(selectionCount), levelInfo.chance());
    }

    public TradeLevelNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        level = buf.readInt();
        selectionCount = NumberExpr.decode(buf);
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

    @NotNull
    private static TooltipNode getTooltip(int level, LootCount selectionCount, float chance) {
        return TooltipBuilder.branch((b) -> {
            b.add(TooltipBuilder.value(level).build(Lang.Value.LEVEL));
            b.add(TooltipUtils.getNumberTooltip(selectionCount).build(Lang.Description.RANDOM_TRADE_SELECTION));

            if (chance < 1.0f) {
                b.add(TooltipBuilder.percent(NumberExpr.constant(chance)).build(Lang.Description.CHANCE));
            }
        }).build();
    }
}
