package com.yanny.alicompat.compat.villagertradingplus;

import com.lion.villagertradingplus.tradeoffers.conditions.ParsedConditions;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.alicompat.IModCompat;
import org.jetbrains.annotations.NotNull;

public class VillagerTradingPlusCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return VillagerTradingPlusLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerValueTooltip(ParsedConditions.class, VillagerTradingPlusTrades::getParsedConditionsTooltip);

        VillagerTradingPlusTrades.registerAccessor(BuyItemTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellItemTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(MultiInputTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(ProcessItemTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(BuyTaggedItemTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellTaggedItemTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellEnchantedBookTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellSpecificEnchantedBookTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellEnchantedBookFromListTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellEnchantedToolTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellSpecificEnchantedToolTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellPotionTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellDyedArmorTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(SellStructureMapTradeOfferAccessor.class);
        VillagerTradingPlusTrades.registerAccessor(WeightedPoolTradeOfferAccessor.class);

        registry.registerTradeOverride(VillagerTradingPlusTrades::getTradeLevels);
    }
}
