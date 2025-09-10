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

@ClassAccessor("svenhjol.charmony.helper.GenericTradeOffers$TagForEmeralds")
public class TagForEmeralds<T extends ItemLike> implements IItemListing {
    @FieldAccessor("villagerXp")
    private int villagerXp;
    @FieldAccessor("baseItems")
    private int baseItems;
    @FieldAccessor("extraItems")
    private int extraItems;
    @FieldAccessor("baseEmeralds")
    private int baseEmeralds;
    @FieldAccessor("extraEmeralds")
    private int extraEmeralds;
    @FieldAccessor("maxUses")
    private int maxUses;
    @FieldAccessor("tag")
    private TagKey<T> tag;

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                Either.right(tag),
                new RangeValue(baseItems, baseItems + extraItems),
                maxUses,
                villagerXp,
                0.05F,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), PluginUtils.getItems(utils, tag));
    }
}
