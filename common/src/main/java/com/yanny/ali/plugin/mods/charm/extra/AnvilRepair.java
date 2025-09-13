package com.yanny.ali.plugin.mods.charm.extra;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ClassAccessor("svenhjol.charm.feature.extra_trades.ExtraTrades$AnvilRepair")
public class AnvilRepair extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int maxUses;

    public AnvilRepair(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new SubTradesNode<>(utils, this, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, AnvilRepair listing) {
                List<Item> items = List.of(Items.DAMAGED_ANVIL, Items.CHIPPED_ANVIL);
                List<IDataNode> nodes = new ArrayList<>();

                for (Item item : items) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(item.getDefaultInstance()),
                            new RangeValue(),
                            Either.left(Items.IRON_INGOT.getDefaultInstance()),
                            new RangeValue(5, 9),
                            Either.left(Items.ANVIL.getDefaultInstance()),
                            new RangeValue(),
                            maxUses,
                            villagerXp,
                            0.2F,
                            Collections.emptyList()
                    ));
                }

                return nodes;
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.DAMAGED_ANVIL, Items.CHIPPED_ANVIL, Items.IRON_INGOT), List.of(Items.ANVIL));
    }
}
