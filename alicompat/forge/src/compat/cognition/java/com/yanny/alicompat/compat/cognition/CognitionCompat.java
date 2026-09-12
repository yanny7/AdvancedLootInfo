package com.yanny.alicompat.compat.cognition;

import com.cyanogen.experienceobelisk.loot_modifiers.AddSingleItem;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class CognitionCompat implements IGlmModCompat {
    private static final String MOD_ID = "experienceobelisk";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerDestination(registry, AddSingleItem.class, AddSingleItemAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddSingleItem.class, AddSingleItemAccessor.class);
    }
}
