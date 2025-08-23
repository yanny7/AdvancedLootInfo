package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.Collections;
import java.util.List;

public class EmeraldsForVillagerTypeItemNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "emerald_for_villager_type_item");

    public EmeraldsForVillagerTypeItemNode(IServerUtils utils, VillagerTrades.EmeraldsForVillagerTypeItem listing) {
        listing.trades.entrySet().forEach((entry) -> addChildren(new ItemsForEmeraldsNode(utils, listing, entry)));
    }

    public EmeraldsForVillagerTypeItemNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {

    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return Collections.emptyList();
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils ignoredUtils, VillagerTrades.EmeraldsForVillagerTypeItem listing) {
        return new Pair<>(
                List.of(Items.EMERALD),
                listing.trades.values().stream().toList()
        );
    }
}
