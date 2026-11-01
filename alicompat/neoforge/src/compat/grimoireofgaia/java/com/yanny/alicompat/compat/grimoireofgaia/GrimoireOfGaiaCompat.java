package com.yanny.alicompat.compat.grimoireofgaia;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.PluginUtils;
import gaia.util.GaiaMerchantTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public class GrimoireOfGaiaCompat implements IModCompat {
    static final String MOD_ID = "grimoireofgaia";
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final String TRADES_CLASS = "gaia.util.GaiaMerchantTrades";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registerTrades(registry, "trader", () -> GaiaMerchantTrades.MERCHANT_TRADES, (level) -> new TradeLevelInfo(NumberExpr.constant(level == 1 ? 10 : 5)));
        registerTrades(registry, "creeper_girl", () -> GaiaMerchantTrades.CREEPER_GIRL_TRADES, (level) -> new TradeLevelInfo(NumberExpr.constant(level == 1 ? 3 : 5)));
        registerTrades(registry, "slime_girl", () -> GaiaMerchantTrades.SLIME_GIRL_TRADES, (level) -> new TradeLevelInfo(NumberExpr.constant(level == 1 ? 4 : 5)));
        registerTrades(registry, "ender_girl", () -> GaiaMerchantTrades.ENDER_GIRL_TRADES, (level) -> new TradeLevelInfo(NumberExpr.constant(level == 1 ? 4 : 5)));

        PluginUtils.registerItemListing(registry, ItemsToItemsAccessor.class);
        registerItemsToItems(registry, "ItemForMerchantToken");
        registerItemsToItems(registry, "MerchantTokenForItem");
    }

    private static void registerTrades(IServerRegistry registry, String name, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> itemListings, IntFunction<TradeLevelInfo> levelInfo) {
        ResourceLocation traderId = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);

        registry.registerTrades(traderId, BuiltInRegistries.ENTITY_TYPE.get(traderId), itemListings, levelInfo);
    }

    private static void registerItemsToItems(IServerRegistry registry, String name) {
        try {
            //noinspection unchecked
            Class<VillagerTrades.ItemListing> type = (Class<VillagerTrades.ItemListing>) Class.forName(TRADES_CLASS + "$" + name);

            PluginUtils.registerItemListing(registry, type, ItemsToItemsAccessor.class);
        } catch (Throwable e) {
            LOGGER.warn("Failed to register item listing for {}${}: {}", TRADES_CLASS, name, e.getMessage());
        }
    }
}
