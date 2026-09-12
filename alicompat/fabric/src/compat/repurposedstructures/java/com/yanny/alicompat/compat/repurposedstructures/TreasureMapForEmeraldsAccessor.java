package com.yanny.alicompat.compat.repurposedstructures;

import com.mojang.datafixers.util.Either;
import com.telepathicgrunt.repurposedstructures.misc.maptrades.StructureSpecificMaps;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.jetbrains.annotations.NotNull;

public class TreasureMapForEmeraldsAccessor extends BaseAccessor<StructureSpecificMaps.TreasureMapForEmeralds> implements IItemListing {
    private static final float PRICE_MULTIPLIER = 0.2F;
    private static final int ZOOM = 2;

    @FieldAccessor
    private int emeraldCost;

    @FieldAccessor
    private ResourceKey<Structure> destination;

    @FieldAccessor
    private TagKey<Structure> destinationTag;

    @FieldAccessor
    private String displayName;

    @FieldAccessor
    private MapDecoration.Type destinationType;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int villagerXp;

    @FieldAccessor
    private int spawnRegionSearchRadius;

    public TreasureMapForEmeraldsAccessor(StructureSpecificMaps.TreasureMapForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(emeraldCost),
                TooltipNode.empty(),
                Either.left(Items.COMPASS.getDefaultInstance()),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(getMapStack()),
                new RangeValue(1),
                TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, destination).build(Lang.Value.DESTINATION));
                    b.add(utils.getValueTooltip(utils, destinationTag).build(Lang.Value.DESTINATION));
                    b.add(utils.getValueTooltip(utils, destinationType).build(Lang.Value.MAP_DECORATION));
                    b.add(utils.getValueTooltip(utils, spawnRegionSearchRadius).build(Lang.Value.SEARCH_RADIUS));
                    b.add(utils.getValueTooltip(utils, true).build(Lang.Value.SKIP_KNOWN_STRUCTURES));
                    b.add(utils.getValueTooltip(utils, ZOOM).build(Lang.Value.ZOOM));
                }).build(),
                maxUses,
                villagerXp,
                PRICE_MULTIPLIER,
                conditions
        );
    }

    @NotNull
    private ItemStack getMapStack() {
        ItemStack stack = Items.FILLED_MAP.getDefaultInstance();

        if (displayName != null && !displayName.isEmpty()) {
            stack.setHoverName(Component.translatable(displayName));
        }

        return stack;
    }
}
