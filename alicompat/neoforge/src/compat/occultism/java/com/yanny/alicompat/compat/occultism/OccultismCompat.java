package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.common.entity.spirit.wonderingtrader.WonderingTrades;
import com.klikli_dev.occultism.loot.AddItemModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.language.ITooltipKey;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import com.yanny.alicompat.accessor.PluginUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class OccultismCompat implements IGlmModCompat {
    private static final ResourceLocation WONDERING_TRADER = ResourceLocation.fromNamespaceAndPath(OccultismLang.MOD_ID, "wondering_trader");

    @NotNull
    @Override
    public String targetModId() {
        return OccultismLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerItemListing(registry, WonderingTrades.ItemTrade.class, ItemTradeAccessor.class);
        PluginUtils.registerSelfItemListing(registry, ConditionalListing.class);

        registry.registerTrades(WONDERING_TRADER, BuiltInRegistries.ENTITY_TYPE.get(WONDERING_TRADER), OccultismCompat::getWonderingTraderTrades, OccultismCompat::getWonderingTraderLevel);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddItemModifier.class, AddItemModifierAccessor.class);
    }

    @NotNull
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> getWonderingTraderTrades() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> listings = new Int2ObjectArrayMap<>();

        putListings(listings, 1, WonderingTrades.WONDERING_TRADES.get(WonderingTrades.HINT), OccultismLang.Value.WITHOUT_THIRD_EYE);
        putListings(listings, 2, VillagerTrades.WANDERING_TRADER_TRADES.get(1), OccultismLang.Value.WITHOUT_THIRD_EYE);
        putListings(listings, 3, VillagerTrades.WANDERING_TRADER_TRADES.get(2), OccultismLang.Value.WITHOUT_THIRD_EYE);

        for (int group = WonderingTrades.BOOK; group <= WonderingTrades.DYE; group++) {
            putListings(listings, group + 3, WonderingTrades.WONDERING_TRADES.get(group), OccultismLang.Value.WITH_THIRD_EYE);
        }

        return listings;
    }

    @NotNull
    private static TradeLevelInfo getWonderingTraderLevel(int level) {
        return switch (level) {
            case 2 -> new TradeLevelInfo(new RangeValue(5));
            case 5, 8 -> new TradeLevelInfo(new RangeValue(1, 3));
            case 6 -> new TradeLevelInfo(new RangeValue(1, 2));
            case 10 -> new TradeLevelInfo(new RangeValue(1), 0.5f);
            case 11 -> new TradeLevelInfo(new RangeValue(1), 0.25f);
            default -> new TradeLevelInfo(new RangeValue(1));
        };
    }

    private static void putListings(Int2ObjectMap<VillagerTrades.ItemListing[]> listings, int level, @Nullable VillagerTrades.ItemListing[] source, ITooltipKey condition) {
        if (source != null) {
            listings.put(level, Arrays.stream(source).map((l) -> new ConditionalListing(l, condition)).toArray(VillagerTrades.ItemListing[]::new));
        }
    }
}
