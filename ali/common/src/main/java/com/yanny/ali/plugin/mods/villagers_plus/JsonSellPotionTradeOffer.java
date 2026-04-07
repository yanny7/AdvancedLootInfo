package com.yanny.ali.plugin.mods.villagers_plus;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import oshi.util.tuples.Pair;

import java.util.List;

@ClassAccessor("com.lion.villagersplus.tradeoffers.trades.JsonSellPotionTradeOffer$Factory")
public class JsonSellPotionTradeOffer extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final List<Potion> POTIONS = BuiltInRegistries.POTION.stream().filter((potion) -> !potion.getEffects().isEmpty() && PotionBrewing.isBrewablePotion(potion)).toList();

    @FieldAccessor
    private ItemStack buy;
    @FieldAccessor
    private ItemStack sell;
    @FieldAccessor
    private ItemStack currency;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int experience;
    @FieldAccessor
    private float multiplier;

    public JsonSellPotionTradeOffer(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @Override
    public IDataNode getNode(IServerUtils utils, ITooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(PotionUtils.setPotion(buy, Potions.WATER)),
                new RangeValue(buy.getCount()),
                EmptyTooltipNode.EMPTY,
                Either.left(currency),
                new RangeValue(currency.getCount()),
                EmptyTooltipNode.EMPTY,
                Either.left(sell),
                new RangeValue(),
                utils.getValueTooltip(utils, POTIONS).build("ali.property.branch.effects"),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @Override
    public Pair<List<Item>, List<Item>> collectItems(IServerUtils utils) {
        return new Pair<>(List.of(buy.getItem(), currency.getItem()), List.of(sell.getItem()));
    }
}
