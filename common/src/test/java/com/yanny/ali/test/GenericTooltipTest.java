package com.yanny.ali.test;

import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import com.yanny.ali.plugin.client.GenericTooltipUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.CatVariant;
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
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.pad;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

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
        chanceMap.put(Holder.direct(Enchantments.LOOTING), bonusChanceMap);
        countMap.put(Holder.direct(Enchantments.FORTUNE), bonusCountMap);

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
    public void testBlockTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockTooltip(UTILS, 1, "ali.property.value.block", Blocks.DIAMOND_BLOCK), List.of("  -> Block: Block of Diamond"));
    }

    @Test
    public void testPropertyTooltip() {
        assertTooltip(GenericTooltipUtils.getPropertyTooltip(UTILS, 1, "ali.property.value.null", EnumProperty.create("bed", BedPart.class)), List.of("  -> bed"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(UTILS, 0, "ali.property.value.enchantment", Enchantments.AQUA_AFFINITY), List.of("Enchantment: Aqua Affinity"));
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(UTILS, 1, "ali.property.value.enchantment", Enchantments.FIRE_ASPECT), List.of("  -> Enchantment: Fire Aspect"));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getModifierTooltip(UTILS, 0, "ali.property.branch.modifier", new SetAttributesFunction.ModifierBuilder(
                "armor",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                UniformGenerator.between(1, 5))
                        .forSlot(EquipmentSlotGroup.HEAD)
                        .forSlot(EquipmentSlotGroup.CHEST)
                        .forSlot(EquipmentSlotGroup.LEGS)
                        .forSlot(EquipmentSlotGroup.FEET)
                        .build()), List.of(
                "Modifier:",
                "  -> Name: armor",
                "  -> Attribute: Armor",
                "  -> Operation: ADD_MULTIPLIED_TOTAL",
                "  -> Amount: 1-5",
                "  -> Equipment Slots:",
                "    -> FEET",
                "    -> LEGS",
                "    -> CHEST",
                "    -> HEAD"
        ));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(GenericTooltipUtils.getAttributeTooltip(UTILS, 0, "ali.property.value.attribute", Attributes.JUMP_STRENGTH.value()), List.of("Attribute: Jump Strength"));
    }

    @Test
    public void testUUIDTooltip() {
        assertTooltip(GenericTooltipUtils.getUUIDTooltip(UTILS, 0, "ali.property.value.uuid", UUID.nameUUIDFromBytes(new byte[]{1, 2, 3})), List.of("UUID: 5289df73-7df5-3326-bcdd-22597afb1fac"));
    }

    @Test
    public void testBannerPatternTooltip() {
        assertTooltip(GenericTooltipUtils.getBannerPatternTooltip(UTILS, 0, TooltipTestSuite.LOOKUP.lookup(Registries.BANNER_PATTERN).orElseThrow().get(BannerPatterns.BASE).orElseThrow().value(), DyeColor.WHITE), List.of(
                "Banner Pattern: Fully White Field"
        ));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, 0, "ali.property.branch.state_properties_predicate", StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockStateProperties.FACING, Direction.EAST)
                .build().orElseThrow()
        ), List.of(
                "State Properties:",
                "  -> facing: east"
        ));
    }

    @Test
    void testDamageSourcePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(UTILS, 0, "ali.property.branch.damage_source_predicate", DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                .build()), List.of(
                "Damage Source:",
                "  -> Tags:",
                "    -> minecraft:bypasses_armor: true",
                "    -> minecraft:is_explosion: false"
        ));
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
                .equipment(EntityEquipmentPredicate.Builder.equipment().head(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE)))
                .subPredicate(EntitySubPredicates.CAT.createPredicate(HolderSet.direct(BuiltInRegistries.CAT_VARIANT.getHolderOrThrow(CatVariant.PERSIAN))))
                .vehicle(EntityPredicate.Builder.entity().team("blue"))
                .passenger(EntityPredicate.Builder.entity().team("white"))
                .targetedEntity(EntityPredicate.Builder.entity().team("red"))
                .team("orange")
                .build()
        ), List.of(
                "Predicate:",
                "  -> Entity Types:",
                "    -> Cat",
                "  -> Distance to Player:",
                "    -> X: =10.0",
                "  -> Location:",
                "    -> Position:",
                "      -> X: ≥20.0",
                "  -> Stepping on Location:",
                "    -> Position:",
                "      -> X: ≤30.0",
                "  -> Mob Effects:",
                "    -> Mob Effect: minecraft:absorption",
                "      -> Is Ambient: true",
                "    -> Mob Effect: minecraft:blindness",
                "      -> Is Visible: false",
                "  -> Nbt: {range:5}",
                "  -> Entity Flags:",
                "    -> Is Baby: true",
                "  -> Entity Equipment:",
                "    -> Head:",
                "      -> Items:",
                "        -> Andesite",
                "        -> Diorite",
                "  -> Entity Sub Predicate:",
                "    -> Type: minecraft:cat",
                "      -> Variants:",
                "        -> Variant: minecraft:persian",
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
                "  -> Cat"
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
                .setX(MinMaxBounds.Doubles.exactly(10D))
                .setY(MinMaxBounds.Doubles.atLeast(20D))
                .setZ(MinMaxBounds.Doubles.atMost(30D))
                .setBiomes(HolderSet.direct(TooltipTestSuite.LOOKUP.lookup(Registries.BIOME).orElseThrow().get(Biomes.PLAINS).orElseThrow()))
                .setStructures(HolderSet.direct(TooltipTestSuite.LOOKUP.lookup(Registries.STRUCTURE).orElseThrow().get(BuiltinStructures.MINESHAFT).orElseThrow()))
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
                "  -> Biomes:",
                "    -> Biome: minecraft:plains",
                "  -> Structures:",
                "    -> Structure: minecraft:mineshaft",
                "  -> Dimension: minecraft:overworld",
                "  -> Smokey: true",
                "  -> Light: 10-15",
                "  -> Block Predicate:",
                "    -> Blocks:",
                "      -> Stone",
                "      -> Cobblestone",
                "  -> Fluid Predicate:",
                "    -> Fluids:",
                "      -> Fluid: minecraft:lava"
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

        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(Blocks.DIRT).build()), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Block: Dirt"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(BlockTags.BEDS).build()), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Tag: minecraft:beds"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block()
                .of(Blocks.STONE, Blocks.COBBLESTONE)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST))
                .hasNbt(compoundTag)
                .build()
        ), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Stone",
                "    -> Cobblestone",
                "State Properties:",
                "  -> facing: east",
                "Nbt: {test:3.0f}"
        ));
    }

    @Test
    public void testNbtPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(UTILS, 0, "ali.property.value.nbt", new NbtPredicate(compoundTag)), List.of("Nbt: {test:3.0f}"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFluidPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(UTILS, 0, "ali.property.branch.fluid_predicate", FluidPredicate.Builder.fluid()
                .of(Fluids.WATER)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build().orElseThrow())
                .build()
        ), List.of(
                "Fluid Predicate:",
                "  -> Fluids:",
                "    -> Fluid: minecraft:water",
                "  -> State Properties:",
                "    -> facing: east"
        ));
        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(UTILS, 0, FluidPredicate.Builder.fluid().of(HolderSet.direct(Fluids.LAVA.builtInRegistryHolder())).build()), List.of(
                "Fluid Predicate:",
                "  -> Fluids:",
                "    -> Fluid: minecraft:lava"
        ));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectPredicateTooltip(UTILS, 0, "ali.property.branch.mob_effects", MobEffectsPredicate.Builder.effects()
                .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.between(10, 15), MinMaxBounds.Ints.atMost(5), Optional.of(true), Optional.of(false)))
                .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.atLeast(5), MinMaxBounds.Ints.between(1, 2), Optional.empty(), Optional.empty())).build().orElseThrow()
        ), List.of(
                "Mob Effects:",
                "  -> Mob Effect: minecraft:absorption",
                "    -> Amplifier: 10-15",
                "    -> Duration: ≤5",
                "    -> Is Ambient: true",
                "    -> Is Visible: false",
                "  -> Mob Effect: minecraft:blindness",
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
                .legs(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(5)))
                .feet(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(1, 2)))
                .mainhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(0, 64)))
                .offhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(32)))
                .build()), List.of(
            "Entity Equipment:",
            "  -> Head:",
            "    -> Count: 10-15",
            "  -> Chest:",
            "    -> Count: ≤5",
            "  -> Legs:",
            "    -> Count: ≥5",
            "  -> Feet:",
            "    -> Count: 1-2",
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

        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, 0, "ali.type.condition.match_tool", ItemPredicate.Builder.item()
                .of(ItemTags.AXES)
                .hasComponents(DataComponentPredicate.builder().expect(DataComponents.BASE_COLOR, DyeColor.BLUE).build())
                .build()
        ), List.of(
                "Items:",
                "  -> Tag: minecraft:axes",
                "Component Predicates:",
                "  -> Component: minecraft:base_color: blue"
        ));
        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, 0, "ali.type.condition.match_tool", ItemPredicate.Builder.item()
                .of(Items.CAKE, Items.NETHERITE_AXE)
                .withCount(MinMaxBounds.Ints.between(10, 15))
                .withSubPredicate(ItemSubPredicates.DAMAGE, ItemDamagePredicate.durability(MinMaxBounds.Ints.atMost(5)))
                .withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.Enchantments.enchantments(List.of(
                        new EnchantmentPredicate(Enchantments.SMITE, MinMaxBounds.Ints.atLeast(1)),
                        new EnchantmentPredicate(Enchantments.MENDING, MinMaxBounds.Ints.between(2, 4))
                )))
                .withSubPredicate(ItemSubPredicates.STORED_ENCHANTMENTS, ItemEnchantmentsPredicate.Enchantments.storedEnchantments(List.of(
                        new EnchantmentPredicate(Enchantments.DEPTH_STRIDER, MinMaxBounds.Ints.atMost(5)),
                        new EnchantmentPredicate(Enchantments.LURE, MinMaxBounds.Ints.atLeast(4))
                )))
                .withSubPredicate(ItemSubPredicates.POTIONS, (ItemPotionsPredicate) ItemPotionsPredicate.potions(HolderSet.direct(Potions.HEALING)))
                .withSubPredicate(ItemSubPredicates.CUSTOM_DATA, ItemCustomDataPredicate.customData(new NbtPredicate(compoundTag)))
                .build()
        ), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> Item: Cake",
                "    -> Item: Netherite Axe",
                "  -> Count: 10-15",
                "  -> Item Predicates:",
                "    -> Damage:",
                "      -> Durability: ≤5",
                "    -> Enchantments:",
                "      -> Enchantment: Smite",
                "        -> Level: ≥1",
                "      -> Enchantment: Mending",
                "        -> Level: 2-4",
                "    -> Stored Enchantments:",
                "      -> Enchantment: Depth Strider",
                "        -> Level: ≤5",
                "      -> Enchantment: Lure",
                "        -> Level: ≥4",
                "    -> Potions:",
                "      -> Potion: minecraft:healing",
                "    -> Custom Data:",
                "      -> Nbt: {healing:1b}"
        ));
    }

    @Test
    public void testEnchantmentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(UTILS, 0, new EnchantmentPredicate(Enchantments.FEATHER_FALLING, MinMaxBounds.Ints.atMost(2))), List.of(
                "Enchantment: Feather Falling",
                "  -> Level: ≤2"
        ));
    }

    @Test
    public void testItemTooltip() {
        assertTooltip(GenericTooltipUtils.getItemTooltip(UTILS, 0, "ali.property.value.item", Items.ACACIA_DOOR), List.of(
                "Item: Acacia Door"
        ));
    }

    @Test
    public void testGameTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getGameTypeTooltip(UTILS, 0, "ali.property.value.game_type", GameType.SPECTATOR), List.of(
                "Game Type: Spectator"
        ));
    }

    @Test
    public void testStatTooltip() {
        PlayerPredicate.StatMatcher<?> statMatcher = new PlayerPredicate.StatMatcher<>(
                Stats.BLOCK_MINED,
                Blocks.COBBLESTONE.builtInRegistryHolder(),
                MinMaxBounds.Ints.atLeast(4)
        );

        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(UTILS, 0, statMatcher), List.of(
                "Block: Cobblestone",
                "  -> Times Mined: ≥4"
        ));
    }

    @Test
    public void testRecipesTooltip() {
        Object2BooleanMap<ResourceLocation> recipeList = new Object2BooleanArrayMap<>();

        recipeList.put(new ResourceLocation("furnace_recipe"), true);
        recipeList.put(new ResourceLocation("apple_recipe"), false);

        assertTooltip(GenericTooltipUtils.getRecipesTooltip(UTILS, 0, "ali.property.branch.recipes", new Object2BooleanArrayMap<>()), List.of());
        assertTooltip(GenericTooltipUtils.getRecipesTooltip(UTILS, 0, "ali.property.branch.recipes", recipeList), List.of(
                "Recipes:",
                "  -> minecraft:furnace_recipe: true",
                "  -> minecraft:apple_recipe: false"
        ));
    }

    @Test
    public void testAdvancementsTooltip() {
        Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap = new LinkedHashMap<>();
        Object2BooleanMap<String> criterions = new Object2BooleanArrayMap<>();

        criterions.put("test", true);
        predicateMap.put(new ResourceLocation("first"), new PlayerPredicate.AdvancementDonePredicate(true));
        predicateMap.put(new ResourceLocation("second"), new PlayerPredicate.AdvancementCriterionsPredicate(criterions));

        assertTooltip(GenericTooltipUtils.getAdvancementsTooltip(UTILS, 0, "ali.property.branch.advancements", Map.of()), List.of());
        assertTooltip(GenericTooltipUtils.getAdvancementsTooltip(UTILS, 0, "ali.property.branch.advancements", predicateMap), List.of(
                "Advancements:",
                "  -> minecraft:first",
                "    -> Done: true",
                "  -> minecraft:second",
                "    -> test: true"
        ));
    }

    @Test
    public void testBlockPosTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockPosTooltip(UTILS, 0, "ali.property.branch.offset", new BlockPos(10, 12, 14)), List.of(
                "Offset:",
                "  -> X: 10",
                "  -> Y: 12",
                "  -> Z: 14"
        ));
    }
}
