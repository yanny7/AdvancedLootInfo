package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class EnchantBookForEmeraldsNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "enchanted_book_for_emeralds");

    private final List<ITooltipNode> tooltip;

    public EnchantBookForEmeraldsNode(IServerUtils utils, VillagerTrades.EnchantBookForEmeralds listing) {
        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue(5, 64)));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemNode(utils, Items.ENCHANTED_BOOK, new RangeValue(), List.of(new TooltipNode(translatable("ali.type.function.enchant_randomly")))));
        tooltip = List.of(
                getIntegerTooltip(utils, "ali.property.value.uses", 12),
                getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.villagerXp),
                getFloatTooltip(utils, "ali.property.value.price_multiplier", 0.2F)
        );
    }

    public EnchantBookForEmeraldsNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EnchantBookForEmeralds ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.ENCHANTED_BOOK)
        );
    }
}
