package com.yanny.ali.test;

import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.ValueTooltipUtils;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.*;

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
        assertTooltip(ValueTooltipUtils.getFormulaTooltip(UTILS, new ApplyBonusCount.OreDrops()).build("ali.property.value.formula"), List.of(
                "Formula: minecraft:ore_drops"
        ));
        assertTooltip(ValueTooltipUtils.getFormulaTooltip(UTILS, new ApplyBonusCount.UniformBonusCount(2)).build("ali.property.value.formula"), List.of(
                "Formula: minecraft:uniform_bonus_count",
                "  -> Bonus Multiplier: 2"
        ));
        assertTooltip(ValueTooltipUtils.getFormulaTooltip(UTILS, new ApplyBonusCount.BinomialWithBonusCount(3, 0.51F)).build("ali.property.value.formula"), List.of(
                "Formula: minecraft:binomial_with_bonus_count",
                "  -> Extra Rounds: 3",
                "  -> Probability: 0.51"
        ));
    }

    @Test
    public void testPropertyTooltip() {
        assertTooltip(ValueTooltipUtils.getPropertyTooltip(UTILS, EnumProperty.create("bed", BedPart.class)).build("ali.property.value.null"), List.of("bed"));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(ValueTooltipUtils.getModifierTooltip(UTILS, new SetAttributesFunction.ModifierBuilder(
                "armor",
                Holder.direct(Attributes.ARMOR),
                AttributeModifier.Operation.MULTIPLY_TOTAL,
                UniformGenerator.between(1, 5))
                .forSlot(EquipmentSlot.HEAD)
                .forSlot(EquipmentSlot.CHEST)
                .forSlot(EquipmentSlot.LEGS)
                .forSlot(EquipmentSlot.FEET)
                .build()).build("ali.property.branch.modifier"), List.of(
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
        assertTooltip(ValueTooltipUtils.getUUIDTooltip(UTILS, UUID.nameUUIDFromBytes(new byte[]{1, 2, 3})).build("ali.property.value.uuid"), List.of("UUID: 5289df73-7df5-3326-bcdd-22597afb1fac"));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockStateProperties.FACING, Direction.EAST)
                .hasProperty(BlockStateProperties.LEVEL, 3)
                .build().orElseThrow()
        ).build("ali.property.branch.properties"), List.of(
                "Properties:",
                "  -> facing: east",
                "  -> level: 3"
        ));
    }

    @Test
    void testDamageSourcePredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getDamageSourcePredicateTooltip(UTILS, DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                .source(EntityPredicate.Builder.entity().of(EntityType.BAT))
                .direct(EntityPredicate.Builder.entity().of(EntityType.ARROW))
                .build()).build("ali.property.branch.predicate"), List.of(
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
        assertTooltip(ValueTooltipUtils.getTagPredicateTooltip(UTILS, TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR)).build("ali.property.value.null"), List.of("minecraft:bypasses_armor: true"));
        assertTooltip(ValueTooltipUtils.getTagPredicateTooltip(UTILS, TagPredicate.isNot(DamageTypeTags.BYPASSES_ARMOR)).build("ali.property.value.null"), List.of("minecraft:bypasses_armor: false"));
    }

    @Test
    public void testEntityPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("range", 5);
        assertTooltip(ValueTooltipUtils.getEntityPredicateTooltip(UTILS, EntityPredicate.Builder.entity()
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
        ).build("ali.property.branch.predicate"), List.of(
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
        assertTooltip(ValueTooltipUtils.getEntityTypePredicateTooltip(UTILS, EntityTypePredicate.of(EntityType.CAT)).build("ali.property.branch.entity_types"), List.of(
                "Entity Types:",
                "  -> minecraft:cat"
        ));
        assertTooltip(ValueTooltipUtils.getEntityTypePredicateTooltip(UTILS, EntityTypePredicate.of(EntityTypeTags.SKELETONS)).build("ali.property.branch.entity_types"), List.of(
                "Entity Types:",
                "  -> Tag: minecraft:skeletons"
        ));
    }

    @Test
    public void testDistancePredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getDistancePredicateTooltip(UTILS, new DistancePredicate(
                MinMaxBounds.Doubles.exactly(10),
                MinMaxBounds.Doubles.atLeast(20),
                MinMaxBounds.Doubles.atMost(30),
                MinMaxBounds.Doubles.atLeast(15),
                MinMaxBounds.Doubles.between(2, 5.5)
        )).build("ali.property.branch.distance_to_player"), List.of(
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
        assertTooltip(ValueTooltipUtils.getLocationPredicateTooltip(UTILS, LocationPredicate.Builder.location()
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
        ).build("ali.property.branch.location"), List.of(
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
        assertTooltip(ValueTooltipUtils.getPositionPredicateTooltip(UTILS, new LocationPredicate.PositionPredicate(
                MinMaxBounds.Doubles.atLeast(3),
                MinMaxBounds.Doubles.between(1, 2),
                MinMaxBounds.Doubles.atMost(4)
        )).key("ali.property.branch.position"), List.of(
                "Position:",
                "  -> X: ≥3.0",
                "  -> Y: 1.0-2.0",
                "  -> Z: ≤4.0"
        ));
    }

    @Test
    public void testLightPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getLightPredicateTooltip(UTILS, LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)).build()).build("ali.property.value.light"), List.of("Light: 10-15"));
    }

    @Test
    public void testBlockPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(ValueTooltipUtils.getBlockPredicateTooltip(UTILS, BlockPredicate.Builder.block().of(BlockTags.DIRT).build()).build("ali.property.branch.block_predicate"), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:dirt"
        ));
        assertTooltip(ValueTooltipUtils.getBlockPredicateTooltip(UTILS, BlockPredicate.Builder.block().of(BlockTags.BEDS).build()).build("ali.property.branch.block_predicate"), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:beds"
        ));
        assertTooltip(ValueTooltipUtils.getBlockPredicateTooltip(UTILS, BlockPredicate.Builder.block()
                .of(Blocks.STONE, Blocks.COBBLESTONE)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST))
                .hasNbt(compoundTag)
                .build()
        ).build("ali.property.branch.block_predicate"), List.of(
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

        assertTooltip(ValueTooltipUtils.getNbtPredicateTooltip(UTILS, new NbtPredicate(compoundTag)).build("ali.property.value.nbt"), List.of("Nbt: {test:3.0f}"));
    }

    @Test
    public void testFluidPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getFluidPredicateTooltip(UTILS, FluidPredicate.Builder.fluid()
                .of(FluidTags.WATER)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build().orElseThrow())
                .build()
        ).build("ali.property.branch.fluid_predicate"), List.of(
                "Fluid Predicate:",
                "  -> Tag: minecraft:water",
                "  -> Properties:",
                "    -> facing: east"
        ));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getMobEffectPredicateTooltip(UTILS, MobEffectsPredicate.Builder.effects()
                .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.between(10, 15), MinMaxBounds.Ints.atMost(5), Optional.of(true), Optional.of(false)))
                .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.atLeast(5), MinMaxBounds.Ints.between(1, 2), Optional.empty(), Optional.empty())).build().orElseThrow()
        ).build("ali.property.branch.mob_effects"), List.of(
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
        assertTooltip(ValueTooltipUtils.getEntityFlagsPredicateTooltip(UTILS, EntityFlagsPredicate.Builder.flags()
                .setOnFire(false)
                .setIsBaby(true)
                .setCrouching(true)
                .setSprinting(true)
                .setSwimming(false)
                .build()
        ).build("ali.property.branch.entity_flags"), List.of(
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
        assertTooltip(ValueTooltipUtils.getEntityEquipmentPredicateTooltip(UTILS, EntityEquipmentPredicate.Builder.equipment()
                .head(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(10, 15)))
                .chest(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atMost(5)))
                .legs(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.atLeast(5)))
                .feet(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.between(1, 2)))
                .mainhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(0, 64)))
                .offhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(32)))
                .build()).build("ali.property.branch.entity_equipment"), List.of(
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

        assertTooltip(ValueTooltipUtils.getItemPredicateTooltip(UTILS, ItemPredicate.Builder.item().of(ItemTags.AXES).build()).build("ali.type.condition.match_tool"), List.of(
                "Match Tool:",
                "  -> Tag: minecraft:axes"
        ));
        assertTooltip(ValueTooltipUtils.getItemPredicateTooltip(UTILS, ItemPredicate.Builder.item()
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
        ).build("ali.type.condition.match_tool"), List.of(
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
        assertTooltip(ValueTooltipUtils.getEnchantmentPredicateTooltip(UTILS, new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1))).build("ali.property.value.enchantment"), List.of(
                "Enchantment: ANY",
                "  -> Level: ≥1"
        ));
        assertTooltip(ValueTooltipUtils.getEnchantmentPredicateTooltip(UTILS, new EnchantmentPredicate(Enchantments.FALL_PROTECTION, MinMaxBounds.Ints.atMost(2))).build("ali.property.value.enchantment"), List.of(
                "Enchantment: minecraft:feather_falling",
                "  -> Level: ≤2"
        ));
    }

    @Test
    public void testEntitySubPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getEntitySubPredicateTooltip(UTILS, EntitySubPredicate.variant(FrogVariant.COLD)).build("ali.property.branch.entity_sub_predicate"), List.of(
                "Entity Sub Predicate:",
                "  -> Variant: minecraft:cold"
        ));
        assertTooltip(ValueTooltipUtils.getEntitySubPredicateTooltip(UTILS, EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.PERSIAN)))).build("ali.property.branch.entity_sub_predicate"), List.of(
                "Entity Sub Predicate:",
                "  -> Variant: minecraft:persian"
        ));
        assertTooltip(ValueTooltipUtils.getEntitySubPredicateTooltip(UTILS, new LightningBoltPredicate(MinMaxBounds.Ints.atLeast(2), Optional.of(EntityPredicate.Builder.entity().team("blue").build()))).build("ali.property.branch.entity_sub_predicate"), List.of(
                "Entity Sub Predicate:",
                "  -> Blocks On Fire: ≥2",
                "  -> Stuck Entity:",
                "    -> Team: blue"
        ));
        assertTooltip(ValueTooltipUtils.getEntitySubPredicateTooltip(UTILS, FishingHookPredicate.inOpenWater(true)).build("ali.property.branch.entity_sub_predicate"), List.of(
                "Entity Sub Predicate:",
                "  -> Is In Open Water: true"
        ));
        //noinspection deprecation
        assertUnorderedTooltip(ValueTooltipUtils.getEntitySubPredicateTooltip(UTILS, PlayerPredicate.Builder.player()
                .setLevel(MinMaxBounds.Ints.atLeast(3))
                .setGameType(GameType.SURVIVAL)
                .addStat(Stats.BLOCK_MINED, Blocks.COBBLESTONE.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(4))
                .addStat(Stats.ITEM_USED, Items.SALMON.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(5))
                .addRecipe(new ResourceLocation("recipe1"), true)
                .addRecipe(new ResourceLocation("recipe2"), false)
                .checkAdvancementDone(new ResourceLocation("first"), true)
                .checkAdvancementCriterions(new ResourceLocation("second"), Map.of("test", false))
                .build()).build("ali.property.branch.entity_sub_predicate"), List.of(
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
                "      -> Criterions:",
                "        -> test: false"
        ));
        assertTooltip(ValueTooltipUtils.getEntitySubPredicateTooltip(UTILS, SlimePredicate.sized(MinMaxBounds.Ints.atLeast(1))).build("ali.property.branch.entity_sub_predicate"), List.of(
                "Entity Sub Predicate:",
                "  -> Size: ≥1"
        ));
        assertTooltip(ValueTooltipUtils.getEntitySubPredicateTooltip(UTILS, EntitySubPredicate.Types.FROG.createPredicate(FrogVariant.COLD)).build("ali.property.branch.entity_sub_predicate"), List.of(
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

        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(UTILS, statMatcher), List.of(
                "Block: minecraft:cobblestone",
                "  -> Times Mined: ≥4"
        ));
    }

    @Test
    public void testBlockPosTooltip() {
        assertTooltip(ValueTooltipUtils.getBlockPosTooltip(UTILS, new BlockPos(10, 12, 14)).build("ali.property.multi.offset"), List.of(
                "Offset: [X: 10, Y: 12, Z: 14]"
        ));
    }

    @Test
    public void testEffectEntryTooltip() {
        assertTooltip(ValueTooltipUtils.getEffectEntryTooltip(UTILS, new SetStewEffectFunction.EffectEntry(
                Holder.direct(MobEffects.LUCK),
                ConstantValue.exactly(3)
        )).key("ali.property.value.mob_effect"), List.of(
                "Mob Effect: minecraft:luck",
                "  -> Duration: 3"
        ));
    }
}
