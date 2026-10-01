package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.trades.TradeCost;
import com.mrcrayfish.goblintraders.trades.price.BasePrice;
import com.mrcrayfish.goblintraders.trades.price.ConstantPrice;
import com.mrcrayfish.goblintraders.trades.price.RangedPrice;
import com.yanny.aci.api.NumberExpr;
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
    static NumberExpr getCount(TradeCost cost) {
        BasePrice price = cost.count();

        if (price instanceof ConstantPrice constantPrice) {
            return NumberExpr.constant(constantPrice.value());
        } else if (price instanceof RangedPrice rangedPrice) {
            return NumberExpr.uniformInt(rangedPrice.min(), rangedPrice.max());
        } else {
            return NumberExpr.opaque(price.getClass().getName());
        }
    }

    @NotNull
    static ItemStack getStack(Optional<TradeCost> cost) {
        return cost.map(GoblinTradeUtils::getStack).orElse(ItemStack.EMPTY);
    }

    @NotNull
    static NumberExpr getCount(Optional<TradeCost> cost) {
        return cost.map(GoblinTradeUtils::getCount).orElse(NumberExpr.constant(1));
    }
}
