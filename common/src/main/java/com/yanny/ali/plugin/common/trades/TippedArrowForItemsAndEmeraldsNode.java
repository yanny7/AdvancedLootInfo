package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getFloatTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getIntegerTooltip;

public class TippedArrowForItemsAndEmeraldsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "tipped_arrow_for_item_and_emeralds");

    private final List<ITooltipNode> tooltip;

    public TippedArrowForItemsAndEmeraldsNode(IServerUtils utils, VillagerTrades.TippedArrowForItemsAndEmeralds listing, List<ITooltipNode> conditions) {
        addChildren(new ItemNode(utils, listing.fromItem, new RangeValue(listing.fromCount)));
        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue(listing.emeraldCost)));
        addChildren(new ItemStackNode(utils, listing.toItem, new RangeValue(listing.toCount)));
        tooltip = new ArrayList<>(conditions);
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.uses", listing.maxUses));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier));
    }

    public TippedArrowForItemsAndEmeraldsNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.TippedArrowForItemsAndEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD, listing.fromItem),
                List.of(Items.TIPPED_ARROW)
        );
    }
}
