package com.yanny.alicompat.compat.dimdungeons;

import com.catastrophe573.dimdungeons.utils.LootModifierNoDrops;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import org.jetbrains.annotations.NotNull;

public class DimDungeonsCompat implements IGlmModCompat {
    private static final String MOD_ID = "dimdungeons";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, LootModifierNoDrops.class, LootModifierNoDropsAccessor.class);
    }
}
