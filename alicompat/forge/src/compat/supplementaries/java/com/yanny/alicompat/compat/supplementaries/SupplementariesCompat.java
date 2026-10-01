package com.yanny.alicompat.compat.supplementaries;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.ModVillagerTrades;
import net.mehvahdjukaar.supplementaries.common.entities.trades.PresentItemListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.RandomAdventurerMapListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.RocketItemListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.StarItemListing;
import net.mehvahdjukaar.supplementaries.common.entities.trades.StructureMapListing;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.mehvahdjukaar.supplementaries.forge.ReplaceRopeByConfigModifier;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SupplementariesCompat implements IGlmModCompat {
    private static final int RED_MERCHANT_TRADE_COUNT = 7;
    private static final int RED_MERCHANT_LEVEL = 1;
    private static final int ADVENTURER_MAP_XP = 72;
    private static final int ADVENTURER_MAP_SEARCH_RADIUS = 150;
    private static final int ADVENTURER_MAP_ZOOM = 2;
    private static final String ADVENTURER_MAP_NAME = "filled_map.adventure";
    private static final int MAX_FIREWORK_STARS = 7;
    private static final double NEXT_STAR_CHANCE = 0.42;

    @NotNull
    @Override
    public String targetModId() {
        return SupplementariesLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CurseLootFunction.class, CurseLootFunctionAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, RandomArrowFunction.class, RandomArrowFunctionAccessor.class);

        registry.registerItemListing(PresentItemListing.class, SupplementariesCompat::getPresentListingNode);
        registry.registerItemListing(StarItemListing.class, SupplementariesCompat::getStarListingNode);
        registry.registerItemListing(RocketItemListing.class, SupplementariesCompat::getRocketListingNode);
        registry.registerItemListing(StructureMapListing.class, SupplementariesCompat::getStructureMapListingNode);
        registry.registerItemListing(RandomAdventurerMapListing.class, SupplementariesCompat::getAdventurerMapListingNode);

        ResourceLocation redMerchant = new ResourceLocation(SupplementariesLang.MOD_ID, "red_merchant");

        registry.registerTrades(
                redMerchant,
                BuiltInRegistries.ENTITY_TYPE.get(redMerchant),
                SupplementariesCompat::getRedMerchantTrades,
                (level) -> new TradeLevelInfo(NumberExpr.constant(RED_MERCHANT_TRADE_COUNT))
        );
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, ReplaceRopeByConfigModifier.class, ReplaceRopeByConfigModifierAccessor.class);
    }

    @NotNull
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> getRedMerchantTrades() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> itemListings = new Int2ObjectArrayMap<>();

        itemListings.put(RED_MERCHANT_LEVEL, ModVillagerTrades.getRedMerchantTrades());
        return itemListings;
    }

    @NotNull
    private static IDataNode getPresentListingNode(IServerUtils utils, PresentItemListing listing, TooltipNode condition) {
        return utils.getItemListing(utils, listing.original(), condition);
    }

    @NotNull
    private static IDataNode getStarListingNode(IServerUtils utils, StarItemListing listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.emeralds()),
                NumberExpr.constant(listing.emeralds().getCount()),
                TooltipNode.empty(),
                Either.left(listing.priceSecondary()),
                NumberExpr.constant(Math.max(1, listing.priceSecondary().getCount())),
                TooltipNode.empty(),
                Either.left(Items.FIREWORK_STAR.getDefaultInstance()),
                NumberExpr.constant(listing.stars()),
                TooltipBuilder.keyOnly(SupplementariesLang.Value.RANDOM_FIREWORK_STAR).build(),
                listing.maxTrades(),
                listing.xp(),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static IDataNode getRocketListingNode(IServerUtils utils, RocketItemListing listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.emeralds()),
                NumberExpr.constant(listing.emeralds().getCount()),
                TooltipNode.empty(),
                Either.left(listing.priceSecondary()),
                NumberExpr.constant(Math.max(1, listing.priceSecondary().getCount())),
                TooltipNode.empty(),
                Either.left(Items.FIREWORK_ROCKET.getDefaultInstance()),
                NumberExpr.constant(listing.rockets()),
                TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, NumberExpr.uniformInt(1, 3)).build(SupplementariesLang.Value.FLIGHT_DURATION));
                    b.add(utils.getValueTooltip(utils, fireworkStars()).build(SupplementariesLang.Value.FIREWORK_STARS));
                }, SupplementariesLang.Value.RANDOM_FIREWORK).build(),
                listing.maxTrades(),
                ModItemListing.defaultXp(true, listing.level()),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static IDataNode getStructureMapListingNode(IServerUtils utils, StructureMapListing listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.cost().getDefaultInstance()),
                NumberExpr.max(NumberExpr.constant(1), NumberExpr.uniformInt(listing.minPrice(), listing.minPrice() + Math.max(1, listing.maxPrice() - listing.minPrice()) - 1)),
                TooltipNode.empty(),
                Either.left(listing.cost2()),
                NumberExpr.constant(Math.max(1, listing.cost2().getCount())),
                TooltipNode.empty(),
                Either.left(getMapStack(listing.mapName())),
                NumberExpr.constant(1),
                TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, listing.structure()).build(Lang.Value.DESTINATION));
                    b.add(utils.getValueTooltip(utils, listing.mapMarker()).build(Lang.Value.MAP_DECORATION));
                    b.add(utils.getValueTooltip(utils, ADVENTURER_MAP_SEARCH_RADIUS).build(Lang.Value.SEARCH_RADIUS));
                    b.add(utils.getValueTooltip(utils, true).build(Lang.Value.SKIP_KNOWN_STRUCTURES));
                    b.add(utils.getValueTooltip(utils, ADVENTURER_MAP_ZOOM).build(Lang.Value.ZOOM));
                }).build(),
                listing.maxTrades(),
                ModItemListing.defaultXp(false, listing.level()),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static IDataNode getAdventurerMapListingNode(IServerUtils utils, RandomAdventurerMapListing listing, TooltipNode condition) {
        return new ItemsToItemsNode(
                utils,
                Either.left(listing.emerald().getDefaultInstance()),
                NumberExpr.uniformInt(listing.priceMax(), 2 * listing.priceMax() - listing.priceMin()),
                TooltipNode.empty(),
                Either.left(listing.priceSecondary()),
                NumberExpr.constant(Math.max(1, listing.priceSecondary().getCount())),
                TooltipNode.empty(),
                Either.left(getMapStack(ADVENTURER_MAP_NAME)),
                NumberExpr.constant(1),
                TooltipBuilder.array((b) -> {
                    b.add(utils.getValueTooltip(utils, ModTags.ADVENTURE_MAP_DESTINATIONS).build(Lang.Value.DESTINATION));
                    b.add(utils.getValueTooltip(utils, ADVENTURER_MAP_SEARCH_RADIUS).build(Lang.Value.SEARCH_RADIUS));
                    b.add(utils.getValueTooltip(utils, true).build(Lang.Value.SKIP_KNOWN_STRUCTURES));
                    b.add(utils.getValueTooltip(utils, ADVENTURER_MAP_ZOOM).build(Lang.Value.ZOOM));
                }).build(),
                listing.maxTrades(),
                ADVENTURER_MAP_XP / Math.max(1, listing.maxTrades()),
                listing.priceMult(),
                condition
        );
    }

    @NotNull
    private static NumberExpr fireworkStars() {
        List<NumberExpr.WeightedEntry> entries = new ArrayList<>();

        for (int stars = 1; stars < MAX_FIREWORK_STARS; stars++) {
            entries.add(new NumberExpr.WeightedEntry(Math.pow(NEXT_STAR_CHANCE, stars - 1) * (1 - NEXT_STAR_CHANCE), NumberExpr.constant(stars)));
        }

        entries.add(new NumberExpr.WeightedEntry(Math.pow(NEXT_STAR_CHANCE, MAX_FIREWORK_STARS - 1), NumberExpr.constant(MAX_FIREWORK_STARS)));
        return NumberExpr.weighted(entries);
    }

    @NotNull
    private static ItemStack getMapStack(String name) {
        ItemStack stack = Items.FILLED_MAP.getDefaultInstance();

        if (!name.isEmpty()) {
            stack.setHoverName(Component.translatable(name));
        }

        return stack;
    }
}
