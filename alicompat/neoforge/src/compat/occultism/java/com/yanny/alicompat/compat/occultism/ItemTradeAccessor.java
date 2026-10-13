package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.common.entity.spirit.wonderingtrader.WonderingTrades;
import com.klikli_dev.occultism.registry.OccultismDataComponents;
import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemTradeAccessor extends BaseAccessor<WonderingTrades.ItemTrade> {
    @FieldAccessor
    private ItemStack input;
    @FieldAccessor
    private int maxUses;
    @FieldAccessor
    private int villagerXp;
    @FieldAccessor
    private ItemStack result;

    public ItemTradeAccessor(WonderingTrades.ItemTrade parent) {
        super(parent);
    }

    public IDataNode getNode(IServerUtils utils, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(withoutSpiritName(input)),
                NumberExpr.constant(input.getCount()),
                TooltipNode.empty(),
                Either.left(ItemStack.EMPTY),
                NumberExpr.constant(1),
                TooltipNode.empty(),
                Either.left(withoutSpiritName(result)),
                NumberExpr.constant(result.getCount()),
                TooltipNode.empty(),
                NumberExpr.constant(maxUses),
                NumberExpr.constant(villagerXp),
                condition
        );
    }

    @NotNull
    private static ItemStack withoutSpiritName(ItemStack stack) {
        ItemStack copy = stack.copyWithCount(1);
        copy.getMaxStackSize();

        if (copy.has(OccultismDataComponents.SPIRIT_NAME)) {
            String defaultName = copy.getPrototype().get(OccultismDataComponents.SPIRIT_NAME.get());

            copy.set(OccultismDataComponents.SPIRIT_NAME, defaultName != null ? defaultName : "");
        }

        return copy;
    }
}
