package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.Collections;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.value;

public class TradeLevelNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "trade_level");

    public final int level;

    public TradeLevelNode(IServerUtils utils, int level, VillagerTrades.ItemListing[] itemListings) {
        this.level = level;

        for (VillagerTrades.ItemListing itemListing : itemListings) {
            if (itemListing != null) {
                addChildren(utils.getItemListing(utils, itemListing, Collections.emptyList()));
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
    public List<ITooltipNode> getTooltip() {
        return List.of(new TooltipNode(translatable("ali.property.value.level", value(level))));
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
