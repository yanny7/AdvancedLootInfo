package com.yanny.alicompat.compat.moonlight;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.mehvahdjukaar.moonlight.api.trades.SimpleItemListing;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoonlightCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return MoonlightLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, OptionalItemPool.class, OptionalItemPoolAccessor.class);
        PluginUtils.registerEntryTooltip(registry, OptionalItemPool.class, OptionalItemPoolAccessor.class);

        PluginUtils.registerConditionTooltip(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);

        PluginUtils.registerDestination(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);

        registry.registerItemListing(SimpleItemListing.class, MoonlightCompat::getSimpleItemListingNode);
        PluginUtils.registerItemListing(registry, SpecialListingAccessor.class);
    }

    @NotNull
    private static IDataNode getSimpleItemListingNode(IServerUtils utils, SimpleItemListing listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.price()),
                new RangeValue(listing.price().getCount()),
                TooltipNode.empty(),
                Either.left(listing.price2()),
                new RangeValue(Math.max(1, listing.price2().getCount())),
                TooltipNode.empty(),
                Either.left(TooltipUtils.getItemStack(utils, listing.offer().copy(), getFunctions(listing))),
                new RangeValue(listing.offer().getCount()),
                utils.getValueTooltip(utils, listing.func()).build(Lang.Branch.MODIFIERS),
                listing.maxTrades(),
                listing.xp(),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static List<LootItemFunction> getFunctions(SimpleItemListing listing) {
        return listing.func() == null ? List.of() : List.of(listing.func());
    }
}
