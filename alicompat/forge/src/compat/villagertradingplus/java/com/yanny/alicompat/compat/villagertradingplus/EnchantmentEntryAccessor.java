package com.yanny.alicompat.compat.villagertradingplus;

import com.yanny.aci.api.RangeValue;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

@ClassAccessor("com.lion.villagertradingplus.tradeoffers.trades.JsonSellEnchantedBookFromListTradeOffer$Entry")
public class EnchantmentEntryAccessor extends BaseAccessor<Object> {
    @FieldAccessor
    private Enchantment enchantment;

    @FieldAccessor
    private int minLevel;

    @FieldAccessor
    private int maxLevel;

    public EnchantmentEntryAccessor(Object parent) {
        super(parent);
    }

    @NotNull
    public static EnchantmentEntryAccessor of(Object entry) {
        return ReflectionUtils.copyClassData(EnchantmentEntryAccessor.class, entry);
    }

    @NotNull
    public Enchantment getEnchantment() {
        return enchantment;
    }

    @NotNull
    public RangeValue getLevels() {
        return new RangeValue(minLevel, maxLevel);
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
