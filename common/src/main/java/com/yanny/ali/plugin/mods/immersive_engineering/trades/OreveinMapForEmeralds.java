package com.yanny.ali.plugin.mods.immersive_engineering.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$OreveinMapForEmeralds")
public class OreveinMapForEmeralds extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    public OreveinMapForEmeralds(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        ItemStack map = Items.MAP.getDefaultInstance();

        map.setHoverName(Component.translatable("item.immersiveengineering.map_orevein"));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(8, 16),
                Either.left(map),
                new RangeValue(1),
                30,
                1,
                0.5F,
                condition
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), List.of(Items.MAP));
    }
}
