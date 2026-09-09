package com.yanny.alicompat.compat.enderio;

import com.enderio.armory.common.item.darksteel.upgrades.direct.DirectUpgradeLootCondition;
import com.enderio.base.common.enchantment.AutoSmeltModifier;
import com.enderio.base.common.loot.BrokenSpawnerLootModifier;
import com.enderio.base.common.loot.ChestLootModifier;
import com.enderio.base.common.loot.SetLootCapacitorFunction;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class EnderIoCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return EnderIoLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, SetLootCapacitorFunction.class, SetLootCapacitorFunctionAccessor.class);

        registry.registerConditionTooltip(DirectUpgradeLootCondition.class, EnderIoCompat::getDirectUpgradeTooltip);

        PluginUtils.registerDestination(registry, AutoSmeltModifier.class, AutoSmeltModifierAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AutoSmeltModifier.class, AutoSmeltModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, BrokenSpawnerLootModifier.class, BrokenSpawnerLootModifierAccessor.class);
        GlmAccessorUtils.registerGlobalLootModifier(registry, ChestLootModifier.class, ChestLootModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getDirectUpgradeTooltip(IServerUtils ignoredUtils, DirectUpgradeLootCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, EnderIoLang.Conditions.HAS_DIRECT_UPGRADE);
    }
}
