package com.yanny.ali.plugin.common.trades;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.NodeUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getResourceKeyTooltip;

public class TypeSpecificTradeNode extends ListNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Utils.MOD_ID, "type_specific_trade");

    private final List<ITooltipNode> tooltip;

    public TypeSpecificTradeNode(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing, List<ITooltipNode> conditions) {
        listing.trades().forEach((key, value) -> {
            List<ITooltipNode> cond = new ArrayList<>(conditions);

            cond.add(getResourceKeyTooltip(utils, "ali.property.value.villager_type", key));
            addChildren(utils.getItemListing(utils, value, cond));
        });
        tooltip = List.of(
        );
    }

    public TypeSpecificTradeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
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
    public static Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, VillagerTrades.TypeSpecificTrade listing) {
        List<Item> inputs = new ArrayList<>();
        List<Item> outputs = new ArrayList<>();

        listing.trades().values().forEach((itemListing) -> {
            Pair<List<Item>, List<Item>> pair = utils.collectItems(utils, itemListing);

            inputs.addAll(pair.getA());
            outputs.addAll(pair.getB());
        });

        return new Pair<>(inputs, outputs);
    }
}
