package com.yanny.ali.test;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yanny.aci.api.RangeValue;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.ValueTooltipUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.*;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.stats.Stats;
import net.minecraft.tags.*;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
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
    public void testTooltip() {
        EnchantedRanges chanceMap = new EnchantedRanges(2.5F);
        EnchantedRanges countMap = new EnchantedRanges(2, 10);

        chanceMap.computeLevels(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.LOOTING).orElseThrow(), (level, value) -> switch (level) {
            case 1 ->  new RangeValue(1.5f);
            case 2 -> new RangeValue(3F);
            case 3 -> new RangeValue(4.5F);
            default -> throw new IllegalStateException("Unexpected value: " + level);
        });
        countMap.computeLevels(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.FORTUNE).orElseThrow(), (level, value) -> switch (level) {
            case 1 -> new RangeValue(1, 2);
            case 2 -> new RangeValue(2, 4);
            case 3 -> new RangeValue(4, 8);
            default -> throw new IllegalStateException("Unexpected value: " + level);
        });

        assertTooltip(EntryTooltipUtils.getTooltip(
                UTILS,
                0,
                new EnchantedRanges(2.5F),
                new EnchantedRanges(2, 10),
                List.of(),
                List.of()
        ).build(), List.of(
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
        ).build(), List.of(
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
        assertTooltip(ValueTooltipUtils.getFormulaTooltip(UTILS, new ApplyBonusCount.OreDrops()).build(Lang.Value.FORMULA), List.of(
                "Formula: minecraft:ore_drops"
        ));
        assertTooltip(ValueTooltipUtils.getFormulaTooltip(UTILS, new ApplyBonusCount.UniformBonusCount(2)).build(Lang.Value.FORMULA), List.of(
                "Formula: minecraft:uniform_bonus_count",
                "  -> Bonus Multiplier: 2"
        ));
        assertTooltip(ValueTooltipUtils.getFormulaTooltip(UTILS, new ApplyBonusCount.BinomialWithBonusCount(3, 0.51F)).build(Lang.Value.FORMULA), List.of(
                "Formula: minecraft:binomial_with_bonus_count",
                "  -> Extra Rounds: 3",
                "  -> Probability: 0.51"
        ));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(ValueTooltipUtils.getModifierTooltip(UTILS, new SetAttributesFunction.ModifierBuilder(
                ResourceLocation.withDefaultNamespace("armor"),
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                UniformGenerator.between(1, 5))
                .forSlot(EquipmentSlotGroup.HEAD)
                .forSlot(EquipmentSlotGroup.CHEST)
                .forSlot(EquipmentSlotGroup.LEGS)
                .forSlot(EquipmentSlotGroup.FEET)
                .build()).build(Lang.Branch.MODIFIER), List.of(
                "Modifier:",
                "  -> Attribute: minecraft:generic.armor",
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
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockStateProperties.FACING, Direction.EAST)
                .hasProperty(BlockStateProperties.LEVEL, 3)
                .build().orElseThrow()
        ).build(Lang.Branch.PROPERTIES), List.of(
                "Properties:",
                "  -> facing: east",
                "  -> level: 3"
        ));
        assertTooltip(ValueTooltipUtils.getStatePropertiesPredicateTooltip(UTILS, new StatePropertiesPredicate(List.of(
                new StatePropertiesPredicate.PropertyMatcher("facing", new StatePropertiesPredicate.ExactMatcher("east")),
                new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.of("1"), Optional.of("5"))),
                new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.empty(), Optional.of("5"))),
                new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.of("1"), Optional.empty())),
                new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.empty(), Optional.empty()))
        ))).build(Lang.Branch.PROPERTIES), List.of(
                "Properties:",
                "  -> facing: east",
                "  -> level: 1-5",
                "  -> level: ≤5",
                "  -> level: ≥1",
                "  -> level: any"
        ));
    }

    @Test
    void testDamageSourcePredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getDamageSourcePredicateTooltip(UTILS, DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                .source(EntityPredicate.Builder.entity().of(EntityType.BAT))
                .direct(EntityPredicate.Builder.entity().of(EntityType.ARROW))
                .isDirect(false)
                .build()).build(Lang.Branch.PREDICATE), List.of(
                "Predicate:",
                "  -> Tags:",
                "    -> minecraft:bypasses_armor: true",
                "    -> minecraft:is_explosion: false",
                "  -> Direct Entity:",
                "    -> Entity Type: minecraft:arrow",
                "  -> Source Entity:",
                "    -> Entity Type: minecraft:bat",
                "  -> Is Direct: false"
        ));
    }

    @Test
    public void testTagPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getTagPredicateTooltip(UTILS, TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR)).build(), List.of("minecraft:bypasses_armor: true"));
        assertTooltip(ValueTooltipUtils.getTagPredicateTooltip(UTILS, TagPredicate.isNot(DamageTypeTags.BYPASSES_ARMOR)).build(), List.of("minecraft:bypasses_armor: false"));
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
                .equipment(EntityEquipmentPredicate.Builder.equipment().head(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE)))
                .subPredicate(EntitySubPredicates.CAT.createPredicate(HolderSet.direct(BuiltInRegistries.CAT_VARIANT.getHolderOrThrow(CatVariant.PERSIAN))))
                .vehicle(EntityPredicate.Builder.entity().team("blue"))
                .passenger(EntityPredicate.Builder.entity().team("white"))
                .targetedEntity(EntityPredicate.Builder.entity().team("red"))
                .periodicTick(1000)
                .moving(new MovementPredicate(MinMaxBounds.Doubles.between(1, 5), MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY))
                .team("orange")
                .slots(new SlotsPredicate(Map.of(SlotRange.of("test", IntList.of(1, 2)), ItemPredicate.Builder.item().of(Items.GRANITE).build()))).build()
        ).build(Lang.Branch.PREDICATE), List.of(
                "Predicate:",
                "  -> Entity Type: minecraft:cat",
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
                "      -> Variant: minecraft:persian",
                "  -> Vehicle:",
                "    -> Team: blue",
                "  -> Passenger:",
                "    -> Team: white",
                "  -> Targeted Entity:",
                "    -> Team: red",
                "  -> Team: orange",
                "  -> Slots:",
                "    -> test: [1, 2]",
                "      -> Predicate:",
                "        -> Item: minecraft:granite"
        ));
    }

    @Test
    public void testEntityTypePredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getEntityTypePredicateTooltip(UTILS, EntityTypePredicate.of(EntityType.CAT)).build(Lang.Branch.ENTITY_TYPES), List.of(
                "Entity Type: minecraft:cat"
        ));
        assertTooltip(ValueTooltipUtils.getEntityTypePredicateTooltip(UTILS, EntityTypePredicate.of(EntityTypeTags.SKELETONS)).build(Lang.Branch.ENTITY_TYPES), List.of(
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
        )).build(Lang.Branch.DISTANCE_TO_PLAYER), List.of(
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
                .setX(MinMaxBounds.Doubles.exactly(10D))
                .setY(MinMaxBounds.Doubles.atLeast(20D))
                .setZ(MinMaxBounds.Doubles.atMost(30D))
                .setBiomes(HolderSet.direct(LOOKUP.lookup(Registries.BIOME).orElseThrow().get(Biomes.PLAINS).orElseThrow()))
                .setStructures(HolderSet.direct(LOOKUP.lookup(Registries.STRUCTURE).orElseThrow().get(BuiltinStructures.MINESHAFT).orElseThrow()))
                .setDimension(Level.OVERWORLD)
                .setSmokey(true)
                .setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)))
                .setBlock(BlockPredicate.Builder.block().of(Blocks.STONE, Blocks.COBBLESTONE))
                .setFluid(FluidPredicate.Builder.fluid().of(Fluids.LAVA))
                .setCanSeeSky(true)
                .build()
        ).build(Lang.Branch.LOCATED), List.of(
                "Located:",
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
                "    -> Fluid: minecraft:lava",
                "  -> Can See Sky: true"
        ));
    }

    @Test
    public void testPositionPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getPositionPredicateTooltip(UTILS, new LocationPredicate.PositionPredicate(
                MinMaxBounds.Doubles.atLeast(3),
                MinMaxBounds.Doubles.between(1, 2),
                MinMaxBounds.Doubles.atMost(4)
        )).build(Lang.Branch.POSITION), List.of(
                "Position:",
                "  -> X: ≥3.0",
                "  -> Y: 1.0-2.0",
                "  -> Z: ≤4.0"
        ));
    }

    @Test
    public void testLightPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getLightPredicateTooltip(UTILS, LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)).build()).build(Lang.Value.LIGHT), List.of("Light: 10-15"));
    }

    @Test
    public void testBlockPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(ValueTooltipUtils.getBlockPredicateTooltip(UTILS, BlockPredicate.Builder.block().of(Blocks.DIRT).build()).build(Lang.Branch.BLOCK_PREDICATE), List.of(
                "Block Predicate:",
                "  -> Block: minecraft:dirt"
        ));
        assertTooltip(ValueTooltipUtils.getBlockPredicateTooltip(UTILS, BlockPredicate.Builder.block().of(BlockTags.BEDS).build()).build(Lang.Branch.BLOCK_PREDICATE), List.of(
                "Block Predicate:",
                "  -> Blocks:",
                "    -> Tag: minecraft:beds"
        ));
        assertTooltip(ValueTooltipUtils.getBlockPredicateTooltip(UTILS, BlockPredicate.Builder.block()
                .of(Blocks.STONE)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST))
                .hasNbt(compoundTag)
                .build()
        ).build(Lang.Branch.BLOCK_PREDICATE), List.of(
                "Block Predicate:",
                "  -> Block: minecraft:stone",
                "  -> Properties:",
                "    -> facing: east",
                "  -> Nbt: {test:3.0f}"
        ));
    }

    @Test
    public void testNbtPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);

        assertTooltip(ValueTooltipUtils.getNbtPredicateTooltip(UTILS, new NbtPredicate(compoundTag)).build(Lang.Value.NBT), List.of("Nbt: {test:3.0f}"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testFluidPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getFluidPredicateTooltip(UTILS, FluidPredicate.Builder.fluid()
                .of(Fluids.WATER)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build().orElseThrow())
                .build()
        ).build(Lang.Branch.FLUID_PREDICATE), List.of(
                "Fluid Predicate:",
                "  -> Fluid: minecraft:water",
                "  -> Properties:",
                "    -> facing: east"
        ));
        assertTooltip(ValueTooltipUtils.getFluidPredicateTooltip(UTILS, FluidPredicate.Builder.fluid().of(HolderSet.direct(Fluids.LAVA.builtInRegistryHolder())).build()).build(Lang.Branch.FLUID_PREDICATE), List.of(
                "Fluid Predicate:",
                "  -> Fluid: minecraft:lava"
        ));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getMobEffectPredicateTooltip(UTILS, MobEffectsPredicate.Builder.effects()
                .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.between(10, 15), MinMaxBounds.Ints.atMost(5), Optional.of(true), Optional.of(false)))
                .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.atLeast(5), MinMaxBounds.Ints.between(1, 2), Optional.empty(), Optional.empty())).build().orElseThrow()
        ).build(Lang.Branch.MOB_EFFECTS), List.of(
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
                .setIsFlying(true)
                .setOnGround(false)
                .build()
        ).build(Lang.Branch.ENTITY_FLAGS), List.of(
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
        assertTooltip(ValueTooltipUtils.getEntityEquipmentPredicateTooltip(UTILS, EntityEquipmentPredicate.Builder.equipment()
                .head(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(10, 15)))
                .chest(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atMost(5)))
                .legs(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(5)))
                .feet(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(1, 2)))
                .body(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(1, 3)))
                .mainhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(0, 64)))
                .offhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(32)))
                .build()).build(Lang.Branch.ENTITY_EQUIPMENT), List.of(
            "Entity Equipment:",
            "  -> Head:",
            "    -> Count: 10-15",
            "  -> Chest:",
            "    -> Count: ≤5",
            "  -> Legs:",
            "    -> Count: ≥5",
            "  -> Feet:",
            "    -> Count: 1-2",
            "  -> Body:",
            "    -> Count: 1-3",
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

        assertTooltip(ValueTooltipUtils.getItemPredicateTooltip(UTILS, ItemPredicate.Builder.item()
                .of(ItemTags.AXES)
                .hasComponents(DataComponentPredicate.builder().expect(DataComponents.BASE_COLOR, DyeColor.BLUE).build())
                .build()
        ).build(Lang.Conditions.MATCH_TOOL), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> Tag: minecraft:axes",
                "  -> Components:",
                "    -> minecraft:base_color",
                "      -> Color: BLUE"
        ));
        assertTooltip(ValueTooltipUtils.getItemPredicateTooltip(UTILS, ItemPredicate.Builder.item()
                .of(Items.CAKE, Items.NETHERITE_AXE)
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
        ).build(Lang.Conditions.MATCH_TOOL), List.of(
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
                "      -> Predicate:",
                "        -> Enchantment: minecraft:smite",
                "        -> Level: ≥1",
                "      -> Predicate:",
                "        -> Enchantment: minecraft:mending",
                "        -> Level: 2-4",
                "    -> Stored Enchantments:",
                "      -> Predicate:",
                "        -> Enchantment: minecraft:depth_strider",
                "        -> Level: ≤5",
                "      -> Predicate:",
                "        -> Enchantment: minecraft:lure",
                "        -> Level: ≥4",
                "    -> Potions:",
                "      -> minecraft:healing",
                "    -> Custom Data:",
                "      -> Nbt: {healing:1b}"
        ));
    }

    @Test
    public void testEnchantmentPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getEnchantmentPredicateTooltip(UTILS, new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1))).build(), List.of(
                "Predicate:",
                "  -> Level: ≥1"
        ));
        assertTooltip(ValueTooltipUtils.getEnchantmentPredicateTooltip(UTILS, new EnchantmentPredicate(
                LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FEATHER_FALLING),
                MinMaxBounds.Ints.atMost(2))
        ).build(), List.of(
                "Predicate:",
                "  -> Enchantment: minecraft:feather_falling",
                "  -> Level: ≤2"
        ));
        assertTooltip(ValueTooltipUtils.getEnchantmentPredicateTooltip(UTILS, new EnchantmentPredicate(
                LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.BOOTS_EXCLUSIVE).orElseThrow(),
                MinMaxBounds.Ints.atMost(2))
        ).build(), List.of(
                "Predicate:",
                "  -> Enchantments:",
                "    -> Tag: minecraft:exclusive_set/boots",
                "  -> Level: ≤2"
        ));
        assertTooltip(ValueTooltipUtils.getEnchantmentPredicateTooltip(UTILS, new EnchantmentPredicate(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FEATHER_FALLING), MinMaxBounds.Ints.ANY)).build(), List.of(
                "Predicate:",
                "  -> Enchantment: minecraft:feather_falling"
        ));
    }

    @Test
    public void testGameTypeTooltip() {
        assertTooltip(ValueTooltipUtils.getGameTypePredicateTooltip(UTILS, GameTypePredicate.of(GameType.SPECTATOR)).build(Lang.Branch.GAME_TYPES), List.of(
                "Game Type: SPECTATOR"
        ));
    }

    @Test
    public void testPropertyMatcherTooltip() {
        assertTooltip(ValueTooltipUtils.getPropertyMatcherTooltip(UTILS, new StatePropertiesPredicate.PropertyMatcher("hello", new StatePropertiesPredicate.ExactMatcher("world"))).build(), List.of("hello: world"));
    }

    @Test
    public void testStatMatcherTooltip() {
        //noinspection deprecation
        PlayerPredicate.StatMatcher<?> statMatcher = new PlayerPredicate.StatMatcher<>(
                Stats.BLOCK_MINED,
                Blocks.COBBLESTONE.builtInRegistryHolder(),
                MinMaxBounds.Ints.atLeast(4)
        );

        assertTooltip(ValueTooltipUtils.getStatMatcherTooltip(UTILS, statMatcher).build(), List.of(
                "Block: minecraft:cobblestone",
                "  -> Times Mined: ≥4"
        ));
    }

    @Test
    public void testBlockPosTooltip() {
        assertTooltip(ValueTooltipUtils.getBlockPosTooltip(UTILS, new BlockPos(10, 12, 14)).build(Lang.Multi.OFFSET), List.of(
                "Offset: [X: 10, Y: 12, Z: 14]"
        ));
    }

    @Test
    public void testListOperationTooltip() {
        assertTooltip(ValueTooltipUtils.getListOperationTooltip(UTILS, new ListOperation.Insert(1)).build(Lang.Value.LIST_OPERATION), List.of(
                "List Operation: INSERT",
                "  -> Offset: 1"
        ));
        assertTooltip(ValueTooltipUtils.getListOperationTooltip(UTILS, new ListOperation.ReplaceSection(1, Optional.of(2))).build(Lang.Value.LIST_OPERATION), List.of(
                "List Operation: REPLACE_SECTION",
                "  -> Offset: 1",
                "  -> Size: 2"
        ));
    }

    @Test
    public void testContainerComponentManipulatorTooltip() {
        assertTooltip(ValueTooltipUtils.getContainerComponentManipulatorTooltip(UTILS, ContainerComponentManipulators.CHARGED_PROJECTILES).build(Lang.Value.CONTAINER), List.of("Container: minecraft:charged_projectiles"));
    }

    @Test
    public void testNbtPathTooltip() throws CommandSyntaxException {
        assertTooltip(ValueTooltipUtils.getNbtPathTooltip(UTILS, NbtPathArgument.NbtPath.of("asdf")).build(Lang.Value.SOURCE_PATH), List.of("Source Path: asdf"));
    }

    @Test
    public void testDataComponentPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getDataComponentPredicateTooltip(UTILS, DataComponentPredicate.builder()
                .expect(DataComponents.DAMAGE, 3)
                .expect(DataComponents.MAX_DAMAGE, 8).build()
        ).build(Lang.Branch.COMPONENT_PREDICATES), List.of(
                "Component Predicates:",
                "  -> minecraft:damage",
                "    -> Value: 3",
                "  -> minecraft:max_damage",
                "    -> Value: 8"
        ));
    }

    @Test
    public void testTypedDataComponentTooltip() {
        assertTooltip(ValueTooltipUtils.getTypedDataComponentTooltip(UTILS, new TypedDataComponent<>(DataComponents.DAMAGE, 3)).build(Lang.Value.COMPONENT), List.of(
                "Component: minecraft:damage",
                "  -> Value: 3"
        ));
    }

    @Test
    public void testCollectionPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getCollectionPredicateTooltip(UTILS,
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
        ).build(Lang.Branch.PAGES), List.of(
                "Pages:",
                "  -> Contains:",
                "    -> Page: Hello",
                "    -> Page: World",
                "  -> Counts:",
                "    -> Page: Star",
                "      -> Count: 1-5",
                "    -> Page: Wars",
                "      -> Count: ≥3",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testFireworkPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getFireworkPredicateTooltip(UTILS, new ItemFireworkExplosionPredicate.FireworkPredicate(
                Optional.of(FireworkExplosion.Shape.CREEPER),
                Optional.of(true),
                Optional.of(false)
        )).build(Lang.Branch.CONDITION), List.of(
                "Predicate:",
                "  -> Shape: CREEPER",
                "  -> Trail: false",
                "  -> Twinkle: true"
        ));
    }

    @Test
    public void testPagePredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getPagePredicateTooltip(UTILS, new ItemWritableBookPredicate.PagePredicate("asdf")).build(Lang.Value.PAGE), List.of("Page: asdf"));
        assertTooltip(ValueTooltipUtils.getPagePredicateTooltip(UTILS, new ItemWrittenBookPredicate.PagePredicate(Component.literal("asdf"))).build(Lang.Value.PAGE), List.of("Page: asdf"));
    }

    @Test
    public void testEntryPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getEntryPredicateTooltip(UTILS, new ItemAttributeModifiersPredicate.EntryPredicate(
                Optional.of(HolderSet.direct(Attributes.ARMOR, Attributes.GRAVITY)),
                Optional.of(ResourceLocation.withDefaultNamespace("test")),
                MinMaxBounds.Doubles.between(1.5, 3.14),
                Optional.of(AttributeModifier.Operation.ADD_VALUE),
                Optional.of(EquipmentSlotGroup.ARMOR)
        )).build(Lang.Branch.CONDITION), List.of(
                "Predicate:",
                "  -> Attributes:",
                "    -> minecraft:generic.armor",
                "    -> minecraft:generic.gravity",
                "  -> Id: minecraft:test",
                "  -> Amount: 1.5-3.1",
                "  -> Operation: ADD_VALUE",
                "  -> Slot: ARMOR"
        ));
    }

    @Test
    public void testDataComponentPatchTooltip() {
        assertTooltip(ValueTooltipUtils.getDataComponentPatchTooltip(UTILS, DataComponentPatch.builder()
                .remove(DataComponents.DAMAGE)
                .remove(DataComponents.MAX_DAMAGE)
                .set(DataComponents.BASE_COLOR, DyeColor.BLUE)
                .set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
                .set(new TypedDataComponent<>(DataComponents.CUSTOM_NAME, Component.literal("Hello"))).build()
        ).build(Lang.Branch.COMPONENTS), List.of(
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
        assertTooltip(ValueTooltipUtils.getFireworkExplosionTooltip(UTILS, new FireworkExplosion(
                FireworkExplosion.Shape.STAR,
                IntList.of(1, 2, 3),
                IntList.of(3, 4, 5),
                true,
                false
        )).build(Lang.Branch.EXPLOSION), List.of(
                "Explosion:",
                "  -> Shape: STAR",
                "  -> Colors: [1, 2, 3]",
                "  -> Fade Colors: [3, 4, 5]",
                "  -> Has Trail: true",
                "  -> Has Twinkle: false"
        ));
    }

    @Test
    public void testFilterableTooltip() {
        assertTooltip(ValueTooltipUtils.getFilterableTooltip(UTILS, new Filterable<>("Hello", Optional.of("World"))).build(Lang.Branch.PAGE), List.of(
                "Page:",
                "  -> Raw: Hello",
                "  -> Filtered: World"
        ));
    }

    @Test
    public void testItemAttributeModifiersEntryTooltip() {
        assertTooltip(ValueTooltipUtils.getItemAttributeModifiersEntryTooltip(UTILS, new ItemAttributeModifiers.Entry(
                Attributes.BLOCK_BREAK_SPEED,
                new AttributeModifier(
                        ResourceLocation.withDefaultNamespace("test"),
                        1.25,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.HEAD
        )).build(Lang.Branch.MODIFIER), List.of(
                "Modifier:",
                "  -> Attribute: minecraft:player.block_break_speed",
                "  -> Modifier:",
                "    -> Id: minecraft:test",
                "    -> Amount: 1.25",
                "    -> Operation: ADD_VALUE",
                "  -> Slot: HEAD"
        ));
    }

    @Test
    public void testAttributeModifierTooltip() {
        assertTooltip(ValueTooltipUtils.getAttributeModifierTooltip(UTILS, new AttributeModifier(
                ResourceLocation.withDefaultNamespace("test"),
                1.25,
                AttributeModifier.Operation.ADD_VALUE
        )).build(Lang.Branch.ATTRIBUTE_MODIFIER), List.of(
                "Attribute Modifier:",
                "  -> Id: minecraft:test",
                "  -> Amount: 1.25",
                "  -> Operation: ADD_VALUE"
        ));
    }

    @Test
    public void testPossibleEffectTooltip() {
        assertTooltip(ValueTooltipUtils.getPossibleEffectTooltip(UTILS, new FoodProperties.PossibleEffect(
                new MobEffectInstance(MobEffects.LUCK, 1),
                0.5f
        )).build(), List.of(
                "Possible Effect:",
                "  -> Effect:",
                "    -> Effect: minecraft:luck",
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
        assertTooltip(ValueTooltipUtils.getMobEffectInstanceTooltip(UTILS, new MobEffectInstance(
                MobEffects.BAD_OMEN,
                1,
                2,
                true,
                false,
                true,
                new MobEffectInstance(MobEffects.UNLUCK, 5)
        )).build(Lang.Branch.EFFECT), List.of(
                "Effect:",
                "  -> Effect: minecraft:bad_omen",
                "  -> Duration: 1",
                "  -> Amplifier: 2",
                "  -> Ambient: true",
                "  -> Is Visible: false",
                "  -> Show Icon: true",
                "  -> Hidden Effect:",
                "    -> Effect: minecraft:unluck",
                "    -> Duration: 5",
                "    -> Amplifier: 0",
                "    -> Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true"
        ));
    }

    @Test
    public void testRuleTooltip() {
        assertTooltip(ValueTooltipUtils.getRuleTooltip(UTILS, new Tool.Rule(
                HolderSet.direct(Holder.direct(Blocks.DIRT), Holder.direct(Blocks.COBBLESTONE)),
                Optional.of(0.25f),
                Optional.of(true)
        )).build(Lang.Branch.RULE), List.of(
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
        assertTooltip(ValueTooltipUtils.getMapDecorationEntryTooltip(UTILS, new MapDecorations.Entry(
                MapDecorationTypes.DESERT_VILLAGE,
                2.5,
                0.25,
                0.3f
        )).build(), List.of(
                "minecraft:village_desert",
                "  -> X: 2.5",
                "  -> Z: 0.25",
                "  -> Rotation: 0.3"
        ));
    }

    @Test
    public void testItemStackTooltip() {
        assertTooltip(ValueTooltipUtils.getItemStackTooltip(UTILS, new ItemStack(
                Holder.direct(Items.ANDESITE),
                10,
                DataComponentPatch.builder()
                        .set(DataComponents.DAMAGE, 2)
                        .remove(DataComponents.HIDE_TOOLTIP)
                        .build()
        )).build(Lang.Branch.ITEM), List.of(
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
        assertTooltip(ValueTooltipUtils.getDataComponentMapTooltip(UTILS, DataComponentMap.builder()
                .set(DataComponents.DAMAGE, 2)
                .set(DataComponents.MAX_DAMAGE, 5)
                .build()
        ).build(Lang.Branch.COMPONENTS), List.of(
                "Components:",
                "  -> minecraft:damage",
                "    -> Value: 2",
                "  -> minecraft:max_damage",
                "    -> Value: 5"
        ));
    }

    @Test
    public void testSuspiciousStewEffectEntryTooltip() {
        assertTooltip(ValueTooltipUtils.getSuspiciousStewEffectEntryTooltip(UTILS, new SuspiciousStewEffects.Entry(
                MobEffects.DAMAGE_BOOST,
                5
        )).build(), List.of(
                "minecraft:strength",
                "  -> Duration: 5"
        ));
    }

    @Test
    public void testGlobalPosTooltip() {
        assertTooltip(ValueTooltipUtils.getGlobalPosTooltip(UTILS, new GlobalPos(
                Level.NETHER,
                new BlockPos(10, 20, 30)
        )).build(Lang.Branch.GLOBAL_POS), List.of(
                "Global Position:",
                "  -> Dimension: minecraft:the_nether",
                "  -> Position: [X: 10, Y: 20, Z: 30]"
        ));
    }

    @Test
    public void testBeehiveBlockEntityOccupantTooltip() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("test", 5);

        assertTooltip(ValueTooltipUtils.getBeehiveBlockEntityOccupantTooltip(UTILS, new BeehiveBlockEntity.Occupant(
                CustomData.of(tag),
                2,
                3
        )).build(Lang.Branch.OCCUPANT), List.of(
                "Occupant:",
                "  -> Entity Data: {test:5}",
                "  -> Ticks In Hive: 2",
                "  -> Min Ticks In Hive: 3"
        ));
    }

    @Test
    public void testEffectEntryTooltip() {
        assertTooltip(ValueTooltipUtils.getEffectEntryTooltip(UTILS, new SetStewEffectFunction.EffectEntry(
                MobEffects.LUCK,
                ConstantValue.exactly(3)
        )).build(Lang.Value.EFFECT), List.of(
                "Effect: minecraft:luck",
                "  -> Duration: 3"
        ));
    }

    @Test
    public void testProperty2Tooltip() {
        assertTooltip(ValueTooltipUtils.getAuthPropertyTooltip(UTILS, new Property("Hello", "World", "Sign")).build(Lang.Branch.VALUES), List.of(
                "Values:",
                "  -> Name: Hello",
                "  -> Value: World",
                "  -> Signature: Sign"
        ));
    }

    @Test
    public void testLevelBasedValueTooltip() {
        assertTooltip(ValueTooltipUtils.getLevelBasedValueTooltip(UTILS, LevelBasedValue.constant(2.5F)).build(Lang.Branch.ENCHANTED_CHANCE), List.of(
                "Enchanted Chance:",
                "  -> Constant: 2.5"
        ));
        assertTooltip(ValueTooltipUtils.getLevelBasedValueTooltip(UTILS, new LevelBasedValue.Clamped(LevelBasedValue.constant(2.5F), 0.5F, 5F)).build(Lang.Branch.ENCHANTED_CHANCE), List.of(
                "Enchanted Chance:",
                "  -> Clamped:",
                "    -> Value:",
                "      -> Constant: 2.5",
                "    -> Min: 0.5",
                "    -> Max: 5.0"
        ));
        assertTooltip(ValueTooltipUtils.getLevelBasedValueTooltip(UTILS, new LevelBasedValue.Fraction(LevelBasedValue.constant(2), LevelBasedValue.constant(3))).build(Lang.Branch.ENCHANTED_CHANCE), List.of(
                "Enchanted Chance:",
                "  -> Fraction:",
                "    -> Numerator:",
                "      -> Constant: 2.0",
                "    -> Denominator:",
                "      -> Constant: 3.0"
        ));
        assertTooltip(ValueTooltipUtils.getLevelBasedValueTooltip(UTILS, new LevelBasedValue.Linear(0.5F, 5F)).build(Lang.Branch.ENCHANTED_CHANCE), List.of(
                "Enchanted Chance:",
                "  -> Linear:",
                "    -> Base: 0.5",
                "    -> Per Level: 5.0"
        ));
        assertTooltip(ValueTooltipUtils.getLevelBasedValueTooltip(UTILS, new LevelBasedValue.LevelsSquared(0.5F)).build(Lang.Branch.ENCHANTED_CHANCE), List.of(
                "Enchanted Chance:",
                "  -> Squared Level:",
                "    -> Added: 0.5"
        ));
        assertTooltip(ValueTooltipUtils.getLevelBasedValueTooltip(UTILS, new LevelBasedValue.Lookup(List.of(0.5F, 1.5F, 2.5F), LevelBasedValue.constant(3.3F))).build(Lang.Branch.ENCHANTED_CHANCE), List.of(
                "Enchanted Chance:",
                "  -> Lookup:",
                "    -> Values: [0.5, 1.5, 2.5]",
                "    -> Fallback:",
                "      -> Constant: 3.3"
        ));
    }

    @Test
    public void testMovementPredicateTooltip() {
        assertTooltip(ValueTooltipUtils.getMovementPredicateTooltip(UTILS, new MovementPredicate(
                MinMaxBounds.Doubles.atMost(3),
                MinMaxBounds.Doubles.between(1, 2),
                MinMaxBounds.Doubles.atLeast(3),
                MinMaxBounds.Doubles.atLeast(2),
                MinMaxBounds.Doubles.atLeast(1.5F),
                MinMaxBounds.Doubles.atLeast(0.5F),
                MinMaxBounds.Doubles.atLeast(10)
        )).build(Lang.Branch.MOVEMENT), List.of(
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
    public void testSlotPredicateTooltip() {
        Map<SlotRange, ItemPredicate> map = new LinkedHashMap<>();

        map.put(SlotRange.of("test", IntList.of(1, 2, 3)), ItemPredicate.Builder.item().of(Items.ANDESITE).build());
        map.put(SlotRange.of("help", IntList.of(2, 1, 0)), ItemPredicate.Builder.item().of(Items.DIORITE, Items.GRANITE).build());

        assertTooltip(ValueTooltipUtils.getSlotPredicateTooltip(UTILS, new SlotsPredicate(map)).build(Lang.Branch.SLOTS), List.of(
                "Slots:",
                "  -> test: [1, 2, 3]",
                "    -> Predicate:",
                "      -> Item: minecraft:andesite",
                "  -> help: [2, 1, 0]",
                "    -> Predicate:",
                "      -> Items:",
                "        -> minecraft:diorite",
                "        -> minecraft:granite"
        ));
    }
}
