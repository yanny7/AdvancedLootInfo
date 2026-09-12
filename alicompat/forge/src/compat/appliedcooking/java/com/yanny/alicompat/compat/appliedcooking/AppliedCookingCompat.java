package com.yanny.alicompat.compat.appliedcooking;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import dev.smolinacadena.appliedcooking.lootable.KitchenStationBlockLootFunction;
import org.jetbrains.annotations.NotNull;

public class AppliedCookingCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return AppliedCookingLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, KitchenStationBlockLootFunction.class, KitchenStationBlockLootFunctionAccessor::new);
    }
}
