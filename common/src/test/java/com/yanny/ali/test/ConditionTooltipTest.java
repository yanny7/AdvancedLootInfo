package com.yanny.ali.test;

import com.yanny.ali.plugin.server.ConditionTooltipUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class ConditionTooltipTest {
    @Test
    public void testAllOfTooltip() {
        assertTooltip(ConditionTooltipUtils.getAllOfTooltip(UTILS, (AllOfCondition) AllOfCondition.allOf(
                TimeCheck.time(IntRange.range(1, 8)).setPeriod(10),
                WeatherCheck.weather().setRaining(true)
        ).build()), List.of(
                "All must pass:",
                "  -> Time Check:",
                "    -> Period: 10",
                "    -> Value: 1 - 8",
                "  -> Weather Check:",
                "    -> Is Raining: true"
        ));
    }

    @Test
    public void testAnyOfTooltip() {
        assertTooltip(ConditionTooltipUtils.getAnyOfTooltip(UTILS, (AnyOfCondition) AnyOfCondition.anyOf(
                TimeCheck.time(IntRange.range(1, 8)).setPeriod(10),
                WeatherCheck.weather().setRaining(true)
        ).build()), List.of(
                "Any of:",
                "  -> Time Check:",
                "    -> Period: 10",
                "    -> Value: 1 - 8",
                "  -> Weather Check:",
                "    -> Is Raining: true"
        ));
    }

    @Test
    public void testBlockStatePropertyTooltip() {
        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(UTILS, (LootItemBlockStatePropertyCondition) LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.FURNACE).build()), List.of(
                "Block State Property:",
                "  -> Block: minecraft:furnace"
        ));
        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(UTILS, (LootItemBlockStatePropertyCondition) LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BAMBOO)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST)).build()), List.of(
                "Block State Property:",
                "  -> Block: minecraft:bamboo",
                "  -> Properties:",
                "    -> facing: east"
        ));
    }

    @Test
    public void testDamageSourceProperties() {
        assertTooltip(ConditionTooltipUtils.getDamageSourcePropertiesTooltip(UTILS, (DamageSourceCondition) DamageSourceCondition.hasDamageSource(
                DamageSourcePredicate.Builder.damageType()
                        .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                        .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                        .direct(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.WARDEN)))
                        .source(EntityPredicate.Builder.entity().team("Blue"))
        ).build()), List.of(
                "Damage Source Properties:",
                "  -> Tags:",
                "    -> minecraft:bypasses_armor: true",
                "    -> minecraft:is_explosion: false",
                "  -> Direct Entity:",
                "    -> Entity Types:",
                "      -> minecraft:warden",
                "  -> Source Entity:",
                "    -> Team: Blue"
        ));
    }

    @Test
    public void testEntityPropertiesTooltip() {
        assertTooltip(ConditionTooltipUtils.getEntityPropertiesTooltip(UTILS, (LootItemEntityPropertyCondition) LootItemEntityPropertyCondition.hasProperties(
                LootContext.EntityTarget.KILLER,
                EntityPredicate.Builder.entity().team("blue")
        ).build()), List.of(
            "Entity Properties:",
            "  -> Target: KILLER",
            "  -> Predicate:",
            "    -> Team: blue"
        ));
    }

    @Test
    public void testEntityScoresTooltip() {
        assertTooltip(ConditionTooltipUtils.getEntityScoresTooltip(UTILS, (EntityHasScoreCondition) EntityHasScoreCondition.hasScores(LootContext.EntityTarget.DIRECT_KILLER).build()), List.of(
                "Entity Scores:",
                "  -> Target: DIRECT_KILLER"
        ));
        assertTooltip(ConditionTooltipUtils.getEntityScoresTooltip(UTILS, (EntityHasScoreCondition) EntityHasScoreCondition.hasScores(LootContext.EntityTarget.DIRECT_KILLER)
                .withScore("single", IntRange.range(2, 5))
                .withScore("double", IntRange.range(1, 7))
                .build()
        ), List.of(
                "Entity Scores:",
                "  -> Target: DIRECT_KILLER",
                "  -> Scores:",
                "    -> single",
                "      -> Limit: 2 - 5",
                "    -> double",
                "      -> Limit: 1 - 7"
        ));
    }

    @Test
    public void testInvertedTooltip() {
        assertTooltip(ConditionTooltipUtils.getInvertedTooltip(UTILS, (InvertedLootItemCondition) InvertedLootItemCondition.invert(
                TimeCheck.time(IntRange.range(1, 8)).setPeriod(10)
        ).build()), List.of(
                "Inverted:",
                "  -> Time Check:",
                "    -> Period: 10",
                "    -> Value: 1 - 8"
        ));
    }

    @Test
    public void testKilledByPlayerTooltip() {
        assertTooltip(ConditionTooltipUtils.getKilledByPlayerTooltip(UTILS, (LootItemKilledByPlayerCondition) LootItemKilledByPlayerCondition.killedByPlayer().build()), List.of("Must be killed by player"));
    }

    @Test
    public void testLocationCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getLocationCheckTooltip(UTILS, (LocationCheck) LocationCheck.checkLocation(
                LocationPredicate.Builder.location().setSmokey(true),
                new BlockPos(2, 4, 6)
        ).build()), List.of(
                "Location Check:",
                "  -> Location:",
                "    -> Smokey: true",
                "  -> Offset: [X: 2, Y: 4, Z: 6]"
        ));
        assertTooltip(ConditionTooltipUtils.getLocationCheckTooltip(UTILS, (LocationCheck) LocationCheck.checkLocation(
                LocationPredicate.Builder.location().setSmokey(true)
        ).build()), List.of(
                "Location Check:",
                "  -> Location:",
                "    -> Smokey: true"
        ));
    }

    @Test
    public void testItemMatchTooltip() {
        assertTooltip(ConditionTooltipUtils.getMatchToolTooltip(UTILS, (MatchTool) MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE)).build()), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> minecraft:andesite",
                "    -> minecraft:diorite"
        ));
    }

    @Test
    public void testRandomChanceTooltip() {
        assertTooltip(ConditionTooltipUtils.getRandomChanceTooltip(UTILS, (LootItemRandomChanceCondition) LootItemRandomChanceCondition.randomChance(0.25F).build()), List.of(
                "Random Chance:",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testRandomChanceWithLootingTooltip() {
        assertTooltip(ConditionTooltipUtils.getRandomChanceWithLootingTooltip(UTILS, (LootItemRandomChanceWithLootingCondition) LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.25F, 5F).build()), List.of(
                "Random Chance With Looting:",
                "  -> Percent: 0.25",
                "  -> Multiplier: 5.0"
        ));
    }

    @Test
    public void testReferenceTooltip() {
        assertTooltip(ConditionTooltipUtils.getReferenceTooltip(UTILS, (ConditionReference) ConditionReference.conditionReference(new ResourceLocation("test")).build()), List.of("Reference: minecraft:test"));
    }

    @Test
    public void testSurvivesExplosionTooltip() {
        assertTooltip(ConditionTooltipUtils.getSurvivesExplosionTooltip(UTILS, (ExplosionCondition) ExplosionCondition.survivesExplosion().build()), List.of("Must survive explosion"));
    }

    @Test
    public void testTableBonusTooltip() {
        assertTooltip(ConditionTooltipUtils.getTableBonusTooltip(UTILS, (BonusLevelTableCondition) BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.MOB_LOOTING, 0.25F, 0.5555F, 0.99F).build()), List.of(
                "Table Bonus:",
                "  -> Enchantment: minecraft:looting",
                "  -> Values: [0.25, 0.555, 0.99]"
        ));
    }

    @Test
    public void testTimeCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getTimeCheckTooltip(UTILS, TimeCheck.time(IntRange.range(5, 10)).setPeriod(24000).build()), List.of(
                "Time Check:",
                "  -> Period: 24000",
                "  -> Value: 5 - 10"
        ));
    }

    @Test
    public void testValueCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getValueCheckTooltip(UTILS, (ValueCheckCondition) ValueCheckCondition.hasValue(UniformGenerator.between(1, 20), IntRange.range(1, 10)).build()), List.of(
                "Value Check:",
                "  -> Provider: 1-20",
                "  -> Range: 1 - 10"
        ));
    }

    @Test
    public void testWeatherCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, WeatherCheck.weather().setRaining(true).setThundering(false).build()), List.of(
                "Weather Check:",
                "  -> Is Raining: true",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, WeatherCheck.weather().setRaining(true).build()), List.of(
                "Weather Check:",
                "  -> Is Raining: true"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, WeatherCheck.weather().setThundering(false).build()), List.of(
                "Weather Check:",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, WeatherCheck.weather().build()), List.of("Weather Check:"));
    }
}
