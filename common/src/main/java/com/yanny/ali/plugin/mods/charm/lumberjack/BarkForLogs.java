package com.yanny.ali.plugin.mods.charm.lumberjack;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ClassAccessor("svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers$BarkForLogs")
public class BarkForLogs extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseCost;
    @FieldAccessor
    private int extraCost;
    @FieldAccessor
    private int maxUses;

    public BarkForLogs(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode conditions) {
        return new SubTradesNode<>(utils, this, conditions) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, BarkForLogs listing) {
                List<IDataNode> nodes = new ArrayList<>();
                Map<Block, Block> map = new HashMap<>();

                map.put(Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD);
                map.put(Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD);
                map.put(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD);
                map.put(Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD);
                map.put(Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD);
                map.put(Blocks.OAK_LOG, Blocks.OAK_WOOD);
                map.put(Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD);

                for (Map.Entry<Block, Block> e : map.entrySet()) {
                    nodes.add(new ItemsToItemsNode(
                            utils,
                            Either.left(Items.EMERALD.getDefaultInstance()),
                            new RangeValue(1),
                            Either.left(e.getKey().asItem().getDefaultInstance()),
                            new RangeValue(baseCost, baseCost + extraCost),
                            Either.left(e.getValue().asItem().getDefaultInstance()),
                            new RangeValue(baseCost, baseCost + extraCost),
                            maxUses,
                            villagerXp,
                            0.2F,
                            EmptyTooltipNode.EMPTY
                    ));
                }

                return nodes;
            }
        };
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(
                List.of(Items.EMERALD, Items.ACACIA_LOG, Items.BIRCH_LOG, Items.DARK_OAK_LOG, Items.JUNGLE_LOG, Items.MANGROVE_LOG, Items.OAK_LOG, Items.SPRUCE_LOG),
                List.of(Items.ACACIA_WOOD, Items.BIRCH_WOOD, Items.DARK_OAK_WOOD, Items.JUNGLE_WOOD, Items.MANGROVE_WOOD, Items.OAK_WOOD, Items.SPRUCE_WOOD)
        );
    }
}
