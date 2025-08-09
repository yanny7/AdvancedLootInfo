package com.yanny.ali.test;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.pad;
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
                0,
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
                5,
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
                "Killed by player",
                "----- Modifiers -----",
                "Explosion Decay",
                "  -> Predicates:",
                "    -> Survives Explosion",
                "Furnace Smelt"
        ));
    }

    @Test
    public void testFormulaTooltip() {
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, "ali.property.value.formula", new ApplyBonusCount.OreDrops()), List.of(
                "Formula: minecraft:ore_drops"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, "ali.property.value.formula", new ApplyBonusCount.UniformBonusCount(2)), List.of(
                "Formula: minecraft:uniform_bonus_count",
                "  -> Bonus Multiplier: 2"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, "ali.property.value.formula", new ApplyBonusCount.BinomialWithBonusCount(3, 0.51F)), List.of(
                "Formula: minecraft:binomial_with_bonus_count",
                "  -> Extra Rounds: 3",
                "  -> Probability: 0.51"
        ));
    }

    @Test
    public void testPropertyTooltip() {
        assertTooltip(GenericTooltipUtils.getPropertyTooltip(UTILS, "ali.property.value.null", EnumProperty.create("bed", BedPart.class)), List.of("bed"));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getModifierTooltip(UTILS, "ali.property.branch.modifier", new SetAttributesFunction.ModifierBuilder(
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
                "  -> Attribute: minecraft:generic.armor",
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
    public void testUUIDTooltip() {
        assertTooltip(GenericTooltipUtils.getUUIDTooltip(UTILS, "ali.property.value.uuid", UUID.nameUUIDFromBytes(new byte[]{1, 2, 3})), List.of("UUID: 5289df73-7df5-3326-bcdd-22597afb1fac"));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, "ali.property.branch.properties", StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockStateProperties.FACING, Direction.EAST)
                .hasProperty(BlockStateProperties.LEVEL, 3)
                .build().orElseThrow()
        ), List.of(
                "Properties:",
                "  -> facing: east",
                "  -> level: 3"
        ));
    }

    @Test
    void testDamageSourcePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(UTILS, "ali.property.branch.predicate", DamageSourcePredicate.Builder.damageType()
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
        assertTooltip(GenericTooltipUtils.getTagPredicateTooltip(UTILS, "ali.property.value.null", TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR)), List.of("minecraft:bypasses_armor: true"));
        assertTooltip(GenericTooltipUtils.getTagPredicateTooltip(UTILS, "ali.property.value.null", TagPredicate.isNot(DamageTypeTags.BYPASSES_ARMOR)), List.of("minecraft:bypasses_armor: false"));
    }

    @Test
    public void testEntityPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("range", 5);
        assertTooltip(GenericTooltipUtils.getEntityPredicateTooltip(UTILS, "ali.property.branch.predicate", EntityPredicate.Builder.entity()
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
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, "ali.property.branch.entity_types", EntityTypePredicate.of(EntityType.CAT)), List.of(
                "Entity Types:",
                "  -> minecraft:cat"
        ));
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, "ali.property.branch.entity_types", EntityTypePredicate.of(EntityTypeTags.SKELETONS)), List.of(
                "Entity Types:",
                "  -> Tag: minecraft:skeletons"
        ));
    }

    @Test
    public void testDistancePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDistancePredicateTooltip(UTILS, "ali.property.branch.distance_to_player", new DistancePredicate(
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
        assertTooltip(GenericTooltipUtils.getLocationPredicateTooltip(UTILS, "ali.property.branch.location", LocationPredicate.Builder.location()
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
                "    -> minecraft:plains",
                "  -> Structures:",
                "    -> minecraft:mineshaft",
                "  -> Dimension: minecraft:overworld",
                "  -> Smokey: true",
                "  -> Light: 10-15",
                "  -> Block Predicate:",
                "    -> Blocks:",
                "      -> minecraft:stone",
                "      -> minecraft:cobblestone",
                "  -> Fluid Predicate:",
                "    -> Fluids:",
                "      -> minecraft:lava"
        ));
    }

    @Test
    public void testPositionPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getPositionPredicateTooltip(UTILS, "ali.property.branch.position", new LocationPredicate.PositionPredicate(
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
        assertTooltip(GenericTooltipUtils.getLightPredicateTooltip(UTILS, "ali.property.value.light", LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)).build()), List.of("Light: 10-15"));
    }

    @Test
    public void testBlockPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(Blocks.DIRT).build()), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> minecraft:dirt"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(BlockTags.BEDS).build()), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Tag: minecraft:beds"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, "ali.property.branch.block_predicate", BlockPredicate.Builder.block()
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

        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(UTILS, "ali.property.value.nbt", new NbtPredicate(compoundTag)), List.of("Nbt: {test:3.0f}"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFluidPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(UTILS, "ali.property.branch.fluid_predicate", FluidPredicate.Builder.fluid()
                .of(Fluids.WATER)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build().orElseThrow())
                .build()
        ), List.of(
                "Fluid Predicate:",
                "  -> Fluids:",
                "    -> minecraft:water",
                "  -> Properties:",
                "    -> facing: east"
        ));
        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(UTILS, "ali.property.branch.fluid_predicate", FluidPredicate.Builder.fluid().of(HolderSet.direct(Fluids.LAVA.builtInRegistryHolder())).build()), List.of(
                "Fluid Predicate:",
                "  -> Fluids:",
                "    -> minecraft:lava"
        ));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectPredicateTooltip(UTILS, "ali.property.branch.mob_effects", MobEffectsPredicate.Builder.effects()
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
    public void testEntityFlagsPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntityFlagsPredicateTooltip(UTILS, "ali.property.branch.entity_flags", EntityFlagsPredicate.Builder.flags()
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
        assertTooltip(GenericTooltipUtils.getEntityEquipmentPredicateTooltip(UTILS, "ali.property.branch.entity_equipment", EntityEquipmentPredicate.Builder.equipment()
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

        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, "ali.type.condition.match_tool", ItemPredicate.Builder.item()
                .of(ItemTags.AXES)
                .hasComponents(DataComponentPredicate.builder().expect(DataComponents.BASE_COLOR, DyeColor.BLUE).build())
                .build()
        ), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> Tag: minecraft:axes",
                "  -> Components:",
                "    -> minecraft:base_color",
                "      -> Color: BLUE"
        ));
        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, "ali.type.condition.match_tool", ItemPredicate.Builder.item()
                .of(Items.CAKE, Items.NETHERITE_AXE)
                .withCount(MinMaxBounds.Ints.between(10, 15))
                .hasComponents(DataComponentPredicate.builder()
                        .expect(DataComponents.DAMAGE, 5)
                        .expect(DataComponents.POT_DECORATIONS, new PotDecorations(Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.STONE))
                        .build())
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
                "    -> minecraft:cake",
                "    -> minecraft:netherite_axe",
                "  -> Count: 10-15",
                "  -> Components:",
                "    -> minecraft:damage",
                "      -> Value: 5",
                "    -> minecraft:pot_decorations",
                "      -> Back: minecraft:andesite",
                "      -> Left: minecraft:diorite",
                "      -> Right: minecraft:granite",
                "      -> Front: minecraft:stone",
                "  -> Item Predicates:",
                "    -> Damage:",
                "      -> Durability: ≤5",
                "    -> Enchantments:",
                "      -> minecraft:smite",
                "        -> Level: ≥1",
                "      -> minecraft:mending",
                "        -> Level: 2-4",
                "    -> Stored Enchantments:",
                "      -> minecraft:depth_strider",
                "        -> Level: ≤5",
                "      -> minecraft:lure",
                "        -> Level: ≥4",
                "    -> Potions:",
                "      -> minecraft:healing",
                "    -> Custom Data:",
                "      -> Nbt: {healing:1b}"
        ));
    }

    @Test
    public void testEnchantmentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(UTILS, "ali.property.value.enchantment", new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1))), List.of(
                "Level: ≥1"
        ));
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(UTILS, "ali.property.value.enchantment", new EnchantmentPredicate(Enchantments.FEATHER_FALLING, MinMaxBounds.Ints.atMost(2))), List.of(
                "Enchantment: minecraft:feather_falling",
                "  -> Level: ≤2"
        ));
    }

    @Test
    public void testStatMatcherTooltip() {
        //noinspection deprecation
        PlayerPredicate.StatMatcher<?> statMatcher = new PlayerPredicate.StatMatcher<>(
                Stats.BLOCK_MINED,
                Blocks.COBBLESTONE.builtInRegistryHolder(),
                MinMaxBounds.Ints.atLeast(4)
        );

        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(UTILS, statMatcher), List.of(
                "Block: minecraft:cobblestone",
                "  -> Times Mined: ≥4"
        ));
    }

    @Test
    public void testBlockPosTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockPosTooltip(UTILS, "ali.property.multi.offset", new BlockPos(10, 12, 14)), List.of(
                "Offset: [X: 10, Y: 12, Z: 14]"
        ));
    }

    @Test
    public void testListOperationTooltip() {
        assertTooltip(GenericTooltipUtils.getListOperationTooltip(UTILS, "ali.property.value.list_operation", new ListOperation.Insert(1)), List.of(
                "List Operation: INSERT",
                "  -> Offset: 1"
        ));
        assertTooltip(GenericTooltipUtils.getListOperationTooltip(UTILS, "ali.property.value.list_operation", new ListOperation.ReplaceSection(1, Optional.of(2))), List.of(
                "List Operation: REPLACE_SECTION",
                "  -> Offset: 1",
                "  -> Size: 2"
        ));
    }

    @Test
    public void testContainerComponentManipulatorTooltip() {
        assertTooltip(GenericTooltipUtils.getContainerComponentManipulatorTooltip(UTILS, "ali.property.value.container", ContainerComponentManipulators.CHARGED_PROJECTILES), List.of("Container: minecraft:charged_projectiles"));
    }

    @Test
    public void testNbtPathTooltip() throws CommandSyntaxException {
        assertTooltip(GenericTooltipUtils.getNbtPathTooltip(UTILS, "ali.property.value.source_path", NbtPathArgument.NbtPath.of("asdf")), List.of("Source Path: asdf"));
    }

    @Test
    public void testDataComponentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDataComponentPredicateTooltip(UTILS, "ali.property.branch.component_predicates", DataComponentPredicate.builder()
                .expect(DataComponents.DAMAGE, 3)
                .expect(DataComponents.MAX_DAMAGE, 8).build()
        ), List.of(
                "Component Predicates:",
                "  -> minecraft:damage",
                "    -> Value: 3",
                "  -> minecraft:max_damage",
                "    -> Value: 8"
        ));
    }

    @Test
    public void testTypedDataComponentTooltip() {
        assertTooltip(GenericTooltipUtils.getTypedDataComponentTooltip(UTILS, "ali.property.value.component", new TypedDataComponent<>(DataComponents.DAMAGE, 3)), List.of(
                "Component: minecraft:damage",
                "  -> Value: 3"
        ));
    }

    @Test
    public void testCollectionPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getCollectionPredicateTooltip(UTILS, "ali.property.branch.pages", "ali.property.value.null", Optional.of(
                new CollectionPredicate<>(
                        Optional.of(CollectionContentsPredicate.of(
                                new ItemWritableBookPredicate.PagePredicate("Hello"),
                                new ItemWritableBookPredicate.PagePredicate("World")
                        )),
                        Optional.of(CollectionCountsPredicate.of(
                                new CollectionCountsPredicate.Entry<>(new ItemWritableBookPredicate.PagePredicate("Star"), MinMaxBounds.Ints.between(1, 5)),
                                new CollectionCountsPredicate.Entry<>(new ItemWritableBookPredicate.PagePredicate("Wars"), MinMaxBounds.Ints.atLeast(3))
                        )),
                        Optional.of(MinMaxBounds.Ints.atLeast(4))
                )
        ), GenericTooltipUtils::getPagePredicateTooltip), List.of(
                "Pages:",
                "  -> Contains:",
                "    -> Hello",
                "    -> World",
                "  -> Counts:",
                "    -> Star",
                "      -> Count: 1-5",
                "    -> Wars",
                "      -> Count: ≥3",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testFireworkPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getFireworkPredicateTooltip(UTILS, "ali.property.branch.predicate", new ItemFireworkExplosionPredicate.FireworkPredicate(
                Optional.of(FireworkExplosion.Shape.CREEPER),
                Optional.of(true),
                Optional.of(false)
        )), List.of(
                "Predicate:",
                "  -> Shape: CREEPER",
                "  -> Trail: false",
                "  -> Twinkle: true"
        ));
    }

    @Test
    public void testPagePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getPagePredicateTooltip(UTILS, "ali.property.value.page", new ItemWritableBookPredicate.PagePredicate("asdf")), List.of("Page: asdf"));
        assertTooltip(GenericTooltipUtils.getPagePredicateTooltip(UTILS, "ali.property.value.page", new ItemWrittenBookPredicate.PagePredicate(Component.literal("asdf"))), List.of("Page: asdf"));
    }

    @Test
    public void testEntryPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntryPredicateTooltip(UTILS, "ali.property.branch.predicate", new ItemAttributeModifiersPredicate.EntryPredicate(
                Optional.of(HolderSet.direct(Attributes.ARMOR, Attributes.GRAVITY)),
                Optional.of(UUID.nameUUIDFromBytes(new byte[]{0, 1, 2, 3})),
                Optional.of("test"),
                MinMaxBounds.Doubles.between(1.5, 3.14),
                Optional.of(AttributeModifier.Operation.ADD_VALUE),
                Optional.of(EquipmentSlotGroup.ARMOR)
        )), List.of(
                "Predicate:",
                "  -> Attributes:",
                "    -> minecraft:generic.armor",
                "    -> minecraft:generic.gravity",
                "  -> UUID: 37b59afd-5927-35f9-b05e-484a5d7f5168",
                "  -> Name: test",
                "  -> Amount: 1.5-3.1",
                "  -> Operation: ADD_VALUE",
                "  -> Slot: ARMOR"
        ));
    }

    @Test
    public void testDataComponentPatchTooltip() {
        assertTooltip(GenericTooltipUtils.getDataComponentPatchTooltip(UTILS, "ali.property.branch.components", DataComponentPatch.builder()
                .remove(DataComponents.DAMAGE)
                .remove(DataComponents.MAX_DAMAGE)
                .set(DataComponents.BASE_COLOR, DyeColor.BLUE)
                .set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
                .set(new TypedDataComponent<>(DataComponents.CUSTOM_NAME, Component.literal("Hello"))).build()
        ), List.of(
                "Components:",
                "  -> minecraft:damage",
                "    -> REMOVED",
                "  -> minecraft:max_damage",
                "    -> REMOVED",
                "  -> minecraft:base_color",
                "    -> Color: BLUE",
                "  -> minecraft:hide_tooltip",
                "  -> minecraft:custom_name",
                "    -> Custom Name: Hello"
        ));
    }

    @Test
    public void testFireworkExplosionTooltip() {
        assertTooltip(GenericTooltipUtils.getFireworkExplosionTooltip(UTILS, "ali.property.branch.explosion", new FireworkExplosion(
                FireworkExplosion.Shape.STAR,
                IntList.of(1, 2, 3),
                IntList.of(3, 4, 5),
                true,
                false
        )), List.of(
                "Explosion:",
                "  -> Shape: STAR",
                "  -> Colors: [1, 2, 3]",
                "  -> Fade Colors: [3, 4, 5]",
                "  -> Has Trail: true",
                "  -> Has Twinkle: false"
        ));
    }

    @Test
    public void testIntListTooltip() {
        assertTooltip(GenericTooltipUtils.getIntListTooltip(UTILS, "ali.property.value.colors", IntList.of(1, 2, 3)), List.of("Colors: [1, 2, 3]"));
    }

    @Test
    public void testFilterableTooltip() {
        assertTooltip(GenericTooltipUtils.getFilterableTooltip(UTILS, "ali.property.branch.page", new Filterable<>("Hello", Optional.of("World")), GenericTooltipUtils::getStringTooltip), List.of(
                "Page:",
                "  -> Raw: Hello",
                "  -> Filtered: World"
        ));
    }

    @Test
    public void testItemAttributeModifiersEntryTooltip() {
        assertTooltip(GenericTooltipUtils.getItemAttributeModifiersEntryTooltip(UTILS, "ali.property.branch.modifier", new ItemAttributeModifiers.Entry(
                Attributes.BLOCK_BREAK_SPEED,
                new AttributeModifier(
                        UUID.nameUUIDFromBytes(new byte[]{1, 2, 3, 4}),
                        "Test",
                        1.25,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.HEAD
        )), List.of(
                "Modifier:",
                "  -> Attribute: minecraft:player.block_break_speed",
                "  -> Modifier:",
                "    -> UUID: 08d6c05a-2151-3a79-a1df-eb9d2a8f262f",
                "    -> Name: Test",
                "    -> Amount: 1.25",
                "    -> Operation: ADD_VALUE",
                "  -> Slot: HEAD"
        ));
    }

    @Test
    public void testAttributeModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getAttributeModifierTooltip(UTILS, "ali.property.branch.attribute_modifier", new AttributeModifier(
                UUID.nameUUIDFromBytes(new byte[]{1, 2, 3, 4}),
                "Test",
                1.25,
                AttributeModifier.Operation.ADD_VALUE
        )), List.of(
                "Attribute Modifier:",
                "  -> UUID: 08d6c05a-2151-3a79-a1df-eb9d2a8f262f",
                "  -> Name: Test",
                "  -> Amount: 1.25",
                "  -> Operation: ADD_VALUE"
        ));
    }

    @Test
    public void testPossibleEffectTooltip() {
        assertTooltip(GenericTooltipUtils.getPossibleEffectTooltip(UTILS, "ali.property.branch.effect", new FoodProperties.PossibleEffect(
                new MobEffectInstance(MobEffects.LUCK, 1),
                0.5f
        )), List.of(
                "Effect:",
                "  -> Effect: minecraft:luck",
                "    -> Duration: 1",
                "    -> Amplifier: 0",
                "    -> Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true",
                "  -> Probability: 0.5"
        ));
    }

    @Test
    public void testMobEffectInstanceTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectInstanceTooltip(UTILS, "ali.property.value.effect", new MobEffectInstance(
                MobEffects.BAD_OMEN,
                1,
                2,
                true,
                false,
                true,
                new MobEffectInstance(MobEffects.UNLUCK, 5)
        )), List.of(
                "Effect: minecraft:bad_omen",
                "  -> Duration: 1",
                "  -> Amplifier: 2",
                "  -> Ambient: true",
                "  -> Is Visible: false",
                "  -> Show Icon: true",
                "  -> Hidden Effect: minecraft:unluck",
                "    -> Duration: 5",
                "    -> Amplifier: 0",
                "    -> Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true"
        ));
    }

    @Test
    public void testRuleTooltip() {
        assertTooltip(GenericTooltipUtils.getRuleTooltip(UTILS, "ali.property.branch.rule", new Tool.Rule(
                HolderSet.direct(Holder.direct(Blocks.DIRT), Holder.direct(Blocks.COBBLESTONE)),
                Optional.of(0.25f),
                Optional.of(true)
        )), List.of(
                "Rule:",
                "  -> Blocks:",
                "    -> minecraft:dirt",
                "    -> minecraft:cobblestone",
                "  -> Correct For Drops: true",
                "  -> Speed: 0.25"
        ));
    }

    @Test
    public void testMapDecorationEntryTooltip() {
        assertTooltip(GenericTooltipUtils.getMapDecorationEntryTooltip(UTILS, "ali.property.value.null", new MapDecorations.Entry(
                MapDecorationTypes.DESERT_VILLAGE,
                2.5,
                0.25,
                0.3f
        )), List.of(
                "minecraft:village_desert",
                "  -> X: 2.5",
                "  -> Z: 0.25",
                "  -> Rotation: 0.3"
        ));
    }

    @Test
    public void testItemStackTooltip() {
        assertTooltip(GenericTooltipUtils.getItemStackTooltip(UTILS, "ali.property.branch.item", new ItemStack(
                Holder.direct(Items.ANDESITE),
                10,
                DataComponentPatch.builder()
                        .set(DataComponents.DAMAGE, 2)
                        .remove(DataComponents.HIDE_TOOLTIP)
                        .build()
        )), List.of(
                "Item:",
                "  -> Item: minecraft:andesite",
                "  -> Count: 10",
                "  -> Components:",
                "    -> minecraft:damage",
                "      -> Value: 2",
                "    -> minecraft:max_stack_size",
                "      -> Value: 64",
                "    -> minecraft:lore",
                "    -> minecraft:enchantments",
                "    -> minecraft:repair_cost",
                "      -> Value: 0",
                "    -> minecraft:attribute_modifiers",
                "      -> Show In Tooltip: true",
                "    -> minecraft:rarity",
                "      -> Rarity: COMMON"
        ));
    }

    @Test
    public void testDataComponentMapTooltip() {
        assertTooltip(GenericTooltipUtils.getDataComponentMapTooltip(UTILS, "ali.property.branch.components", DataComponentMap.builder()
                .set(DataComponents.DAMAGE, 2)
                .set(DataComponents.MAX_DAMAGE, 5)
                .build()
        ), List.of(
                "Components:",
                "  -> minecraft:damage",
                "    -> Value: 2",
                "  -> minecraft:max_damage",
                "    -> Value: 5"
        ));
    }

    @Test
    public void testSuspiciousStewEffectEntryTooltip() {
        assertTooltip(GenericTooltipUtils.getSuspiciousStewEffectEntryTooltip(UTILS, new SuspiciousStewEffects.Entry(
                MobEffects.DAMAGE_BOOST,
                5
        )), List.of(
                "minecraft:strength",
                "  -> Duration: 5"
        ));
    }

    @Test
    public void testGlobalPosTooltip() {
        assertTooltip(GenericTooltipUtils.getGlobalPosTooltip(UTILS, "ali.property.branch.global_pos", new GlobalPos(
                Level.NETHER,
                new BlockPos(10, 20, 30)
        )), List.of(
                "Global Position:",
                "  -> Dimension: minecraft:the_nether",
                "  -> Position: [X: 10, Y: 20, Z: 30]"
        ));
    }

    @Test
    public void testBeehiveBlockEntityOccupantTooltip() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("test", 5);

        assertTooltip(GenericTooltipUtils.getBeehiveBlockEntityOccupantTooltip(UTILS, "ali.property.branch.occupant", new BeehiveBlockEntity.Occupant(
                CustomData.of(tag),
                2,
                3
        )), List.of(
                "Occupant:",
                "  -> Entity Data: {test:5}",
                "  -> Ticks In Hive: 2",
                "  -> Min Ticks In Hive: 3"
        ));
    }

    @Test
    public void testEffectEntryTooltip() {
        assertTooltip(GenericTooltipUtils.getEffectEntryTooltip(UTILS, "ali.property.value.effect", new SetStewEffectFunction.EffectEntry(
                MobEffects.LUCK,
                ConstantValue.exactly(3)
        )), List.of(
                "Effect: minecraft:luck",
                "  -> Duration: 3"
        ));
    }

    @Test
    public void testProperty2Tooltip() {
        assertTooltip(GenericTooltipUtils.getAuthPropertyTooltip(UTILS, new Property("Hello", "World", "Sign")), List.of(
                "Name: Hello",
                "Value: World",
                "Signature: Sign"
        ));
    }
}
