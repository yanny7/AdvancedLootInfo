package com.yanny.alicompat.compat.charm;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.feature.beekeepers.common.Trades;

import java.util.List;

public class EnchantedShearsForEmeraldsAccessor extends BaseAccessor<Trades.EnchantedShearsForEmeralds> implements IItemListing {
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public EnchantedShearsForEmeraldsAccessor(Trades.EnchantedShearsForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TooltipNode enchantment = TooltipBuilder.branch((b) -> {
            b.add(utils.getValueTooltip(utils, Enchantments.UNBREAKING).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, NumberExpr.weighted(List.of(
                    new NumberExpr.WeightedEntry(0.45, NumberExpr.constant(1)),
                    new NumberExpr.WeightedEntry(0.45, NumberExpr.constant(2)),
                    new NumberExpr.WeightedEntry(0.1, NumberExpr.constant(3))
            ))).build(Lang.Value.LEVELS));
        }).build(Lang.Functions.SET_ENCHANTMENTS);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                NumberExpr.add(NumberExpr.weighted(List.of(
                        new NumberExpr.WeightedEntry(0.45, NumberExpr.constant(baseEmeralds)),
                        new NumberExpr.WeightedEntry(0.45, NumberExpr.constant(baseEmeralds + 4)),
                        new NumberExpr.WeightedEntry(0.1, NumberExpr.constant(baseEmeralds + 9))
                )), NumberExpr.uniformInt(0, extraEmeralds)),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(Items.SHEARS.getDefaultInstance()),
                NumberExpr.constant(1),
                enchantment,
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
