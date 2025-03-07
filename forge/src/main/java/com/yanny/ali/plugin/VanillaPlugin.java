package com.yanny.ali.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.plugin.condition.CanToolPerformActionCondition;
import net.minecraftforge.common.loot.CanToolPerformAction;

@AliEntrypoint
public class VanillaPlugin extends CommonPlugin {
    @Override
    public void registerCommon(ICommonRegistry registry) {
        super.registerCommon(registry);
        registry.registerCondition(CanToolPerformActionCondition.class, getKey(CanToolPerformAction.LOOT_CONDITION_TYPE), CanToolPerformActionCondition::new, CanToolPerformActionCondition::new);
    }
}
