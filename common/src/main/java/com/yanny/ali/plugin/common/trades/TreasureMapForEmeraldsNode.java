package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class TreasureMapForEmeraldsNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "treasure_map_for_emeralds");

    private final List<ITooltipNode> tooltip;

    public TreasureMapForEmeraldsNode(IServerUtils utils, VillagerTrades.TreasureMapForEmeralds listing, List<ITooltipNode> conditions) {
        ItemStack map = Items.MAP.getDefaultInstance();

        map.set(DataComponents.ITEM_NAME, Component.translatable(listing.displayName));

        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue(listing.emeraldCost)));
        addChildren(new ItemNode(utils, Items.COMPASS, new RangeValue()));
        addChildren(new ItemStackNode(utils, map, new RangeValue(), List.of(
                getTagKeyTooltip(utils, "ali.property.value.destination", listing.destination),
                getHolderTooltip(utils, "ali.property.value.map_decoration", listing.destinationType, RegistriesTooltipUtils::getMapDecorationTypeTooltip)
        )));
        tooltip = new ArrayList<>(conditions);
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.uses", listing.maxUses));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.price_multiplier", 0.2F));
    }

    public TreasureMapForEmeraldsNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.TreasureMapForEmeralds ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD, Items.COMPASS),
                List.of(Items.MAP)
        );
    }
}
