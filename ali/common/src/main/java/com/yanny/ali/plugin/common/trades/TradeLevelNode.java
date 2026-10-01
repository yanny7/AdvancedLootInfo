package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

public class TradeLevelNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("trade_level");

    public final int level;
    public final NumberExpr selectionCount;
    public final float chance;
    private final TooltipNode tooltip;

    // a trader adds every entry instead of picking randomly once its pool is no bigger than the number it picks
    public TradeLevelNode(IServerUtils utils, int level, VillagerTrades.ItemListing[] itemListings, TradeLevelInfo levelInfo) {
        this.level = level;
        this.selectionCount = NumberExpr.min(levelInfo.offers(), NumberExpr.constant(itemListings.length));
        this.chance = levelInfo.chance();

        for (VillagerTrades.ItemListing itemListing : itemListings) {
            if (itemListing != null) {
                addChildren(utils.getItemListing(utils, itemListing, TooltipNode.empty()));
            }
        }

        tooltip = TooltipBuilder.branch((b) -> {
            b.add(TooltipBuilder.value(this.level).build(Lang.Value.LEVEL));
            b.add(TooltipBuilder.number(this.selectionCount).build(Lang.Description.RANDOM_TRADE_SELECTION));

            if (this.chance < 1.0f) {
                b.add(TooltipBuilder.percent(NumberExpr.constant(this.chance)).build(Lang.Description.CHANCE));
            }
        }).build();
    }

    public TradeLevelNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        level = buf.readInt();
        selectionCount = NumberExpr.decode(buf);
        chance = buf.readFloat();
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeInt(level);
        selectionCount.encode(buf);
        buf.writeFloat(chance);
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
