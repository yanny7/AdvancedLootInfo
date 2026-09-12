package com.yanny.alicompat.compat.arsnouveau;

import com.hollingsworth.arsnouveau.api.loot.DungeonLootEnhancerModifier;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import org.jetbrains.annotations.NotNull;

public class ArsNouveauCompat implements IGlmModCompat {
    private static final String MOD_ID = "ars_nouveau";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, DungeonLootEnhancerModifier.class, DungeonLootEnhancerModifierAccessor.class);
    }
}
