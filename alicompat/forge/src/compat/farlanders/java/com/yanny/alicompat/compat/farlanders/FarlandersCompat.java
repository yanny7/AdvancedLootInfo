package com.yanny.alicompat.compat.farlanders;

import com.legacy.farlanders.entity.util.FarlanderTrades;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.ReflectionUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FarlandersCompat implements IModCompat {
    private static final String MOD_ID = "farlanders";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemListing(FarlanderTrades.Trade.class, (utils, listing, condition) ->
                ReflectionUtils.copyClassData(TradeAccessor.class, listing, FarlanderTrades.Trade.class).getNode(utils, condition));

        registry.registerTrades(new ResourceLocation(MOD_ID, "farlander"), () -> FarlanderTrades.FARLANDER_TRADES, (level) -> new TradeLevelInfo(new RangeValue(2)));
        registry.registerTrades(new ResourceLocation(MOD_ID, "elder_farlander"), () -> FarlanderTrades.ELDER_TRADES, (level) -> new TradeLevelInfo(new RangeValue(2)));
        registry.registerTrades(new ResourceLocation(MOD_ID, "wanderer"), () -> FarlanderTrades.WANDERER_TRADES, (level) -> new TradeLevelInfo(new RangeValue(5)));
    }
}
