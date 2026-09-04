package com.yanny.alicompat.compat.goblintraders;

import com.mojang.datafixers.util.Either;
import com.mrcrayfish.goblintraders.trades.TradeCost;
import com.mrcrayfish.goblintraders.trades.type.TreasureMapTrade;
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
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.Optional;

public class TreasureMapTradeAccessor extends BaseAccessor<TreasureMapTrade> implements IItemListing {
    @FieldAccessor
    private TagKey<Structure> structure;
    @FieldAccessor
    private Holder<MapDecorationType> mapDecoration;
    @FieldAccessor
    private Component name;
    @FieldAccessor
    private TradeCost primaryPayment;
    @FieldAccessor
    private Optional<TradeCost> secondaryPayment;
    @FieldAccessor
    private float priceMultiplier;
    @FieldAccessor
    private int maxTrades;
    @FieldAccessor
    private int experience;

    public TreasureMapTradeAccessor(TreasureMapTrade parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        ItemStack map = Items.MAP.getDefaultInstance();

        map.set(DataComponents.ITEM_NAME, name);

        return new ItemsToItemsNode(
                utils,
                Either.left(GoblinTradeUtils.getStack(primaryPayment)),
                GoblinTradeUtils.getCount(primaryPayment),
                TooltipNode.empty(),
                Either.left(GoblinTradeUtils.getStack(secondaryPayment)),
                GoblinTradeUtils.getCount(secondaryPayment),
                TooltipNode.empty(),
                Either.left(map),
                new RangeValue(1),
                TooltipBuilder.array((b) -> b
                                .add(utils.getValueTooltip(utils, structure).build(Lang.Value.DESTINATION))
                                .add(utils.getValueTooltip(utils, mapDecoration).build(Lang.Value.MAP_DECORATION))
                        )
                        .build(),
                maxTrades,
                experience,
                priceMultiplier,
                conditions
        );
    }
}
