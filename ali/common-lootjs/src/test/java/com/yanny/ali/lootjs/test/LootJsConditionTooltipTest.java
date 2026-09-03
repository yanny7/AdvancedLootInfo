package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.lootjs.mixin.MixinCustomParamPredicate;
import com.yanny.ali.lootjs.server.LootJsConditionTooltipUtils;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.mock;
import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsConditionTooltipTest {
    @Test
    public void testMatchBiomeTooltip() {
        MatchBiome condition = new MatchBiome(HolderSet.direct(
                LOOKUP.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS),
                LOOKUP.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.DESERT)
        ));

        assertTooltip(LootJsConditionTooltipUtils.matchBiomeTooltip(UTILS, condition).build(), List.of(
                "Match Biome:",
                "  -> Biomes:",
                "    -> minecraft:plains",
                "    -> minecraft:desert"
        ));
    }

    @Test
    public void testMatchDimensionTooltip() {
        MatchDimension condition = new MatchDimension(new ResourceLocation[]{
                ResourceLocation.withDefaultNamespace("overworld"),
                ResourceLocation.withDefaultNamespace("the_nether")
        });

        assertTooltip(LootJsConditionTooltipUtils.matchDimensionTooltip(UTILS, condition).build(), List.of(
                "Match Dimension:",
                "  -> Dimensions:",
                "    -> minecraft:overworld",
                "    -> minecraft:the_nether"
        ));
    }

    @Test
    public void testMatchStructureTooltip() {
        MatchStructure condition = new MatchStructure(HolderSet.direct(
                LOOKUP.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.IGLOO),
                LOOKUP.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT)
        ), true);

        assertTooltip(LootJsConditionTooltipUtils.matchStructureTooltip(UTILS, condition).build(), List.of(
                "Match Structure:",
                "  -> Structures:",
                "    -> minecraft:igloo",
                "    -> minecraft:mineshaft",
                "  -> Exact: true"
        ));
    }

    @Test
    public void testCustomParamPredicateTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.THIS_ENTITY)).build(), List.of(
                "Match Entity Custom:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.ATTACKING_ENTITY)).build(), List.of(
                "Match Attacker Custom:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.DIRECT_ATTACKING_ENTITY)).build(), List.of(
                "Match Direct Attacker Custom:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.BLOCK_ENTITY)).build(), List.of(
                "Block Entity:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.ORIGIN)).build(), List.of());
    }

    @Test
    public void testIsLightLevelTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.isLightLevelTooltip(UTILS, new IsLightLevel(3, 7)).build(), List.of(
                "Is Light Level:",
                "  -> Value: 3 - 7"
        ));
    }

    @Test
    public void testMatchEquipmentSlotTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.MAINHAND)).build(), List.of(
                "Match Mainhand:",
                "  -> Item Filter: ARMOR"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.OFFHAND)).build(), List.of(
                "Match Offhand:",
                "  -> Item Filter: ARMOR"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.FEET)).build(), List.of(
                "Match Feet:",
                "  -> Item Filter: ARMOR"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.LEGS)).build(), List.of(
                "Match Legs:",
                "  -> Item Filter: ARMOR"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.CHEST)).build(), List.of(
                "Match Chest:",
                "  -> Item Filter: ARMOR"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.HEAD)).build(), List.of(
                "Match Head:",
                "  -> Item Filter: ARMOR"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.BODY)).build(), List.of(
                "Match Equipment Slot:",
                "  -> Item Filter: ARMOR",
                "  -> Slot: Body"
        ));
    }

    @Test
    public void testMatchKillerDistanceTooltip() {
        MatchKillerDistance condition = new MatchKillerDistance(new DistancePredicate(
                MinMaxBounds.Doubles.exactly(10),
                MinMaxBounds.Doubles.ANY,
                MinMaxBounds.Doubles.ANY,
                MinMaxBounds.Doubles.ANY,
                MinMaxBounds.Doubles.atMost(5)
        ));

        assertTooltip(LootJsConditionTooltipUtils.matchKillerDistanceTooltip(UTILS, condition).build(), List.of(
                "Match Distance:",
                "  -> Predicate:",
                "    -> X: =10.0",
                "    -> Absolute: ≤5.0"
        ));
    }

    @Test
    public void testMatchPlayerTooltip() {
        MatchPlayer condition = new MatchPlayer(EntityPredicate.Builder.entity().of(EntityType.PLAYER).team("blue").build());

        assertTooltip(LootJsConditionTooltipUtils.matchPlayerTooltip(UTILS, condition).build(), List.of(
                "Match Player:",
                "  -> Predicate:",
                "    -> Entity Type: minecraft:player",
                "    -> Team: blue"
        ));
    }

    @Test
    public void testPlayerParamPredicateTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.playerParamPredicateTooltip(UTILS, new PlayerParamPredicate((player) -> true)).build(), List.of(
                "Match Player Custom:",
                "  -> Detail Not Available"
        ));
    }

    @Test
    public void testMatchAnyInventorySlotTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.matchAnyInventorySlot(UTILS, new MatchAnyInventorySlot(ItemFilter.ENCHANTED, true)).build(), List.of(
                "Match Any Inventory Slot:",
                "  -> Item Filter: ENCHANTED",
                "  -> Hotbar: true"
        ));
    }

    private static CustomParamPredicate<?> customParamPredicate(LootContextParam<?> param) {
        CustomParamPredicate<?> condition = mock(CustomParamPredicate.class, MixinCustomParamPredicate.class);

        Mockito.doReturn(param).when((MixinCustomParamPredicate<?>) condition).getParam();
        return condition;
    }

    private static MatchEquipmentSlot matchEquipmentSlot(EquipmentSlot slot) {
        return new MatchEquipmentSlot(slot, ItemFilter.ARMOR);
    }
}
