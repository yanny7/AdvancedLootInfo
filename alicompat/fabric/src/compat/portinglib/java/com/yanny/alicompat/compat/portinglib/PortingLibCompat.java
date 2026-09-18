package com.yanny.alicompat.compat.portinglib;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import io.github.fabricators_of_create.porting_lib.tool.loot.CanItemPerformAbility;
import org.jetbrains.annotations.NotNull;

public class PortingLibCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return "porting_lib_item_abilities";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerConditionTooltip(registry, CanItemPerformAbility.class, CanItemPerformAbilityAccessor.class);
    }
}
