package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ItemStackNode;
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

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

public class SuspiciousStewForEmeraldNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "suspicious_stew_for_emerald");

    private final List<ITooltipNode> tooltip;

    public SuspiciousStewForEmeraldNode(IServerUtils utils, VillagerTrades.SuspiciousStewForEmerald listing, List<ITooltipNode> conditions) {
        ItemStack stew = Items.SUSPICIOUS_STEW.getDefaultInstance();

        SuspiciousStewItem.saveMobEffects(stew, listing.effects);

        addChildren(new ItemNode(utils, Items.EMERALD, new RangeValue()));
        addChildren(new ItemNode(utils, Items.AIR, new RangeValue()));
        addChildren(new ItemStackNode(utils, stew, new RangeValue(), List.of(
                getCollectionTooltip(utils, "ali.type.function.set_stew_effect", "ali.property.value.null", listing.effects, (u, k, effect) -> {
                    ITooltipNode tooltip = RegistriesTooltipUtils.getMobEffectTooltip(utils, "ali.property.value.effect", effect.effect());
                    tooltip.add(getIntegerTooltip(utils, "ali.property.value.duration", effect.duration()));
                    return tooltip;
                })
        )));
        tooltip = new ArrayList<>(conditions);
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.uses", 12));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.villager_xp", listing.xp));
        tooltip.add(getFloatTooltip(utils, "ali.property.value.price_multiplier", listing.priceMultiplier));
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
