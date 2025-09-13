package com.yanny.ali.plugin.mods.villagers_plus;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IItemListing;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import oshi.util.tuples.Pair;

import java.util.Collections;
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
    public IDataNode getNode(IServerUtils utils, List<ITooltipNode> conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(PotionUtils.setPotion(buy, Potions.WATER)),
                new RangeValue(buy.getCount()),
                Collections.emptyList(),
                Either.left(currency),
                new RangeValue(currency.getCount()),
                Collections.emptyList(),
                Either.left(sell),
                new RangeValue(),
                List.of(GenericTooltipUtils.getCollectionTooltip(utils, "ali.property.branch.effects", "ali.property.value.null", POTIONS, RegistriesTooltipUtils::getPotionTooltip)),
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
