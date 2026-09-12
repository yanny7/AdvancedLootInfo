package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.loot.AddItemModifier;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import org.jetbrains.annotations.NotNull;

public class OccultismCompat implements IGlmModCompat {
    private static final String MOD_ID = "occultism";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddItemModifier.class, AddItemModifierAccessor.class);
    }
}
