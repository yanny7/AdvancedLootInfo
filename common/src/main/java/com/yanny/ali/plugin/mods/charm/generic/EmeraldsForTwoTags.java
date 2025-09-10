package com.yanny.ali.plugin.mods.charm.generic;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import com.yanny.ali.plugin.mods.PluginUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.stream.Stream;

@ClassAccessor("svenhjol.charmony.helper.GenericTradeOffers$EmeraldsForTwoTags")
public class EmeraldsForTwoTags<T extends ItemLike, U extends ItemLike> implements IItemListing {
    @FieldAccessor("villagerXp")
    private int villagerXp;
    @FieldAccessor("baseCost")
    private int baseCost;
    @FieldAccessor("extraCost")
    private int extraCost;
    @FieldAccessor("baseEmeralds")
    private int baseEmeralds;
    @FieldAccessor("extraEmeralds")
    private int extraEmeralds;
    @FieldAccessor("maxUses")
    private int maxUses;
    @FieldAccessor("tag1")
    private TagKey<T> tag1;
    @FieldAccessor("tag2")
    private TagKey<U> tag2;

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.right(tag1),
                new RangeValue(baseCost, baseCost + extraCost),
                Either.right(tag2),
                new RangeValue(baseCost, baseCost + extraCost),
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                maxUses,
                villagerXp,
                0.05F,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(
                Stream.concat(
                        PluginUtils.getItems(utils, tag1).stream(),
                        PluginUtils.getItems(utils, tag2).stream()
                ).toList(),
                List.of(Items.EMERALD)
        );
    }
}
