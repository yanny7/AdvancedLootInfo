package com.yanny.alicompat.compat.morejs;

import com.almostreliable.morejs.features.villager.IntRange;
import com.almostreliable.morejs.features.villager.TradeItem;
import com.yanny.aci.api.RangeValue;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradeItemAccessor extends BaseAccessor<TradeItem> {
    @FieldAccessor
    private ItemStack itemStack;

    @Nullable
    @FieldAccessor
    private IntRange countRange;

    public TradeItemAccessor(TradeItem parent) {
        super(parent);
    }

    @NotNull
    public static TradeItemAccessor of(TradeItem tradeItem) {
        return ReflectionUtils.copyClassData(TradeItemAccessor.class, tradeItem, TradeItem.class);
    }

    @NotNull
    public ItemStack getStack() {
        return itemStack;
    }

    @NotNull
    public RangeValue getCount() {
        if (countRange == null) {
            return new RangeValue(itemStack.getCount());
        }

        return new RangeValue(countRange.getMin(), countRange.getMax());
    }
}
