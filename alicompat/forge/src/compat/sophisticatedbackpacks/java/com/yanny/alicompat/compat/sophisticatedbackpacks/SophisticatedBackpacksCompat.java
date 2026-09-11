package com.yanny.alicompat.compat.sophisticatedbackpacks;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import net.p3pp3rf1y.sophisticatedbackpacks.data.CopyBackpackDataFunction;
import net.p3pp3rf1y.sophisticatedbackpacks.data.SBLootEnabledCondition;
import net.p3pp3rf1y.sophisticatedbackpacks.data.SBLootModifierProvider;
import org.jetbrains.annotations.NotNull;

public class SophisticatedBackpacksCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return SophisticatedBackpacksLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CopyBackpackDataFunction.class, CopyBackpackDataFunctionAccessor::new);

        registry.registerConditionTooltip(SBLootEnabledCondition.class, SophisticatedBackpacksCompat::getLootEnabledTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, SBLootModifierProvider.InjectLootModifier.class, InjectLootModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getLootEnabledTooltip(IServerUtils ignoredUtils, SBLootEnabledCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, SophisticatedBackpacksLang.Conditions.LOOT_ENABLED);
    }
}
