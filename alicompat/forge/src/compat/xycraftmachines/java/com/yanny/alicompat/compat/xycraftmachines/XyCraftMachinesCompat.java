package com.yanny.alicompat.compat.xycraftmachines;

import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import org.jetbrains.annotations.NotNull;
import tv.soaryn.xycraft.machines.data.AutoSmeltLootModifier;

public class XyCraftMachinesCompat implements IGlmModCompat {
    private static final String MOD_ID = "xycraft_machines";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AutoSmeltLootModifier.class, AutoSmeltLootModifierAccessor.class);
    }
}
