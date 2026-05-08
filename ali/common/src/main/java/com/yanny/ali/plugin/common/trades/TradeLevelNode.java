package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

public class TradeLevelNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("trade_level");

    public final int level;

    public TradeLevelNode(IServerUtils utils, int level, VillagerTrades.ItemListing[] itemListings) {
        this.level = level;

        for (VillagerTrades.ItemListing itemListing : itemListings) {
            if (itemListing != null) {
                addChildren(utils.getItemListing(utils, itemListing, TooltipNode.EMPTY_INSTANCE));
            }
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
    public TooltipNode getTooltip() {
        return TooltipBuilder.value(level).build("ali.property.value.level");
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
