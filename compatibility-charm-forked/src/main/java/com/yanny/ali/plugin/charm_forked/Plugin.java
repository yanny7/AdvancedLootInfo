package com.yanny.ali.plugin.charm_forked;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IPlugin;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.mixin.MixinAnvilRepair;
import net.minecraft.world.entity.npc.VillagerTrades;
import svenhjol.charm.feature.beekeepers.BeekeeperTradeOffers;
import svenhjol.charm.feature.lumberjacks.LumberjackTradeOffers;
import svenhjol.charmony.helper.GenericTradeOffers;

@AliEntrypoint
public class Plugin implements IPlugin {
    @Override
    public String getModId() {
        return "charm";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemListing(BeekeeperTradeOffers.EmeraldsForFlowers.class, TradeUtils::getNode);
        registry.registerItemListing(BeekeeperTradeOffers.EnchantedShearsForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(BeekeeperTradeOffers.PopulatedBeehiveForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(BeekeeperTradeOffers.TallFlowerForEmeralds.class, TradeUtils::getNode);

        registry.registerItemListing(LumberjackTradeOffers.SaplingsForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(LumberjackTradeOffers.BarkForLogs.class, TradeUtils::getNode);

        registry.registerItemListing(GenericTradeOffers.EmeraldsForTag.class, TradeUtils::getNode);
        registry.registerItemListing(GenericTradeOffers.EmeraldsForTwoTags.class, TradeUtils::getNode);
        registry.registerItemListing(GenericTradeOffers.EmeraldsForItems.class, TradeUtils::getNode);
        registry.registerItemListing(GenericTradeOffers.TagForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(GenericTradeOffers.ItemsForEmeralds.class, TradeUtils::getNode);
        registry.registerItemListing(GenericTradeOffers.ItemsForItems.class, TradeUtils::getNode);

        registry.registerItemListingCollector(BeekeeperTradeOffers.EmeraldsForFlowers.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(BeekeeperTradeOffers.EnchantedShearsForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(BeekeeperTradeOffers.PopulatedBeehiveForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(BeekeeperTradeOffers.TallFlowerForEmeralds.class, TradeUtils::collectItems);

        registry.registerItemListingCollector(LumberjackTradeOffers.SaplingsForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(LumberjackTradeOffers.BarkForLogs.class, TradeUtils::collectItems);

        registry.registerItemListingCollector(GenericTradeOffers.EmeraldsForTag.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(GenericTradeOffers.EmeraldsForTwoTags.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(GenericTradeOffers.EmeraldsForItems.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(GenericTradeOffers.TagForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(GenericTradeOffers.ItemsForEmeralds.class, TradeUtils::collectItems);
        registry.registerItemListingCollector(GenericTradeOffers.ItemsForItems.class, TradeUtils::collectItems);

        try {
            //noinspection unchecked
            Class<? extends VillagerTrades.ItemListing> clazz = (Class<? extends VillagerTrades.ItemListing>) Class.forName("svenhjol.charm.feature.extra_trades.ExtraTrades$AnvilRepair");

            registry.registerItemListing(clazz, (utils, listing, conditions) -> TradeUtils.getNode(utils, (MixinAnvilRepair) listing, conditions));
            registry.registerItemListingCollector(clazz, (utils, listing) -> TradeUtils.collectItems(utils, (MixinAnvilRepair) listing));
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
