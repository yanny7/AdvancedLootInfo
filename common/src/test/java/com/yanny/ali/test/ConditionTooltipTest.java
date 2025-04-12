package com.yanny.ali.test;

import com.yanny.ali.plugin.client.ConditionTooltipUtils;
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
        assertTooltip(ConditionTooltipUtils.getAllOfTooltip(UTILS, 0, (AllOfCondition) AllOfCondition.allOf(
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
        assertTooltip(ConditionTooltipUtils.getAnyOfTooltip(UTILS, 0, (AnyOfCondition) AnyOfCondition.anyOf(
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
        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(UTILS, 0, (LootItemBlockStatePropertyCondition) LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.FURNACE).build()), List.of(
                "Block State Property:",
                "  -> Block: Furnace"
        ));
        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(UTILS, 0, (LootItemBlockStatePropertyCondition) LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BAMBOO)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST)).build()), List.of(
                "Block State Property:",
                "  -> Block: Bamboo",
                "  -> State Properties:",
                "    -> facing: east"
        ));
    }

    @Test
    public void testDamageSourceProperties() {
        assertTooltip(ConditionTooltipUtils.getDamageSourcePropertiesTooltip(UTILS, 0, (DamageSourceCondition) DamageSourceCondition.hasDamageSource(
                DamageSourcePredicate.Builder.damageType()
                        .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                        .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                        .direct(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.WARDEN)))
                        .source(EntityPredicate.Builder.entity().team("Blue"))
        ).build()), List.of(
                "Damage Source Properties:",
                "  -> Damage Source:",
                "    -> Tags:",
                "      -> minecraft:bypasses_armor: true",
                "      -> minecraft:is_explosion: false",
                "    -> Direct Entity:",
                "      -> Entity Type: Warden",
                "    -> Source Entity:",
                "      -> Team: Blue"
        ));
    }

    @Test
    public void testEntityPropertiesTooltip() {
        assertTooltip(ConditionTooltipUtils.getEntityPropertiesTooltip(UTILS, 0, (LootItemEntityPropertyCondition) LootItemEntityPropertyCondition.hasProperties(
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
        assertTooltip(ConditionTooltipUtils.getEntityScoresTooltip(UTILS, 0, (EntityHasScoreCondition) EntityHasScoreCondition.hasScores(LootContext.EntityTarget.DIRECT_KILLER).build()), List.of(
                "Entity Scores:",
                "  -> Target: DIRECT_KILLER"
        ));
        assertTooltip(ConditionTooltipUtils.getEntityScoresTooltip(UTILS, 0, (EntityHasScoreCondition) EntityHasScoreCondition.hasScores(LootContext.EntityTarget.DIRECT_KILLER)
                .withScore("single", IntRange.range(2, 5))
                .withScore("double", IntRange.range(1, 7))
                .build()
        ), List.of(
                "Entity Scores:",
                "  -> Target: DIRECT_KILLER",
                "  -> Scores:",
                "    -> Score: single",
                "      -> Limit: 2 - 5",
                "    -> Score: double",
                "      -> Limit: 1 - 7"
        ));
    }

    @Test
    public void testInvertedTooltip() {
        assertTooltip(ConditionTooltipUtils.getInvertedTooltip(UTILS, 0, (InvertedLootItemCondition) InvertedLootItemCondition.invert(
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
        assertTooltip(ConditionTooltipUtils.getKilledByPlayerTooltip(UTILS, 0, (LootItemKilledByPlayerCondition) LootItemKilledByPlayerCondition.killedByPlayer().build()), List.of("Must be killed by player"));
    }

    @Test
    public void testLocationCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getLocationCheckTooltip(UTILS, 0, (LocationCheck) LocationCheck.checkLocation(
                LocationPredicate.Builder.location().setSmokey(true),
                new BlockPos(2, 4, 6)
        ).build()), List.of(
                "Location Check:",
                "  -> Location:",
                "    -> Smokey: true",
                "  -> Offset:",
                "    -> X: 2",
                "    -> Y: 4",
                "    -> Z: 6"
        ));
    }

    @Test
    public void testItemMatchTooltip() {
        assertTooltip(ConditionTooltipUtils.getMatchToolTooltip(UTILS, 0, (MatchTool) MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE)).build()), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> Item: Andesite",
                "    -> Item: Diorite"
        ));
    }

    @Test
    public void testRandomChanceTooltip() {
        assertTooltip(ConditionTooltipUtils.getRandomChanceTooltip(UTILS, 0, (LootItemRandomChanceCondition) LootItemRandomChanceCondition.randomChance(0.25F).build()), List.of(
                "Random Chance:",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testRandomChanceWithLootingTooltip() {
        assertTooltip(ConditionTooltipUtils.getRandomChanceWithLootingTooltip(UTILS, 0, (LootItemRandomChanceWithLootingCondition) LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.25F, 5F).build()), List.of(
                "Random Chance With Looting:",
                "  -> Percent: 0.25",
                "  -> Multiplier: 5.0"
        ));
    }

    @Test
    public void testReferenceTooltip() {
        assertTooltip(ConditionTooltipUtils.getReferenceTooltip(UTILS, 0, (ConditionReference) ConditionReference.conditionReference(new ResourceLocation("test")).build()), List.of("Reference: minecraft:test"));
    }

    @Test
    public void testSurvivesExplosionTooltip() {
        assertTooltip(ConditionTooltipUtils.getSurvivesExplosionTooltip(UTILS, 0, (ExplosionCondition) ExplosionCondition.survivesExplosion().build()), List.of("Must survive explosion"));
    }

    @Test
    public void testTableBonusTooltip() {
        assertTooltip(ConditionTooltipUtils.getTableBonusTooltip(UTILS, 0, (BonusLevelTableCondition) BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.MOB_LOOTING, 0.25F, 0.5555F, 0.99F).build()), List.of(
                "Table Bonus:",
                "  -> Enchantment: Looting",
                "  -> Values: [0.25, 0.5555, 0.99]" //FIXME to 2 decimal places
        ));
    }

    @Test
    public void testTimeCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getTimeCheckTooltip(UTILS, 0, TimeCheck.time(IntRange.range(5, 10)).setPeriod(24000).build()), List.of(
                "Time Check:",
                "  -> Period: 24000",
                "  -> Value: 5 - 10"
        ));
    }

    @Test
    public void testValueCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getValueCheckTooltip(UTILS, 0, (ValueCheckCondition) ValueCheckCondition.hasValue(UniformGenerator.between(1, 20), IntRange.range(1, 10)).build()), List.of(
                "Value Check:",
                "  -> Provider: 1-20",
                "  -> Range: 1 - 10"
        ));
    }

    @Test
    public void testWeatherCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, 0, WeatherCheck.weather().setRaining(true).setThundering(false).build()), List.of(
                "Weather Check:",
                "  -> Is Raining: true",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, 0, WeatherCheck.weather().setRaining(true).build()), List.of(
                "Weather Check:",
                "  -> Is Raining: true"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, 0, WeatherCheck.weather().setThundering(false).build()), List.of(
                "Weather Check:",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(UTILS, 0, WeatherCheck.weather().build()), List.of("Weather Check:"));
    }
}
