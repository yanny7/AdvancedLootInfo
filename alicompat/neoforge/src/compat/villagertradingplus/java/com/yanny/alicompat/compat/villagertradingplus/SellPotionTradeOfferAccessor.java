package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.TradeFactory;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.TradeUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellPotionTradeOffer$Factory")
public class SellPotionTradeOfferAccessor extends BaseAccessor<TradeFactory> implements ITradeFactoryAccessor {
    @FieldAccessor
    private ItemStack buy;

    @FieldAccessor
    private ItemStack sell;

    @FieldAccessor
    private ItemStack currency;

    @Nullable
    @FieldAccessor
    private TagKey<Potion> potions;

    @FieldAccessor
    private int maxUses;

    @FieldAccessor
    private int experience;

    @FieldAccessor
    private float multiplier;

    public SellPotionTradeOfferAccessor(TradeFactory parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        if (potions != null) {
            return VillagerTradingPlusTrades.getTradeNode(
                    TradeUtils.getItemSlotNode(List.of(currency), NumberExpr.constant(currency.getCount()), TooltipNode.empty()),
                    buy.isEmpty() ? TradeUtils.getEmptySlotNode() : TradeUtils.getItemSlotNode(List.of(buy), NumberExpr.constant(buy.getCount()), TooltipNode.empty()),
                    TradeUtils.getItemSlotNode(getTagPotions(), NumberExpr.constant(getSellCount()), TooltipNode.empty()),
                    maxUses,
                    experience,
                    multiplier,
                    conditions
            );
        }

        return VillagerTradingPlusTrades.getTradeNode(
                utils,
                Either.left(getPotion(buy.copy(), Potions.WATER)),
                NumberExpr.constant(buy.getCount()),
                TooltipNode.empty(),
                Either.left(currency),
                NumberExpr.constant(currency.getCount()),
                TooltipNode.empty(),
                Either.left(new ItemStack(sell.getItem())),
                NumberExpr.constant(getSellCount()),
                TooltipBuilder.keyOnly(VillagerTradingPlusLang.Functions.RANDOM_POTION).build(),
                maxUses,
                experience,
                multiplier,
                conditions
        );
    }

    @NotNull
    private List<ItemStack> getTagPotions() {
        return BuiltInRegistries.POTION.get(potions)
                .map((s) -> s.stream().map((p) -> getPotion(new ItemStack(sell.getItem(), getSellCount()), p)).toList())
                .orElse(List.of());
    }

    private int getSellCount() {
        return Math.max(1, sell.getCount());
    }

    @NotNull
    private static ItemStack getPotion(ItemStack stack, Holder<Potion> potion) {
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }
}
