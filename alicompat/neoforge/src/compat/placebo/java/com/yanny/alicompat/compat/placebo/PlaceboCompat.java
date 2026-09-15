package com.yanny.alicompat.compat.placebo;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import dev.shadowsoffire.placebo.loot.StackLootEntry;
import dev.shadowsoffire.placebo.systems.wanderer.BasicWandererTrade;
import org.jetbrains.annotations.NotNull;

public class PlaceboCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return PlaceboLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, StackLootEntry.class, StackLootEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, StackLootEntry.class, StackLootEntryAccessor.class);

        PluginUtils.registerItemListing(registry, BasicWandererTrade.class, BasicWandererTradeAccessor.class);
    }
}
