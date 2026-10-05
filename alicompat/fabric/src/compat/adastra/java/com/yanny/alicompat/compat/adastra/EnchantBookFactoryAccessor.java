package com.yanny.alicompat.compat.adastra;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ClassAccessor("earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffer$EnchantBookFactory")
public class EnchantBookFactoryAccessor extends BaseAccessor<VillagerTrades.ItemListing> implements IItemListing {
    private static final int MAX_COST = 64;
    private static final int MAX_USES = 12;
    private static final float MULTIPLIER = 0.2F;

    @FieldAccessor
    private int experience;

    public EnchantBookFactoryAccessor(VillagerTrades.ItemListing parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                getCost(),
                TooltipNode.empty(),
                Either.left(Items.BOOK.getDefaultInstance()),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(Items.ENCHANTED_BOOK.getDefaultInstance()),
                NumberExpr.constant(1),
                TooltipBuilder.keyOnly(Lang.Functions.ENCHANT_RANDOMLY).build(),
                MAX_USES,
                experience,
                MULTIPLIER,
                conditions
        );
    }

    @NotNull
    private static NumberExpr getCost() {
        List<Enchantment> enchantments = BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isTradeable).toList();
        Map<List<Integer>, Double> weights = new LinkedHashMap<>();

        for (Enchantment enchantment : enchantments) {
            int levels = enchantment.getMaxLevel() - enchantment.getMinLevel() + 1;

            for (int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++) {
                weights.merge(List.of(enchantment.isTreasureOnly() ? 2 : 1, level), 1.0 / enchantments.size() / levels, Double::sum);
            }
        }

        if (weights.isEmpty()) {
            return NumberExpr.constant(MAX_COST);
        }

        NumberExpr price = NumberExpr.weighted(weights.entrySet().stream()
                .map((e) -> new NumberExpr.WeightedEntry(e.getValue(), NumberExpr.mul(
                        NumberExpr.constant(e.getKey().get(0)),
                        NumberExpr.add(NumberExpr.constant(2 + 3 * e.getKey().get(1)), NumberExpr.uniformInt(0, 4 + 10 * e.getKey().get(1)))
                )))
                .toList());

        return NumberExpr.min(price, NumberExpr.constant(MAX_COST));
    }
}
