package com.yanny.alicompat.compat.goblintraders;

import com.mojang.datafixers.util.Either;
import com.mrcrayfish.goblintraders.Config;
import com.mrcrayfish.goblintraders.entity.TraderCreatureEntity;
import com.mrcrayfish.goblintraders.trades.EntityTrades;
import com.mrcrayfish.goblintraders.trades.GoblinTrade;
import com.mrcrayfish.goblintraders.trades.IRaritySettings;
import com.mrcrayfish.goblintraders.trades.TradeManager;
import com.mrcrayfish.goblintraders.trades.TradeRarity;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.alicompat.IModCompat;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GoblinTradersCompat implements IModCompat {
    private static final String MOD_ID = "goblintraders";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerItemListing(GoblinTrade.class, GoblinTradersCompat::getNode);

        registerTrader(registry, "goblin_trader", Config.ENTITIES.goblinTrader.trades);
        registerTrader(registry, "vein_goblin_trader", Config.ENTITIES.veinGoblinTrader.trades);
    }

    private static void registerTrader(IServerRegistry registry, String name, Config.Entities.Goblin.Trades trades) {
        ResourceLocation traderId = new ResourceLocation(MOD_ID, name);

        registry.registerTrades(traderId, () -> getItemListings(traderId, trades), (level) -> getLevelInfo(trades, level));
    }

    @NotNull
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> getItemListings(ResourceLocation traderId, Config.Entities.Goblin.Trades trades) {
        Int2ObjectMap<VillagerTrades.ItemListing[]> itemListings = new Int2ObjectArrayMap<>();
        EntityTrades entityTrades = getEntityTrades(traderId);

        if (entityTrades != null) {
            entityTrades.getTradeMap().forEach((rarity, listings) -> {
                if (trades.getSettings(rarity).includeChance() > 0) {
                    itemListings.put(getLevel(rarity), listings.toArray(new VillagerTrades.ItemListing[0]));
                }
            });
        }

        return itemListings;
    }

    @NotNull
    private static TradeLevelInfo getLevelInfo(Config.Entities.Goblin.Trades trades, int level) {
        IRaritySettings settings = trades.getSettings(getRarity(level));

        return new TradeLevelInfo(new RangeValue(settings.getMinValue(), settings.getMaxValue()), (float) settings.includeChance());
    }

    @Nullable
    private static EntityTrades getEntityTrades(ResourceLocation traderId) {
        //noinspection unchecked
        EntityType<? extends TraderCreatureEntity> entityType = (EntityType<? extends TraderCreatureEntity>) BuiltInRegistries.ENTITY_TYPE.get(traderId);

        return TradeManager.instance().getTrades(entityType);
    }

    private static int getLevel(TradeRarity rarity) {
        return rarity.ordinal() + 1;
    }

    @NotNull
    private static TradeRarity getRarity(int level) {
        return TradeRarity.values()[level - 1];
    }

    @NotNull
    private static IDataNode getNode(IServerUtils utils, GoblinTrade trade, TooltipNode conditions) {
        return new ItemsToItemsNode(
                utils,
                Either.left(trade.paymentStack()),
                new RangeValue(trade.paymentStack().getCount()),
                Either.left(trade.secondaryPaymentStack()),
                new RangeValue(trade.secondaryPaymentStack().getCount()),
                Either.left(trade.offerStack()),
                new RangeValue(trade.offerStack().getCount()),
                trade.maxUses(),
                trade.experience(),
                trade.priceMultiplier(),
                conditions
        );
    }
}
