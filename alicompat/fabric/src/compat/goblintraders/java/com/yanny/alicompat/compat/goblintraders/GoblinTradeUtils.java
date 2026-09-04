package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.trades.TradeCost;
import com.mrcrayfish.goblintraders.trades.price.BasePrice;
import com.mrcrayfish.goblintraders.trades.price.ConstantPrice;
import com.mrcrayfish.goblintraders.trades.price.RangedPrice;
import com.yanny.aci.api.RangeValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

class GoblinTradeUtils {
    @NotNull
    static ItemStack getStack(TradeCost cost) {
        return new ItemCost(cost.item(), 1, cost.components()).itemStack();
    }

    @NotNull
    static RangeValue getCount(TradeCost cost) {
        BasePrice price = cost.count();

        if (price instanceof ConstantPrice(int value)) {
            return new RangeValue(value);
        } else if (price instanceof RangedPrice(int min, int max)) {
            return new RangeValue(min, max);
        } else {
            return new RangeValue(false, true);
        }
    }

    @NotNull
    static ItemStack getStack(Optional<TradeCost> cost) {
        return cost.map(GoblinTradeUtils::getStack).orElse(ItemStack.EMPTY);
    }

    @NotNull
    static RangeValue getCount(Optional<TradeCost> cost) {
        return cost.map(GoblinTradeUtils::getCount).orElse(new RangeValue(1));
    }
}
