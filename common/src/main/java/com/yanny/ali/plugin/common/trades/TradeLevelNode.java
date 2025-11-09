package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;

public class TradeLevelNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "trade_level");

    public final int level;

    public TradeLevelNode(IServerUtils utils, int level, VillagerTrades.ItemListing[] itemListings) {
        this.level = level;

        for (VillagerTrades.ItemListing itemListing : itemListings) {
            if (itemListing != null) {
                addChildren(utils.getItemListing(utils, itemListing, EmptyTooltipNode.EMPTY));
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

    @Override
    public ITooltipNode getTooltip() {
        return ValueTooltipNode.value(level).key("ali.property.value.level");
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
