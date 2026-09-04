package com.yanny.alicompat.compat.goblintraders;

import com.mrcrayfish.goblintraders.Config;
import com.mrcrayfish.goblintraders.entity.TraderCreatureEntity;
import com.mrcrayfish.goblintraders.trades.EntityTrades;
import com.mrcrayfish.goblintraders.trades.IRaritySettings;
import com.mrcrayfish.goblintraders.trades.TradeManager;
import com.mrcrayfish.goblintraders.trades.TradeRarity;
import com.mrcrayfish.goblintraders.trades.type.BaseTrade;
import com.mrcrayfish.goblintraders.trades.type.BasicTrade;
import com.mrcrayfish.goblintraders.trades.type.TreasureMapTrade;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.plugin.common.nodes.MissingNode;
import com.yanny.ali.plugin.server.MissingTooltipUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.ReflectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
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
        registry.registerItemListing(GoblinTradeListing.class, GoblinTradersCompat::getNode);

        registerTrader(registry, "goblin_trader", Config.ENTITIES.goblinTrader.trades);
        registerTrader(registry, "vein_goblin_trader", Config.ENTITIES.veinGoblinTrader.trades);
    }

    private static void registerTrader(IServerRegistry registry, String name, Config.Entities.Goblin.Trades trades) {
        Identifier traderId = Identifier.fromNamespaceAndPath(MOD_ID, name);

        registry.registerTrades(traderId, () -> getItemListings(traderId, trades), (level) -> getLevelInfo(trades, level));
    }

    @NotNull
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> getItemListings(Identifier traderId, Config.Entities.Goblin.Trades trades) {
        Int2ObjectMap<VillagerTrades.ItemListing[]> itemListings = new Int2ObjectArrayMap<>();
        EntityTrades entityTrades = getEntityTrades(traderId);

        if (entityTrades != null) {
            entityTrades.map().forEach((rarity, baseTrades) -> {
                if (trades.getSettings(rarity).includeChance() > 0) {
                    itemListings.put(getLevel(rarity), baseTrades.stream().map(GoblinTradeListing::new).toArray(VillagerTrades.ItemListing[]::new));
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
    private static EntityTrades getEntityTrades(Identifier traderId) {
        //noinspection unchecked
        EntityType<? extends TraderCreatureEntity> entityType = (EntityType<? extends TraderCreatureEntity>) BuiltInRegistries.ENTITY_TYPE.getOptional(traderId).orElse(null);

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
    private static IDataNode getNode(IServerUtils utils, GoblinTradeListing listing, TooltipNode conditions) {
        BaseTrade trade = listing.trade();

        if (trade instanceof BasicTrade basicTrade) {
            return ReflectionUtils.copyClassData(BasicTradeAccessor.class, basicTrade, BasicTrade.class).getNode(utils, conditions);
        } else if (trade instanceof TreasureMapTrade treasureMapTrade) {
            return ReflectionUtils.copyClassData(TreasureMapTradeAccessor.class, treasureMapTrade, TreasureMapTrade.class).getNode(utils, conditions);
        } else {
            return new MissingNode(MissingTooltipUtils.getMissingItemListingTooltip(utils, listing).build());
        }
    }
}
