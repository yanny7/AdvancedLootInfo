package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import com.yanny.ali.plugin.client.GenericTooltipUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.pad;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.utils.TestUtils.assertUnorderedTooltip;

public class GenericTooltipTest {
    @Test
    public void testRangeValue() {
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(123))), "123");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(1, 5))), "1-5");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(-1, 3))), "-1-3");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(456, 789))), "456-789");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(2.5F))), "2.50");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(2.5F, 3.6F))), "2.50-3.60");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(true, false))), "1[+Score]");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(false, true))), "1[+???]");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(true, true))), "1[+Score][+???]");
    }

    @Test
    public void testPad() {
        assertTooltip(pad(0, 123), "123");
        assertTooltip(pad(1, 123), "  -> 123");
        assertTooltip(pad(2, 123), "    -> 123");
        assertTooltip(pad(3, 123), "      -> 123");
        assertTooltip(pad(4, 123), "        -> 123");
        assertTooltip(pad(5, 123), "          -> 123");
        assertTooltip(pad(6, 123), "            -> 123");
        assertTooltip(pad(7, 123), "              -> 123");
        assertTooltip(pad(8, 123), "                -> 123");
        assertTooltip(pad(9, 123), "                  -> 123");
        assertTooltip(pad(10, 123), "                    -> 123");

        assertTooltip(pad(1, Component.literal("Hello")), "  -> Hello");
    }

    @Test
    public void testTooltip() {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap = EntryTooltipUtils.getBaseMap(2.5F);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> countMap = EntryTooltipUtils.getBaseMap(2, 10);
        Map<Integer, RangeValue> bonusChanceMap = new LinkedHashMap<>();
        Map<Integer, RangeValue> bonusCountMap = new LinkedHashMap<>();

        bonusChanceMap.put(1, new RangeValue(1.5F));
        bonusChanceMap.put(2, new RangeValue(3F));
        bonusChanceMap.put(3, new RangeValue(4.5F));
        bonusCountMap.put(1, new RangeValue(1, 2));
        bonusCountMap.put(2, new RangeValue(2, 4));
        bonusCountMap.put(3, new RangeValue(4, 8));
        chanceMap.put(Holder.direct(Enchantments.MOB_LOOTING), bonusChanceMap);
        countMap.put(Holder.direct(Enchantments.BLOCK_FORTUNE), bonusCountMap);

        assertTooltip(EntryTooltipUtils.getTooltip(
                UTILS,
                AlternativesEntry.alternatives().build(),
                EntryTooltipUtils.getBaseMap(2.5F),
                EntryTooltipUtils.getBaseMap(2, 10),
                List.of(),
                List.of()
        ), List.of(
                "Chance: 2.50%",
                "Count: 2-10"
        ));
        assertTooltip(EntryTooltipUtils.getTooltip(
                UTILS,
                EmptyLootItem.emptyItem().setQuality(5).build(),
                chanceMap,
                countMap,
                List.of(ApplyExplosionDecay.explosionDecay().when(ExplosionCondition.survivesExplosion()).build(), SmeltItemFunction.smelted().build()),
                List.of(LootItemKilledByPlayerCondition.killedByPlayer().build())
        ), List.of(
                "Quality: 5",
                "Chance: 2.50%",
                "  -> 1.50% (Looting I)",
                "  -> 3% (Looting II)",
                "  -> 4.50% (Looting III)",
                "Count: 2-10",
                "  -> 1-2 (Fortune I)",
                "  -> 2-4 (Fortune II)",
                "  -> 4-8 (Fortune III)",
                "----- Predicates -----",
                "Must be killed by player",
                "----- Modifiers -----",
                "Explosion Decay",
                "  -> Predicates:",
                "    -> Must survive explosion",
                "Use Smelting Recipe On Item"
        ));
    }

    @Test
    public void testFormulaTooltip() {
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, 0, "ali.property.value.formula", new ApplyBonusCount.OreDrops()), List.of(
                "Formula: minecraft:ore_drops"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, 0, "ali.property.value.formula", new ApplyBonusCount.UniformBonusCount(2)), List.of(
                "Formula: minecraft:uniform_bonus_count",
                "  -> Bonus Multiplier: 2"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, 0, "ali.property.value.formula", new ApplyBonusCount.BinomialWithBonusCount(3, 0.51F)), List.of(
                "Formula: minecraft:binomial_with_bonus_count",
                "  -> Extra Rounds: 3",
                "  -> Probability: 0.51"
        ));
    }

    @Test
    public void testPropertyTooltip() {
        assertTooltip(GenericTooltipUtils.getPropertyTooltip(UTILS, 1, "ali.property.value.null", EnumProperty.create("bed", BedPart.class)), List.of("  -> bed"));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getModifierTooltip(UTILS, 0, "ali.property.branch.modifier", new SetAttributesFunction.ModifierBuilder(
                "armor",
                Holder.direct(Attributes.ARMOR),
                AttributeModifier.Operation.MULTIPLY_TOTAL,
                UniformGenerator.between(1, 5))
                        .forSlot(EquipmentSlot.HEAD)
                        .forSlot(EquipmentSlot.CHEST)
                        .forSlot(EquipmentSlot.LEGS)
                        .forSlot(EquipmentSlot.FEET)
                        .build()), List.of(
                "Modifier:",
                "  -> Name: armor",
                "  -> Attribute: minecraft:generic.armor",
                "  -> Operation: MULTIPLY_TOTAL",
                "  -> Amount: 1-5",
                "  -> Equipment Slots:",
                "    -> FEET",
                "    -> LEGS",
                "    -> CHEST",
                "    -> HEAD"
        ));
    }

    @Test
    public void testUUIDTooltip() {
        assertTooltip(GenericTooltipUtils.getUUIDTooltip(UTILS, 0, "ali.property.value.uuid", UUID.nameUUIDFromBytes(new byte[]{1, 2, 3})), List.of("UUID: 5289df73-7df5-3326-bcdd-22597afb1fac"));
    }

    @Test
    public void testBannerPatternsSlotsTooltip() {
        assertTooltip(GenericTooltipUtils.getBannerPatternTooltip(UTILS, 0, "ali.property.value.banner_pattern", Pair.of(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.BASE))), DyeColor.WHITE)), List.of(
                "Banner Pattern: minecraft:base",
                "  -> Color: WHITE"
        ));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, 0, "ali.property.branch.properties", StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockStateProperties.FACING, Direction.EAST)
                .hasProperty(BlockStateProperties.LEVEL, 3)
                .build().orElseThrow()
        ), List.of(
                "Properties:",
                "  -> facing: east",
                "  -> level: 3"
        ));
    }

    @Disabled("internal function")
    @Test
    public void testPropertyMatcherTooltip() {}

    @Test
    void testDamageSourcePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(UTILS, 0, "ali.property.branch.predicate", DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                .source(EntityPredicate.Builder.entity().of(EntityType.BAT))
                .direct(EntityPredicate.Builder.entity().of(EntityType.ARROW))
                .build()), List.of(
                "Predicate:",
                "  -> Tags:",
                "    -> minecraft:bypasses_armor: true",
                "    -> minecraft:is_explosion: false",
                "  -> Direct Entity:",
                "    -> Entity Types:",
                "      -> minecraft:arrow",
                "  -> Source Entity:",
                "    -> Entity Types:",
                "      -> minecraft:bat"
        ));
    }

    @Test
    public void testTagPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getTagPredicateTooltip(UTILS, 0, "ali.property.value.null", TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR)), List.of("minecraft:bypasses_armor: true"));
        assertTooltip(GenericTooltipUtils.getTagPredicateTooltip(UTILS, 0, "ali.property.value.null", TagPredicate.isNot(DamageTypeTags.BYPASSES_ARMOR)), List.of("minecraft:bypasses_armor: false"));
    }

    @Test
    public void testEntityPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("range", 5);
        assertTooltip(GenericTooltipUtils.getEntityPredicateTooltip(UTILS, 0, "ali.property.branch.predicate", EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(EntityType.CAT))
                .distance(new DistancePredicate(MinMaxBounds.Doubles.exactly(10), MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY))
                .located(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atLeast(20)))
                .steppingOn(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atMost(30)))
                .effects(MobEffectsPredicate.Builder.effects()
                        .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.of(true), Optional.empty()))
                        .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.of(false)))
                )
                .nbt(new NbtPredicate(compoundTag))
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true))
                .equipment(EntityEquipmentPredicate.Builder.equipment().head(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE)).build())
                .subPredicate(EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.CALICO))))
                .vehicle(EntityPredicate.Builder.entity().team("blue"))
                .passenger(EntityPredicate.Builder.entity().team("white"))
                .targetedEntity(EntityPredicate.Builder.entity().team("red"))
                .team("orange")
                .build()
        ), List.of(
                "Predicate:",
                "  -> Entity Types:",
                "    -> minecraft:cat",
                "  -> Distance to Player:",
                "    -> X: =10.0",
                "  -> Location:",
                "    -> Position:",
                "      -> X: ≥20.0",
                "  -> Stepping on Location:",
                "    -> Position:",
                "      -> X: ≤30.0",
                "  -> Mob Effects:",
                "    -> minecraft:absorption",
                "      -> Is Ambient: true",
                "    -> minecraft:blindness",
                "      -> Is Visible: false",
                "  -> Nbt: {range:5}",
                "  -> Entity Flags:",
                "    -> Is Baby: true",
                "  -> Entity Equipment:",
                "    -> Head:",
                "      -> Items:",
                "        -> minecraft:andesite",
                "        -> minecraft:diorite",
                "  -> Entity Sub Predicate:",
                "    -> Variant: minecraft:calico",
                "  -> Vehicle:",
                "    -> Team: blue",
                "  -> Passenger:",
                "    -> Team: white",
                "  -> Targeted Entity:",
                "    -> Team: red",
                "  -> Team: orange"
        ));
    }

    @Test
    public void testEntityTypePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, 0, "ali.property.branch.entity_types", EntityTypePredicate.of(EntityType.CAT)), List.of(
                "Entity Types:",
                "  -> minecraft:cat"
        ));
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, 0, "ali.property.branch.entity_types", EntityTypePredicate.of(EntityTypeTags.SKELETONS)), List.of(
                "Entity Types:",
                "  -> Tag: minecraft:skeletons"
        ));
    }

    @Test
    public void testDistancePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDistancePredicateTooltip(UTILS, 0, "ali.property.branch.distance_to_player", new DistancePredicate(
                MinMaxBounds.Doubles.exactly(10),
                MinMaxBounds.Doubles.atLeast(20),
                MinMaxBounds.Doubles.atMost(30),
                MinMaxBounds.Doubles.atLeast(15),
                MinMaxBounds.Doubles.between(2, 5.5)
        )), List.of(
                "Distance to Player:",
                "  -> X: =10.0",
                "  -> Y: ≥20.0",
                "  -> Z: ≤30.0",
                "  -> Horizontal: ≥15.0",
                "  -> Absolute: 2.0-5.5"
        ));
    }

    @Test
    public void testLocationPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getLocationPredicateTooltip(UTILS, 0, "ali.property.branch.location", LocationPredicate.Builder.location()
                .setX(MinMaxBounds.Doubles.exactly(10))
                .setY(MinMaxBounds.Doubles.atLeast(20))
                .setZ(MinMaxBounds.Doubles.atMost(30))
                .setBiome(Biomes.PLAINS)
                .setStructure(BuiltinStructures.MINESHAFT)
                .setDimension(Level.OVERWORLD)
                .setSmokey(true)
                .setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)))
                .setBlock(BlockPredicate.Builder.block().of(Blocks.STONE, Blocks.COBBLESTONE))
                .setFluid(FluidPredicate.Builder.fluid().of(Fluids.LAVA))
                .build()
        ), List.of(
                "Location:",
                "  -> Position:",
                "    -> X: =10.0",
                "    -> Y: ≥20.0",
                "    -> Z: ≤30.0",
                "  -> Biome: minecraft:plains",
                "  -> Structure: minecraft:mineshaft",
                "  -> Dimension: minecraft:overworld",
                "  -> Smokey: true",
                "  -> Light: 10-15",
                "  -> Block Predicate:",
                "    -> Blocks:",
                "      -> minecraft:stone",
                "      -> minecraft:cobblestone",
                "  -> Fluid Predicate:",
                "    -> Fluid: minecraft:lava"
        ));
    }

    @Test
    public void testPositionPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getPositionPredicateTooltip(UTILS, 0, "ali.property.branch.position", new LocationPredicate.PositionPredicate(
                MinMaxBounds.Doubles.atLeast(3),
                MinMaxBounds.Doubles.between(1, 2),
                MinMaxBounds.Doubles.atMost(4)
        )), List.of(
                "Position:",
                "  -> X: ≥3.0",
                "  -> Y: 1.0-2.0",
                "  -> Z: ≤4.0"
        ));
    }

    @Test
    public void testLightPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getLightPredicateTooltip(UTILS, 0, "ali.property.value.light", LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)).build()), List.of("Light: 10-15"));
    }

    @Test
    public void testBlockPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(BlockTags.DIRT).build()), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:dirt"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(BlockTags.BEDS).build()), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:beds"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block()
                .of(Blocks.STONE, Blocks.COBBLESTONE)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST))
                .hasNbt(compoundTag)
                .build()
        ), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> minecraft:stone",
                "    -> minecraft:cobblestone",
                "  -> Properties:",
                "    -> facing: east",
                "  -> Nbt: {test:3.0f}"
        ));
    }

    @Test
    public void testNbtPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(UTILS, 0, "ali.property.value.nbt", new NbtPredicate(compoundTag)), List.of("Nbt: {test:3.0f}"));
    }

    @Test
    public void testFluidPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(UTILS, 0, "ali.property.branch.fluid_predicate", FluidPredicate.Builder.fluid()
                .of(FluidTags.WATER)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build().orElseThrow())
                .build()
        ), List.of(
                "Fluid Predicate:",
                "  -> Tag: minecraft:water",
                "  -> Properties:",
                "    -> facing: east"
        ));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectPredicateTooltip(UTILS, 0, "ali.property.branch.mob_effects", MobEffectsPredicate.Builder.effects()
                .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.between(10, 15), MinMaxBounds.Ints.atMost(5), Optional.of(true), Optional.of(false)))
                .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.atLeast(5), MinMaxBounds.Ints.between(1, 2), Optional.empty(), Optional.empty())).build().orElseThrow()
        ), List.of(
                "Mob Effects:",
                "  -> minecraft:absorption",
                "    -> Amplifier: 10-15",
                "    -> Duration: ≤5",
                "    -> Is Ambient: true",
                "    -> Is Visible: false",
                "  -> minecraft:blindness",
                "    -> Amplifier: ≥5",
                "    -> Duration: 1-2"
        ));
    }

    @Test
    public void testMobEffectInstancePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectInstancePredicateTooltip(UTILS, 0, new MobEffectsPredicate.MobEffectInstancePredicate(
                MinMaxBounds.Ints.between(10, 15),
                MinMaxBounds.Ints.atMost(5),
                Optional.of(true),
                Optional.of(false))
        ), List.of(
                "Amplifier: 10-15",
                "Duration: ≤5",
                "Is Ambient: true",
                "Is Visible: false"
        ));
    }

    @Test
    public void testEntityFlagsPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntityFlagsPredicateTooltip(UTILS, 0, "ali.property.branch.entity_flags", EntityFlagsPredicate.Builder.flags()
                .setOnFire(false)
                .setIsBaby(true)
                .setCrouching(true)
                .setSprinting(true)
                .setSwimming(false)
                .build()
        ), List.of(
                "Entity Flags:",
                "  -> Is On Fire: false",
                "  -> Is Baby: true",
                "  -> Is Crouching: true",
                "  -> Is Sprinting: true",
                "  -> Is Swimming: false"
        ));
    }

    @Test
    public void testEntityEquipmentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntityEquipmentPredicateTooltip(UTILS, 0, "ali.property.branch.entity_equipment", EntityEquipmentPredicate.Builder.equipment()
                .head(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(10, 15)))
                .chest(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atMost(5)))
                .legs(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.atLeast(5)))
                .feet(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.between(1, 2)))
                .mainhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(0, 64)))
                .offhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(32)))
                .build()), List.of(
            "Entity Equipment:",
            "  -> Head:",
            "    -> Count: 10-15",
            "  -> Chest:",
            "    -> Count: ≤5",
            "  -> Legs:",
            "    -> Durability: ≥5",
            "  -> Feet:",
            "    -> Durability: 1-2",
            "  -> Main Hand:",
            "    -> Count: 0-64",
            "  -> Offhand:",
            "    -> Count: ≥32"
        ));
    }

    @Test
    public void testItemPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putBoolean("healing", true);

        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, 0, "ali.type.condition.match_tool", ItemPredicate.Builder.item().of(ItemTags.AXES).build()), List.of(
                "Match Tool:",
                "  -> Tag: minecraft:axes"
        ));
        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, 0, "ali.type.condition.match_tool", ItemPredicate.Builder.item()
                .of(Items.CAKE, Items.NETHERITE_AXE)
                .withCount(MinMaxBounds.Ints.between(10, 15))
                .hasDurability(MinMaxBounds.Ints.atMost(5))
                .hasEnchantment(new EnchantmentPredicate(Enchantments.SMITE, MinMaxBounds.Ints.atLeast(1)))
                .hasEnchantment(new EnchantmentPredicate(Enchantments.MENDING, MinMaxBounds.Ints.between(2, 4)))
                .hasStoredEnchantment(new EnchantmentPredicate(Enchantments.DEPTH_STRIDER, MinMaxBounds.Ints.atMost(5)))
                .hasStoredEnchantment(new EnchantmentPredicate(Enchantments.FISHING_SPEED, MinMaxBounds.Ints.atLeast(4)))
                .isPotion(Potions.HEALING)
                .hasNbt(compoundTag)
                .build()
        ), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> minecraft:cake",
                "    -> minecraft:netherite_axe",
                "  -> Count: 10-15",
                "  -> Durability: ≤5",
                "  -> Enchantments:",
                "    -> minecraft:smite",
                "      -> Level: ≥1",
                "    -> minecraft:mending",
                "      -> Level: 2-4",
                "  -> Stored Enchantments:",
                "    -> minecraft:depth_strider",
                "      -> Level: ≤5",
                "    -> minecraft:lure",
                "      -> Level: ≥4",
                "  -> Potion: minecraft:healing",
                "  -> Nbt: {healing:1b}"
        ));
    }

    @Test
    public void testEnchantmentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(UTILS, 0, "ali.property.value.enchantment", new EnchantmentPredicate(Enchantments.FALL_PROTECTION, MinMaxBounds.Ints.atMost(2))), List.of(
                "Enchantment: minecraft:feather_falling",
                "  -> Level: ≤2"
        ));
    }

    @Test
    public void testEntitySubPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.branch.entity_sub_predicate", EntitySubPredicate.variant(FrogVariant.COLD)), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:cold"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.branch.entity_sub_predicate", EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.PERSIAN)))), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:persian"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.branch.entity_sub_predicate", new LightningBoltPredicate(MinMaxBounds.Ints.atLeast(2), Optional.of(EntityPredicate.Builder.entity().team("blue").build()))), List.of(
                "Entity Sub Predicate:",
                "  -> Blocks On Fire: ≥2",
                "  -> Stuck Entity:",
                "    -> Team: blue"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.branch.entity_sub_predicate", FishingHookPredicate.inOpenWater(true)), List.of(
                "Entity Sub Predicate:",
                "  -> Is In Open Water: true"
        ));
        //noinspection deprecation
        assertUnorderedTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.branch.entity_sub_predicate", PlayerPredicate.Builder.player()
                .setLevel(MinMaxBounds.Ints.atLeast(3))
                .setGameType(GameType.SURVIVAL)
                .addStat(Stats.BLOCK_MINED, Blocks.COBBLESTONE.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(4))
                .addStat(Stats.ITEM_USED, Items.SALMON.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(5))
                .addRecipe(new ResourceLocation("recipe1"), true)
                .addRecipe(new ResourceLocation("recipe2"), false)
                .checkAdvancementDone(new ResourceLocation("first"), true)
                .checkAdvancementCriterions(new ResourceLocation("second"), Map.of("test", false))
                .build()), List.of(
                "Entity Sub Predicate:",
                "  -> Level: ≥3",
                "  -> Game Type: SURVIVAL",
                "  -> Stats:",
                List.of(
                        "    -> Item: minecraft:salmon",
                        "      -> Times Used: ≥5",
                        "    -> Block: minecraft:cobblestone",
                        "      -> Times Mined: ≥4"
                ),
                "  -> Recipes:",
                "    -> minecraft:recipe1: true",
                "    -> minecraft:recipe2: false",
                "  -> Advancements:",
                "    -> minecraft:first",
                "      -> Done: true",
                "    -> minecraft:second",
                "      -> test: false"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.branch.entity_sub_predicate", SlimePredicate.sized(MinMaxBounds.Ints.atLeast(1))), List.of(
                "Entity Sub Predicate:",
                "  -> Size: ≥1"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, "ali.property.branch.entity_sub_predicate", EntitySubPredicate.Types.FROG.createPredicate(FrogVariant.COLD)), List.of(
                "Entity Sub Predicate:",
                "  -> Variant: minecraft:cold"
        ));
    }

    @Test
    public void testStatTooltip() {
        //noinspection deprecation
        PlayerPredicate.StatMatcher<?> statMatcher = new PlayerPredicate.StatMatcher<>(
                Stats.BLOCK_MINED,
                Blocks.COBBLESTONE.builtInRegistryHolder(),
                MinMaxBounds.Ints.atLeast(4)
        );

        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(UTILS, 0, statMatcher), List.of(
                "Block: minecraft:cobblestone",
                "  -> Times Mined: ≥4"
        ));
    }

    @Test
    public void testBlockPosTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockPosTooltip(UTILS, 0, "ali.property.multi.offset", new BlockPos(10, 12, 14)), List.of(
                "Offset: [X: 10, Y: 12, Z: 14]"
        ));
    }

    @Disabled("internal function")
    @Test
    public void testCopyOperationTooltip() {}

    @Test
    public void testEffectEntryTooltip() {
        assertTooltip(GenericTooltipUtils.getEffectEntryTooltip(UTILS, 0, "ali.property.value.mob_effect", new SetStewEffectFunction.EffectEntry(
                Holder.direct(MobEffects.LUCK),
                ConstantValue.exactly(3)
        )), List.of(
                "Mob Effect: minecraft:luck",
                "  -> Duration: 3"
        ));
    }
}
