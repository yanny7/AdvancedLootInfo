package com.yanny.ali.plugin.mods.repurposed_structures;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

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
    private Holder.Reference<MapDecorationType> destinationType;
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
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        List<ITooltipNode> tooltip = new ArrayList<>();
        ItemStack map = Items.MAP.getDefaultInstance();

        if (destinationTag != null) {
            tooltip.add(getTagKeyTooltip(utils, "ali.property.value.destination", destinationTag));
        } else {
            tooltip.add(getResourceKeyTooltip(utils, "ali.property.value.destination", destination));
        }

        tooltip.add(RegistriesTooltipUtils.getMapDecorationTypeTooltip(utils, "ali.property.value.map_decoration", destinationType.value()));
        tooltip.add(getIntegerTooltip(utils, "ali.property.value.search_radius", spawnRegionSearchRadius));
        map.set(DataComponents.ITEM_NAME, Component.translatable(displayName));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(emeraldCost),
                Collections.emptyList(),
                Either.left(Items.COMPASS.getDefaultInstance()),
                new RangeValue(),
                Collections.emptyList(),
                Either.left(map),
                new RangeValue(),
                tooltip,
                maxUses,
                villagerXp,
                0.2F,
                Collections.emptyList()
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD, Items.COMPASS), List.of(Items.MAP));
    }
}
