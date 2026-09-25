package com.yanny.alicompat.compat.grimoireofgaia;

import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.IModCompat;
import gaia.util.GaiaMerchantTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public class GrimoireOfGaiaCompat implements IModCompat {
    static final String MOD_ID = "grimoireofgaia";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registerTrades(registry, "trader", () -> GaiaMerchantTrades.MERCHANT_TRADES, (level) -> new TradeLevelInfo(new RangeValue(level == 1 ? 10 : 5)));
        registerTrades(registry, "creeper_girl", () -> GaiaMerchantTrades.CREEPER_GIRL_TRADES, (level) -> new TradeLevelInfo(new RangeValue(level == 1 ? 3 : 5)));
        registerTrades(registry, "slime_girl", () -> GaiaMerchantTrades.SLIME_GIRL_TRADES, (level) -> new TradeLevelInfo(new RangeValue(level == 1 ? 4 : 5)));
        registerTrades(registry, "ender_girl", () -> GaiaMerchantTrades.ENDER_GIRL_TRADES, (level) -> new TradeLevelInfo(new RangeValue(level == 1 ? 4 : 5)));
    }

    private static void registerTrades(IServerRegistry registry, String name, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> itemListings, IntFunction<TradeLevelInfo> levelInfo) {
        ResourceLocation traderId = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);

        registry.registerTrades(traderId, BuiltInRegistries.ENTITY_TYPE.get(traderId), itemListings, levelInfo);
    }
}
