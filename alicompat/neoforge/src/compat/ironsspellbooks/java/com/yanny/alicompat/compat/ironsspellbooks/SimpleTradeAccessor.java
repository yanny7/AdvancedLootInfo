package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.ReflectionUtils;
import io.redspace.ironsspellbooks.player.AdditionalWanderingTrades;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class SimpleTradeAccessor extends BaseAccessor<AdditionalWanderingTrades.SimpleTrade> {
    @FieldAccessor
    private BiFunction<Entity, RandomSource, MerchantOffer> getOffer;

    public SimpleTradeAccessor(AdditionalWanderingTrades.SimpleTrade parent) {
        super(parent);
    }

    @NotNull
    public static SimpleTradeAccessor of(AdditionalWanderingTrades.SimpleTrade listing) {
        return ReflectionUtils.copyClassData(SimpleTradeAccessor.class, listing, AdditionalWanderingTrades.SimpleTrade.class);
    }

    @Nullable
    public <T> T getCaptured(Class<T> type) {
        List<T> captured = com.yanny.ali.plugin.common.ReflectionUtils.getCapturedInstances(getOffer, type);

        return captured.isEmpty() ? null : captured.get(0);
    }
}
