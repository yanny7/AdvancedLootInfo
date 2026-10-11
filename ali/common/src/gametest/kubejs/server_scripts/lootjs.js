LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/add")
        .addLoot("minecraft:diamond")
        .addLoot(LootEntry.of("minecraft:emerald", 3))
        .addWeightedLoot([1, 3], [Item.of("minecraft:gold_ingot").withChance(30), Item.of("minecraft:iron_ingot").withChance(70)])
        .addAlternativesLoot(LootEntry.of("minecraft:apple").when((c) => c.randomChance(0.5)), LootEntry.of("minecraft:stick"));
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/remove").removeLoot("minecraft:flint");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/replace")
        .replaceLoot("minecraft:iron_ingot", "minecraft:gold_ingot")
        .replaceLoot("minecraft:copper_ingot", "minecraft:emerald");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/modify").modifyLoot(["minecraft:arrow", "minecraft:spectral_arrow"], (stack) => {
        stack.setCount(stack.getCount() * 2);
        return stack;
    });
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/conditions")
        .randomChance(0.5)
        .killedByPlayer()
        .matchMainHand("minecraft:diamond_sword")
        .biome("minecraft:desert")
        .addLoot("minecraft:bone");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/composite_conditions")
        .or((or) => or.anyDimension("minecraft:the_nether").lightLevel(0, 7))
        .not((not) => not.anyStructure(["minecraft:village_plains"], false))
        .addLoot("minecraft:string");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/pool").pool((pool) => {
        pool.rolls([1, 3]);
        pool.randomChance(0.3).addLoot("minecraft:diamond");
    });
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/functions")
        .addLoot(LootEntry.of("minecraft:diamond_sword").enchantRandomly().damage([0.3, 0.9]));
});

LootJS.modifiers((event) => {
    event.addLootTableModifier(/^ali_gametest:lootjs\/regex.*/).addLoot("minecraft:slime_ball");
});

LootJS.modifiers((event) => {
    event.addBlockLootModifier("minecraft:gravel").removeLoot("minecraft:flint");
});

LootJS.modifiers((event) => {
    event.addEntityLootModifier("minecraft:creeper").addLoot("minecraft:tnt");
});

LootJS.modifiers((event) => {
    event.addLootTypeModifier(LootType.FISHING).addLoot("minecraft:nautilus_shell");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/actions").dropExperience(5).addLoot("minecraft:feather");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/actions").triggerLightningStrike(false).addLoot("minecraft:blaze_rod");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/actions").playerAction((player) => {}).addLoot("minecraft:ender_pearl");
});

LootJS.modifiers((event) => {
    event.addLootTableModifier("ali_gametest:lootjs/actions").triggerExplosion(1, "keep", false).addLoot("minecraft:gunpowder");
});
