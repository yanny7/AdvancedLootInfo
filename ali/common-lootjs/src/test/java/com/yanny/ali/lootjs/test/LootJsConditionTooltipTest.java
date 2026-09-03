package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.ILootCondition;
import com.almostreliable.lootjs.filters.ItemFilter;
import com.almostreliable.lootjs.loot.condition.*;
import com.yanny.ali.lootjs.mixin.*;
import com.yanny.ali.lootjs.server.LootJsConditionTooltipUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.mock;
import static com.yanny.ali.lootjs.test.LootJsTestUtils.wrap;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootJsConditionTooltipTest {
    @Test
    public void testAndConditionTooltip() {
        ILootCondition[] children = {
                wrap(ExplosionCondition.survivesExplosion().build()),
                wrap(WeatherCheck.weather().setRaining(true).build())
        };
        AndCondition condition = mock(AndCondition.class, MixinAndCondition.class);

        Mockito.when(((MixinAndCondition) condition).getConditions()).thenReturn(children);
        assertTooltip(LootJsConditionTooltipUtils.andConditionTooltip(UTILS, condition).build(), List.of(
                "And:",
                "  -> Survives Explosion",
                "  -> Weather Check:",
                "    -> Is Raining: true"
        ));
    }

    @Test
    public void testAnyBiomeCheckTooltip() {
        AnyBiomeCheck condition = mock(AnyBiomeCheck.class, MixinBiomeCheck.class);

        Mockito.when(((MixinBiomeCheck) condition).getBiomes()).thenReturn(List.of(Biomes.PLAINS, Biomes.DESERT));
        Mockito.when(((MixinBiomeCheck) condition).getTags()).thenReturn(List.of(BiomeTags.IS_FOREST));
        assertTooltip(LootJsConditionTooltipUtils.anyBiomeCheckTooltip(UTILS, condition).build(), List.of(
                "Any Biome:",
                "  -> Biomes:",
                "    -> minecraft:plains",
                "    -> minecraft:desert",
                "  -> Tags:",
                "    -> minecraft:is_forest"
        ));
    }

    @Test
    public void testAnyDimensionTooltip() {
        AnyDimension condition = mock(AnyDimension.class, MixinAnyDimension.class);

        Mockito.when(((MixinAnyDimension) condition).getDimensions()).thenReturn(new ResourceLocation[]{
                new ResourceLocation("minecraft", "overworld"),
                new ResourceLocation("minecraft", "the_nether")
        });
        assertTooltip(LootJsConditionTooltipUtils.anyDimensionTooltip(UTILS, condition).build(), List.of(
                "Any Dimension:",
                "  -> Dimensions:",
                "    -> minecraft:overworld",
                "    -> minecraft:the_nether"
        ));
    }

    @Test
    public void testAnyStructureTooltip() {
        AnyStructure condition = mock(AnyStructure.class, MixinAnyStructure.class);

        Mockito.when(((MixinAnyStructure) condition).getStructureLocators()).thenReturn(List.of(
                new AnyStructure.ById(BuiltinStructures.IGLOO),
                new AnyStructure.ByTag(StructureTags.VILLAGE)
        ));
        Mockito.when(((MixinAnyStructure) condition).getExact()).thenReturn(true);
        assertTooltip(LootJsConditionTooltipUtils.anyStructureTooltip(UTILS, condition).build(), List.of(
                "Any Structure:",
                "  -> Structures:",
                "    -> minecraft:igloo",
                "    -> minecraft:village",
                "  -> Exact: true"
        ));
    }

    @Test
    public void testBiomeCheckTooltip() {
        BiomeCheck condition = mock(BiomeCheck.class, MixinBiomeCheck.class);

        Mockito.when(((MixinBiomeCheck) condition).getBiomes()).thenReturn(List.of(Biomes.JUNGLE));
        Mockito.when(((MixinBiomeCheck) condition).getTags()).thenReturn(List.of());
        assertTooltip(LootJsConditionTooltipUtils.biomeCheckTooltip(UTILS, condition).build(), List.of(
                "Biome:",
                "  -> Biomes:",
                "    -> minecraft:jungle"
        ));
    }

    @Test
    public void testContainsLootConditionTooltip() {
        ContainsLootCondition condition = mock(ContainsLootCondition.class, MixinContainsLootCondition.class);

        Mockito.when(((MixinContainsLootCondition) condition).getPredicate()).thenReturn(ItemFilter.SWORD);
        Mockito.when(((MixinContainsLootCondition) condition).getExact()).thenReturn(true);
        assertTooltip(LootJsConditionTooltipUtils.containsLootConditionTooltip(UTILS, condition).build(), List.of(
                "Match Loot:",
                "  -> Item Filter: SWORD",
                "  -> Exact: true"
        ));
    }

    @Test
    public void testCustomParamPredicateTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.THIS_ENTITY)).build(), List.of(
                "Entity Predicate:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.KILLER_ENTITY)).build(), List.of(
                "Killer Predicate:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.DIRECT_KILLER_ENTITY)).build(), List.of(
                "Direct Killer Predicate:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.BLOCK_ENTITY)).build(), List.of(
                "Block Predicate:",
                "  -> Detail Not Available"
        ));
        assertTooltip(LootJsConditionTooltipUtils.customParamPredicateTooltip(UTILS, customParamPredicate(LootContextParams.ORIGIN)).build(), List.of());
    }

    @Test
    public void testIsLightLevelTooltip() {
        IsLightLevel condition = mock(IsLightLevel.class, MixinIsLightLevel.class);

        Mockito.when(((MixinIsLightLevel) condition).getMin()).thenReturn(3);
        Mockito.when(((MixinIsLightLevel) condition).getMax()).thenReturn(7);
        assertTooltip(LootJsConditionTooltipUtils.isLightLevelTooltip(UTILS, condition).build(), List.of(
                "Light Level:",
                "  -> Value: 3 - 7"
        ));
    }

    @Test
    public void testLootItemConditionWrapperTooltip() {
        LootItemConditionWrapper condition = mock(LootItemConditionWrapper.class, MixinLootItemConditionWrapper.class);

        Mockito.when(((MixinLootItemConditionWrapper) condition).getCondition()).thenReturn(WeatherCheck.weather().setThundering(false).build());
        assertTooltip(LootJsConditionTooltipUtils.lootItemConditionWrapperTooltip(UTILS, condition).build(), List.of(
                "Weather Check:",
                "  -> Is Thundering: false"
        ));
    }

    @Test
    public void testMainHandTableBonusTooltip() {
        MainHandTableBonus condition = mock(MainHandTableBonus.class, MixinMainHandTableBonus.class);

        Mockito.when(((MixinMainHandTableBonus) condition).getEnchantment()).thenReturn(Enchantments.BLOCK_FORTUNE);
        Mockito.when(((MixinMainHandTableBonus) condition).getValues()).thenReturn(new float[]{0.1F, 0.5F, 1.0F});
        assertTooltip(LootJsConditionTooltipUtils.mainHandTableBonusTooltip(UTILS, condition).build(), List.of(
                "Random Chance With Enchantment:",
                "  -> Enchantment: minecraft:fortune",
                "  -> Values: [0.1, 0.5, 1.0]"
        ));
    }

    @Test
    public void testMatchEquipmentSlotTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.MAINHAND)).build(), List.of(
                "Match Mainhand:",
                "  -> Item Filter: AXE"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.OFFHAND)).build(), List.of(
                "Match Offhand:",
                "  -> Item Filter: AXE"
        ));
        assertTooltip(LootJsConditionTooltipUtils.getMatchEquipmentSlotTooltip(UTILS, matchEquipmentSlot(EquipmentSlot.HEAD)).build(), List.of(
                "Match Equipment Slot:",
                "  -> Item Filter: AXE",
                "  -> Slot: Head"
        ));
    }

    @Test
    public void testMatchKillerDistanceTooltip() {
        MatchKillerDistance condition = mock(MatchKillerDistance.class, MixinMatchKillerDistance.class);

        Mockito.when(((MixinMatchKillerDistance) condition).getPredicate()).thenReturn(new DistancePredicate(
                MinMaxBounds.Doubles.exactly(10),
                MinMaxBounds.Doubles.ANY,
                MinMaxBounds.Doubles.ANY,
                MinMaxBounds.Doubles.ANY,
                MinMaxBounds.Doubles.atMost(5)
        ));
        assertTooltip(LootJsConditionTooltipUtils.matchKillerDistanceTooltip(UTILS, condition).build(), List.of(
                "Distance To Killer:",
                "  -> Predicate:",
                "    -> X: =10.0",
                "    -> Absolute: ≤5.0"
        ));
    }

    @Test
    public void testMatchPlayerTooltip() {
        MatchPlayer condition = mock(MatchPlayer.class, MixinMatchPlayer.class);

        Mockito.when(((MixinMatchPlayer) condition).getPredicate()).thenReturn(EntityPredicate.Builder.entity().of(EntityType.PLAYER).team("blue").build());
        assertTooltip(LootJsConditionTooltipUtils.matchPlayerTooltip(UTILS, condition).build(), List.of(
                "Match Player:",
                "  -> Predicate:",
                "    -> Entity Type: minecraft:player",
                "    -> Team: blue"
        ));
    }

    @Test
    public void testNotConditionTooltip() {
        ILootCondition child = wrap(ExplosionCondition.survivesExplosion().build());
        NotCondition condition = mock(NotCondition.class, MixinNotCondition.class);

        Mockito.when(((MixinNotCondition) condition).getCondition()).thenReturn(child);
        assertTooltip(LootJsConditionTooltipUtils.notConditionTooltip(UTILS, condition).build(), List.of(
                "Not:",
                "  -> Survives Explosion"
        ));
    }

    @Test
    public void testOrConditionTooltip() {
        ILootCondition[] children = {
                wrap(ExplosionCondition.survivesExplosion().build()),
                wrap(WeatherCheck.weather().setRaining(true).build())
        };
        OrCondition condition = mock(OrCondition.class, MixinOrCondition.class);

        Mockito.when(((MixinOrCondition) condition).getConditions()).thenReturn(children);
        assertTooltip(LootJsConditionTooltipUtils.orConditionTooltip(UTILS, condition).build(), List.of(
                "Or:",
                "  -> Survives Explosion",
                "  -> Weather Check:",
                "    -> Is Raining: true"
        ));
    }

    @Test
    public void testPlayerParamPredicateTooltip() {
        assertTooltip(LootJsConditionTooltipUtils.playerParamPredicateTooltip(UTILS, new PlayerParamPredicate((s) -> true)).build(), List.of(
                "Player Predicate:",
                "  -> Detail Not Available"
        ));
    }

    @Test
    public void testWrappedDamageSourceConditionTooltip() {
        WrappedDamageSourceCondition condition = mock(WrappedDamageSourceCondition.class, MixinWrappedDamageSourceCondition.class);

        Mockito.when(((MixinWrappedDamageSourceCondition) condition).getPredicate()).thenReturn(DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .build());
        Mockito.when(((MixinWrappedDamageSourceCondition) condition).getSourceNames()).thenReturn(new String[]{"minecraft:on_fire", "minecraft:lava"});
        assertTooltip(LootJsConditionTooltipUtils.wrapperDamageSourceConditionTooltip(UTILS, condition).build(), List.of(
                "Match Damage Source:",
                "  -> Predicate:",
                "    -> Tags:",
                "      -> minecraft:bypasses_armor: true",
                "  -> Source Names:",
                "    -> minecraft:on_fire",
                "    -> minecraft:lava"
        ));
    }

    private static CustomParamPredicate<?> customParamPredicate(LootContextParam<?> param) {
        CustomParamPredicate<?> condition = mock(CustomParamPredicate.class, MixinCustomParamPredicate.class);

        Mockito.doReturn(param).when((MixinCustomParamPredicate<?>) condition).getParam();
        return condition;
    }

    private static MatchEquipmentSlot matchEquipmentSlot(EquipmentSlot slot) {
        MatchEquipmentSlot condition = mock(MatchEquipmentSlot.class, MixinMatchEquipmentSlot.class);

        Mockito.when(((MixinMatchEquipmentSlot) condition).getPredicate()).thenReturn(ItemFilter.AXE);
        Mockito.when(((MixinMatchEquipmentSlot) condition).getSlot()).thenReturn(slot);
        return condition;
    }
}
