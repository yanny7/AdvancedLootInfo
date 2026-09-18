package com.yanny.alicompat.compat.charm;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;
import svenhjol.charm.charmony.common.villages.GenericTrades;
import svenhjol.charm.feature.beekeepers.common.Trades;
import svenhjol.charm.feature.trade_improvements.common.Registers;

public class CharmCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return CharmLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, GenericTrades.EmeraldsForItems.class, EmeraldsForItemsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTrades.EmeraldsForTag.class, EmeraldsForTagAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTrades.EmeraldsForTwoTags.class, EmeraldsForTwoTagsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTrades.ItemsForEmeralds.class, ItemsForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTrades.ItemsForItems.class, ItemsForItemsAccessor.class);
        PluginUtils.registerItemListing(registry, GenericTrades.TagForEmeralds.class, TagForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, Trades.EmeraldsForFlowers.class, EmeraldsForFlowersAccessor.class);
        PluginUtils.registerItemListing(registry, Trades.EnchantedShearsForEmeralds.class, EnchantedShearsForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, Trades.PopulatedBeehiveForEmeralds.class, PopulatedBeehiveForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, Trades.TallFlowerForEmeralds.class, TallFlowerForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, svenhjol.charm.feature.lumberjacks.common.Trades.BarkForLogs.class, BarkForLogsAccessor.class);
        PluginUtils.registerItemListing(registry, svenhjol.charm.feature.lumberjacks.common.Trades.SaplingsForEmeralds.class, SaplingsForEmeraldsAccessor.class);
        PluginUtils.registerItemListing(registry, Registers.AnvilRepair.class, AnvilRepairAccessor.class);
    }
}
