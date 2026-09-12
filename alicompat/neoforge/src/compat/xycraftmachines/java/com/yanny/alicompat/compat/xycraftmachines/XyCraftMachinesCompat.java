package com.yanny.alicompat.compat.xycraftmachines;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.server.DataComponentTooltipUtils;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;
import tv.soaryn.xycraft.machines.content.registries.MachinesDataComponents;
import tv.soaryn.xycraft.machines.datagen.AutoSmeltLootModifier;

public class XyCraftMachinesCompat implements IGlmModCompat {
    private static final String MOD_ID = "xycraft_machines";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerDestination(registry, AutoSmeltLootModifier.class, AutoSmeltLootModifierAccessor.class);

        registry.registerDataComponentTypeTooltip(MachinesDataComponents.AutoSmelt.get(), DataComponentTooltipUtils::getEmptyTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AutoSmeltLootModifier.class, AutoSmeltLootModifierAccessor.class);
    }
}
