MoreJSEvents.villagerTrades((event) => {
    event.addTrade("minecraft:farmer", 1, [TradeItem.of("minecraft:apple", 3, 7), TradeItem.of("minecraft:sugar", 2)], TradeItem.of("minecraft:cake", 1))
        .maxUses(4)
        .villagerExperience(3)
        .priceMultiplier(0.2);

    event.addTrade("minecraft:farmer", 2, VillagerUtils.createStewTrade([TradeItem.of("minecraft:emerald", 2)], ["minecraft:night_vision", "minecraft:jump_boost"], 200));
    event.addTrade("minecraft:weaponsmith", 2, VillagerUtils.createEnchantedItemTrade([TradeItem.of("minecraft:emerald", 5, 9)], "minecraft:iron_sword"));
    event.addTrade("minecraft:cleric", 2, VillagerUtils.createPotionTrade([TradeItem.of("minecraft:emerald", 3), TradeItem.of("minecraft:glass_bottle", 1)]));
    event.addTrade("minecraft:cartographer", 2, VillagerUtils.createStructureMapTrade([TradeItem.of("minecraft:emerald", 8)],
        MoreJS.weightedList().add(3, "minecraft:village_plains").add(1, "minecraft:desert_pyramid").build()));
    event.addTrade("minecraft:cartographer", 3, VillagerUtils.createBiomeMapTrade([TradeItem.of("minecraft:emerald", 10)],
        MoreJS.weightedList().add(1, "minecraft:jungle").build()));
    event.addCustomTrade("minecraft:librarian", 2, (offer, entity, random) => {
        offer.setOutput("minecraft:diamond");
    });
});

MoreJSEvents.wandererTrades((event) => {
    event.addTrade(1, [TradeItem.of("minecraft:emerald", 1, 3)], TradeItem.of("minecraft:cactus", 2, 4));
});
