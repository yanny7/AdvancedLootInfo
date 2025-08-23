package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.Map;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class ItemsForEmeraldsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "item_for_emeralds");

    private final List<ITooltipNode> tooltip;

    public ItemsForEmeraldsNode(IServerUtils utils, VillagerTrades.ItemsForEmeralds listing) {
        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue(listing.emeraldCost)));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemStackNode(utils, listing.itemStack, new RangeValue(listing.itemStack.getCount())));
        tooltip = List.of(
                getIntegerTooltip(utils, "ali.property.value.uses", listing.maxUses),
                getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp),
                getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier)
        );
    }

    public ItemsForEmeraldsNode(IServerUtils utils, VillagerTrades.EmeraldsForVillagerTypeItem listing, Map.Entry<VillagerType, Item> entry) {
        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue(listing.cost)));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemNode(utils, entry.getValue(), new RangeValue()));
        tooltip = List.of(
                getIntegerTooltip(utils, "ali.property.value.uses", listing.maxUses),
                getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp),
                getFloatTooltip(utils, "ali.property.value.price_multiplier", 0.05F),
                getStringTooltip(utils, "ali.property.value.villager_type", entry.getKey().toString())
        );
    }

    public ItemsForEmeraldsNode(IClientUtils utils, FriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.ItemsForEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(listing.itemStack.getItem())
        );
    }
}
