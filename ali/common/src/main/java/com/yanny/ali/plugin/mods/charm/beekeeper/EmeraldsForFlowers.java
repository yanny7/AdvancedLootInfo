package com.yanny.ali.plugin.mods.charm.beekeeper;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("svenhjol.charm.feature.beekeepers.common.Trades$EmeraldsForFlowers")
public class EmeraldsForFlowers extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final TagKey<Item> BEEKEEPER_SELLS_FLOWERS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("charm", "beekeeper_sells_flowers"));

    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseCost;
    @FieldAccessor
    private int extraCost;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;

    public EmeraldsForFlowers(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.right(BEEKEEPER_SELLS_FLOWERS),
                new RangeValue(baseCost, baseCost + extraCost),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(PluginUtils.getItems(utils, BEEKEEPER_SELLS_FLOWERS), List.of(Items.EMERALD));
    }
}
