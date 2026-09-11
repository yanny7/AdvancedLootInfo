package com.yanny.alicompat.compat.charm;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;
import svenhjol.charmony.helper.GenericTradeOffers;

public class CharmCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return CharmLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, GenericTradeOffers.EmeraldsForItems.class, EmeraldsForItemsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTradeOffers.EmeraldsForTag.class, EmeraldsForTagAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTradeOffers.EmeraldsForTwoTags.class, EmeraldsForTwoTagsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTradeOffers.ItemsForEmeralds.class, ItemsForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTradeOffers.ItemsForItems.class, ItemsForItemsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTradeOffers.TagForEmeralds.class, TagForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, BeekeeperTradeOffers.EmeraldsForFlowers.class, EmeraldsForFlowersAccessor.class);
        PluginUtils.registerItemListing(registry, BeekeeperTradeOffers.EnchantedShearsForEmeralds.class, EnchantedShearsForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, BeekeeperTradeOffers.PopulatedBeehiveForEmeralds.class, PopulatedBeehiveForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, BeekeeperTradeOffers.TallFlowerForEmeralds.class, TallFlowerForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, LumberjackTradeOffers.BarkForLogs.class, BarkForLogsAccessor.class);
        PluginUtils.registerItemListing(registry, LumberjackTradeOffers.SaplingsForEmeralds.class, SaplingsForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, AnvilRepairAccessor.class);
    }
}
