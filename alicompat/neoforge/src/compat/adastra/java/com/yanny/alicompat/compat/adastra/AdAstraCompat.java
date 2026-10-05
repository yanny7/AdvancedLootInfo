package com.yanny.alicompat.compat.adastra;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import earth.terrarium.adastra.common.entities.mob.lunarians.LunarianMerchantOffers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class AdAstraCompat implements IModCompat {
    static final String MOD_ID = "ad_astra";
    private static final int VILLAGER_TRADE_COUNT = 2;
    private static final int WANDERING_TRADE_COUNT = 5;
    private static final int WANDERING_RARE_TRADE_COUNT = 1;
    private static final int WANDERING_RARE_LEVEL = 2;

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, BuyForOneEmeraldFactoryAccessor.class);
        PluginUtils.registerItemListing(registry, EnchantBookFactoryAccessor.class);
        PluginUtils.registerItemListing(registry, ProcessItemFactoryAccessor.class);
        PluginUtils.registerItemListing(registry, SellDyedArmorFactoryAccessor.class);
        PluginUtils.registerItemListing(registry, SellEnchantedToolFactoryAccessor.class);
        PluginUtils.registerItemListing(registry, SellItemFactoryAccessor.class);
        PluginUtils.registerItemListing(registry, SellPotionHoldingItemFactoryAccessor.class);
        PluginUtils.registerItemListing(registry, SellSuspiciousStewFactoryAccessor.class);

        EntityType<?> lunarian = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(MOD_ID, "lunarian"));
        ResourceLocation wanderingTraderId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lunarian_wandering_trader");

        LunarianMerchantOffers.PROFESSION_TO_LEVELED_TRADE.forEach((profession, listings) -> {
            ResourceLocation traderId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lunarian." + BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).getPath());

            registry.registerTrades(traderId, lunarian, () -> listings, (level) -> new TradeLevelInfo(NumberExpr.constant(VILLAGER_TRADE_COUNT)));
        });

        registry.registerTrades(wanderingTraderId, BuiltInRegistries.ENTITY_TYPE.get(wanderingTraderId), () -> LunarianMerchantOffers.WANDERING_TRADER_TRADES,
                (level) -> new TradeLevelInfo(NumberExpr.constant(level == WANDERING_RARE_LEVEL ? WANDERING_RARE_TRADE_COUNT : WANDERING_TRADE_COUNT)));
    }
}
