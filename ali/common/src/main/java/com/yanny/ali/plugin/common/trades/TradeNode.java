package com.yanny.ali.plugin.common.trades;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.api.TradeLevel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class TradeNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("trade");

    private final TooltipNode tooltip;
    @Nullable
    private final EntityType<?> entityType;

    public TradeNode(IServerUtils utils, @Nullable EntityType<?> entityType, Int2ObjectMap<TradeLevel> levels) {
        List<Int2ObjectMap.Entry<TradeLevel>> entries = levels.int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .toList();

        for (Int2ObjectMap.Entry<TradeLevel> entry : entries) {
            if (entry.getValue() instanceof TradeLevel.OfListings listings) {
                if (listings.listings().length > 0) {
                    addChildren(new TradeLevelNode(utils, entry.getIntKey(), listings.listings(), listings.levelInfo()));
                }
            } else if (entry.getValue() instanceof TradeLevel.OfTrades trades) {
                List<IDataNode> nodes = trades.trades().apply(utils);

                if (!nodes.isEmpty()) {
                    addChildren(new TradeLevelNode(entry.getIntKey(), trades.levelInfo(), nodes));
                }
            }
        }

        tooltip = TooltipNode.empty();
        this.entityType = entityType;
    }

    public TradeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        entityType = buf.readNullable((b) -> BuiltInRegistries.ENTITY_TYPE.getOptional(b.readResourceLocation()).orElse(null));
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeNullable(entityType, (b, type) -> b.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @Nullable
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
