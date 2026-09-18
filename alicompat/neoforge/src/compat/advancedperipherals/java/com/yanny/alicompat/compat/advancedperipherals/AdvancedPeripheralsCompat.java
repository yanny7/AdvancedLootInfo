package com.yanny.alicompat.compat.advancedperipherals;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import de.srendi.advancedperipherals.common.village.VillagerTrade;
import org.jetbrains.annotations.NotNull;

public class AdvancedPeripheralsCompat implements IModCompat {
    private static final String MOD_ID = "advancedperipherals";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, VillagerTrade.class, VillagerTradeAccessor.class);
    }
}
