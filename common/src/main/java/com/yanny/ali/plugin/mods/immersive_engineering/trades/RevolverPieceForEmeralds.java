package com.yanny.ali.plugin.mods.immersive_engineering.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$RevolverPieceForEmeralds")
public class RevolverPieceForEmeralds extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final List<Item> ITEMS = List.of(getItem("gunpart_barrel"), getItem("gunpart_drum"), getItem("gunpart_hammer"));

    public RevolverPieceForEmeralds(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode condition) {
        return new SubTradesNode<>(utils, this, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, RevolverPieceForEmeralds listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Item item : ITEMS) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(Items.EMERALD.getDefaultInstance()),
                            new RangeValue(5, 64),
                            Either.left(item.getDefaultInstance()),
                            new RangeValue(1),
                            30,
                            1,
                            0.5F,
                            EmptyTooltipNode.EMPTY
                    ));
                }

                return nodes;
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), ITEMS);
    }

    private static Item getItem(String name) {
        return BuiltInRegistries.ITEM.get(new ResourceLocation("immersiveengineering", name));
    }
}
