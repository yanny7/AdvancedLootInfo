package com.yanny.ali.plugin.farmers_delight_refabricated;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.loot.function.CopyMealFunction;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "farmersdelight";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(CopySkilletFunction.class, Plugin::getCopySkilletTooltip);
        registry.registerFunctionTooltip(CopyMealFunction.class, Plugin::getCopyMealTooltip);
    }

    @NotNull
    private static ITooltipNode getCopySkilletTooltip(IServerUtils ignoredUtils, CopySkilletFunction ignoredFunction) {
        return new TooltipNode(GenericTooltipUtils.translatable("ali.type.function.copy_skillet"));
    }

    @NotNull
    private static ITooltipNode getCopyMealTooltip(IServerUtils ignoredUtils, CopyMealFunction ignoredFunction) {
        return new TooltipNode(GenericTooltipUtils.translatable("ali.type.function.copy_meal"));
    }
}
