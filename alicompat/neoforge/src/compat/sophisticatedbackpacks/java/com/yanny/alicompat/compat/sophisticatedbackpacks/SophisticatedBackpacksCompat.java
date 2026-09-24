package com.yanny.alicompat.compat.sophisticatedbackpacks;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.p3pp3rf1y.sophisticatedbackpacks.data.CopyBackpackDataFunction;
import net.p3pp3rf1y.sophisticatedbackpacks.data.BackpackLootEnabledCondition;
import net.p3pp3rf1y.sophisticatedbackpacks.data.BackpackLootModifierProvider;
import org.jetbrains.annotations.NotNull;

public class SophisticatedBackpacksCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return SophisticatedBackpacksLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(CopyBackpackDataFunction.class, SophisticatedBackpacksCompat::getCopyBackpackDataTooltip);

        registry.registerConditionTooltip(BackpackLootEnabledCondition.class, SophisticatedBackpacksCompat::getLootEnabledTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, BackpackLootModifierProvider.InjectLootModifier.class, InjectLootModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getCopyBackpackDataTooltip(IServerUtils utils, CopyBackpackDataFunction fun) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, SophisticatedBackpacksLang.Functions.COPY_BACKPACK_DATA);
    }

    @NotNull
    private static TooltipBuilder getLootEnabledTooltip(IServerUtils ignoredUtils, BackpackLootEnabledCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, SophisticatedBackpacksLang.Conditions.LOOT_ENABLED);
    }
}
