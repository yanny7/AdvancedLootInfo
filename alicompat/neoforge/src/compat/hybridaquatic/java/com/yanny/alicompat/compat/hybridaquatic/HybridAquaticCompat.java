package com.yanny.alicompat.compat.hybridaquatic;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import dev.hybridlabs.aquatic.loot.HAGlobalLootModifier;
import dev.hybridlabs.aquatic.loot.entry.MessageInABottleItemEntry;
import org.jetbrains.annotations.NotNull;

public class HybridAquaticCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return HybridAquaticLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, MessageInABottleItemEntry.class, MessageInABottleItemEntryAccessor::new);
        PluginUtils.registerEntryTooltip(registry, MessageInABottleItemEntry.class, MessageInABottleItemEntryAccessor::new);

        PluginUtils.registerDestination(registry, HAGlobalLootModifier.class, HAGlobalLootModifierAccessor.class);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, HAGlobalLootModifier.class, HAGlobalLootModifierAccessor.class);
    }
}
