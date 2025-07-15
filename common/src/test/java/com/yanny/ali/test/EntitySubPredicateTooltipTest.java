package com.yanny.ali.test;

import com.yanny.ali.plugin.server.EntitySubPredicateTooltipUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                .checkAdvancementDone(ResourceLocation.withDefaultNamespace("test"), true)
                .addRecipe(ResourceLocation.withDefaultNamespace("test"), false)
                .checkAdvancementCriterions(ResourceLocation.withDefaultNamespace("criterion"), Map.of("test", true))
                .addStat(Stats.BLOCK_MINED, Blocks.COBBLESTONE.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(100))
                .addStat(Stats.ITEM_USED, Items.CHICKEN.builtInRegistryHolder(), MinMaxBounds.Ints.atMost(10))
                .setLookingAt(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.WARDEN)))
                .setGameType(GameTypePredicate.of(GameType.SURVIVAL))
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
                "      -> test: true",
                "  -> Looking At:",
                "    -> Entity Types:",
                "      -> minecraft:warden"
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

    @SuppressWarnings("unchecked")
    @Test
    public void testVariantPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getVariantPredicateTooltip(UTILS, (EntitySubPredicates.EntityVariantPredicateType<Axolotl>.Instance)EntitySubPredicates.AXOLOTL.createPredicate(Axolotl.Variant.BLUE)), List.of(
                "Type: minecraft:axolotl",
                "  -> Variant: BLUE"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHolderVariantPredicateTooltip() {
        assertTooltip(EntitySubPredicateTooltipUtils.getHolderVariantPredicateTooltip(UTILS, (EntitySubPredicates.EntityHolderVariantPredicateType<CatVariant>.Instance) EntitySubPredicates.catVariant(
                Holder.direct(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.CALICO)))
        )), List.of(
                "Type: minecraft:cat",
                "  -> Variants:",
                "    -> Variant: minecraft:calico"
        ));

        assertTooltip(EntitySubPredicateTooltipUtils.getHolderVariantPredicateTooltip(UTILS, (EntitySubPredicates.EntityHolderVariantPredicateType<CatVariant>.Instance) EntitySubPredicates.PAINTING.createPredicate(
                HolderSet.direct(LOOKUP.lookup(Registries.PAINTING_VARIANT).orElseThrow().get(PaintingVariants.BOMB).orElseThrow())
        )), List.of(
                "Type: minecraft:painting",
                "  -> Variants:",
                "    -> Variant: minecraft:bomb"
        ));

        assertTooltip(EntitySubPredicateTooltipUtils.getHolderVariantPredicateTooltip(UTILS, (EntitySubPredicates.EntityHolderVariantPredicateType<CatVariant>.Instance) EntitySubPredicates.frogVariant(
                Holder.direct(Objects.requireNonNull(BuiltInRegistries.FROG_VARIANT.get(FrogVariant.TEMPERATE)))
        )), List.of(
                "Type: minecraft:frog",
                "  -> Variants:",
                "    -> Variant: minecraft:temperate"
        ));

        assertTooltip(EntitySubPredicateTooltipUtils.getHolderVariantPredicateTooltip(UTILS, (EntitySubPredicates.EntityHolderVariantPredicateType<CatVariant>.Instance) EntitySubPredicates.wolfVariant(
                HolderSet.direct(LOOKUP.lookupOrThrow(Registries.WOLF_VARIANT).get(WolfVariants.ASHEN).orElseThrow())
        )), List.of(
                "Type: minecraft:wolf",
                "  -> Variants:",
                "    -> Variant: minecraft:ashen"
        ));
    }
}
