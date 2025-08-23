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
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class EnchantedItemForEmeraldsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "enchanted_item_for_emeralds");

    private final List<ITooltipNode> tooltip;

    public EnchantedItemForEmeraldsNode(IServerUtils utils, VillagerTrades.EnchantedItemForEmeralds listing) {
        ITooltipNode t = new TooltipNode(translatable("ali.type.function.enchant_with_levels"));

        t.add(getNumberProviderTooltip(utils, "ali.property.value.levels", UniformGenerator.between(5, 19)));
        t.add(getBooleanTooltip(utils, "ali.property.value.treasure", false));

        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue(listing.baseEmeraldCost + 5, listing.baseEmeraldCost + 19)));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemStackNode(utils, listing.itemStack, new RangeValue(), List.of(t)));
        tooltip = List.of(
                getIntegerTooltip(utils, "ali.property.value.uses", listing.maxUses),
                getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp),
                getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier)
        );
    }

    public EnchantedItemForEmeraldsNode(IClientUtils utils, FriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EnchantedItemForEmeralds listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(listing.itemStack.getItem())
        );
    }
}
