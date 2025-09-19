package com.yanny.ali.plugin.mods.charm;

import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.ali.plugin.mods.charm.beekeeper.EmeraldsForFlowers;
import com.yanny.ali.plugin.mods.charm.beekeeper.EnchantedShearsForEmeralds;
import com.yanny.ali.plugin.mods.charm.beekeeper.PopulatedBeehiveForEmeralds;
import com.yanny.ali.plugin.mods.charm.beekeeper.TallFlowerForEmeralds;
import com.yanny.ali.plugin.mods.charm.extra.AnvilRepair;
import com.yanny.ali.plugin.mods.charm.generic.*;
import com.yanny.ali.plugin.mods.charm.lumberjack.BarkForLogs;
import com.yanny.ali.plugin.mods.charm.lumberjack.SaplingsForEmeralds;

//@AliEntrypoint
public class Plugin implements IPlugin {

    @Override
    public String getModId() {
        return "charm";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, EmeraldsForFlowers.class);
        PluginUtils.registerItemListing(registry, TallFlowerForEmeralds.class);
        PluginUtils.registerItemListing(registry, EnchantedShearsForEmeralds.class);
        PluginUtils.registerItemListing(registry, PopulatedBeehiveForEmeralds.class);
        PluginUtils.registerItemListing(registry, SaplingsForEmeralds.class);
        PluginUtils.registerItemListing(registry, BarkForLogs.class);
        PluginUtils.registerItemListing(registry, EmeraldsForTag.class);
        PluginUtils.registerItemListing(registry, EmeraldsForTwoTags.class);
        PluginUtils.registerItemListing(registry, EmeraldsForItems.class);
        PluginUtils.registerItemListing(registry, TagForEmeralds.class);
        PluginUtils.registerItemListing(registry, ItemsForEmeralds.class);
        PluginUtils.registerItemListing(registry, ItemsForItems.class);
        PluginUtils.registerItemListing(registry, AnvilRepair.class);

        PluginUtils.registerItemListingCollector(registry, EmeraldsForFlowers.class);
        PluginUtils.registerItemListingCollector(registry, TallFlowerForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, EnchantedShearsForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, PopulatedBeehiveForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, SaplingsForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, BarkForLogs.class);
        PluginUtils.registerItemListingCollector(registry, EmeraldsForTag.class);
        PluginUtils.registerItemListingCollector(registry, EmeraldsForTwoTags.class);
        PluginUtils.registerItemListingCollector(registry, EmeraldsForItems.class);
        PluginUtils.registerItemListingCollector(registry, TagForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, ItemsForEmeralds.class);
        PluginUtils.registerItemListingCollector(registry, ItemsForItems.class);
        PluginUtils.registerItemListingCollector(registry, AnvilRepair.class);
    }
}
