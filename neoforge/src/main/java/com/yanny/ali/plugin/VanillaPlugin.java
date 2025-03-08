package com.yanny.ali.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.ICommonRegistry;
import com.yanny.ali.plugin.condition.CanToolPerformActionCondition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.loot.CanToolPerformAction;

@AliEntrypoint
public class VanillaPlugin extends CommonPlugin {
    public static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @Override
    public void registerCommon(ICommonRegistry registry) {
        super.registerCommon(registry);
        registry.registerCondition(CanToolPerformActionCondition.class, getKey(CanToolPerformAction.LOOT_CONDITION_TYPE), CanToolPerformActionCondition::new, CanToolPerformActionCondition::new);
    }
}
