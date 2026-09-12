package com.yanny.alicompat.compat.charm;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
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
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;

public class EnchantedShearsForEmeraldsAccessor extends BaseAccessor<BeekeeperTradeOffers.EnchantedShearsForEmeralds> implements IItemListing {
    @FieldAccessor
    private int baseEmeralds;
    @FieldAccessor
    private int extraEmeralds;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;

    public EnchantedShearsForEmeraldsAccessor(BeekeeperTradeOffers.EnchantedShearsForEmeralds parent) {
        super(parent);
    }

    @NotNull
    @Override
    public IDataNode getNode(IServerUtils utils, TooltipNode conditions) {
        TooltipNode enchantment = TooltipBuilder.branch((b) -> {
            b.add(utils.getValueTooltip(utils, Enchantments.UNBREAKING).build(Lang.Value.ENCHANTMENT));
            b.add(utils.getValueTooltip(utils, new RangeValue(1, 3)).build(Lang.Value.LEVELS));
        }).build(Lang.Functions.SET_ENCHANTMENTS);

        return new ItemsToItemsNode(
                utils,
                Either.left(Items.EMERALD.getDefaultInstance()),
                new RangeValue(baseEmeralds, baseEmeralds + 9 + extraEmeralds),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                new RangeValue(1),
                TooltipNode.empty(),
                Either.left(Items.SHEARS.getDefaultInstance()),
                new RangeValue(1),
                enchantment,
                maxUses,
                villagerXp,
                0.2F,
                conditions
        );
    }
}
