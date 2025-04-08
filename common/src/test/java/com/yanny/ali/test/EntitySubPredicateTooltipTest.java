package com.yanny.ali.test;

import com.yanny.ali.plugin.EntitySubPredicateTooltipUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.GameType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class EntitySubPredicateTooltipTest {
    @Test
    public void testLightningBoltPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getLightningBoltPredicateTooltip(UTILS, 0, LightningBoltPredicate.blockSetOnFire(MinMaxBounds.Ints.atMost(5))), List.of(
                "Lightning Bolt:",
                "  -> Blocks On Fire: ≤5"
        ));
        assertTooltip(EntitySubPredicateTooltipUtils.getLightningBoltPredicateTooltip(UTILS, 0,
                new LightningBoltPredicate(MinMaxBounds.Ints.between(1, 5), Optional.of(EntityPredicate.Builder.entity().team("blue").build()))), List.of(
                "Lightning Bolt:",
                "  -> Blocks On Fire: 1-5",
                "  -> Stuck Entity:",
                "    -> Team: blue"
        ));
    }

    @Test
    public void testFishingHookPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getFishingHookPredicateTooltip(UTILS, 0, FishingHookPredicate.ANY), List.of(
                "Fishing Hook:"
        ));
        assertTooltip(EntitySubPredicateTooltipUtils.getFishingHookPredicateTooltip(UTILS, 0, FishingHookPredicate.inOpenWater(true)), List.of(
                "Fishing Hook:",
                "  -> Is In Open Water: true"
        ));
    }

    @Test
    public void testPlayerPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getPlayerPredicateTooltip(UTILS, 0, PlayerPredicate.Builder.player()
                .checkAdvancementDone(new ResourceLocation("test"), true)
                .addRecipe(new ResourceLocation("test"), false)
                .setGameType(GameType.SURVIVAL)
                .build()), List.of(
                "Player:",
                "  -> Game Type: Survival",
                "  -> Recipes:",
                "    -> minecraft:test: false",
                "  -> Advancements:",
                "    -> minecraft:test",
                "      -> Done: true"
        ));
    }

    @Test
    public void testSlimePredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getSlimePredicateTooltip(UTILS, 0, SlimePredicate.sized(MinMaxBounds.Ints.between(0, 2))), List.of(
                "Slime:",
                "  -> Size: 0-2"
        ));
    }

    @Test
    public void testRaiderPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getRaiderPredicateTooltip(UTILS, 0, RaiderPredicate.CAPTAIN_WITHOUT_RAID), List.of(
                "Raider:",
                "  -> Has Raid: false",
                "  -> Is Captain: true"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testVariantPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getVariantPredicateTooltip(UTILS, 0, (EntitySubPredicates.EntityVariantPredicateType<Axolotl>.Instance)EntitySubPredicates.AXOLOTL.createPredicate(Axolotl.Variant.BLUE)), List.of(
                "Type: minecraft:axolotl",
                "  -> Variant: BLUE"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHolderVariantPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getHolderVariantPredicateTooltip(UTILS, 0, (EntitySubPredicates.EntityHolderVariantPredicateType<CatVariant>.Instance) EntitySubPredicates.catVariant(
                Holder.direct(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.CALICO)))
        )), List.of(
                "Type: minecraft:cat",
                "  -> Variants:",
                "    -> Variant: minecraft:calico"
        ));
    }
}
