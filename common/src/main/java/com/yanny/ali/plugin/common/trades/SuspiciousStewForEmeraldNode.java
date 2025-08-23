package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import com.yanny.ali.plugin.server.DataComponentTooltipUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getFloatTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.getIntegerTooltip;

public class SuspiciousStewForEmeraldNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "suspicious_stew_for_emerald");

    private final List<ITooltipNode> tooltip;

    public SuspiciousStewForEmeraldNode(IServerUtils utils, VillagerTrades.SuspiciousStewForEmerald listing) {
        ItemStack stew = Items.SUSPICIOUS_STEW.getDefaultInstance();

        stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, listing.effects);

        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue()));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemStackNode(utils, stew, new RangeValue(), List.of(DataComponentTooltipUtils.getSuspiciousStewEffectsTooltip(utils, listing.effects))));
        tooltip = List.of(
                getIntegerTooltip(utils, "ali.property.value.uses", 12),
                getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.xp),
                getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier)
        );
    }

    public SuspiciousStewForEmeraldNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.SuspiciousStewForEmerald ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.SUSPICIOUS_STEW)
        );
    }
}
