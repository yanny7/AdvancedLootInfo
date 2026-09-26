package com.yanny.alicompat.compat.relics;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import it.hurts.sskirillss.relics.level.RelicLootModifier;
import org.jetbrains.annotations.NotNull;

public class RelicsCompat implements IGlmModCompat {
    private static final String MOD_ID = "relics";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerPageResolver(registry, RelicLootModifier.class, RelicLootModifierAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, RelicLootModifier.class, RelicLootModifierAccessor.class);
    }
}
