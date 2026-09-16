package com.yanny.alicompat.compat.moonlight;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.common.trades.SubTradesNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.mehvahdjukaar.moonlight.api.trades.BiomeVariantItemListing;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.mehvahdjukaar.moonlight.api.trades.SimpleItemListing;
import net.mehvahdjukaar.moonlight.core.loot.ConfigItemPoolEntry;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPoolEntry;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.mehvahdjukaar.moonlight.core.loot.PatternMatchLootItemCondition;
import net.mehvahdjukaar.moonlight.core.loot.ResourceLootItemCondition;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MoonlightCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return MoonlightLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerEntry(registry, OptionalItemPoolEntry.class, OptionalItemPoolEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, OptionalItemPoolEntry.class, OptionalItemPoolEntryAccessor.class);
        PluginUtils.registerEntry(registry, ConfigItemPoolEntry.class, ConfigItemPoolEntryAccessor.class);
        PluginUtils.registerEntryTooltip(registry, ConfigItemPoolEntry.class, ConfigItemPoolEntryAccessor.class);

        PluginUtils.registerConditionTooltip(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);
        registry.registerConditionTooltip(ResourceLootItemCondition.class, MoonlightCompat::getDataConditionsTooltip);
        registry.registerConditionTooltip(PatternMatchLootItemCondition.class, MoonlightCompat::getPatternMatchTooltip);

        registry.registerValueTooltip(Pattern.class, MoonlightCompat::getPatternTooltip);

        PluginUtils.registerPageResolver(registry, OptionalPropertyCondition.class, OptionalPropertyConditionAccessor.class);
        registry.registerDestination(PatternMatchLootItemCondition.class, MoonlightCompat::getPatternMatchDestination);

        registry.registerItemListing(SimpleItemListing.class, MoonlightCompat::getSimpleItemListingNode);
        registry.registerItemListing(BiomeVariantItemListing.class, MoonlightCompat::getBiomeVariantItemListingNode);
        PluginUtils.registerItemListing(registry, SpecialListingAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getDataConditionsTooltip(IServerUtils utils, ResourceLootItemCondition cond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, MoonlightLang.Conditions.DATA_CONDITIONS);
    }

    @NotNull
    private static TooltipBuilder getPatternMatchTooltip(IServerUtils utils, PatternMatchLootItemCondition cond) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, cond.patterns()));
            b.showEmpty();
        }, MoonlightLang.Conditions.LOOT_TABLE_ID_PATTERN);
    }

    @NotNull
    private static TooltipBuilder getPatternTooltip(IServerUtils ignoredUtils, Pattern pattern) {
        return TooltipBuilder.value(pattern.pattern());
    }

    @NotNull
    private static Destination getPatternMatchDestination(IServerUtils ignoredUtils, PatternMatchLootItemCondition cond) {
        return new Destination.Table((id) -> matches(cond.patterns(), id.toString()), true);
    }

    private static boolean matches(List<Pattern> patterns, String id) {
        return patterns.stream().anyMatch((p) -> id.equals(p.pattern()) || p.matcher(id).find());
    }

    @NotNull
    private static IDataNode getSimpleItemListingNode(IServerUtils utils, SimpleItemListing listing, TooltipNode condition) {
        ItemStack price = listing.price().itemStack();
        ItemStack price2 = listing.price2().map(ItemCost::itemStack).orElse(ItemStack.EMPTY);

        return new ItemsToItemsNode(
                utils,
                Either.left(price),
                new RangeValue(price.getCount()),
                TooltipNode.empty(),
                Either.left(price2),
                new RangeValue(Math.max(1, price2.getCount())),
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
    private static IDataNode getBiomeVariantItemListingNode(IServerUtils utils, BiomeVariantItemListing listing, TooltipNode condition) {
        return new SubTradesNode<>(utils, listing, condition) {
            @Override
            public List<IDataNode> getSubTrades(IServerUtils utils, BiomeVariantItemListing listing) {
                List<IDataNode> nodes = new ArrayList<>();

                for (Map.Entry<VillagerType, ModItemListing> entry : listing.listingMap().entrySet()) {
                    TooltipNode cond = utils.getValueTooltip(utils, entry.getKey().toString()).build(Lang.Value.VILLAGER_TYPE);

                    nodes.add(utils.getItemListing(utils, entry.getValue(), cond));
                }

                nodes.add(utils.getItemListing(utils, listing.defaultListing(), TooltipBuilder.keyOnly(Lang.Branch.FALLBACK).build()));
                return nodes;
            }
        };
    }

    @NotNull
    private static List<LootItemFunction> getFunctions(SimpleItemListing listing) {
        return listing.func() == null ? List.of() : List.of(listing.func());
    }
}
