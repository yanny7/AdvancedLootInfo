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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.trading.TradeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class TradeNode extends ListNode {
    public static final Identifier ID = Utils.modLoc("trade");

    private final TooltipNode tooltip;
    @Nullable
    private final EntityType<?> entityType;

    public TradeNode(IServerUtils utils, @Nullable EntityType<?> entityType, Int2ObjectMap<TradeLevel> levels) {
        List<Int2ObjectMap.Entry<TradeLevel>> entries = levels.int2ObjectEntrySet()
                .stream()
                .sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
                .toList();
        HolderLookup.RegistryLookup<TradeSet> lookup = utils.lookupProvider().lookup(Registries.TRADE_SET).orElseThrow();

        for (Int2ObjectMap.Entry<TradeLevel> entry : entries) {
            int level = entry.getIntKey();

            switch (entry.getValue()) {
                case TradeLevel.OfSet(ResourceKey<TradeSet> tradeSet) ->
                        lookup.get(tradeSet).ifPresent((reference) -> addChildren(new TradeLevelNode(utils, level, reference.value())));
                case TradeLevel.OfTrades(TradeLevelInfo levelInfo, Function<IServerUtils, List<IDataNode>> trades) ->
                        addChildren(new TradeLevelNode(level, levelInfo, trades.apply(utils)));
            }
        }

        tooltip = TooltipNode.empty();
        this.entityType = entityType;
    }

    public TradeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        entityType = buf.readNullable((b) -> BuiltInRegistries.ENTITY_TYPE.getOptional(b.readIdentifier()).orElse(null));
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeNullable(entityType, (b, type) -> b.writeIdentifier(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
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
    public Identifier getId() {
        return ID;
    }
}
