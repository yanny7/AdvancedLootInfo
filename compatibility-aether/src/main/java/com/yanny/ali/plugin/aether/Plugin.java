package com.yanny.ali.plugin.aether;

import com.aetherteam.aether.loot.conditions.ConfigEnabled;
import com.aetherteam.aether.loot.functions.DoubleDrops;
import com.aetherteam.aether.loot.functions.SpawnTNT;
import com.aetherteam.aether.loot.functions.SpawnXP;
import com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity;
import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "aether";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(SpawnTNT.class, TooltipUtils::getSpawnTntTooltip);
        registry.registerFunctionTooltip(SpawnXP.class, TooltipUtils::getSpawnXpTooltip);
        registry.registerFunctionTooltip(DoubleDrops.class, TooltipUtils::getDoubleDropsTooltip);
        registry.registerFunctionTooltip(WhirlwindSpawnEntity.class, TooltipUtils::getWhirlwindSpawnEntityTooltip);

        registry.registerConditionTooltip(ConfigEnabled.class, TooltipUtils::getConfigEnabledTooltip);
    }
}
