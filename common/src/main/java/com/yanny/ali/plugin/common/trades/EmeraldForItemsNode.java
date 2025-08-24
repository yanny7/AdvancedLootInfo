package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public class EmeraldForItemsNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "emerald_for_items");

    private final List<ITooltipNode> tooltip;

    public EmeraldForItemsNode(IServerUtils utils, VillagerTrades.EmeraldForItems listing, List<ITooltipNode> conditions) {
        addChildren(new ItemStackNode(utils, listing.itemStack.itemStack(), new RangeValue(listing.itemStack.count())));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue()));
        tooltip = new ArrayList<>(conditions);
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.uses", listing.maxUses));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier));
    }

    public EmeraldForItemsNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EmeraldForItems listing) {
        return new Pair<>(
                List.of(listing.itemStack.item().value()),
                List.of(Items.EMERALD)
        );
    }
}
