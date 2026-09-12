package com.yanny.alicompat.compat.hybridaquatic;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import dev.hybridlabs.aquatic.loot.entry.MessageInABottleItemEntry;
import org.jetbrains.annotations.NotNull;

public class HybridAquaticCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return HybridAquaticLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, MessageInABottleItemEntry.class, MessageInABottleItemEntryAccessor::new);
        PluginUtils.registerEntryTooltip(registry, MessageInABottleItemEntry.class, MessageInABottleItemEntryAccessor::new);
    }
}
