package com.yanny.alicompat.compat.villagersplus;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagersplus.tradeoffers.trades.JsonSellStructureMapTradeOffer$Factory")
public class SellStructureMapTradeOfferAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final int SEARCH_RADIUS = 100;
    private static final int ZOOM = 2;

    @FieldAccessor
    private ItemStack currency;

    @FieldAccessor
    private ItemStack buy;

    @FieldAccessor
    private String nameKey;

    @FieldAccessor
    private TagKey<Structure> structure;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellStructureMapTradeOfferAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(currency),
                new RangeValue(currency.getCount()),
                TooltipNode.empty(),
                Either.left(buy),
                new RangeValue(Math.max(1, buy.getCount())),
                TooltipNode.empty(),
                Either.left(getMapStack()),
                new RangeValue(1),
                TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, structure).build(Lang.Value.DESTINATION));
                    b.add(utils.getValueTooltip(utils, MapDecoration.Type.RED_X).build(Lang.Value.MAP_DECORATION));
                    b.add(utils.getValueTooltip(utils, SEARCH_RADIUS).build(Lang.Value.SEARCH_RADIUS));
                    b.add(utils.getValueTooltip(utils, true).build(Lang.Value.SKIP_KNOWN_STRUCTURES));
                    b.add(utils.getValueTooltip(utils, ZOOM).build(Lang.Value.ZOOM));
                }).build(),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @NotNull
    private ItemStack getMapStack() {
        ItemStack stack = Items.FILLED_MAP.getDefaultInstance();

        if (!nameKey.isEmpty()) {
            stack.setHoverName(Component.translatable(nameKey));
        }

        return stack;
    }
}
