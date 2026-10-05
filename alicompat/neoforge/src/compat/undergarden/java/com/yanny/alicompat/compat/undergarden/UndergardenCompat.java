package com.yanny.alicompat.compat.undergarden;

import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import quek.undergarden.entity.monster.stoneborn.trading.BuyForRegaliumTrade;
import quek.undergarden.entity.monster.stoneborn.trading.SellForRegaliumTrade;
import quek.undergarden.entity.monster.stoneborn.trading.StonebornTrades;

public class UndergardenCompat implements IModCompat {
    static final String MOD_ID = "undergarden";
    private static final int STONEBORN_TRADE_COUNT = 4;

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, BuyForRegaliumTrade.class, BuyForRegaliumTradeAccessor.class);
        PluginUtils.registerItemListing(registry, SellForRegaliumTrade.class, SellForRegaliumTradeAccessor.class);

        ResourceLocation stonebornId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "stoneborn");

        registry.registerTrades(stonebornId, BuiltInRegistries.ENTITY_TYPE.get(stonebornId), () -> StonebornTrades.VAGABOND_TRADES, (level) -> new TradeLevelInfo(NumberExpr.constant(STONEBORN_TRADE_COUNT)));
    }
}
