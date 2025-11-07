package com.yanny.ali.plugin.mods.repurposed_structures;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("com.telepathicgrunt.repurposedstructures.misc.maptrades.StructureSpecificMaps$TreasureMapForEmeralds")
public class TreasureMapForEmeralds extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
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

    public TreasureMapForEmeralds(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode conditions) {
        ArrayTooltipNode tooltip = ArrayTooltipNode.array();
        ItemStack map = Items.MAP.getDefaultInstance();

        if (destinationTag != null) {
            tooltip.add(utils.getValueTooltip(utils, destinationTag).key("ali.property.value.destination"));
        } else {
            tooltip.add(utils.getValueTooltip(utils, destination).key("ali.property.value.destination"));
        }

        tooltip.add(utils.getValueTooltip(utils, destinationType).key("ali.property.value.map_decoration"));
        tooltip.add(utils.getValueTooltip(utils, spawnRegionSearchRadius).key("ali.property.value.search_radius"));
        map.setHoverName(Component.translatable(displayName));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(emeraldCost),
                EmptyTooltipNode.EMPTY,
                Either.left(Items.COMPASS.getDefaultInstance()),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(map),
                new RangeValue(),
                tooltip,
                maxUses,
                villagerXp,
                0.2F,
                EmptyTooltipNode.EMPTY
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD, Items.COMPASS), List.of(Items.MAP));
    }
}
