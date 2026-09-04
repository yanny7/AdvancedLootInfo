package com.yanny.alicompat.compat.ribbits;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.ReflectionUtils;
import com.yungnickyoung.minecraft.ribbits.data.RibbitProfession;
import com.yungnickyoung.minecraft.ribbits.entity.trade.AmethystForItems;
import com.yungnickyoung.minecraft.ribbits.entity.trade.EnchantedItemForAmethyst;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemListing;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemsAndAmethystsToItems;
import com.yungnickyoung.minecraft.ribbits.entity.trade.ItemsForAmethysts;
import com.yungnickyoung.minecraft.ribbits.entity.trade.PotionForAmethyst;
import com.yungnickyoung.minecraft.ribbits.module.RibbitProfessionModule;
import com.yungnickyoung.minecraft.ribbits.module.RibbitTradeModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RibbitsCompat implements IModCompat {
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);
    private static final String MOD_ID = "ribbits";
    private static final int TRADE_COUNT = 4;
    private static final int MERCHANT_TRADE_COUNT = 10;
    private static final int LEVEL = 1;

    private static final Map<Class<? extends ItemListing>, Function<ItemListing, VillagerTrades.ItemListing>> WRAPPERS = createWrappers();

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemListing(ItemsForAmethystsAccessor.class, (utils, listing, condition) -> listing.getNode(utils, condition));
        registry.registerItemListing(AmethystForItemsAccessor.class, (utils, listing, condition) -> listing.getNode(utils, condition));
        registry.registerItemListing(ItemsAndAmethystsToItemsAccessor.class, (utils, listing, condition) -> listing.getNode(utils, condition));
        registry.registerItemListing(EnchantedItemForAmethystAccessor.class, (utils, listing, condition) -> listing.getNode(utils, condition));
        registry.registerItemListing(PotionForAmethystAccessor.class, (utils, listing, condition) -> listing.getNode(utils, condition));

        RibbitTradeModule.TRADES_BY_PROFESSION.forEach((profession, listings) -> {
            ResourceLocation professionId = profession.getId();
            ResourceLocation traderId = new ResourceLocation(professionId.getNamespace(), "ribbit_" + professionId.getPath());
            int tradeCount = RibbitProfessionModule.MERCHANT.equals(profession) ? MERCHANT_TRADE_COUNT : TRADE_COUNT;

            registry.registerTrades(traderId, () -> getItemListings(profession), (level) -> new TradeLevelInfo(new RangeValue(tradeCount)));
        });
    }

    @NotNull
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> getItemListings(RibbitProfession profession) {
        Int2ObjectMap<VillagerTrades.ItemListing[]> itemListings = new Int2ObjectArrayMap<>();
        ItemListing[] listings = RibbitTradeModule.TRADES_BY_PROFESSION.get(profession);

        if (listings != null) {
            List<VillagerTrades.ItemListing> wrapped = new ArrayList<>();

            for (ItemListing listing : listings) {
                VillagerTrades.ItemListing accessor = wrap(listing);

                if (accessor != null) {
                    wrapped.add(accessor);
                }
            }

            itemListings.put(LEVEL, wrapped.toArray(new VillagerTrades.ItemListing[0]));
        }

        return itemListings;
    }

    @Nullable
    private static VillagerTrades.ItemListing wrap(@Nullable ItemListing listing) {
        if (listing == null) {
            return null;
        }

        Function<ItemListing, VillagerTrades.ItemListing> wrapper = WRAPPERS.get(listing.getClass());

        if (wrapper == null) {
            LOGGER.warn("No item listing accessor for {}", listing.getClass().getName());
            return null;
        }

        return wrapper.apply(listing);
    }

    @NotNull
    private static Map<Class<? extends ItemListing>, Function<ItemListing, VillagerTrades.ItemListing>> createWrappers() {
        Map<Class<? extends ItemListing>, Function<ItemListing, VillagerTrades.ItemListing>> wrappers = new LinkedHashMap<>();

        wrappers.put(ItemsForAmethysts.class, (l) -> ReflectionUtils.copyClassData(ItemsForAmethystsAccessor.class, l, ItemsForAmethysts.class));
        wrappers.put(AmethystForItems.class, (l) -> ReflectionUtils.copyClassData(AmethystForItemsAccessor.class, l, AmethystForItems.class));
        wrappers.put(ItemsAndAmethystsToItems.class, (l) -> ReflectionUtils.copyClassData(ItemsAndAmethystsToItemsAccessor.class, l, ItemsAndAmethystsToItems.class));
        wrappers.put(EnchantedItemForAmethyst.class, (l) -> ReflectionUtils.copyClassData(EnchantedItemForAmethystAccessor.class, l, EnchantedItemForAmethyst.class));
        wrappers.put(PotionForAmethyst.class, (l) -> ReflectionUtils.copyClassData(PotionForAmethystAccessor.class, l, PotionForAmethyst.class));

        return wrappers;
    }
}
