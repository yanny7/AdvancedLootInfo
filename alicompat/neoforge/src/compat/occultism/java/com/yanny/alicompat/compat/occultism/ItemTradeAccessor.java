package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.common.entity.spirit.wonderingtrader.WonderingTrades;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
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

    @FieldAccessor
    private float priceMultiplier;

    public ItemTradeAccessor(WonderingTrades.ItemTrade parent) {
        super(parent);
    }

    @NotNull
    public VillagerTrade getTrade() {
        return VillagerTrade.builder(
                new TradeCost(input.getItem(), input.getCount()),
                ItemStackTemplate.fromNonEmptyStack(result),
                maxUses,
                villagerXp,
                priceMultiplier
        ).build();
    }
}
