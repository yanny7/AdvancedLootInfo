package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.api.TradeLevel;
import com.yanny.ali.api.TradeLevelInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class TradeNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("trade");

    private final TooltipNode tooltip;
    private final EntityType<?> entityType;

    public TradeNode(IServerUtils utils, EntityType<?> entityType, Int2ObjectMap<TradeLevel> levels) {
        List<Int2ObjectMap.Entry<TradeLevel>> entries = levels.int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .toList();

        for (Int2ObjectMap.Entry<TradeLevel> entry : entries) {
            if (entry.getValue() instanceof TradeLevel.OfListings(VillagerTrades.ItemListing[] listings, TradeLevelInfo levelInfo)) {
                if (listings.length > 0) {
                    addChildren(new TradeLevelNode(utils, entry.getIntKey(), listings, levelInfo));
                }
            } else if (entry.getValue() instanceof TradeLevel.OfTrades(TradeLevelInfo levelInfo, Function<IServerUtils, List<IDataNode>> trades)) {
                List<IDataNode> nodes = trades.apply(utils);

                if (!nodes.isEmpty()) {
                    addChildren(new TradeLevelNode(entry.getIntKey(), levelInfo, nodes));
                }
            }
        }

        tooltip = TooltipNode.empty();
        this.entityType = entityType;
    }

    public TradeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        entityType = BuiltInRegistries.ENTITY_TYPE.get(buf.readResourceLocation());
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
