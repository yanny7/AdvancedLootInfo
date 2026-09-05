package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.TradeItem;
import com.almostreliable.morejs.features.villager.trades.TreasureMapTrade;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.jetbrains.annotations.Nullable;

public class TreasureMapTradeAccessor extends BaseAccessor<TreasureMapTrade> implements IItemListing {
    @FieldAccessor
    private TradeItem firstInput;

    @FieldAccessor
    private TradeItem secondInput;

    @Nullable
    @FieldAccessor
    private Component displayName;

    @FieldAccessor
    private MapDecoration.Type destinationType;

    @FieldAccessor
    private byte mapViewScale;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int villagerExperience;

    @FieldAccessor
    private float priceMultiplier;

    public TreasureMapTradeAccessor(TreasureMapTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TradeItemAccessor first = TradeItemAccessor.of(firstInput);
        TradeItemAccessor second = TradeItemAccessor.of(secondInput);
        ItemStack map = Items.FILLED_MAP.getDefaultInstance();

        if (displayName != null) {
            map.setHoverName(displayName);
        }

        TooltipNode tooltip = TooltipBuilder.array((b) -> b
                .add(utils.getValueTooltip(utils, destinationType).build(Lang.Value.MAP_DECORATION))
                .add(utils.getValueTooltip(utils, (int) mapViewScale).build(Lang.Value.ZOOM))
        ).build();

        return new ItemsToItemsNode(
                utils,
                Either.left(first.getStack()),
                first.getCount(),
                TooltipNode.empty(),
                Either.left(second.getStack()),
                second.getCount(),
                TooltipNode.empty(),
                Either.left(map),
                new RangeValue(1),
                tooltip,
                maxUses,
                villagerExperience,
                priceMultiplier,
                conditions
        );
    }
}
