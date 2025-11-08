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
import net.minecraft.world.level.storage.loot.IntRange;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers$EnchantedShearsForEmeralds")
public class EnchantedShearsForEmeralds extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;

    public EnchantedShearsForEmeralds(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode conditions) {
        ITooltipNode tooltip = utils.getValueTooltip(utils, Enchantments.UNBREAKING).key("ali.property.value.enchantment")
                .add(utils.getValueTooltip(utils, IntRange.range(1, 3)).key("ali.property.value.levels"));

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + 3 * 3 + extraEmeralds),
                EmptyTooltipNode.EMPTY,
                Either.left(ItemStack.EMPTY),
                new RangeValue(),
                EmptyTooltipNode.EMPTY,
                Either.left(Items.SHEARS.getDefaultInstance()),
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
        return new Pair<>(List.of(Items.EMERALD), List.of(Items.SHEARS));
    }
}
