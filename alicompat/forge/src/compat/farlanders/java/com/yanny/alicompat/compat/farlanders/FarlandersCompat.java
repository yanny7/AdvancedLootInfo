package com.yanny.alicompat.compat.farlanders;

import com.legacy.farlanders.entity.util.FarlanderTrades;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public class FarlandersCompat implements IModCompat {
    private static final String MOD_ID = "farlanders";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, FarlanderTrades.Trade.class, TradeAccessor.class);

        registerTrades(registry, "farlander", () -> FarlanderTrades.FARLANDER_TRADES, (level) -> new TradeLevelInfo(new RangeValue(2)));
        registerTrades(registry, "elder_farlander", () -> FarlanderTrades.ELDER_TRADES, (level) -> new TradeLevelInfo(new RangeValue(2)));
        registerTrades(registry, "wanderer", () -> FarlanderTrades.WANDERER_TRADES, (level) -> new TradeLevelInfo(new RangeValue(5)));
    }

    private static void registerTrades(IServerRegistry registry, String name, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> itemListings, IntFunction<TradeLevelInfo> levelInfo) {
        ResourceLocation traderId = new ResourceLocation(MOD_ID, name);

        registry.registerTrades(traderId, BuiltInRegistries.ENTITY_TYPE.get(traderId), itemListings, levelInfo);
    }
}
