package com.yanny.ali.test;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import com.yanny.ali.plugin.client.GenericTooltipUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yanny.ali.plugin.client.GenericTooltipUtils.pad;
import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
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
        chanceMap.put(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.LOOTING).orElseThrow(), bonusChanceMap);
        countMap.put(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), bonusCountMap);

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
                ResourceLocation.withDefaultNamespace("armor"),
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                UniformGenerator.between(1, 5))
                        .forSlot(EquipmentSlotGroup.HEAD)
                        .forSlot(EquipmentSlotGroup.CHEST)
                        .forSlot(EquipmentSlotGroup.LEGS)
                        .forSlot(EquipmentSlotGroup.FEET)
                        .build()), List.of(
                "Modifier:",
                "  -> Attribute: minecraft:armor",
                "  -> Operation: ADD_MULTIPLIED_TOTAL",
                "  -> Amount: 1-5",
                "  -> Id: minecraft:armor",
                "  -> Equipment Slots:",
                "    -> FEET",
                "    -> LEGS",
                "    -> CHEST",
                "    -> HEAD"
        ));
    }

    @Test
    public void testBannerPatternTooltip() {
        assertTooltip(GenericTooltipUtils.getBannerPatternTooltip(UTILS, 0, "ali.property.value.banner_pattern", LOOKUP.lookup(Registries.BANNER_PATTERN).orElseThrow().get(BannerPatterns.BASE).orElseThrow().value(), DyeColor.WHITE), List.of(
                "Banner Pattern: Fully White Field"
        ));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, 0, "ali.property.branch.state_properties_predicate", StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockStateProperties.FACING, Direction.EAST)
                .hasProperty(BlockStateProperties.LEVEL, 3)
                .build().orElseThrow()
        ), List.of(
                "State Properties:",
                "  -> facing: east",
                "  -> level: 3"
        ));
    }

    @Disabled("internal function")
    @Test
    public void testPropertyMatcherTooltip() {}

    @Test
    void testDamageSourcePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(UTILS, 0, "ali.property.branch.damage_source_predicate", DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                .source(EntityPredicate.Builder.entity().of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.BAT))
                .direct(EntityPredicate.Builder.entity().of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.ARROW))
                .isDirect(false)
                .build()), List.of(
                "Damage Source:",
                "  -> Tags:",
                "    -> minecraft:bypasses_armor: true",
                "    -> minecraft:is_explosion: false",
                "  -> Direct Entity:",
                "    -> Entity Types:",
                "      -> minecraft:arrow",
                "  -> Source Entity:",
                "    -> Entity Types:",
                "      -> minecraft:bat",
                "  -> Is Direct: false"
        ));
    }

    @Test
    public void testTagPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getTagPredicateTooltip(UTILS, 0, TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR)), List.of("minecraft:bypasses_armor: true"));
        assertTooltip(GenericTooltipUtils.getTagPredicateTooltip(UTILS, 0, TagPredicate.isNot(DamageTypeTags.BYPASSES_ARMOR)), List.of("minecraft:bypasses_armor: false"));
    }

    @Test
    public void testEntityPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("range", 5);

        assertTooltip(GenericTooltipUtils.getEntityPredicateTooltip(UTILS, 0, "ali.property.branch.predicate", EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.CAT))
                .distance(new DistancePredicate(MinMaxBounds.Doubles.exactly(10), MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY))
                .located(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atLeast(20)))
                .steppingOn(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atMost(30)))
                .effects(MobEffectsPredicate.Builder.effects()
                        .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.of(true), Optional.empty()))
                        .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.of(false)))
                )
                .nbt(new NbtPredicate(compoundTag))
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true))
                .equipment(EntityEquipmentPredicate.Builder.equipment().head(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.ANDESITE, Items.DIORITE)))
                .subPredicate(EntitySubPredicates.CAT.createPredicate(HolderSet.direct(BuiltInRegistries.CAT_VARIANT.get(CatVariant.PERSIAN).orElseThrow())))
                .vehicle(EntityPredicate.Builder.entity().team("blue"))
                .passenger(EntityPredicate.Builder.entity().team("white"))
                .targetedEntity(EntityPredicate.Builder.entity().team("red"))
                .periodicTick(1000)
                .moving(new MovementPredicate(MinMaxBounds.Doubles.between(1, 5), MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY))
                .team("orange")
                .build()
        ), List.of(
                "Predicate:",
                "  -> Entity Types:",
                "    -> minecraft:cat",
                "  -> Distance to Player:",
                "    -> X: =10.0",
                "  -> Location:",
                "    -> Located:",
                "      -> Position:",
                "        -> X: ≥20.0",
                "    -> Stepping on Location:",
                "      -> Position:",
                "        -> X: ≤30.0",
                "  -> Movement:",
                "    -> X: 1.0-5.0",
                "  -> Periodic Tick: 1000",
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
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, 0, "ali.property.branch.entity_types", EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.CAT)), List.of(
                "Entity Types:",
                "  -> minecraft:cat"
        ));
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, 0, "ali.property.branch.entity_types", EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityTypeTags.SKELETONS)), List.of(
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
        assertTooltip(GenericTooltipUtils.getLocationPredicateTooltip(UTILS, 0, "ali.property.branch.located", LocationPredicate.Builder.location()
                .setX(MinMaxBounds.Doubles.exactly(10D))
                .setY(MinMaxBounds.Doubles.atLeast(20D))
                .setZ(MinMaxBounds.Doubles.atMost(30D))
                .setBiomes(HolderSet.direct(LOOKUP.lookup(Registries.BIOME).orElseThrow().get(Biomes.PLAINS).orElseThrow()))
                .setStructures(HolderSet.direct(LOOKUP.lookup(Registries.STRUCTURE).orElseThrow().get(BuiltinStructures.MINESHAFT).orElseThrow()))
                .setDimension(Level.OVERWORLD)
                .setSmokey(true)
                .setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)))
                .setBlock(BlockPredicate.Builder.block().of(LOOKUP.lookupOrThrow(Registries.BLOCK), Blocks.STONE, Blocks.COBBLESTONE))
                .setFluid(FluidPredicate.Builder.fluid().of(Fluids.LAVA))
                .setCanSeeSky(true)
                .build()
        ), List.of(
                "Located:",
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
                "      -> minecraft:lava",
                "  -> Can See Sky: true"
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

        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(LOOKUP.lookupOrThrow(Registries.BLOCK), Blocks.DIRT).build()), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> minecraft:dirt"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block().of(LOOKUP.lookupOrThrow(Registries.BLOCK), BlockTags.BEDS).build()), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Tag: minecraft:beds"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, "ali.property.branch.block_predicate", BlockPredicate.Builder.block()
                .of(LOOKUP.lookupOrThrow(Registries.BLOCK), Blocks.STONE, Blocks.COBBLESTONE)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST))
                .hasNbt(compoundTag)
                .build()
        ), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> minecraft:stone",
                "    -> minecraft:cobblestone",
                "  -> State Properties:",
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
                "    -> minecraft:water",
                "  -> State Properties:",
                "    -> facing: east"
        ));
        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(UTILS, 0, "ali.property.branch.fluid_predicate", FluidPredicate.Builder.fluid().of(HolderSet.direct(Fluids.LAVA.builtInRegistryHolder())).build()), List.of(
                "Fluid Predicate:",
                "  -> Fluids:",
                "    -> minecraft:lava"
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
                .setIsFlying(true)
                .setOnGround(false)
                .build()
        ), List.of(
                "Entity Flags:",
                "  -> Is On Ground: false",
                "  -> Is On Fire: false",
                "  -> Is Baby: true",
                "  -> Is Crouching: true",
                "  -> Is Sprinting: true",
                "  -> Is Swimming: false",
                "  -> Is Flying: true"
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
                .of(LOOKUP.lookupOrThrow(Registries.ITEM), ItemTags.AXES)
                .hasComponents(DataComponentPredicate.builder().expect(DataComponents.BASE_COLOR, DyeColor.BLUE).build())
                .build()
        ), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> Tag: minecraft:axes",
                "  -> Component Predicates:",
                "    -> Component: minecraft:base_color",
                "      -> Color: BLUE"
        ));
        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, 0, "ali.type.condition.match_tool", ItemPredicate.Builder.item()
                .of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.CAKE, Items.NETHERITE_AXE)
                .withCount(MinMaxBounds.Ints.between(10, 15))
                .hasComponents(DataComponentPredicate.builder()
                        .expect(DataComponents.DAMAGE, 5)
                        .expect(DataComponents.POT_DECORATIONS, new PotDecorations(Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.STONE))
                        .build())
                .withSubPredicate(ItemSubPredicates.DAMAGE, ItemDamagePredicate.durability(MinMaxBounds.Ints.atMost(5)))
                .withSubPredicate(ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.Enchantments.enchantments(List.of(
                        new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.SMITE).orElseThrow(), MinMaxBounds.Ints.atLeast(1)),
                        new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.MENDING).orElseThrow(), MinMaxBounds.Ints.between(2, 4))
                )))
                .withSubPredicate(ItemSubPredicates.STORED_ENCHANTMENTS, ItemEnchantmentsPredicate.Enchantments.storedEnchantments(List.of(
                        new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.DEPTH_STRIDER).orElseThrow(), MinMaxBounds.Ints.atMost(5)),
                        new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.LURE).orElseThrow(), MinMaxBounds.Ints.atLeast(4))
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
                "  -> Component Predicates:",
                "    -> Component: minecraft:damage",
                "      -> Value: 5",
                "    -> Component: minecraft:pot_decorations",
                "      -> Back: minecraft:andesite",
                "      -> Left: minecraft:diorite",
                "      -> Right: minecraft:granite",
                "      -> Front: minecraft:stone",
                "  -> Item Predicates:",
                "    -> Damage:",
                "      -> Durability: ≤5",
                "    -> Enchantments:",
                "      -> Enchantments:",
                "        -> minecraft:smite",
                "        -> Level: ≥1",
                "      -> Enchantments:",
                "        -> minecraft:mending",
                "        -> Level: 2-4",
                "    -> Stored Enchantments:",
                "      -> Enchantments:",
                "        -> minecraft:depth_strider",
                "        -> Level: ≤5",
                "      -> Enchantments:",
                "        -> minecraft:lure",
                "        -> Level: ≥4",
                "    -> Potions:",
                "      -> minecraft:healing",
                "    -> Custom Data:",
                "      -> Nbt: {healing:1b}"
        ));
    }

    @Test
    public void testEnchantmentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(UTILS, 0, "ali.property.branch.enchantments", new EnchantmentPredicate(
                LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FEATHER_FALLING),
                MinMaxBounds.Ints.atMost(2))
        ), List.of(
                "Enchantments:",
                "  -> minecraft:feather_falling",
                "  -> Level: ≤2"
        ));
    }

    @Test
    public void testGameTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getGameTypePredicateTooltip(UTILS, 0, "ali.property.branch.game_types", GameTypePredicate.of(GameType.SPECTATOR)), List.of(
                "Game Types:",
                "  -> SPECTATOR"
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

        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(UTILS, 0, statMatcher), List.of(
                "Block: minecraft:cobblestone",
                "  -> Times Mined: ≥4"
        ));
    }

    @Test
    public void testRecipesTooltip() {
        Object2BooleanMap<ResourceKey<Recipe<?>>> recipeList = new Object2BooleanArrayMap<>();

        recipeList.put(ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("furnace_recipe")), true);
        recipeList.put(ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("apple_recipe")), false);

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
        predicateMap.put(ResourceLocation.withDefaultNamespace("first"), new PlayerPredicate.AdvancementDonePredicate(true));
        predicateMap.put(ResourceLocation.withDefaultNamespace("second"), new PlayerPredicate.AdvancementCriterionsPredicate(criterions));

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

    @Test
    public void testMapDecorationTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getMapDecorationTypeTooltip(UTILS, 0, "ali.property.value.map_decoration", MapDecorationTypes.OCEAN_MONUMENT.value()), List.of("Map Decoration: minecraft:monument"));
    }

    @Test
    public void testListOperationTooltip() {
        assertTooltip(GenericTooltipUtils.getListOperationTooltip(UTILS, 0, "ali.property.value.list_operation", new ListOperation.Insert(1)), List.of(
                "List Operation: INSERT",
                "  -> Offset: 1"
        ));
        assertTooltip(GenericTooltipUtils.getListOperationTooltip(UTILS, 0, "ali.property.value.list_operation", new ListOperation.ReplaceSection(1, Optional.of(2))), List.of(
                "List Operation: REPLACE_SECTION",
                "  -> Offset: 1",
                "  -> Size: 2"
        ));
    }

    @Test
    public void testContainerComponentManipulatorTooltip() {
        assertTooltip(GenericTooltipUtils.getContainerComponentManipulatorTooltip(UTILS, 0, "ali.property.value.container", ContainerComponentManipulators.CHARGED_PROJECTILES), List.of("Container: minecraft:charged_projectiles"));
    }

    @Disabled("internal function")
    @Test
    public void testCopyOperationTooltip() {}

    @Test
    public void testNbtPathTooltip() throws CommandSyntaxException {
        assertTooltip(GenericTooltipUtils.getNbtPathTooltip(UTILS, 0, "ali.property.value.source_path", NbtPathArgument.NbtPath.of("asdf")), List.of("Source Path: asdf"));
    }

    @Test
    public void testDataComponentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDataComponentPredicateTooltip(UTILS, 0, "ali.property.branch.component_predicates", DataComponentPredicate.builder()
                .expect(DataComponents.DAMAGE, 3)
                .expect(DataComponents.MAX_DAMAGE, 8).build()
        ), List.of(
                "Component Predicates:",
                "  -> Component: minecraft:damage",
                "    -> Value: 3",
                "  -> Component: minecraft:max_damage",
                "    -> Value: 8"
        ));
    }

    @Test
    public void testTypedDataComponentTooltip() {
        assertTooltip(GenericTooltipUtils.getTypedDataComponentTooltip(UTILS, 0, "ali.property.value.component", new TypedDataComponent<>(DataComponents.DAMAGE, 3)), List.of(
                "Component: minecraft:damage",
                "  -> Value: 3"
        ));
    }

    @Test
    public void testCollectionPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getCollectionPredicateTooltip(UTILS, 0, "ali.property.branch.pages", "ali.property.value.null", Optional.of(
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
        assertTooltip(GenericTooltipUtils.getFireworkPredicateTooltip(UTILS, 0, "ali.property.branch.predicate", new ItemFireworkExplosionPredicate.FireworkPredicate(
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
        assertTooltip(GenericTooltipUtils.getPagePredicateTooltip(UTILS, 0, "ali.property.value.page", new ItemWritableBookPredicate.PagePredicate("asdf")), List.of("Page: asdf"));
        assertTooltip(GenericTooltipUtils.getPagePredicateTooltip(UTILS, 0, "ali.property.value.page", new ItemWrittenBookPredicate.PagePredicate(Component.literal("asdf"))), List.of("Page: asdf"));
    }

    @Test
    public void testEntryPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntryPredicateTooltip(UTILS, 0, "ali.property.branch.predicate", new ItemAttributeModifiersPredicate.EntryPredicate(
                Optional.of(HolderSet.direct(Attributes.ARMOR, Attributes.GRAVITY)),
                Optional.of(ResourceLocation.withDefaultNamespace("test")),
                MinMaxBounds.Doubles.between(1.5, 3.14),
                Optional.of(AttributeModifier.Operation.ADD_VALUE),
                Optional.of(EquipmentSlotGroup.ARMOR)
        )), List.of(
                "Predicate:",
                "  -> Attributes:",
                "    -> minecraft:armor",
                "    -> minecraft:gravity",
                "  -> Id: minecraft:test",
                "  -> Amount: 1.5-3.1",
                "  -> Operation: ADD_VALUE",
                "  -> Slot: ARMOR"
        ));
    }

    @Test
    public void testDataComponentPatchTooltip() {
        assertTooltip(GenericTooltipUtils.getDataComponentPatchTooltip(UTILS, 0, "ali.property.branch.components", DataComponentPatch.builder()
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
        assertTooltip(GenericTooltipUtils.getFireworkExplosionTooltip(UTILS, 0, "ali.property.branch.explosion", new FireworkExplosion(
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
        assertTooltip(GenericTooltipUtils.getIntListTooltip(UTILS, 0, "ali.property.value.colors", IntList.of(1, 2, 3)), List.of("Colors: [1, 2, 3]"));
    }

    @Test
    public void testFilterableTooltip() {
        assertTooltip(GenericTooltipUtils.getFilterableTooltip(UTILS, 0, "ali.property.branch.page", new Filterable<>("Hello", Optional.of("World")), GenericTooltipUtils::getStringTooltip), List.of(
                "Page:",
                "  -> Raw: Hello",
                "  -> Filtered: World"
        ));
    }

    @Test
    public void testItemAttributeModifiersEntryTooltip() {
        assertTooltip(GenericTooltipUtils.getItemAttributeModifiersEntryTooltip(UTILS, 0, "ali.property.branch.modifier", new ItemAttributeModifiers.Entry(
                Attributes.BLOCK_BREAK_SPEED,
                new AttributeModifier(
                        ResourceLocation.withDefaultNamespace("test"),
                        1.25,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.HEAD
        )), List.of(
                "Modifier:",
                "  -> Attribute: minecraft:block_break_speed",
                "  -> Attribute Modifier:",
                "    -> Id: minecraft:test",
                "    -> Amount: 1.25",
                "    -> Operation: ADD_VALUE",
                "  -> Slot: HEAD"
        ));
    }

    @Test
    public void testAttributeModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getAttributeModifierTooltip(UTILS, 0, "ali.property.branch.attribute_modifier", new AttributeModifier(
                ResourceLocation.withDefaultNamespace("test"),
                1.25,
                AttributeModifier.Operation.ADD_VALUE
        )), List.of(
                "Attribute Modifier:",
                "  -> Id: minecraft:test",
                "  -> Amount: 1.25",
                "  -> Operation: ADD_VALUE"
        ));
    }

    @Test
    public void testMobEffectInstanceTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectInstanceTooltip(UTILS, 0, "ali.property.branch.effect", new MobEffectInstance(
                MobEffects.BAD_OMEN,
                1,
                2,
                true,
                false,
                true,
                new MobEffectInstance(MobEffects.UNLUCK, 5)
        )), List.of(
                "Effect:",
                "  -> Mob Effect: minecraft:bad_omen",
                "  -> Duration: 1",
                "  -> Amplifier: 2",
                "  -> Ambient: true",
                "  -> Is Visible: false",
                "  -> Show Icon: true",
                "  -> Hidden Effect:",
                "    -> Mob Effect: minecraft:unluck",
                "    -> Duration: 5",
                "    -> Amplifier: 0",
                "    -> Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true"
        ));
    }

    @Test
    public void testRuleTooltip() {
        assertTooltip(GenericTooltipUtils.getRuleTooltip(UTILS, 0, "ali.property.branch.rule", new Tool.Rule(
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
        assertTooltip(GenericTooltipUtils.getMapDecorationEntryTooltip(UTILS, 0, new MapDecorations.Entry(
                MapDecorationTypes.DESERT_VILLAGE,
                2.5,
                0.25,
                0.3f
        )), List.of(
                "Type: minecraft:village_desert",
                "  -> X: 2.5",
                "  -> Z: 0.25",
                "  -> Rotation: 0.3"
        ));
    }

    @Test
    public void testItemStackTooltip() {
        assertUnorderedTooltip(GenericTooltipUtils.getItemStackTooltip(UTILS, 0, "ali.property.branch.item", new ItemStack(
                Holder.direct(Items.ANDESITE),
                10
        )), List.of(
                "Item:",
                "  -> Item: minecraft:andesite",
                "  -> Count: 10",
                "  -> Components:",
                List.of(
                        "    -> Type: minecraft:attribute_modifiers",
                        "      -> Show In Tooltip: true",
                        "    -> Type: minecraft:repair_cost",
                        "      -> Value: 0",
                        "    -> Type: minecraft:item_name",
                        "      -> Item Name: Andesite",
                        "    -> Type: minecraft:rarity",
                        "      -> Rarity: COMMON",
                        "    -> Type: minecraft:lore",
                        "    -> Type: minecraft:max_stack_size",
                        "      -> Value: 64",
                        "    -> Type: minecraft:enchantments",
                        "    -> Type: minecraft:item_model",
                        "      -> Id: minecraft:andesite"
                )
        ));
    }

    @Test
    public void testDataComponentMapTooltip() {
        assertTooltip(GenericTooltipUtils.getDataComponentMapTooltip(UTILS, 0, "ali.property.branch.components", DataComponentMap.builder()
                .set(DataComponents.DAMAGE, 2)
                .set(DataComponents.MAX_DAMAGE, 5)
                .build()
        ), List.of(
                "Components:",
                "  -> Type: minecraft:damage",
                "    -> Value: 2",
                "  -> Type: minecraft:max_damage",
                "    -> Value: 5"
        ));
    }

    @Test
    public void testSuspiciousStewEffectEntryTooltip() {
        assertTooltip(GenericTooltipUtils.getSuspiciousStewEffectEntryTooltip(UTILS, 0, "ali.property.branch.effect", new SuspiciousStewEffects.Entry(
                MobEffects.DAMAGE_BOOST,
                5
        )), List.of(
                "Effect:",
                "  -> Type: minecraft:strength",
                "  -> Duration: 5"
        ));
    }

    @Test
    public void testGlobalPosTooltip() {
        assertTooltip(GenericTooltipUtils.getGlobalPosTooltip(UTILS, 0, "ali.property.branch.global_pos", new GlobalPos(
                Level.NETHER,
                new BlockPos(10, 20, 30)
        )), List.of(
                "Global Position:",
                "  -> Dimension: minecraft:the_nether",
                "  -> Position:",
                "    -> X: 10",
                "    -> Y: 20",
                "    -> Z: 30"
        ));
    }

    @Test
    public void testBeehiveBlockEntityOccupantTooltip() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("test", 5);

        assertTooltip(GenericTooltipUtils.getBeehiveBlockEntityOccupantTooltip(UTILS, 0, "ali.property.branch.occupant", new BeehiveBlockEntity.Occupant(
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
        assertTooltip(GenericTooltipUtils.getEffectEntryTooltip(UTILS, 0, "ali.property.value.mob_effect", new SetStewEffectFunction.EffectEntry(
                MobEffects.LUCK,
                ConstantValue.exactly(3)
        )), List.of(
                "Mob Effect: minecraft:luck",
                "  -> Duration: 3"
        ));
    }

    @Test
    public void testProperty2Tooltip() {
        assertTooltip(GenericTooltipUtils.getPropertyTooltip(UTILS, 0, new Property("Hello", "World", "Sign")), List.of(
                "Name: Hello",
                "Value: World",
                "Signature: Sign"
        ));
    }

    @Test
    public void testLevelBasedValueTooltip() {
        assertTooltip(GenericTooltipUtils.getLevelBasedValueTooltip(UTILS, 0, "ali.property.branch.enchanted_chance", LevelBasedValue.constant(2.5F)), List.of(
                "Enchanted Chance:",
                "  -> Constant: 2.5"
        ));
        assertTooltip(GenericTooltipUtils.getLevelBasedValueTooltip(UTILS, 0, "ali.property.branch.enchanted_chance", new LevelBasedValue.Clamped(LevelBasedValue.constant(2.5F), 0.5F, 5F)), List.of(
                "Enchanted Chance:",
                "  -> Clamped:",
                "    -> Value:",
                "      -> Constant: 2.5",
                "    -> Min: 0.5",
                "    -> Max: 5.0"
        ));
        assertTooltip(GenericTooltipUtils.getLevelBasedValueTooltip(UTILS, 0, "ali.property.branch.enchanted_chance", new LevelBasedValue.Fraction(LevelBasedValue.constant(2), LevelBasedValue.constant(3))), List.of(
                "Enchanted Chance:",
                "  -> Fraction:",
                "    -> Numerator:",
                "      -> Constant: 2.0",
                "    -> Denominator:",
                "      -> Constant: 3.0"
        ));
        assertTooltip(GenericTooltipUtils.getLevelBasedValueTooltip(UTILS, 0, "ali.property.branch.enchanted_chance", new LevelBasedValue.Linear(0.5F, 5F)), List.of(
                "Enchanted Chance:",
                "  -> Linear:",
                "    -> Base: 0.5",
                "    -> Per Level: 5.0"
        ));
        assertTooltip(GenericTooltipUtils.getLevelBasedValueTooltip(UTILS, 0, "ali.property.branch.enchanted_chance", new LevelBasedValue.LevelsSquared(0.5F)), List.of(
                "Enchanted Chance:",
                "  -> Squared Level:",
                "    -> Added: 0.5"
        ));
        assertTooltip(GenericTooltipUtils.getLevelBasedValueTooltip(UTILS, 0, "ali.property.branch.enchanted_chance", new LevelBasedValue.Lookup(List.of(0.5F, 1.5F, 2.5F), LevelBasedValue.constant(3.3F))), List.of(
                "Enchanted Chance:",
                "  -> Lookup:",
                "    -> Values: [0.5, 1.5, 2.5]",
                "    -> Fallback:",
                "      -> Constant: 3.3"
        ));
    }

    @Test
    public void testMovementPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getMovementPredicateTooltip(UTILS, 0, "ali.property.branch.movement", new MovementPredicate(
                MinMaxBounds.Doubles.atMost(3),
                MinMaxBounds.Doubles.between(1, 2),
                MinMaxBounds.Doubles.atLeast(3),
                MinMaxBounds.Doubles.atLeast(2),
                MinMaxBounds.Doubles.atLeast(1.5F),
                MinMaxBounds.Doubles.atLeast(0.5F),
                MinMaxBounds.Doubles.atLeast(10)
        )), List.of(
                "Movement:",
                "  -> X: ≤3.0",
                "  -> Y: 1.0-2.0",
                "  -> Z: ≥3.0",
                "  -> Speed: ≥2.0",
                "  -> Horizontal Speed: ≥1.5",
                "  -> Vertical Speed: ≥0.5",
                "  -> Fall Distance: ≥10.0"
        ));
    }

    @Test
    public void testInputPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getInputPredicateTooltip(UTILS, 0, "ali.property.branch.input", new InputPredicate(
                Optional.of(true),
                Optional.of(true),
                Optional.of(false),
                Optional.of(false),
                Optional.of(false),
                Optional.of(true),
                Optional.of(true)
        )), List.of(
                "Input:",
                "  -> Forward: true",
                "  -> Backward: true",
                "  -> Left: false",
                "  -> Right: false",
                "  -> Jump: false",
                "  -> Sneak: true",
                "  -> Sprint: true"
        ));
    }
}
