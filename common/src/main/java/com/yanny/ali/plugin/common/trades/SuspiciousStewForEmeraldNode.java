package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
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

        SuspiciousStewItem.saveMobEffect(stew, listing.effect, listing.duration);

        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue()));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemStackNode(utils, stew, new RangeValue(), List.of(
                RegistriesTooltipUtils.getMobEffectTooltip(utils, "ali.property.value.effect", listing.effect),
                GenericTooltipUtils.getIntegerTooltip(utils, "ali.property.value.duration", listing.duration)
        )));
        tooltip = List.of(
                getIntegerTooltip(utils, "ali.property.value.uses", 12),
                getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.xp),
                getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier)
        );
    }

    public SuspiciousStewForEmeraldNode(IClientUtils utils, FriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.SuspiciousStewForEmerald ignoredListing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                List.of(Items.SUSPICIOUS_STEW)
        );
    }
}
