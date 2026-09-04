package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.language.Lang;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import org.jetbrains.annotations.NotNull;

public class TradeLevelNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade_level");

    public final int level;
    public final int selectionCount;
    private final TooltipNode tooltip;

    // a trader adds every entry instead of picking randomly once its pool is no bigger than the number it picks
    public TradeLevelNode(IServerUtils utils, int level, VillagerTrades.ItemListing[] itemListings, int offers) {
        this.level = level;
        this.selectionCount = Math.min(offers, itemListings.length);

        for (VillagerTrades.ItemListing itemListing : itemListings) {
            if (itemListing != null) {
                addChildren(utils.getItemListing(utils, itemListing, TooltipNode.empty()));
            }
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
