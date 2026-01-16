package com.yanny.ali.test;

import com.yanny.ali.plugin.server.EntitySubPredicateTooltipUtils;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class EntitySubPredicateTooltipTest {
    @Test
    public void testLightningBoltPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getLightningBoltPredicateTooltip(UTILS, LightningBoltPredicate.blockSetOnFire(MinMaxBounds.Ints.atMost(5))), List.of(
                "Lightning Bolt:",
                "  -> Blocks On Fire: ≤5"
        ));
        assertTooltip(EntitySubPredicateTooltipUtils.getLightningBoltPredicateTooltip(UTILS,
                new LightningBoltPredicate(MinMaxBounds.Ints.between(1, 5), Optional.of(EntityPredicate.Builder.entity().team("blue").build()))), List.of(
                "Lightning Bolt:",
                "  -> Blocks On Fire: 1-5",
                "  -> Stuck Entity:",
                "    -> Team: blue"
        ));
    }

    @Test
    public void testFishingHookPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getFishingHookPredicateTooltip(UTILS, FishingHookPredicate.ANY), List.of(
                "Fishing Hook:"
        ));
        assertTooltip(EntitySubPredicateTooltipUtils.getFishingHookPredicateTooltip(UTILS, FishingHookPredicate.inOpenWater(true)), List.of(
                "Fishing Hook:",
                "  -> Is In Open Water: true"
        ));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testPlayerPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getPlayerPredicateTooltip(UTILS, PlayerPredicate.Builder.player()
                .checkAdvancementDone(Identifier.withDefaultNamespace("test"), true)
                .addRecipe(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("test")), false)
                .checkAdvancementCriterions(Identifier.withDefaultNamespace("criterion"), Map.of("test", true, "test2", false))
                .addStat(Stats.BLOCK_MINED, Blocks.COBBLESTONE.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(100))
                .addStat(Stats.ITEM_USED, Items.CHICKEN.builtInRegistryHolder(), MinMaxBounds.Ints.atMost(10))
                .setLookingAt(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.WARDEN)))
                .setGameType(GameTypePredicate.of(GameType.SURVIVAL))
                .hasInput(new InputPredicate(Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .build()), List.of(
                "Player:",
                "  -> Game Types:",
                "    -> SURVIVAL",
                "  -> Stats:",
                "    -> Block: minecraft:cobblestone",
                "      -> Times Mined: ≥100",
                "    -> Item: minecraft:chicken",
                "      -> Times Used: ≤10",
                "  -> Recipes:",
                "    -> minecraft:test: false",
                "  -> Advancements:",
                "    -> minecraft:test",
                "      -> Done: true",
                "    -> minecraft:criterion",
                "      -> Criterions:",
                "        -> test2: false",
                "        -> test: true",
                "  -> Looking At:",
                "    -> Entity Types:",
                "      -> minecraft:warden",
                "  -> Input:",
                "    -> Forward: true"
        ));
    }

    @Test
    public void testSlimePredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getSlimePredicateTooltip(UTILS, SlimePredicate.sized(MinMaxBounds.Ints.between(0, 2))), List.of(
                "Slime:",
                "  -> Size: 0-2"
        ));
    }

    @Test
    public void testRaiderPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getRaiderPredicateTooltip(UTILS, RaiderPredicate.CAPTAIN_WITHOUT_RAID), List.of(
                "Raider:",
                "  -> Has Raid: false",
                "  -> Is Captain: true"
        ));
    }

    @Test
    public void testSheepPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getSheepPredicateTooltip(UTILS, new SheepPredicate(Optional.of(false))), List.of(
                "Sheep:",
                "  -> Sheared: false"
        ));
    }
}
