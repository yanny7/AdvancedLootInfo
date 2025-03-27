package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinApplyBonusCount;
import com.yanny.ali.mixin.MixinLootPoolEntryContainer;
import com.yanny.ali.mixin.MixinLootPoolSingletonContainer;
import com.yanny.ali.plugin.GenericTooltipUtils;
import com.yanny.ali.plugin.condition.KilledByPlayerAliCondition;
import com.yanny.ali.plugin.condition.SurvivesExplosionAliCondition;
import com.yanny.ali.plugin.entry.AlternativesEntry;
import com.yanny.ali.plugin.entry.EmptyEntry;
import com.yanny.ali.plugin.function.ExplosionDecayAliFunction;
import com.yanny.ali.plugin.function.FurnaceSmeltAliFunction;
import com.yanny.ali.plugin.function.SetAttributesAliFunction;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.plugin.GenericTooltipUtils.pad;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;
import static org.mockito.Mockito.*;

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

        assertTooltip(pad(1, Component.literal("Hello")), "  -> Hello");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTooltip() {
        Map<Integer, RangeValue> bonusChanceMap = new LinkedHashMap<>();
        Map<Integer, RangeValue> bonusCountMap = new LinkedHashMap<>();
        IContext context = mock(IContext.class);
        ICommonUtils utils = mock(ICommonUtils.class);
        LootPoolEntryContainer container = mock(LootPoolEntryContainer.class, withSettings().extraInterfaces(MixinLootPoolSingletonContainer.class, MixinLootPoolEntryContainer.class));
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        bonusChanceMap.put(1, new RangeValue(1.5F));
        bonusChanceMap.put(2, new RangeValue(3F));
        bonusChanceMap.put(3, new RangeValue(4.5F));
        bonusCountMap.put(1, new RangeValue(1, 2));
        bonusCountMap.put(2, new RangeValue(2, 4));
        bonusCountMap.put(3, new RangeValue(4, 8));

        when(((MixinLootPoolSingletonContainer) container).getQuality()).thenReturn(5);
        when(((MixinLootPoolSingletonContainer) container).getFunctions()).thenReturn(List.of());
        when(((MixinLootPoolEntryContainer) container).getConditions()).thenReturn(List.of());
        when(context.utils()).thenReturn(utils);
        when(utils.convertConditions(any(), any())).thenReturn(List.of());
        when(utils.convertFunctions(any(), any())).thenReturn(List.of());
        when(utils.decodeConditions(any(), any())).thenReturn(List.of(new SurvivesExplosionAliCondition(context, buf)), List.of());

        assertTooltip(GenericTooltipUtils.getTooltip(
                mock(AlternativesEntry.class),
                new RangeValue(2.5F),
                Optional.empty(),
                new RangeValue(2, 10),
                Optional.empty(),
                List.of(),
                List.of()
        ), List.of(
                "Chance: 2.50%",
                "Count: 2-10"
        ));
        assertTooltip(GenericTooltipUtils.getTooltip(
                new EmptyEntry(context, container),
                new RangeValue(2.5F),
                Optional.of(Pair.of(Holder.direct(Enchantments.LOOTING), bonusChanceMap)),
                new RangeValue(2, 10),
                Optional.of(Pair.of(Holder.direct(Enchantments.FORTUNE), bonusCountMap)),
                List.of(new ExplosionDecayAliFunction(context, buf), new FurnaceSmeltAliFunction(context, buf)),
                List.of(new KilledByPlayerAliCondition(context, buf))
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
                "----- Conditions -----",
                "Must be killed by player",
                "----- Functions -----",
                "Explosion Decay",
                "  -> Conditions:",
                "    -> Must survive explosion",
                "Use Smelting Recipe On Item"
        ));
    }

    @Test
    public void testFormulaTooltip() {
        ApplyBonusCount.UniformBonusCount uniformBonusCount = mock(ApplyBonusCount.UniformBonusCount.class, withSettings().extraInterfaces(MixinApplyBonusCount.UniformBonusCount.class));
        ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCount = mock(ApplyBonusCount.BinomialWithBonusCount.class, withSettings().extraInterfaces(MixinApplyBonusCount.BinomialWithBonusCount.class));

        when(uniformBonusCount.getType()).thenReturn(ApplyBonusCount.UniformBonusCount.TYPE);
        when(binomialWithBonusCount.getType()).thenReturn(ApplyBonusCount.BinomialWithBonusCount.TYPE);
        when(((MixinApplyBonusCount.UniformBonusCount) uniformBonusCount).getBonusMultiplier()).thenReturn(2);
        when(((MixinApplyBonusCount.BinomialWithBonusCount) binomialWithBonusCount).getExtraRounds()).thenReturn(3);
        when(((MixinApplyBonusCount.BinomialWithBonusCount) binomialWithBonusCount).getProbability()).thenReturn(0.51F);

        assertTooltip(GenericTooltipUtils.getFormulaTooltip(0, new ApplyBonusCount.OreDrops()), List.of(
                "Formula: minecraft:ore_drops"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(0, uniformBonusCount), List.of(
                "Formula: minecraft:uniform_bonus_count",
                "  -> Bonus Multiplier: 2"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(0, binomialWithBonusCount), List.of(
                "Formula: minecraft:binomial_with_bonus_count",
                "  -> Extra Rounds: 3",
                "  -> Probability: 0.51"
        ));
    }

    @Test
    public void testNameSourceTooltip() {
        assertTooltip(GenericTooltipUtils.getNameSourceTooltip(0, CopyNameFunction.NameSource.THIS), List.of("Source: This Entity"));
        assertTooltip(GenericTooltipUtils.getNameSourceTooltip(1, CopyNameFunction.NameSource.BLOCK_ENTITY), List.of("  -> Source: Block Entity"));
        assertTooltip(GenericTooltipUtils.getNameSourceTooltip(2, CopyNameFunction.NameSource.KILLER), List.of("    -> Source: Killer Entity"));
        assertTooltip(GenericTooltipUtils.getNameSourceTooltip(3, CopyNameFunction.NameSource.KILLER_PLAYER), List.of("      -> Source: Last Damaged By Player"));
    }

    @Test
    public void testBlockTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockTooltip(1, Blocks.DIAMOND_BLOCK), List.of("  -> Block: Block of Diamond"));
    }

    @Test
    public void testPropertyTooltip() {
        assertTooltip(GenericTooltipUtils.getPropertyTooltip(1, EnumProperty.create("bed", BedPart.class)), List.of("  -> bed [head, foot]"));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(0, Enchantments.AQUA_AFFINITY), List.of("Enchantment: Aqua Affinity"));
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(1, Enchantments.FIRE_ASPECT), List.of("  -> Enchantment: Fire Aspect"));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getModifierTooltip(0, new SetAttributesAliFunction.Modifier(
                "armor",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                new RangeValue(1, 5),
                Optional.empty(),
                List.of(new EquipmentSlotGroup[]{EquipmentSlotGroup.HEAD, EquipmentSlotGroup.CHEST, EquipmentSlotGroup.LEGS, EquipmentSlotGroup.FEET})
        )), List.of(
                "Modifier:",
                "  -> Name: armor",
                "  -> Attribute: Armor",
                "  -> Operation: MULTIPLY_TOTAL",
                "  -> Amount: 1-5",
                "  -> Equipment Slots:",
                "    -> HEAD",
                "    -> CHEST",
                "    -> LEGS",
                "    -> FEET"
        ));
    }

    @Test
    public void testAttributeTooltip() {
        assertTooltip(GenericTooltipUtils.getAttributeTooltip(0, Attributes.JUMP_STRENGTH), List.of("Attribute: Jump Strength"));
    }

    @Test
    public void testOperationTooltip() {
        assertTooltip(GenericTooltipUtils.getOperationTooltip(0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), List.of("Operation: MULTIPLY_BASE"));
    }

    @Test
    public void testUUIDTooltip() {
        assertTooltip(GenericTooltipUtils.getUUIDTooltip(0, UUID.nameUUIDFromBytes(new byte[]{1, 2, 3})), List.of("UUID: 5289df73-7df5-3326-bcdd-22597afb1fac"));
    }

    @Test
    public void testBannerPatternsSlotsTooltip() {
        /* FIXME
        assertTooltip(GenericTooltipUtils.getBannerPatternLayersTooltip(0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getBannerPatternLayersTooltip(0, List.of(
                Pair.of(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.BASE))), DyeColor.WHITE),
                Pair.of(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.CREEPER))), DyeColor.GREEN)
        )), List.of(
                "Banner Patterns:",
                "  -> Banner Pattern: minecraft:base",
                "    -> Color: WHITE",
                "  -> Banner Pattern: minecraft:creeper",
                "    -> Color: GREEN"
        ));*/
    }

    @Test
    public void testBannerPatternSlotsTooltip() {
        //FIXME
        //assertTooltip(GenericTooltipUtils.getBannerPatternTooltip(0, Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.FLOWER))), List.of("Banner Pattern: minecraft:flower"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockEntityTypeTooltip(0, BlockEntityType.BEACON), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(GenericTooltipUtils.getPotionTooltip(0, Optional.of(Potions.HEALING)), List.of(
                "Potion:",
                "  -> Mob Effects:",
                "    -> Mob Effect: minecraft:instant_health",
                "      -> Amplifier: 0",
                "      -> Duration: 1",
                "      -> Is Ambient: false",
                "      -> Is Visible: true",
                "      -> Show Icon: true"
        ));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectTooltip(0, MobEffects.CONFUSION), List.of("Mob Effect: minecraft:nausea"));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        assertTooltip(GenericTooltipUtils.getStatePropertiesPredicateTooltip(0, StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST).build().orElseThrow()), List.of(
                "State Properties:",
                "  -> facing: east"
        ));
    }

    @Test
    public void testPropertyMatcherTooltip() {
        StatePropertiesPredicate.PropertyMatcher exactMatcher = new StatePropertiesPredicate.PropertyMatcher("facing", new StatePropertiesPredicate.ExactMatcher("east"));
        StatePropertiesPredicate.PropertyMatcher rangedMatcher1 = new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.of("1"), Optional.of("7")));
        StatePropertiesPredicate.PropertyMatcher rangedMatcher2 = new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.of("2"), Optional.empty()));
        StatePropertiesPredicate.PropertyMatcher rangedMatcher3 = new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.empty(), Optional.of("6")));

        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, exactMatcher), List.of("facing: east"));
        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, rangedMatcher1), List.of("level: 1-7"));
        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, rangedMatcher2), List.of("level: ≥2"));
        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, rangedMatcher3), List.of("level: ≤6"));
    }

    @Test
    void testDamageSourcePredicateTooltip() {
        DamageSourcePredicate damageSourcePredicate = DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                .build();

        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(0, damageSourcePredicate), List.of(
                "Damage Source:",
                "  -> Tags:",
                "    -> minecraft:bypasses_armor: true",
                "    -> minecraft:is_explosion: false"
        ));
    }

    @Test
    public void testTagPredicatesTooltip() {
        assertTooltip(GenericTooltipUtils.getTagPredicateTooltip(0, TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR)), List.of("minecraft:bypasses_armor: true"));
    }

    @Test
    public void testEntityPredicateTooltip() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("range", 5);

        EntityPredicate entityPredicate = EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(EntityType.CAT))
                .distance(new DistancePredicate(MinMaxBounds.Doubles.exactly(10), MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY))
                .located(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atLeast(20)))
                .steppingOn(LocationPredicate.Builder.location().setX(MinMaxBounds.Doubles.atMost(30)))
                .effects(MobEffectsPredicate.Builder.effects()
                        .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.of(true), Optional.empty()))
                        .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.of(false))))
                .nbt(new NbtPredicate(tag))
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true))
                .equipment(EntityEquipmentPredicate.Builder.equipment().head(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE)))
//                .subPredicate(EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.CALICO)))) FIXME
                .vehicle(EntityPredicate.Builder.entity().team("blue"))
                .passenger(EntityPredicate.Builder.entity().team("white"))
                .targetedEntity(EntityPredicate.Builder.entity().team("red"))
                .team("orange")
                .build();

        assertTooltip(GenericTooltipUtils.getEntityPredicateTooltip(0, entityPredicate), List.of(
                "Entity Types:",
                "  -> Entity Type: Cat",
                "Distance to Player:",
                "  -> X: =10.0",
                "Location:",
                "  -> Position:",
                "    -> X: ≥20.0",
                "Stepping on Location:",
                "  -> Position:",
                "    -> X: ≤30.0",
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
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(0, new EntityTypePredicate(HolderSet.direct())), List.of());
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(0, EntityTypePredicate.of(EntityType.CAT)), List.of(
                "Entity Types:",
                "  -> Entity Type: Cat"
        ));
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(0, EntityTypePredicate.of(EntityTypeTags.SKELETONS)), List.of(
                "Entity Types:",
                "  -> Tag: minecraft:skeletons"
        ));
    }

    @Test
    public void testDistancePredicateTooltip() {
        DistancePredicate distancePredicate = new DistancePredicate(
                MinMaxBounds.Doubles.exactly(10D),
                MinMaxBounds.Doubles.atLeast(20D),
                MinMaxBounds.Doubles.atMost(30D),
                MinMaxBounds.Doubles.atLeast(15D),
                MinMaxBounds.Doubles.between(2D, 5.5D)
        );

        assertTooltip(GenericTooltipUtils.getDistancePredicateTooltip(0, distancePredicate), List.of(
                "X: =10.0",
                "Y: ≥20.0",
                "Z: ≤30.0",
                "Horizontal: ≥15.0",
                "Absolute: 2.0-5.5"
        ));
    }

    @Test
    public void testLocationPredicateTooltip() {
        LocationPredicate locationPredicate = LocationPredicate.Builder.location()
                .setX(MinMaxBounds.Doubles.exactly(10D))
                .setY(MinMaxBounds.Doubles.atLeast(20D))
                .setZ(MinMaxBounds.Doubles.atMost(30D))
//                .setBiome(Biomes.PLAINS) FIXME
//                .setStructure(BuiltinStructures.MINESHAFT)
                .setDimension(Level.OVERWORLD)
                .setSmokey(true)
                .setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)))
                .setBlock(BlockPredicate.Builder.block().of(Blocks.STONE, Blocks.COBBLESTONE))
                .setFluid(FluidPredicate.Builder.fluid().of(Fluids.LAVA))
                .build();

        assertTooltip(GenericTooltipUtils.getLocationPredicateTooltip(0, locationPredicate), List.of(
                "Position:",
                "  -> X: =10.0",
                "  -> Y: ≥20.0",
                "  -> Z: ≤30.0",
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
        LightPredicate lightPredicate = LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.between(10, 15)).build();

        assertTooltip(GenericTooltipUtils.getLightPredicateTooltip(0, lightPredicate), List.of("Light: 10-15"));
    }

    @Test
    public void testBlockPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();
        BlockPredicate blockPredicate = BlockPredicate.Builder.block()
                .of(BlockTags.BASE_STONE_OVERWORLD)
                .of(Blocks.STONE, Blocks.COBBLESTONE)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST))
                .hasNbt(compoundTag).build();

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(0, BlockPredicate.Builder.block().of(BlockTags.DIRT).of().build()), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:dirt"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(0, BlockPredicate.Builder.block().of(BlockTags.BEDS).build()), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:beds"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(0, blockPredicate), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:base_stone_overworld",
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
        NbtPredicate nbtPredicate = new NbtPredicate(compoundTag);

        compoundTag.putFloat("test", 3F);

        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(0, nbtPredicate), List.of("Nbt: {test:3.0f}"));
    }

    @Test
    public void testFluidPredicateTooltip() {
        FluidPredicate fluidPredicate = FluidPredicate.Builder.fluid()
//                .of(FluidTags.WATER) FIXME
                .of(Fluids.WATER)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.FACING, Direction.EAST).build().orElseThrow())
                .build();

        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(0, fluidPredicate), List.of(
                "Fluid Predicate:",
                "  -> Tag: minecraft:water",
                "  -> Fluid: minecraft:water",
                "  -> State Properties:",
                "    -> facing: east"
        ));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(GenericTooltipUtils.getFluidTooltip(0, Fluids.WATER), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        Optional<MobEffectsPredicate> mobEffectPredicate = MobEffectsPredicate.Builder.effects()
                .and(MobEffects.ABSORPTION, new MobEffectsPredicate.MobEffectInstancePredicate(
                        MinMaxBounds.Ints.between(10, 15),
                        MinMaxBounds.Ints.atMost(5),
                        Optional.of(true),
                        Optional.of(false)
                ))
                .and(MobEffects.BLINDNESS, new MobEffectsPredicate.MobEffectInstancePredicate(
                        MinMaxBounds.Ints.atLeast(5),
                        MinMaxBounds.Ints.between(1, 2),
                        Optional.empty(),
                        Optional.empty()
                ))
                .build();

        assertTooltip(GenericTooltipUtils.getMobEffectPredicateTooltip(0, mobEffectPredicate.orElseThrow()), List.of(
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
        MobEffectsPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = new MobEffectsPredicate.MobEffectInstancePredicate(
                MinMaxBounds.Ints.between(10, 15),
                MinMaxBounds.Ints.atMost(5),
                Optional.of(true),
                Optional.of(false)
        );

        assertTooltip(GenericTooltipUtils.getMobEffectInstancePredicateTooltip(0, mobEffectInstancePredicate), List.of(
                "Amplifier: 10-15",
                "Duration: ≤5",
                "Is Ambient: true",
                "Is Visible: false"
        ));
    }

    @Test
    public void testEntityFlagsPredicateTooltip() {
        EntityFlagsPredicate entityFlagsPredicate = EntityFlagsPredicate.Builder.flags()
                .setIsBaby(true)
                .setOnFire(false)
                .setCrouching(true)
                .setSprinting(true)
                .setSwimming(false)
                .build();

        assertTooltip(GenericTooltipUtils.getEntityFlagsPredicateTooltip(0, entityFlagsPredicate), List.of(
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
        EntityEquipmentPredicate entityEquipmentPredicate = EntityEquipmentPredicate.Builder.equipment()
                .head(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(10, 15)))
                .chest(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atMost(5)))
//                .legs(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.atLeast(5))) FIXME
//                .feet(ItemPredicate.Builder.item().hasDurability(MinMaxBounds.Ints.between(1, 2)))
                .mainhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.between(0, 64)))
                .offhand(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(32)))
                .build();

        assertTooltip(GenericTooltipUtils.getEntityEquipmentPredicateTooltip(0, entityEquipmentPredicate), List.of(
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
        ItemPredicate itemPredicate = ItemPredicate.Builder.item()
                .of(ItemTags.BANNERS)
                .of(Items.CAKE, Items.NETHERITE_AXE)
                .withCount(MinMaxBounds.Ints.between(10, 15))
//                .hasDurability(MinMaxBounds.Ints.atMost(5)) FIXME
//                .hasEnchantment(new EnchantmentPredicate(Enchantments.SMITE, MinMaxBounds.Ints.atLeast(1)))
//                .hasEnchantment(new EnchantmentPredicate(Enchantments.MENDING, MinMaxBounds.Ints.between(2, 4)))
//                .hasStoredEnchantment(new EnchantmentPredicate(Enchantments.DEPTH_STRIDER, MinMaxBounds.Ints.atMost(5)))
//                .hasStoredEnchantment(new EnchantmentPredicate(Enchantments.FISHING_SPEED, MinMaxBounds.Ints.atLeast(4)))
//                .isPotion(Potions.HEALING)
//                .hasNbt(compoundTag)
                .build();

        compoundTag.putBoolean("healing", true);

        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(0, ItemPredicate.Builder.item().of(ItemTags.AXES).of().build()), List.of(
                "Tag: minecraft:axes"
        ));
        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(0, itemPredicate), List.of(
                "Tag: minecraft:banners",
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
                "Potion:",
                "  -> Mob Effects:",
                "    -> Mob Effect: minecraft:instant_health",
                "      -> Amplifier: 0",
                "      -> Duration: 1",
                "      -> Is Ambient: false",
                "      -> Is Visible: true",
                "      -> Show Icon: true",
                "Nbt: {healing:1b}"
        ));
    }

    @Test
    public void testEnchantmentPredicateTooltip() {
        EnchantmentPredicate enchantmentPredicate = new EnchantmentPredicate(Enchantments.FEATHER_FALLING, MinMaxBounds.Ints.atMost(2));

        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(0, enchantmentPredicate), List.of(
                "Enchantment: Feather Falling",
                "  -> Level: ≤2"
        ));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testEntitySubPredicateTooltip() {
        EntitySubPredicate lightningBoltPredicate = new LightningBoltPredicate(
                MinMaxBounds.Ints.atLeast(2),
                Optional.of(EntityPredicate.Builder.entity().team("blue").build())
        );
        EntitySubPredicate fishingHookPredicate = FishingHookPredicate.inOpenWater(true);
        EntitySubPredicate playerPredicate = PlayerPredicate.Builder.player()
                .setLevel(MinMaxBounds.Ints.atLeast(3))
                .setGameType(GameType.SURVIVAL)
                .addStat(Stats.BLOCK_MINED, Blocks.COBBLESTONE.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(4))
                .addStat(Stats.ITEM_USED, Items.SALMON.builtInRegistryHolder(), MinMaxBounds.Ints.atLeast(5))
                .addRecipe(new ResourceLocation("recipe1"), true)
                .addRecipe(new ResourceLocation("recipe2"), false)
                .checkAdvancementDone(new ResourceLocation("first"), true)
                .checkAdvancementDone(new ResourceLocation("second"), false)
                .build();
        EntitySubPredicate slimePredicate = SlimePredicate.sized(MinMaxBounds.Ints.atLeast(1));

        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, EntitySubPredicate.variant(FrogVariant.COLD)), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:cold"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.PERSIAN)))), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:persian"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, lightningBoltPredicate), List.of(
                "Entity Sub Predicate:",
                "  -> Blocks On Fire: ≥2",
                "  -> Stuck Entity:",
                "    -> Team: blue"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, fishingHookPredicate), List.of(
                "Entity Sub Predicate:",
                "  -> Is In Open Water: true"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, playerPredicate), List.of(
                "Entity Sub Predicate:",
                "  -> Level: ≥3",
                "  -> Game Type: Survival",
                "  -> Stats:",
                "    -> Block: Cobblestone",
                "      -> Times Mined: ≥4",
                "    -> Item: Raw Salmon",
                "      -> Times Used: ≥5",
                "  -> Recipes:",
                "    -> minecraft:recipe1: true",
                "    -> minecraft:recipe2: false",
                "  -> Advancements:",
                "    -> minecraft:first",
                "      -> Done: true",
                "    -> minecraft:second",
                "      -> Done: false"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, slimePredicate), List.of(
                "Entity Sub Predicate:",
                "  -> Size: ≥1"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, EntitySubPredicate.Types.FROG.createPredicate(FrogVariant.COLD)), List.of(
                "Entity Sub Predicate:",
                "  -> Variant: minecraft:cold"
        ));
    }

    @Test
    public void testItemTooltip() {
        assertTooltip(GenericTooltipUtils.getItemTooltip(0, Items.ACACIA_DOOR), List.of(
                "Item: Acacia Door"
        ));
    }

    @Test
    public void testGameTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getGameTypeTooltip(0, GameType.SPECTATOR), List.of(
                "Game Type: Spectator"
        ));
    }

    @Test
    public void testStatsTooltip() {
        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(0, new PlayerPredicate.StatMatcher<>(Stats.BLOCK_MINED, Holder.direct(Blocks.COBBLESTONE), MinMaxBounds.Ints.atLeast(2))), List.of(
                "Block: Cobblestone",
                "  -> Times Mined: ≥2"
        ));
        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(0, new PlayerPredicate.StatMatcher<>(Stats.ITEM_USED, Holder.direct(Items.SALMON), MinMaxBounds.Ints.atLeast(3))), List.of(
                "Item: Raw Salmon",
                "  -> Times Used: ≥3"
        ));
        assertTooltip(GenericTooltipUtils.getStatMatcherTooltip(0, new PlayerPredicate.StatMatcher<>(Stats.ENTITY_KILLED, Holder.direct(EntityType.BAT), MinMaxBounds.Ints.atLeast(5))), List.of(
                "entity.minecraft.bat",
                "  -> You killed %s %s: ≥5"
        ));
    }

    @Test
    public void testRecipesTooltip() {
        Object2BooleanMap<ResourceLocation> recipeList = new Object2BooleanArrayMap<>();

        recipeList.put(new ResourceLocation("furnace_recipe"), true);
        recipeList.put(new ResourceLocation("apple_recipe"), false);

        assertTooltip(GenericTooltipUtils.getRecipesTooltip(0, new Object2BooleanArrayMap<>()), List.of());
        assertTooltip(GenericTooltipUtils.getRecipesTooltip(0, recipeList), List.of(
                "Recipes:",
                "  -> minecraft:furnace_recipe: true",
                "  -> minecraft:apple_recipe: false"
        ));
    }

    @Test
    public void testAdvancementsTooltip() {
        Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap = new LinkedHashMap<>();
        Object2BooleanMap<String> criterions = new Object2BooleanArrayMap<>();

        predicateMap.put(new ResourceLocation("first"), new PlayerPredicate.AdvancementDonePredicate(true));
        predicateMap.put(new ResourceLocation("second"), new PlayerPredicate.AdvancementCriterionsPredicate(criterions));
        criterions.put("test", true);

        assertTooltip(GenericTooltipUtils.getAdvancementsTooltip(0, Map.of()), List.of());
        assertTooltip(GenericTooltipUtils.getAdvancementsTooltip(0, predicateMap), List.of(
                "Advancements:",
                "  -> minecraft:first",
                "    -> Done: true",
                "  -> minecraft:second",
                "    -> test: true"
        ));
    }

    @Test
    public void testBlockPosTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockPosTooltip(0, new BlockPos(10, 12, 14)), List.of(
                "X: 10",
                "Y: 12",
                "Z: 14"
        ));
    }
}
