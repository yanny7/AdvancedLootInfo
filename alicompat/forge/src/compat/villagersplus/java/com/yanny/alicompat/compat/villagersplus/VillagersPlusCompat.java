package com.yanny.alicompat.compat.villagersplus;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class VillagersPlusCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return VillagersPlusLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, SellPotionTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellEnchantedBookTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellEnchantedToolTradeOfferAccessor.class);
        PluginUtils.registerItemListing(registry, SellStructureMapTradeOfferAccessor.class);
    }
}
