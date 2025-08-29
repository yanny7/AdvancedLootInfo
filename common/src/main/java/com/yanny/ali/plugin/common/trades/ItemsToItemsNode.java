package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getFloatTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getIntegerTooltip;

public class ItemsToItemsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "items_to_items");

    private final List<ITooltipNode> tooltip;

    public ItemsToItemsNode(IServerUtils utils, MerchantOffer offer, List<ITooltipNode> conditions) {
        addChildren(new ItemStackNode(utils, offer.getBaseCostA(), new RangeValue(offer.getBaseCostA().getCount())));
        addChildren(new ItemStackNode(utils, offer.getCostB(), new RangeValue(offer.getCostB().getCount())));
        addChildren(new ItemStackNode(utils, offer.getResult(), new RangeValue(offer.getResult().getCount())));
        tooltip = new ArrayList<>(conditions);
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.uses", offer.getMaxUses()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.villager_xp", offer.getXp()));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.price_multiplier", offer.getPriceMultiplier()));
    }

    public ItemsToItemsNode(IClientUtils utils, FriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, MerchantOffer offer) {
        return new Pair<>(
                List.of(offer.getBaseCostA().getItem(), offer.getCostB().getItem()),
                List.of(offer.getResult().getItem())
        );
    }
}
