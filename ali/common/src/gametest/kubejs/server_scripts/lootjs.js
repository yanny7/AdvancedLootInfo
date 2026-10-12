LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/add")
        .addLoot("minecraft:diamond")
        .addLoot(LootEntry.of("minecraft:emerald", 3))
        .pool((pool) => {
            pool.rolls([1, 3]);
            pool.addEntry(LootEntry.of("minecraft:gold_ingot").withWeight(30));
            pool.addEntry(LootEntry.of("minecraft:iron_ingot").withWeight(70));
        })
        .addAlternativesLoot(LootEntry.of("minecraft:apple").when((c) => c.randomChance(0.5)), LootEntry.of("minecraft:stick"));
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/remove").removeLoot("minecraft:flint");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/replace")
        .replaceLoot("minecraft:iron_ingot", "minecraft:gold_ingot")
        .replaceLoot("minecraft:copper_ingot", "minecraft:emerald");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/modify").modifyLoot(["minecraft:arrow", "minecraft:spectral_arrow"], (stack) => {
        stack.setCount(stack.getCount() * 2);
        return stack;
    });
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/conditions")
        .randomChance(0.5)
        .killedByPlayer()
        .matchMainHand("minecraft:diamond_sword")
        .matchBiome("minecraft:desert")
        .addLoot("minecraft:bone");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/composite_conditions")
        .matchAnyOf(LootCondition.matchDimension("minecraft:the_nether"), LootCondition.isLightLevel(0, 7))
        .matchCustomCondition({
            condition: "minecraft:inverted",
            term: { condition: "minecraft:location_check", predicate: { structures: "minecraft:village_plains" } }
        })
        .addLoot("minecraft:string");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/pool").pool((pool) => {
        pool.rolls([1, 3]);
        pool.when((c) => c.randomChance(0.3));
        pool.addEntry(LootEntry.of("minecraft:diamond"));
    });
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/functions")
        .addLoot(LootEntry.of("minecraft:diamond_sword").enchantRandomly().damage([0.3, 0.9]));
});

LootJS.modifiers((event) => {
    event.addTableModifier(/^ali_gametest:lootjs\/regex.*/).addLoot("minecraft:slime_ball");
});

LootJS.modifiers((event) => {
    event.addBlockModifier("minecraft:gravel").removeLoot("minecraft:flint");
});

LootJS.modifiers((event) => {
    event.addEntityModifier("minecraft:creeper").addLoot("minecraft:tnt");
});

LootJS.modifiers((event) => {
    event.addTableModifier(LootType.FISHING).addLoot("minecraft:nautilus_shell");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/actions").dropExperience(5).addLoot("minecraft:feather");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/actions").triggerLightningStrike(false).addLoot("minecraft:blaze_rod");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/actions").playerAction((player) => {}).addLoot("minecraft:ender_pearl");
});

LootJS.modifiers((event) => {
    event.addTableModifier("ali_gametest:lootjs/actions").triggerExplosion(1, "keep", false).addLoot("minecraft:gunpowder");
});
