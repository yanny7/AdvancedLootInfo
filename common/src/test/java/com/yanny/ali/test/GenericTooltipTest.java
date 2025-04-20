package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.client.EntryTooltipUtils;
import com.yanny.ali.plugin.client.GenericTooltipUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
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
        Map<Enchantment, Map<Integer, RangeValue>> chanceMap = EntryTooltipUtils.getBaseMap(2.5F);
        Map<Enchantment, Map<Integer, RangeValue>> countMap = EntryTooltipUtils.getBaseMap(2, 10);
        Map<Integer, RangeValue> bonusChanceMap = new LinkedHashMap<>();
        Map<Integer, RangeValue> bonusCountMap = new LinkedHashMap<>();

        bonusChanceMap.put(1, new RangeValue(1.5F));
        bonusChanceMap.put(2, new RangeValue(3F));
        bonusChanceMap.put(3, new RangeValue(4.5F));
        bonusCountMap.put(1, new RangeValue(1, 2));
        bonusCountMap.put(2, new RangeValue(2, 4));
        bonusCountMap.put(3, new RangeValue(4, 8));
        chanceMap.put(Enchantments.MOB_LOOTING, bonusChanceMap);
        countMap.put(Enchantments.BLOCK_FORTUNE, bonusCountMap);

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
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, 0, new ApplyBonusCount.OreDrops()), List.of(
                "Formula: minecraft:ore_drops"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, 0, new ApplyBonusCount.UniformBonusCount(2)), List.of(
                "Formula: minecraft:uniform_bonus_count",
                "  -> Bonus Multiplier: 2"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(UTILS, 0, new ApplyBonusCount.BinomialWithBonusCount(3, 0.51F)), List.of(
                "Formula: minecraft:binomial_with_bonus_count",
                "  -> Extra Rounds: 3",
                "  -> Probability: 0.51"
        ));
    }

    @Test
    public void testBlockTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockTooltip(UTILS, 1, Blocks.DIAMOND_BLOCK), List.of("  -> Block: Block of Diamond"));
    }

    @Test
    public void testPropertyTooltip() {
        assertTooltip(GenericTooltipUtils.getPropertyTooltip(UTILS, 1, EnumProperty.create("bed", BedPart.class)), List.of("  -> bed [head, foot]"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(UTILS, 0, Enchantments.AQUA_AFFINITY), List.of("Enchantment: Aqua Affinity"));
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(UTILS, 1, Enchantments.FIRE_ASPECT), List.of("  -> Enchantment: Fire Aspect"));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getModifierTooltip(UTILS, 0, new SetAttributesFunction.ModifierBuilder(
                "armor",
                Attributes.ARMOR,
                AttributeModifier.Operation.MULTIPLY_TOTAL,
                UniformGenerator.between(1, 5))
                        .forSlot(EquipmentSlot.HEAD)
                        .forSlot(EquipmentSlot.CHEST)
                        .forSlot(EquipmentSlot.LEGS)
                        .forSlot(EquipmentSlot.FEET)
                        .build()), List.of(
                "Modifier:",
                "  -> Name: armor",
                "  -> Attribute: Armor",
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
    public void testAttributeTooltip() {
        assertTooltip(GenericTooltipUtils.getAttributeTooltip(UTILS, 0, Attributes.JUMP_STRENGTH), List.of("Attribute: Horse Jump Strength"));
    }

    @Test
    public void testUUIDTooltip() {
        assertTooltip(GenericTooltipUtils.getUUIDTooltip(UTILS, 0, UUID.nameUUIDFromBytes(new byte[]{1, 2, 3})), List.of("UUID: 5289df73-7df5-3326-bcdd-22597afb1fac"));
    }

    @Test
    public void testBannerPatternsSlotsTooltip() {
        assertTooltip(GenericTooltipUtils.getBannerPatternsTooltip(UTILS, 0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getBannerPatternsTooltip(UTILS, 0, List.of(
                Pair.of(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.BASE))), DyeColor.WHITE),
                Pair.of(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.CREEPER))), DyeColor.GREEN)
        )), List.of(
                "Banner Patterns:",
                "  -> Banner Pattern: minecraft:base",
                "    -> Color: WHITE",
                "  -> Banner Pattern: minecraft:creeper",
                "    -> Color: GREEN"
        ));
    }

    @Test
    public void testBannerPatternSlotsTooltip() {
        assertTooltip(GenericTooltipUtils.getBannerPatternTooltip(UTILS, 0, Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.FLOWER))), List.of("Banner Pattern: minecraft:flower"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockEntityTypeTooltip(UTILS, 0, BlockEntityType.BEACON), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(GenericTooltipUtils.getPotionTooltip(UTILS, 0, Potions.HEALING), List.of(
                "Potion: minecraft:healing"
        ));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectTooltip(UTILS, 0, MobEffects.CONFUSION), List.of("Mob Effect: minecraft:nausea"));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, 0, StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockStateProperties.FACING, Direction.EAST)
                .build()
        ), List.of(
                "State Properties:",
                "  -> facing: east"
        ));
    }

    @Test
    void testDamageSourcePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(UTILS, 0, DamageSourcePredicate.ANY), List.of());
        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(UTILS, 0, DamageSourcePredicate.Builder.damageType()
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

        assertTooltip(GenericTooltipUtils.getEntityPredicateTooltip(UTILS, 0, EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(EntityType.CAT))
                .distance(new DistancePredicate(MinMaxBounds.Doubles.exactly(10), MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY))
                .located(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atLeast(20)).build())
                .steppingOn(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atMost(30)).build())
                .effects(MobEffectsPredicate.effects()
                        .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, true, null))
                        .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, null, false))
                )
                .nbt(new NbtPredicate(compoundTag))
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true).build())
                .equipment(EntityEquipmentPredicate.Builder.equipment().head(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE).build()).build())
                .subPredicate(EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.CALICO))))
                .vehicle(EntityPredicate.Builder.entity().team("blue").build())
                .passenger(EntityPredicate.Builder.entity().team("white").build())
                .targetedEntity(EntityPredicate.Builder.entity().team("red").build())
                .team("orange")
                .build()
        ), List.of(
                "Entity Type: Cat",
                "Distance to Player:",
                "  -> X: =10.0",
                "Location:",
                "  -> X: ≥20.0",
                "Stepping on Location:",
                "  -> X: ≤30.0",
                "Mob Effects:",
                "  -> Mob Effect: minecraft:absorption",
                "    -> Is Ambient: true",
                "  -> Mob Effect: minecraft:blindness",
                "    -> Is Visible: false",
                "Nbt: {range:5}",
                "Entity Flags:",
                "  -> Is Baby: true",
                "Entity Equipment:",
                "  -> Head:",
                "    -> Items:",
                "      -> Item: Andesite",
                "      -> Item: Diorite",
                "Entity Sub Predicate:",
                "  -> Variant: minecraft:calico",
                "Vehicle:",
                "  -> Team: blue",
                "Passenger:",
                "  -> Team: white",
                "Targeted Entity:",
                "  -> Team: red",
                "Team: orange"
        ));
    }

    @Test
    public void testEntityTypePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, 0, EntityTypePredicate.of(EntityType.CAT)), List.of("Entity Type: Cat"));
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(UTILS, 0, EntityTypePredicate.of(EntityTypeTags.SKELETONS)), List.of("Entity Type: minecraft:skeletons"));
    }

    @Test
    public void testDistancePredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getDistancePredicateTooltip(UTILS, 0, new DistancePredicate(
                MinMaxBounds.Doubles.exactly(10),
                MinMaxBounds.Doubles.atLeast(20),
                MinMaxBounds.Doubles.atMost(30),
                MinMaxBounds.Doubles.atLeast(15),
                MinMaxBounds.Doubles.between(2, 5.5)
        )), List.of(
                "X: =10.0",
                "Y: ≥20.0",
                "Z: ≤30.0",
                "Horizontal: ≥15.0",
                "Absolute: 2.0-5.5"
        ));
    }

    @Test
    public void testLocationPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getLocationPredicateTooltip(UTILS, 0, LocationPredicate.Builder.location()
                .setX(MinMaxBounds.Doubles.exactly(10))
                .setY(MinMaxBounds.Doubles.atLeast(20))
                .setZ(MinMaxBounds.Doubles.atMost(30))
                .setBiome(Biomes.PLAINS)
                .setStructure(BuiltinStructures.MINESHAFT)
                .setDimension(Level.OVERWORLD)
                .setSmokey(true)
                .setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)).build())
                .setBlock(BlockPredicate.Builder.block().of(Blocks.STONE, Blocks.COBBLESTONE).build())
                .setFluid(FluidPredicate.Builder.fluid().of(Fluids.LAVA).build())
                .build()
        ), List.of(
                "X: =10.0",
                "Y: ≥20.0",
                "Z: ≤30.0",
                "Biome: minecraft:plains",
                "Structure: minecraft:mineshaft",
                "Dimension: minecraft:overworld",
                "Smokey: true",
                "Light: 10-15",
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Block: Stone",
                "    -> Block: Cobblestone",
                "Fluid Predicate:",
                "  -> Fluid: minecraft:lava"
        ));
    }

    @Test
    public void testLightPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getLightPredicateTooltip(UTILS, 0, LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)).build()), List.of("Light: 10-15"));
    }

    @Test
    public void testBlockPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, BlockPredicate.Builder.block().of(BlockTags.DIRT).build()), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:dirt"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, BlockPredicate.Builder.block().of(BlockTags.BEDS).build()), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:beds"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(UTILS, 0, BlockPredicate.Builder.block()
                .of(Blocks.STONE, Blocks.COBBLESTONE)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build())
                .hasNbt(compoundTag)
                .build()
        ), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Block: Stone",
                "    -> Block: Cobblestone",
                "State Properties:",
                "  -> facing: east",
                "Nbt: {test:3.0f}"
        ));
    }

    @Test
    public void testNbtPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(UTILS, 0, new NbtPredicate(null)), List.of());
        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(UTILS, 0, new NbtPredicate(compoundTag)), List.of("Nbt: {test:3.0f}"));
    }

    @Test
    public void testFluidPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(UTILS, 0, FluidPredicate.Builder.fluid()
                .of(FluidTags.WATER)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build())
                .build()
        ), List.of(
                "Fluid Predicate:",
                "  -> Tag: minecraft:water",
                "  -> State Properties:",
                "    -> facing: east"
        ));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(GenericTooltipUtils.getFluidTooltip(UTILS, 0, Fluids.WATER), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectPredicateTooltip(UTILS, 0, MobEffectsPredicate.effects()
                .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.between(10, 15), MinMaxBounds.Ints.atMost(5), true, false))
                .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.atLeast(5), MinMaxBounds.Ints.between(1, 2), null, null))
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
        assertTooltip(GenericTooltipUtils.getMobEffectInstancePredicateTooltip(UTILS, 0, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.between(10, 15), MinMaxBounds.Ints.atMost(5), true, false)), List.of(
                "Amplifier: 10-15",
                "Duration: ≤5",
                "Is Ambient: true",
                "Is Visible: false"
        ));
    }

    @Test
    public void testEntityFlagsPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntityFlagsPredicateTooltip(UTILS, 0, EntityFlagsPredicate.Builder.flags()
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
        assertTooltip(GenericTooltipUtils.getEntityEquipmentPredicateTooltip(UTILS, 0, EntityEquipmentPredicate.Builder.equipment()
                .head(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(10, 15)).build())
                .chest(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atMost(5)).build())
                .legs(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.atLeast(5)).build())
                .feet(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.between(1, 2)).build())
                .mainhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(0, 64)).build())
                .offhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(32)).build())
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

        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, 0, ItemPredicate.Builder.item().of(ItemTags.AXES).build()), List.of(
                "Tag: minecraft:axes"
        ));
        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(UTILS, 0, ItemPredicate.Builder.item()
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
                "Items:",
                "  -> Item: Cake",
                "  -> Item: Netherite Axe",
                "Count: 10-15",
                "Durability: ≤5",
                "Enchantments:",
                "  -> Enchantment: Smite",
                "    -> Level: ≥1",
                "  -> Enchantment: Mending",
                "    -> Level: 2-4",
                "Stored Enchantments:",
                "  -> Enchantment: Depth Strider",
                "    -> Level: ≤5",
                "  -> Enchantment: Lure",
                "    -> Level: ≥4",
                "Potion: minecraft:healing",
                "Nbt: {healing:1b}"
        ));
    }

    @Test
    public void testEnchantmentPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(UTILS, 0, EnchantmentPredicate.ANY), List.of());
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(UTILS, 0, new EnchantmentPredicate(Enchantments.FALL_PROTECTION, MinMaxBounds.Ints.atMost(2))), List.of(
                "Enchantment: Feather Falling",
                "  -> Level: ≤2"
        ));
    }

    @Test
    public void testEntitySubPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, EntitySubPredicate.variant(FrogVariant.COLD)), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:cold"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.PERSIAN)))), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:persian"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, new LighthingBoltPredicate(MinMaxBounds.Ints.atLeast(2), EntityPredicate.Builder.entity().team("blue").build())), List.of(
                "Entity Sub Predicate:",
                "  -> Blocks On Fire: ≥2",
                "  -> Stuck Entity:",
                "    -> Team: blue"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, FishingHookPredicate.inOpenWater(true)), List.of(
                "Entity Sub Predicate:",
                "  -> Is In Open Water: true"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, PlayerPredicate.Builder.player()
                .setLevel(MinMaxBounds.Ints.atLeast(3))
                .setGameType(GameType.SURVIVAL)
                .addStat(Stats.BLOCK_MINED.get(Blocks.COBBLESTONE), MinMaxBounds.Ints.atLeast(4))
                .addStat(Stats.ITEM_USED.get(Items.SALMON), MinMaxBounds.Ints.atLeast(5))
                .addRecipe(new ResourceLocation("recipe1"), true)
                .addRecipe(new ResourceLocation("recipe2"), false)
                .checkAdvancementDone(new ResourceLocation("first"), true)
                .checkAdvancementDone(new ResourceLocation("second"), false)
                .build()), List.of(
                "Entity Sub Predicate:",
                "  -> Level: ≥3",
                "  -> Game Type: Survival",
                "  -> Stats:",
                "    -> Item: Raw Salmon",
                "      -> Times Used: ≥5",
                "    -> Block: Cobblestone",
                "      -> Times Mined: ≥4",
                "  -> Recipes:",
                "    -> minecraft:recipe1: true",
                "    -> minecraft:recipe2: false",
                "  -> Advancements:",
                "    -> minecraft:first",
                "      -> Done: true",
                "    -> minecraft:second",
                "      -> Done: false"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, SlimePredicate.sized(MinMaxBounds.Ints.atLeast(1))), List.of(
                "Entity Sub Predicate:",
                "  -> Size: ≥1"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(UTILS, 0, EntitySubPredicate.Types.FROG.createPredicate(FrogVariant.COLD)), List.of(
                "Entity Sub Predicate:",
                "  -> Variant: minecraft:cold"
        ));
    }

    @Test
    public void testItemTooltip() {
        assertTooltip(GenericTooltipUtils.getItemTooltip(UTILS, 0, Items.ACACIA_DOOR), List.of(
                "Item: Acacia Door"
        ));
    }

    @Test
    public void testGameTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getGameTypeTooltip(UTILS, 0, GameType.SPECTATOR), List.of(
                "Game Type: Spectator"
        ));
    }

    @Test
    public void testStatsTooltip() {
        Map<Stat<?>, MinMaxBounds.Ints> statMap = new LinkedHashMap<>();

        statMap.put(Stats.BLOCK_MINED.get(Blocks.COBBLESTONE), MinMaxBounds.Ints.atLeast(2));
        statMap.put(Stats.ITEM_USED.get(Items.SALMON), MinMaxBounds.Ints.atLeast(3));
        statMap.put(Stats.ENTITY_KILLED.get(EntityType.BAT), MinMaxBounds.Ints.atLeast(5));

        assertTooltip(GenericTooltipUtils.getStatsTooltip(UTILS, 0, Map.of()), List.of());
        assertTooltip(GenericTooltipUtils.getStatsTooltip(UTILS, 0, statMap), List.of(
                "Stats:",
                "  -> Block: Cobblestone",
                "    -> Times Mined: ≥2",
                "  -> Item: Raw Salmon",
                "    -> Times Used: ≥3",
                "  -> entity.minecraft.bat",
                "    -> You killed %s %s: ≥5" //FIXME this should be fixed
        ));
    }

    @Test
    public void testRecipesTooltip() {
        Object2BooleanMap<ResourceLocation> recipeList = new Object2BooleanArrayMap<>();

        recipeList.put(new ResourceLocation("furnace_recipe"), true);
        recipeList.put(new ResourceLocation("apple_recipe"), false);

        assertTooltip(GenericTooltipUtils.getRecipesTooltip(UTILS, 0, new Object2BooleanArrayMap<>()), List.of());
        assertTooltip(GenericTooltipUtils.getRecipesTooltip(UTILS, 0, recipeList), List.of(
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

        assertTooltip(GenericTooltipUtils.getAdvancementsTooltip(UTILS, 0, Map.of()), List.of());
        assertTooltip(GenericTooltipUtils.getAdvancementsTooltip(UTILS, 0, predicateMap), List.of(
                "Advancements:",
                "  -> minecraft:first",
                "    -> Done: true",
                "  -> minecraft:second",
                "    -> test: true"
        ));
    }

    @Test
    public void testBlockPosTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockPosTooltip(UTILS, 0, new BlockPos(10, 12, 14)), List.of(
                "X: 10",
                "Y: 12",
                "Z: 14"
        ));
    }
}
