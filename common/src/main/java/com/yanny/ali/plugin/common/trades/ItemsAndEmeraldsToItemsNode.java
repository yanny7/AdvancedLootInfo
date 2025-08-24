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

public class ItemsAndEmeraldsToItemsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "items_and_emeralds_to_items");

    private final List<ITooltipNode> tooltip;

    public ItemsAndEmeraldsToItemsNode(IServerUtils utils, VillagerTrades.ItemsAndEmeraldsToItems listing, List<ITooltipNode> conditions) {
        addChildren(new ItemNode(utils, listing.fromItem.getItem(), new RangeValue(listing.fromItem.getCount())));
        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue(listing.emeraldCost)));
        addChildren(new ItemStackNode(utils, listing.toItem, new RangeValue(listing.toItem.getCount())));
        tooltip = new ArrayList<>(conditions);
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.uses", listing.maxUses));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier));
    }

    public ItemsAndEmeraldsToItemsNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.ItemsAndEmeraldsToItems listing) {
        return new Pair<>(
                List.of(Items.EMERALD, listing.fromItem.getItem()),
                List.of(listing.toItem.getItem())
        );
    }
}
