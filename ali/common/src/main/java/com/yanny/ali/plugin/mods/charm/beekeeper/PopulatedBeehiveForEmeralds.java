package com.yanny.ali.plugin.mods.charm.beekeeper;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers$PopulatedBeehiveForEmeralds")
public class PopulatedBeehiveForEmeralds extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;

    public PopulatedBeehiveForEmeralds(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode conditions) {
        ITooltipNode tooltip = utils.getValueTooltip(utils, Enchantments.UNBREAKING)
                .add(utils.getValueTooltip(utils, 2).build("ali.property.value.count"))
                .build("ali.property.branch.bees");

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + extraEmeralds),
                EmptyTooltipNode.EMPTY,
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(Items.BEEHIVE.getDefaultInstance()),
                new RangeValue(),
                tooltip,
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(Items.EMERALD), List.of(Items.BEEHIVE));
    }
}
