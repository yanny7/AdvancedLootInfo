package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.*;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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
        when(((MixinLootPoolSingletonContainer) container).getFunctions()).thenReturn(new LootItemFunction[0]);
        when(((MixinLootPoolEntryContainer) container).getConditions()).thenReturn(new LootItemCondition[0]);
        when(context.utils()).thenReturn(utils);
        when(utils.convertConditions(any(), any())).thenReturn(List.of());
        when(utils.convertFunctions(any(), any())).thenReturn(List.of());
        when(utils.decodeConditions(any(), any())).thenReturn(List.of(new SurvivesExplosionAliCondition(context, buf)), List.of());

        assertTooltip(GenericTooltipUtils.getTooltip(
                mock(AlternativesEntry.class),
                new RangeValue(2.5F),
                null,
                new RangeValue(2, 10),
                null,
                List.of(),
                List.of()
        ), List.of(
                "Chance: 2.50%",
                "Count: 2-10"
        ));
        assertTooltip(GenericTooltipUtils.getTooltip(
                new EmptyEntry(context, container),
                new RangeValue(2.5F),
                Pair.of(Enchantments.MOB_LOOTING, bonusChanceMap),
                new RangeValue(2, 10),
                Pair.of(Enchantments.BLOCK_FORTUNE, bonusCountMap),
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
        ApplyBonusCount.UniformBonusCount uniformBonusCountOriginal = new ApplyBonusCount.UniformBonusCount(2);
        ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCountOriginal = new ApplyBonusCount.BinomialWithBonusCount(3, 0.51F);
        ApplyBonusCount.UniformBonusCount uniformBonusCountMocked = mock(ApplyBonusCount.UniformBonusCount.class, withSettings().extraInterfaces(MixinApplyBonusCount.UniformBonusCount.class));
        ApplyBonusCount.BinomialWithBonusCount binomialWithBonusCountMocked = mock(ApplyBonusCount.BinomialWithBonusCount.class, withSettings().extraInterfaces(MixinApplyBonusCount.BinomialWithBonusCount.class));

        when(uniformBonusCountMocked.getType()).thenReturn(uniformBonusCountOriginal.getType());
        when(binomialWithBonusCountMocked.getType()).thenReturn(binomialWithBonusCountOriginal.getType());
        when(((MixinApplyBonusCount.UniformBonusCount) uniformBonusCountMocked).getBonusMultiplier()).thenReturn(2);
        when(((MixinApplyBonusCount.BinomialWithBonusCount) binomialWithBonusCountMocked).getExtraRounds()).thenReturn(3);
        when(((MixinApplyBonusCount.BinomialWithBonusCount) binomialWithBonusCountMocked).getProbability()).thenReturn(0.51F);

        assertTooltip(GenericTooltipUtils.getFormulaTooltip(0, new ApplyBonusCount.OreDrops()), List.of(
                "Formula: minecraft:ore_drops"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(0, uniformBonusCountMocked), List.of(
                "Formula: minecraft:uniform_bonus_count",
                "  -> Bonus Multiplier: 2"
        ));
        assertTooltip(GenericTooltipUtils.getFormulaTooltip(0, binomialWithBonusCountMocked), List.of(
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
    public void testPropertiesTooltip() {
        Set<Property<?>> properties = new LinkedHashSet<>();

        assertTooltip(GenericTooltipUtils.getPropertiesTooltip(0, properties), List.of());

        properties.add(BooleanProperty.create("bool"));
        properties.add(IntegerProperty.create("int", 0, 3));
        properties.add(DirectionProperty.create("direction", Direction.UP, Direction.DOWN));

        assertTooltip(GenericTooltipUtils.getPropertiesTooltip(0, properties), List.of(
                "Properties:",
                "  -> bool [true, false]",
                "  -> int [0, 1, 2, 3]",
                "  -> direction [up, down]"
        ));
    }

    @Test
    public void testPropertyTooltip() {
        assertTooltip(GenericTooltipUtils.getPropertyTooltip(1, EnumProperty.create("bed", BedPart.class)), List.of("  -> bed [head, foot]"));
    }

    @Test
    public void testEnchantmentsTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentsTooltip(0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getEnchantmentsTooltip(0, List.of(Enchantments.RIPTIDE, Enchantments.SMITE)), List.of(
                "Enchantments:",
                "  -> Enchantment: Riptide",
                "  -> Enchantment: Smite"
        ));
    }

    @Test
    public void testEnchantmentTooltip() {
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(0, null), List.of());
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(0, Enchantments.AQUA_AFFINITY), List.of("Enchantment: Aqua Affinity"));
        assertTooltip(GenericTooltipUtils.getEnchantmentTooltip(1, Enchantments.FIRE_ASPECT), List.of("  -> Enchantment: Fire Aspect"));
    }

    @Test
    public void testModifiersTooltip() {
        assertTooltip(GenericTooltipUtils.getModifiersTooltip(0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getModifiersTooltip(0, List.of(
                new SetAttributesAliFunction.Modifier(
                        "armor_toughness",
                        Attributes.ARMOR_TOUGHNESS,
                        AttributeModifier.Operation.ADDITION,
                        new RangeValue(1, 2),
                        null,
                        new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}
                ),
                new SetAttributesAliFunction.Modifier(
                        "attack_speed",
                        Attributes.ATTACK_SPEED,
                        AttributeModifier.Operation.MULTIPLY_BASE,
                        new RangeValue(1, 2),
                        UUID.nameUUIDFromBytes(new byte[]{1, 2, 3, 4, 5}),
                        new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}
                )
        )), List.of(
                "Modifiers:",
                "  -> Modifier:",
                "    -> Name: armor_toughness",
                "    -> Attribute: Armor Toughness",
                "    -> Operation: ADDITION",
                "    -> Amount: 1-2",
                "    -> Equipment Slots:",
                "      -> HEAD",
                "      -> CHEST",
                "      -> LEGS",
                "      -> FEET",
                "  -> Modifier:",
                "    -> Name: attack_speed",
                "    -> Attribute: Attack Speed",
                "    -> Operation: MULTIPLY_BASE",
                "    -> Amount: 1-2",
                "    -> UUID: 7cfdd078-89b3-395d-aa55-0914ab35e068",
                "    -> Equipment Slots:",
                "      -> MAINHAND",
                "      -> OFFHAND"
        ));
    }

    @Test
    public void testModifierTooltip() {
        assertTooltip(GenericTooltipUtils.getModifierTooltip(0, new SetAttributesAliFunction.Modifier(
                "armor",
                Attributes.ARMOR,
                AttributeModifier.Operation.MULTIPLY_TOTAL,
                new RangeValue(1, 5),
                null,
                new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}
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
        assertTooltip(GenericTooltipUtils.getAttributeTooltip(0, Attributes.JUMP_STRENGTH), List.of("Attribute: Horse Jump Strength"));
    }

    @Test
    public void testOperationTooltip() {
        assertTooltip(GenericTooltipUtils.getOperationTooltip(0, AttributeModifier.Operation.MULTIPLY_BASE), List.of("Operation: MULTIPLY_BASE"));
    }

    @Test
    public void testUUIDTooltip() {
        assertTooltip(GenericTooltipUtils.getUUIDTooltip(0, UUID.nameUUIDFromBytes(new byte[]{1, 2, 3})), List.of("UUID: 5289df73-7df5-3326-bcdd-22597afb1fac"));
    }

    @Test
    public void testEquipmentSlotsTooltip() {
        assertTooltip(GenericTooltipUtils.getEquipmentSlotsTooltip(0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getEquipmentSlotsTooltip(0, List.of(
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        )), List.of(
                "Equipment Slots:",
                "  -> HEAD",
                "  -> CHEST",
                "  -> LEGS",
                "  -> FEET"
        ));
    }

    @Test
    public void testBannerPatternsSlotsTooltip() {
        assertTooltip(GenericTooltipUtils.getBannerPatternsTooltip(0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getBannerPatternsTooltip(0, List.of(
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
        assertTooltip(GenericTooltipUtils.getBannerPatternTooltip(0, Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.FLOWER))), List.of("Banner Pattern: minecraft:flower"));
    }

    @Test
    public void testBlockEntityTypeTooltip() {
        assertTooltip(GenericTooltipUtils.getBlockEntityTypeTooltip(0, BlockEntityType.BEACON), List.of("Block Entity Type: minecraft:beacon"));
    }

    @Test
    public void testPotionTooltip() {
        assertTooltip(GenericTooltipUtils.getPotionTooltip(0, Potions.HEALING), List.of(
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
    public void testMobEffectInstancesTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectInstancesTooltip(0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getMobEffectInstancesTooltip(0, List.of(
                new MobEffectInstance(MobEffects.ABSORPTION, 1, 5),
                new MobEffectInstance(MobEffects.BLINDNESS, 1, 2)
        )), List.of(
                "Mob Effects:",
                "  -> Mob Effect: minecraft:absorption",
                "    -> Amplifier: 5",
                "    -> Duration: 1",
                "    -> Is Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true",
                "  -> Mob Effect: minecraft:blindness",
                "    -> Amplifier: 2",
                "    -> Duration: 1",
                "    -> Is Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true"
        ));
    }

    @Test
    public void testMobEffectTooltip() {
        assertTooltip(GenericTooltipUtils.getMobEffectTooltip(0, MobEffects.CONFUSION), List.of("Mob Effect: minecraft:nausea"));
    }

    @Test
    public void testStatePropertiesPredicateTooltip() {
        MixinStatePropertiesPredicate propertiesPredicate = (MixinStatePropertiesPredicate) mock(StatePropertiesPredicate.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.class));
        MixinStatePropertiesPredicate.ExactPropertyMatcher exactPropertyMatcher = (MixinStatePropertiesPredicate.ExactPropertyMatcher) mock(StatePropertiesPredicate.ExactPropertyMatcher.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.ExactPropertyMatcher.class));

        when(propertiesPredicate.getProperties()).thenReturn(List.of(
                (StatePropertiesPredicate.PropertyMatcher) exactPropertyMatcher
        ));
        when(exactPropertyMatcher.getName()).thenReturn("facing");
        when(exactPropertyMatcher.getValue()).thenReturn("east");
        assertTooltip(GenericTooltipUtils.getStatePropertiesPredicateTooltip(0, (StatePropertiesPredicate) propertiesPredicate), List.of(
                "State Properties:",
                "  -> facing: east"
        ));
    }

    @Test
    public void testPropertyMatcherTooltip() {
        MixinStatePropertiesPredicate.ExactPropertyMatcher exactPropertyMatcher = (MixinStatePropertiesPredicate.ExactPropertyMatcher) mock(StatePropertiesPredicate.ExactPropertyMatcher.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.ExactPropertyMatcher.class));
        MixinStatePropertiesPredicate.RangedPropertyMatcher rangedPropertyMatcher = (MixinStatePropertiesPredicate.RangedPropertyMatcher) mock(StatePropertiesPredicate.RangedPropertyMatcher.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.RangedPropertyMatcher.class));

        when(exactPropertyMatcher.getName()).thenReturn("facing");
        when(exactPropertyMatcher.getValue()).thenReturn("east");
        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, (StatePropertiesPredicate.PropertyMatcher) exactPropertyMatcher), List.of("facing: east"));

        when(rangedPropertyMatcher.getName()).thenReturn("level");
        when(rangedPropertyMatcher.getMinValue()).thenReturn("1", "2", null);
        when(rangedPropertyMatcher.getMaxValue()).thenReturn("7", null, "6");
        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, (StatePropertiesPredicate.PropertyMatcher) rangedPropertyMatcher), List.of("level: 1-7"));
        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, (StatePropertiesPredicate.PropertyMatcher) rangedPropertyMatcher), List.of("level: ≥2"));
        assertTooltip(GenericTooltipUtils.getPropertyMatcherTooltip(0, (StatePropertiesPredicate.PropertyMatcher) rangedPropertyMatcher), List.of("level: ≤6"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDamageSourcePredicateTooltip() {
        MixinDamageSourcePredicate damageSourcePredicate = (MixinDamageSourcePredicate) mock(DamageSourcePredicate.class, withSettings().extraInterfaces(MixinDamageSourcePredicate.class));
        TagPredicate<DamageType> tagPredicate1 = mock(TagPredicate.class, withSettings().extraInterfaces(MixinTagPredicate.class));
        TagPredicate<DamageType> tagPredicate2 = mock(TagPredicate.class, withSettings().extraInterfaces(MixinTagPredicate.class));
        MixinEntityPredicate sourcePredicate = (MixinEntityPredicate) mock(EntityPredicate.class, withSettings().extraInterfaces(MixinEntityPredicate.class));
        MixinEntityPredicate directPredicate = (MixinEntityPredicate) mock(EntityPredicate.class, withSettings().extraInterfaces(MixinEntityPredicate.class));

        MixinTagPredicate<DamageType> mixinTagPredicate1 = (MixinTagPredicate<DamageType>) tagPredicate1;
        MixinTagPredicate<DamageType> mixinTagPredicate2 = (MixinTagPredicate<DamageType>) tagPredicate2;

        when(sourcePredicate.getEntityType()).thenReturn(EntityTypePredicate.ANY);
        when(sourcePredicate.getDistanceToPlayer()).thenReturn(DistancePredicate.ANY);
        when(sourcePredicate.getLocation()).thenReturn(LocationPredicate.ANY);
        when(sourcePredicate.getSteppingOnLocation()).thenReturn(LocationPredicate.ANY);
        when(sourcePredicate.getEffects()).thenReturn(MobEffectsPredicate.ANY);
        when(sourcePredicate.getNbt()).thenReturn(NbtPredicate.ANY);
        when(sourcePredicate.getFlags()).thenReturn(EntityFlagsPredicate.ANY);
        when(sourcePredicate.getEquipment()).thenReturn(EntityEquipmentPredicate.ANY);
        when(sourcePredicate.getSubPredicate()).thenReturn(EntitySubPredicate.ANY);
        when(sourcePredicate.getVehicle()).thenReturn(EntityPredicate.ANY);
        when(sourcePredicate.getPassenger()).thenReturn(EntityPredicate.ANY);
        when(sourcePredicate.getTargetedEntity()).thenReturn(EntityPredicate.ANY);

        when(directPredicate.getEntityType()).thenReturn(EntityTypePredicate.ANY);
        when(directPredicate.getDistanceToPlayer()).thenReturn(DistancePredicate.ANY);
        when(directPredicate.getLocation()).thenReturn(LocationPredicate.ANY);
        when(directPredicate.getSteppingOnLocation()).thenReturn(LocationPredicate.ANY);
        when(directPredicate.getEffects()).thenReturn(MobEffectsPredicate.ANY);
        when(directPredicate.getNbt()).thenReturn(NbtPredicate.ANY);
        when(directPredicate.getFlags()).thenReturn(EntityFlagsPredicate.ANY);
        when(directPredicate.getEquipment()).thenReturn(EntityEquipmentPredicate.ANY);
        when(directPredicate.getSubPredicate()).thenReturn(EntitySubPredicate.ANY);
        when(directPredicate.getVehicle()).thenReturn(EntityPredicate.ANY);
        when(directPredicate.getPassenger()).thenReturn(EntityPredicate.ANY);
        when(directPredicate.getTargetedEntity()).thenReturn(EntityPredicate.ANY);

        when(mixinTagPredicate1.getTag()).thenReturn(DamageTypeTags.BYPASSES_ARMOR);
        when(mixinTagPredicate1.getExpected()).thenReturn(true);
        when(mixinTagPredicate2.getTag()).thenReturn(DamageTypeTags.IS_EXPLOSION);
        when(mixinTagPredicate2.getExpected()).thenReturn(false);
        when(damageSourcePredicate.getTags()).thenReturn(List.of(tagPredicate1, tagPredicate2));
        when(damageSourcePredicate.getSourceEntity()).thenReturn((EntityPredicate) sourcePredicate);
        when(damageSourcePredicate.getDirectEntity()).thenReturn((EntityPredicate) directPredicate);

        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(0, DamageSourcePredicate.ANY), List.of());
        assertTooltip(GenericTooltipUtils.getDamageSourcePredicateTooltip(0, (DamageSourcePredicate) damageSourcePredicate), List.of(
                "Damage Source:",
                "  -> Tags:",
                "    -> minecraft:bypasses_armor: true",
                "    -> minecraft:is_explosion: false"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTagPredicatesTooltip() {
        TagPredicate<DamageType> tagPredicate1 = mock(TagPredicate.class, withSettings().extraInterfaces(MixinTagPredicate.class));
        TagPredicate<DamageType> tagPredicate2 = mock(TagPredicate.class, withSettings().extraInterfaces(MixinTagPredicate.class));

        MixinTagPredicate<DamageType> mixinTagPredicate1 = (MixinTagPredicate<DamageType>) tagPredicate1;
        MixinTagPredicate<DamageType> mixinTagPredicate2 = (MixinTagPredicate<DamageType>) tagPredicate2;

        when(mixinTagPredicate1.getTag()).thenReturn(DamageTypeTags.BYPASSES_ARMOR);
        when(mixinTagPredicate1.getExpected()).thenReturn(true);
        when(mixinTagPredicate2.getTag()).thenReturn(DamageTypeTags.IS_EXPLOSION);
        when(mixinTagPredicate2.getExpected()).thenReturn(false);

        assertTooltip(GenericTooltipUtils.getTagPredicatesTooltip(0, List.of()), List.of());
        assertTooltip(GenericTooltipUtils.getTagPredicatesTooltip(0, List.of(tagPredicate1, tagPredicate2)), List.of(
                "Tags:",
                "  -> minecraft:bypasses_armor: true",
                "  -> minecraft:is_explosion: false"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEntityPredicateTooltip() {
        MixinEntityPredicate entityPredicate = (MixinEntityPredicate) mock(EntityPredicate.class, withSettings().extraInterfaces(MixinEntityPredicate.class));
        MixinEntityTypePredicate.TypePredicate entityTypePredicate = (MixinEntityTypePredicate.TypePredicate) mock(EntityTypePredicate.TypePredicate.class, withSettings().extraInterfaces(MixinEntityTypePredicate.TypePredicate.class));
        MixinDistancePredicate distancePredicate = (MixinDistancePredicate) mock(DistancePredicate.class, withSettings().extraInterfaces(MixinDistancePredicate.class));
        MixinMinMaxBounds.Doubles doubles = (MixinMinMaxBounds.Doubles) mock(MinMaxBounds.Doubles.class, withSettings().extraInterfaces(MixinMinMaxBounds.Doubles.class));
        MixinLocationPredicate locationPredicate = (MixinLocationPredicate) mock(LocationPredicate.class, withSettings().extraInterfaces(MixinLocationPredicate.class));
        MixinMobEffectPredicate mobEffectPredicate = (MixinMobEffectPredicate) mock(MobEffectsPredicate.class, withSettings().extraInterfaces(MixinMobEffectPredicate.class));
        MixinMobEffectPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = (MixinMobEffectPredicate.MobEffectInstancePredicate) mock(MobEffectsPredicate.MobEffectInstancePredicate.class, withSettings().extraInterfaces(MixinMobEffectPredicate.MobEffectInstancePredicate.class));
        MixinNbtPredicate nbtPredicate = (MixinNbtPredicate) mock(NbtPredicate.class, withSettings().extraInterfaces(MixinNbtPredicate.class));
        MixinEntityFlagsPredicate entityFlagsPredicate = (MixinEntityFlagsPredicate) mock(EntityFlagsPredicate.class, withSettings().extraInterfaces(MixinEntityFlagsPredicate.class));
        MixinEntityEquipmentPredicate entityEquipmentPredicate = (MixinEntityEquipmentPredicate) mock(EntityEquipmentPredicate.class, withSettings().extraInterfaces(MixinEntityEquipmentPredicate.class));
        MixinItemPredicate itemPredicate = (MixinItemPredicate) mock(ItemPredicate.class, withSettings().extraInterfaces(MixinItemPredicate.class));

        Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> mobEffectPredicateMap = new LinkedHashMap<>();
        Set<Item> items = new LinkedHashSet<>();
        CompoundTag compoundTag = new CompoundTag();

        mobEffectPredicateMap.put(MobEffects.ABSORPTION, (MobEffectsPredicate.MobEffectInstancePredicate) mobEffectInstancePredicate);
        mobEffectPredicateMap.put(MobEffects.BLINDNESS, (MobEffectsPredicate.MobEffectInstancePredicate) mobEffectInstancePredicate);

        items.add(Items.ANDESITE);
        items.add(Items.DIORITE);

        compoundTag.putInt("range", 5);

        when(doubles.getMin()).thenReturn(10D, 20D, null);
        when(doubles.getMax()).thenReturn(10D, null, 30D);

        when(mobEffectInstancePredicate.getAmbient()).thenReturn(Boolean.TRUE, (Boolean) null);
        when(mobEffectInstancePredicate.getAmplifier()).thenReturn(MinMaxBounds.Ints.ANY);
        when(mobEffectInstancePredicate.getDuration()).thenReturn(MinMaxBounds.Ints.ANY);
        when(mobEffectInstancePredicate.getVisible()).thenReturn(null, Boolean.FALSE);

        when(itemPredicate.getItems()).thenReturn(items);
        when(itemPredicate.getCount()).thenReturn(MinMaxBounds.Ints.ANY);
        when(itemPredicate.getDurability()).thenReturn(MinMaxBounds.Ints.ANY);
        when(itemPredicate.getEnchantments()).thenReturn(new EnchantmentPredicate[0]);
        when(itemPredicate.getStoredEnchantments()).thenReturn(new EnchantmentPredicate[0]);
        when(itemPredicate.getNbt()).thenReturn(NbtPredicate.ANY);

        when((EntityType<Cat>)entityTypePredicate.getType()).thenReturn(EntityType.CAT);

        when(distancePredicate.getX()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(distancePredicate.getY()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(distancePredicate.getZ()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(distancePredicate.getHorizontal()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(distancePredicate.getAbsolute()).thenReturn(MinMaxBounds.Doubles.ANY);

        when(locationPredicate.getX()).thenReturn((MinMaxBounds.Doubles) doubles, (MinMaxBounds.Doubles) doubles);
        when(locationPredicate.getY()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(locationPredicate.getZ()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(locationPredicate.getLight()).thenReturn(LightPredicate.ANY);
        when(locationPredicate.getBlock()).thenReturn(BlockPredicate.ANY);
        when(locationPredicate.getFluid()).thenReturn(FluidPredicate.ANY);
        when(locationPredicate.getSmokey()).thenReturn(null);

        when(mobEffectPredicate.getEffects()).thenReturn(mobEffectPredicateMap);

        when(nbtPredicate.getTag()).thenReturn(compoundTag);

        when(entityFlagsPredicate.getIsBaby()).thenReturn(Boolean.TRUE);
        when(entityFlagsPredicate.getIsCrouching()).thenReturn(null);
        when(entityFlagsPredicate.getIsOnFire()).thenReturn(null);
        when(entityFlagsPredicate.getIsSprinting()).thenReturn(null);
        when(entityFlagsPredicate.getIsSwimming()).thenReturn(null);

        when(entityEquipmentPredicate.getHead()).thenReturn((ItemPredicate) itemPredicate);
        when(entityEquipmentPredicate.getChest()).thenReturn(ItemPredicate.ANY);
        when(entityEquipmentPredicate.getFeet()).thenReturn(ItemPredicate.ANY);
        when(entityEquipmentPredicate.getLegs()).thenReturn(ItemPredicate.ANY);
        when(entityEquipmentPredicate.getMainhand()).thenReturn(ItemPredicate.ANY);
        when(entityEquipmentPredicate.getOffhand()).thenReturn(ItemPredicate.ANY);

        when(entityPredicate.getEntityType()).thenReturn((EntityTypePredicate) entityTypePredicate, EntityTypePredicate.ANY);
        when(entityPredicate.getDistanceToPlayer()).thenReturn((DistancePredicate) distancePredicate, DistancePredicate.ANY);
        when(entityPredicate.getLocation()).thenReturn((LocationPredicate) locationPredicate, LocationPredicate.ANY);
        when(entityPredicate.getSteppingOnLocation()).thenReturn((LocationPredicate) locationPredicate, LocationPredicate.ANY);
        when(entityPredicate.getEffects()).thenReturn((MobEffectsPredicate) mobEffectPredicate, MobEffectsPredicate.ANY);
        when(entityPredicate.getNbt()).thenReturn((NbtPredicate) nbtPredicate, NbtPredicate.ANY);
        when(entityPredicate.getFlags()).thenReturn((EntityFlagsPredicate) entityFlagsPredicate, EntityFlagsPredicate.ANY);
        when(entityPredicate.getEquipment()).thenReturn((EntityEquipmentPredicate) entityEquipmentPredicate, EntityEquipmentPredicate.ANY);
        when(entityPredicate.getSubPredicate()).thenReturn(EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.CALICO))), EntitySubPredicate.ANY);
        when(entityPredicate.getVehicle()).thenReturn((EntityPredicate) entityPredicate, EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY);
        when(entityPredicate.getPassenger()).thenReturn(EntityPredicate.ANY, (EntityPredicate) entityPredicate, EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY);
        when(entityPredicate.getTargetedEntity()).thenReturn(EntityPredicate.ANY, EntityPredicate.ANY, (EntityPredicate) entityPredicate, EntityPredicate.ANY, EntityPredicate.ANY);
        when(entityPredicate.getTeam()).thenReturn("blue", "white", "red", "orange");

        assertTooltip(GenericTooltipUtils.getEntityPredicateTooltip(0, (EntityPredicate) entityPredicate), List.of(
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

    @SuppressWarnings("unchecked")
    @Test
    public void testEntityTypePredicateTooltip() {
        MixinEntityTypePredicate.TypePredicate entityTypePredicate = (MixinEntityTypePredicate.TypePredicate) mock(EntityTypePredicate.TypePredicate.class, withSettings().extraInterfaces(MixinEntityTypePredicate.TypePredicate.class));
        MixinEntityTypePredicate.TagPredicate entityTagPredicate = (MixinEntityTypePredicate.TagPredicate) mock(EntityTypePredicate.TagPredicate.class, withSettings().extraInterfaces(MixinEntityTypePredicate.TagPredicate.class));

        when((EntityType<Cat>)entityTypePredicate.getType()).thenReturn(EntityType.CAT);
        when(entityTagPredicate.getTag()).thenReturn(EntityTypeTags.SKELETONS);

        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(0, (EntityTypePredicate) entityTypePredicate), List.of("Entity Type: Cat"));
        assertTooltip(GenericTooltipUtils.getEntityTypePredicateTooltip(0, (EntityTypePredicate) entityTagPredicate), List.of("Entity Type: minecraft:skeletons"));
    }

    @Test
    public void testDistancePredicateTooltip() {
        MixinDistancePredicate distancePredicate = (MixinDistancePredicate) mock(DistancePredicate.class, withSettings().extraInterfaces(MixinDistancePredicate.class));
        MixinMinMaxBounds.Doubles doubles = (MixinMinMaxBounds.Doubles) mock(MinMaxBounds.Doubles.class, withSettings().extraInterfaces(MixinMinMaxBounds.Doubles.class));

        when(doubles.getMin()).thenReturn(10D, 20D, null, 15D, 2D);
        when(doubles.getMax()).thenReturn(10D, null, 30D, null, 5.5D);

        when(distancePredicate.getX()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(distancePredicate.getY()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(distancePredicate.getZ()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(distancePredicate.getHorizontal()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(distancePredicate.getAbsolute()).thenReturn((MinMaxBounds.Doubles) doubles);

        assertTooltip(GenericTooltipUtils.getDistancePredicateTooltip(0, (DistancePredicate) distancePredicate), List.of(
                "X: =10.0",
                "Y: ≥20.0",
                "Z: ≤30.0",
                "Horizontal: ≥15.0",
                "Absolute: 2.0-5.5"
        ));
    }

    @Test
    public void testLocationPredicateTooltip() {
        MixinLocationPredicate locationPredicate = (MixinLocationPredicate) mock(LocationPredicate.class, withSettings().extraInterfaces(MixinLocationPredicate.class));
        MixinMinMaxBounds.Doubles doubles = (MixinMinMaxBounds.Doubles) mock(MinMaxBounds.Doubles.class, withSettings().extraInterfaces(MixinMinMaxBounds.Doubles.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));
        MixinBlockPredicate blockPredicate = (MixinBlockPredicate) mock(BlockPredicate.class, withSettings().extraInterfaces(MixinBlockPredicate.class));
        MixinFluidPredicate fluidPredicate = (MixinFluidPredicate) mock(FluidPredicate.class, withSettings().extraInterfaces(MixinFluidPredicate.class));
        MixinLightPredicate lightPredicate = (MixinLightPredicate) mock(LightPredicate.class, withSettings().extraInterfaces(MixinLightPredicate.class));

        Set<Block> blockSet = new LinkedHashSet<>();

        blockSet.add(Blocks.STONE);
        blockSet.add(Blocks.COBBLESTONE);

        when(doubles.getMin()).thenReturn(10D, 20D, null);
        when(doubles.getMax()).thenReturn(10D, null, 30D);

        when(ints.getMin()).thenReturn(10);
        when(ints.getMax()).thenReturn(15);

        when(lightPredicate.getComposite()).thenReturn((MinMaxBounds.Ints) ints);

        when(blockPredicate.getBlocks()).thenReturn(blockSet);
        when(blockPredicate.getNbt()).thenReturn(NbtPredicate.ANY);
        when(blockPredicate.getProperties()).thenReturn(StatePropertiesPredicate.ANY);
        when(blockPredicate.getTag()).thenReturn(null);

        when(fluidPredicate.getFluid()).thenReturn(Fluids.LAVA);
        when(fluidPredicate.getProperties()).thenReturn(StatePropertiesPredicate.ANY);
        when(fluidPredicate.getTag()).thenReturn(null);

        when(locationPredicate.getX()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(locationPredicate.getY()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(locationPredicate.getZ()).thenReturn((MinMaxBounds.Doubles) doubles);
        when(locationPredicate.getLight()).thenReturn((LightPredicate) lightPredicate);
        when(locationPredicate.getBlock()).thenReturn((BlockPredicate) blockPredicate);
        when(locationPredicate.getFluid()).thenReturn((FluidPredicate) fluidPredicate);
        when(locationPredicate.getSmokey()).thenReturn(true);
        when(locationPredicate.getStructure()).thenReturn(BuiltinStructures.MINESHAFT);
        when(locationPredicate.getDimension()).thenReturn(Level.OVERWORLD);
        when(locationPredicate.getBiome()).thenReturn(Biomes.PLAINS);

        assertTooltip(GenericTooltipUtils.getLocationPredicateTooltip(0, (LocationPredicate) locationPredicate), List.of(
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
        MixinLightPredicate lightPredicate = (MixinLightPredicate) mock(LightPredicate.class, withSettings().extraInterfaces(MixinLightPredicate.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));

        when(ints.getMin()).thenReturn(10);
        when(ints.getMax()).thenReturn(15);

        when(lightPredicate.getComposite()).thenReturn((MinMaxBounds.Ints) ints);

        assertTooltip(GenericTooltipUtils.getLightPredicateTooltip(0, (LightPredicate) lightPredicate), List.of("Light: 10-15"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBlockPredicateTooltip() {
        MixinBlockPredicate blockPredicate = (MixinBlockPredicate) mock(BlockPredicate.class, withSettings().extraInterfaces(MixinBlockPredicate.class));
        MixinNbtPredicate nbtPredicate = (MixinNbtPredicate) mock(NbtPredicate.class, withSettings().extraInterfaces(MixinNbtPredicate.class));
        MixinStatePropertiesPredicate propertiesPredicate = (MixinStatePropertiesPredicate) mock(StatePropertiesPredicate.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.class));
        MixinStatePropertiesPredicate.ExactPropertyMatcher exactPropertyMatcher = (MixinStatePropertiesPredicate.ExactPropertyMatcher) mock(StatePropertiesPredicate.ExactPropertyMatcher.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.ExactPropertyMatcher.class));

        CompoundTag compoundTag = new CompoundTag();
        Set<Block> blockSet = new LinkedHashSet<>();
        List<StatePropertiesPredicate.PropertyMatcher> propertyMatcherList = new LinkedList<>();

        compoundTag.putFloat("test", 3F);
        blockSet.add(Blocks.STONE);
        blockSet.add(Blocks.COBBLESTONE);
        propertyMatcherList.add((StatePropertiesPredicate.PropertyMatcher) exactPropertyMatcher);

        when(nbtPredicate.getTag()).thenReturn(compoundTag);

        when(propertiesPredicate.getProperties()).thenReturn(propertyMatcherList);

        when(exactPropertyMatcher.getName()).thenReturn("facing");
        when(exactPropertyMatcher.getValue()).thenReturn("east");

        when(blockPredicate.getBlocks()).thenReturn(null, Set.of(), blockSet);
        when(blockPredicate.getNbt()).thenReturn(NbtPredicate.ANY, NbtPredicate.ANY, (NbtPredicate) nbtPredicate);
        when(blockPredicate.getProperties()).thenReturn(StatePropertiesPredicate.ANY, StatePropertiesPredicate.ANY, (StatePropertiesPredicate) propertiesPredicate);
        when(blockPredicate.getTag()).thenReturn(BlockTags.DIRT, BlockTags.BEDS, BlockTags.BASE_STONE_OVERWORLD);

        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(0, (BlockPredicate) blockPredicate), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:dirt"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(0, (BlockPredicate) blockPredicate), List.of(
                "Block Predicate:",
                "  -> Tag: minecraft:beds"
        ));
        assertTooltip(GenericTooltipUtils.getBlockPredicateTooltip(0, (BlockPredicate) blockPredicate), List.of(
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
        MixinNbtPredicate nbtPredicate = (MixinNbtPredicate) mock(NbtPredicate.class, withSettings().extraInterfaces(MixinNbtPredicate.class));
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putFloat("test", 3F);
        when(nbtPredicate.getTag()).thenReturn(null, compoundTag);
        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(0, (NbtPredicate) nbtPredicate), List.of());
        assertTooltip(GenericTooltipUtils.getNbtPredicateTooltip(0, (NbtPredicate) nbtPredicate), List.of("Nbt: {test:3.0f}"));
    }

    @Test
    public void testFluidPredicateTooltip() {
        MixinFluidPredicate fluidPredicate = (MixinFluidPredicate) mock(FluidPredicate.class, withSettings().extraInterfaces(MixinFluidPredicate.class));
        MixinStatePropertiesPredicate propertiesPredicate = (MixinStatePropertiesPredicate) mock(StatePropertiesPredicate.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.class));
        MixinStatePropertiesPredicate.ExactPropertyMatcher exactPropertyMatcher = (MixinStatePropertiesPredicate.ExactPropertyMatcher) mock(StatePropertiesPredicate.ExactPropertyMatcher.class, withSettings().extraInterfaces(MixinStatePropertiesPredicate.ExactPropertyMatcher.class));

        List<StatePropertiesPredicate.PropertyMatcher> propertyMatcherList = new LinkedList<>();

        propertyMatcherList.add((StatePropertiesPredicate.PropertyMatcher) exactPropertyMatcher);

        when(exactPropertyMatcher.getName()).thenReturn("facing");
        when(exactPropertyMatcher.getValue()).thenReturn("east");

        when(propertiesPredicate.getProperties()).thenReturn(propertyMatcherList);

        when(fluidPredicate.getFluid()).thenReturn(Fluids.WATER);
        when(fluidPredicate.getTag()).thenReturn(FluidTags.WATER);
        when(fluidPredicate.getProperties()).thenReturn((StatePropertiesPredicate) propertiesPredicate);

        assertTooltip(GenericTooltipUtils.getFluidPredicateTooltip(0, (FluidPredicate) fluidPredicate), List.of(
                "Fluid Predicate:",
                "  -> Tag: minecraft:water",
                "  -> Fluid: minecraft:water",
                "  -> State Properties:",
                "    -> facing: east"
        ));
    }

    @Test
    public void testFluidTooltip() {
        assertTooltip(GenericTooltipUtils.getFluidTooltip(0, null), List.of());
        assertTooltip(GenericTooltipUtils.getFluidTooltip(0, Fluids.WATER), List.of("Fluid: minecraft:water"));
    }

    @Test
    public void testMobEffectPredicateTooltip() {
        MixinMobEffectPredicate mobEffectPredicate = (MixinMobEffectPredicate) mock(MobEffectsPredicate.class, withSettings().extraInterfaces(MixinMobEffectPredicate.class));
        MixinMobEffectPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = (MixinMobEffectPredicate.MobEffectInstancePredicate) mock(MobEffectsPredicate.MobEffectInstancePredicate.class, withSettings().extraInterfaces(MixinMobEffectPredicate.MobEffectInstancePredicate.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));

        Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> mobEffectPredicateMap = new LinkedHashMap<>();

        mobEffectPredicateMap.put(MobEffects.ABSORPTION, (MobEffectsPredicate.MobEffectInstancePredicate) mobEffectInstancePredicate);
        mobEffectPredicateMap.put(MobEffects.BLINDNESS, (MobEffectsPredicate.MobEffectInstancePredicate) mobEffectInstancePredicate);

        when(ints.getMin()).thenReturn(10, null, 5, 1);
        when(ints.getMax()).thenReturn(15, 5, null, 2);

        when(mobEffectInstancePredicate.getAmbient()).thenReturn(Boolean.TRUE, (Boolean) null);
        when(mobEffectInstancePredicate.getAmplifier()).thenReturn((MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints);
        when(mobEffectInstancePredicate.getDuration()).thenReturn((MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints);
        when(mobEffectInstancePredicate.getVisible()).thenReturn(Boolean.FALSE, (Boolean) null);

        when(mobEffectPredicate.getEffects()).thenReturn(mobEffectPredicateMap);

        assertTooltip(GenericTooltipUtils.getMobEffectPredicateTooltip(0, (MobEffectsPredicate) mobEffectPredicate), List.of(
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
        MixinMobEffectPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = (MixinMobEffectPredicate.MobEffectInstancePredicate) mock(MobEffectsPredicate.MobEffectInstancePredicate.class, withSettings().extraInterfaces(MixinMobEffectPredicate.MobEffectInstancePredicate.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));

        when(ints.getMin()).thenReturn(10, null, 5, 1);
        when(ints.getMax()).thenReturn(15, 5, null, 2);

        when(mobEffectInstancePredicate.getAmbient()).thenReturn(Boolean.TRUE);
        when(mobEffectInstancePredicate.getAmplifier()).thenReturn((MinMaxBounds.Ints) ints);
        when(mobEffectInstancePredicate.getDuration()).thenReturn((MinMaxBounds.Ints) ints);
        when(mobEffectInstancePredicate.getVisible()).thenReturn(Boolean.FALSE);

        assertTooltip(GenericTooltipUtils.getMobEffectInstancePredicateTooltip(0, (MobEffectsPredicate.MobEffectInstancePredicate) mobEffectInstancePredicate), List.of(
                "Amplifier: 10-15",
                "Duration: ≤5",
                "Is Ambient: true",
                "Is Visible: false"
        ));
    }

    @Test
    public void testEntityFlagsPredicateTooltip() {
        MixinEntityFlagsPredicate entityFlagsPredicate = (MixinEntityFlagsPredicate) mock(EntityFlagsPredicate.class, withSettings().extraInterfaces(MixinEntityFlagsPredicate.class));

        when(entityFlagsPredicate.getIsBaby()).thenReturn(true);
        when(entityFlagsPredicate.getIsOnFire()).thenReturn(false);
        when(entityFlagsPredicate.getIsCrouching()).thenReturn(true);
        when(entityFlagsPredicate.getIsSwimming()).thenReturn(false);
        when(entityFlagsPredicate.getIsSprinting()).thenReturn(true);

        assertTooltip(GenericTooltipUtils.getEntityFlagsPredicateTooltip(0, (EntityFlagsPredicate) entityFlagsPredicate), List.of(
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
        MixinEntityEquipmentPredicate entityEquipmentPredicate = (MixinEntityEquipmentPredicate) mock(EntityEquipmentPredicate.class, withSettings().extraInterfaces(MixinEntityEquipmentPredicate.class));
        MixinItemPredicate itemPredicate = (MixinItemPredicate) mock(ItemPredicate.class, withSettings().extraInterfaces(MixinItemPredicate.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));

        when(ints.getMin()).thenReturn(10, null, 5, 1, 0, 32);
        when(ints.getMax()).thenReturn(15, 5, null, 2, 64, null);

        when(itemPredicate.getNbt()).thenReturn(NbtPredicate.ANY, NbtPredicate.ANY, NbtPredicate.ANY, NbtPredicate.ANY, NbtPredicate.ANY, NbtPredicate.ANY);
        when(itemPredicate.getCount()).thenReturn((MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, (MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints);
        when(itemPredicate.getDurability()).thenReturn(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, (MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY);
        when(itemPredicate.getEnchantments()).thenReturn(new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0]);
        when(itemPredicate.getStoredEnchantments()).thenReturn(new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0], new EnchantmentPredicate[0]);

        when(entityEquipmentPredicate.getHead()).thenReturn((ItemPredicate) itemPredicate);
        when(entityEquipmentPredicate.getChest()).thenReturn((ItemPredicate) itemPredicate);
        when(entityEquipmentPredicate.getLegs()).thenReturn((ItemPredicate) itemPredicate);
        when(entityEquipmentPredicate.getFeet()).thenReturn((ItemPredicate) itemPredicate);
        when(entityEquipmentPredicate.getMainhand()).thenReturn((ItemPredicate) itemPredicate);
        when(entityEquipmentPredicate.getOffhand()).thenReturn((ItemPredicate) itemPredicate);

        assertTooltip(GenericTooltipUtils.getEntityEquipmentPredicateTooltip(0, (EntityEquipmentPredicate) entityEquipmentPredicate), List.of(
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

    @SuppressWarnings("unchecked")
    @Test
    public void testItemPredicateTooltip() {
        MixinItemPredicate itemPredicate = (MixinItemPredicate) mock(ItemPredicate.class, withSettings().extraInterfaces(MixinItemPredicate.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));
        MixinNbtPredicate nbtPredicate = (MixinNbtPredicate) mock(NbtPredicate.class, withSettings().extraInterfaces(MixinNbtPredicate.class));
        MixinEnchantmentPredicate enchantmentPredicate = (MixinEnchantmentPredicate) mock(EnchantmentPredicate.class, withSettings().extraInterfaces(MixinEnchantmentPredicate.class));

        CompoundTag compoundTag = new CompoundTag();
        Set<Item> itemSet = new LinkedHashSet<>();

        compoundTag.putBoolean("healing", true);
        itemSet.add(Items.CAKE);
        itemSet.add(Items.NETHERITE_AXE);

        when(ints.getMin()).thenReturn(10, null, 1, 2, null, 4);
        when(ints.getMax()).thenReturn(15, 5, null, 4, 5, null);

        when(nbtPredicate.getTag()).thenReturn(compoundTag);

        when(itemPredicate.getNbt()).thenReturn(NbtPredicate.ANY, (NbtPredicate) nbtPredicate);
        when(itemPredicate.getCount()).thenReturn(MinMaxBounds.Ints.ANY, (MinMaxBounds.Ints) ints);
        when(itemPredicate.getDurability()).thenReturn(MinMaxBounds.Ints.ANY, (MinMaxBounds.Ints) ints);
        when(itemPredicate.getEnchantments()).thenReturn(new EnchantmentPredicate[0], new EnchantmentPredicate[]{(EnchantmentPredicate) enchantmentPredicate, (EnchantmentPredicate) enchantmentPredicate});
        when(itemPredicate.getStoredEnchantments()).thenReturn(new EnchantmentPredicate[0], new EnchantmentPredicate[]{(EnchantmentPredicate) enchantmentPredicate, (EnchantmentPredicate) enchantmentPredicate});
        when(itemPredicate.getPotion()).thenReturn(null, Potions.HEALING);
        when(itemPredicate.getItems()).thenReturn(null, itemSet);
        when(itemPredicate.getTag()).thenReturn(ItemTags.AXES, ItemTags.BANNERS);

        when(enchantmentPredicate.getEnchantment()).thenReturn(Enchantments.SMITE, Enchantments.MENDING, Enchantments.DEPTH_STRIDER, Enchantments.FISHING_SPEED);
        when(enchantmentPredicate.getLevel()).thenReturn((MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints, (MinMaxBounds.Ints) ints);

        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(0, (ItemPredicate) itemPredicate), List.of(
                "Tag: minecraft:axes"
        ));
        assertTooltip(GenericTooltipUtils.getItemPredicateTooltip(0, (ItemPredicate) itemPredicate), List.of(
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
        MixinEnchantmentPredicate enchantmentPredicate = (MixinEnchantmentPredicate) mock(EnchantmentPredicate.class, withSettings().extraInterfaces(MixinEnchantmentPredicate.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));

        when(ints.getMax()).thenReturn(2);

        when(enchantmentPredicate.getEnchantment()).thenReturn(Enchantments.FALL_PROTECTION);
        when(enchantmentPredicate.getLevel()).thenReturn((MinMaxBounds.Ints) ints);

        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(0, EnchantmentPredicate.ANY), List.of());
        assertTooltip(GenericTooltipUtils.getEnchantmentPredicateTooltip(0, (EnchantmentPredicate) enchantmentPredicate), List.of(
                "Enchantment: Feather Falling",
                "  -> Level: ≤2"
        ));
    }

    @Test
    public void testEntitySubPredicateTooltip() {
        MixinLighthingBoltPredicate lightningBoltPredicate = (MixinLighthingBoltPredicate) mock(LighthingBoltPredicate.class, withSettings().extraInterfaces(MixinLighthingBoltPredicate.class));
        MixinFishingHookPredicate fishingHookPredicate = (MixinFishingHookPredicate) mock(FishingHookPredicate.class, withSettings().extraInterfaces(MixinFishingHookPredicate.class));
        MixinPlayerPredicate playerPredicate = (MixinPlayerPredicate) mock(PlayerPredicate.class, withSettings().extraInterfaces(MixinPlayerPredicate.class));
        MixinEntityPredicate entityPredicate = (MixinEntityPredicate) mock(EntityPredicate.class, withSettings().extraInterfaces(MixinEntityPredicate.class));
        MixinSlimePredicate slimePredicate = (MixinSlimePredicate) mock(SlimePredicate.class, withSettings().extraInterfaces(MixinSlimePredicate.class));
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));
        MixinPlayerPredicate.AdvancementDonePredicate advancementDonePredicate = (MixinPlayerPredicate.AdvancementDonePredicate) mock(PlayerPredicate.AdvancementDonePredicate.class, withSettings().extraInterfaces(MixinPlayerPredicate.AdvancementDonePredicate.class));

        Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap = new LinkedHashMap<>();
        Object2BooleanMap<ResourceLocation> recipeList = new Object2BooleanArrayMap<>();
        Map<Stat<?>, MinMaxBounds.Ints> statMap = new LinkedHashMap<>();

        predicateMap.put(new ResourceLocation("first"), (PlayerPredicate.AdvancementPredicate) advancementDonePredicate);
        predicateMap.put(new ResourceLocation("second"), (PlayerPredicate.AdvancementPredicate) advancementDonePredicate);
        recipeList.put(new ResourceLocation("recipe1"), true);
        recipeList.put(new ResourceLocation("recipe2"), false);
        statMap.put(Stats.BLOCK_MINED.get(Blocks.COBBLESTONE), (MinMaxBounds.Ints) ints);
        statMap.put(Stats.ITEM_USED.get(Items.SALMON), (MinMaxBounds.Ints) ints);

        when(advancementDonePredicate.getState()).thenReturn(true, false);

        when(entityPredicate.getEntityType()).thenReturn(EntityTypePredicate.ANY, EntityTypePredicate.ANY);
        when(entityPredicate.getDistanceToPlayer()).thenReturn(DistancePredicate.ANY, DistancePredicate.ANY);
        when(entityPredicate.getLocation()).thenReturn(LocationPredicate.ANY, LocationPredicate.ANY);
        when(entityPredicate.getSteppingOnLocation()).thenReturn(LocationPredicate.ANY, LocationPredicate.ANY);
        when(entityPredicate.getEffects()).thenReturn(MobEffectsPredicate.ANY, MobEffectsPredicate.ANY);
        when(entityPredicate.getNbt()).thenReturn(NbtPredicate.ANY, NbtPredicate.ANY);
        when(entityPredicate.getFlags()).thenReturn(EntityFlagsPredicate.ANY, EntityFlagsPredicate.ANY);
        when(entityPredicate.getEquipment()).thenReturn(EntityEquipmentPredicate.ANY, EntityEquipmentPredicate.ANY);
        when(entityPredicate.getSubPredicate()).thenReturn(EntitySubPredicate.ANY, EntitySubPredicate.ANY);
        when(entityPredicate.getVehicle()).thenReturn(EntityPredicate.ANY, EntityPredicate.ANY);
        when(entityPredicate.getPassenger()).thenReturn(EntityPredicate.ANY, EntityPredicate.ANY);
        when(entityPredicate.getTargetedEntity()).thenReturn(EntityPredicate.ANY, EntityPredicate.ANY);
        when(entityPredicate.getTeam()).thenReturn("blue", "red");

        when(ints.getMin()).thenReturn(2, 3, 4, 5, 1);

        when(((EntitySubPredicate) lightningBoltPredicate).type()).thenReturn(EntitySubPredicate.Types.LIGHTNING);
        when(lightningBoltPredicate.getEntityStruck()).thenReturn((EntityPredicate) entityPredicate);
        when(lightningBoltPredicate.getBlocksSetOnFire()).thenReturn((MinMaxBounds.Ints) ints);

        when(((EntitySubPredicate) fishingHookPredicate).type()).thenReturn(EntitySubPredicate.Types.FISHING_HOOK);
        when(fishingHookPredicate.isInOpenWater()).thenReturn(true);

        when(((EntitySubPredicate) playerPredicate).type()).thenReturn(EntitySubPredicate.Types.PLAYER);
        when(playerPredicate.getGameType()).thenReturn(GameType.SURVIVAL);
        when(playerPredicate.getLevel()).thenReturn((MinMaxBounds.Ints) ints);
        when(playerPredicate.getAdvancements()).thenReturn(predicateMap);
        when(playerPredicate.getRecipes()).thenReturn(recipeList);
        when(playerPredicate.getLookingAt()).thenReturn((EntityPredicate) entityPredicate);
        when(playerPredicate.getStats()).thenReturn(statMap);

        when(((EntitySubPredicate) slimePredicate).type()).thenReturn(EntitySubPredicate.Types.SLIME);
        when(slimePredicate.getSize()).thenReturn((MinMaxBounds.Ints) ints);

        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, EntitySubPredicate.variant(FrogVariant.COLD)), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:cold"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, EntitySubPredicate.variant(Objects.requireNonNull(BuiltInRegistries.CAT_VARIANT.get(CatVariant.PERSIAN)))), List.of(
            "Entity Sub Predicate:",
            "  -> Variant: minecraft:persian"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, (LighthingBoltPredicate) lightningBoltPredicate), List.of(
                "Entity Sub Predicate:",
                "  -> Blocks On Fire: ≥2",
                "  -> Stuck Entity:",
                "    -> Team: blue"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, (FishingHookPredicate) fishingHookPredicate), List.of(
                "Entity Sub Predicate:",
                "  -> Is In Open Water: true"
        ));
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, (PlayerPredicate) playerPredicate), List.of(
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
        assertTooltip(GenericTooltipUtils.getEntitySubPredicateTooltip(0, (SlimePredicate) slimePredicate), List.of(
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
        assertTooltip(GenericTooltipUtils.getGameTypeTooltip(0, null), List.of());
        assertTooltip(GenericTooltipUtils.getGameTypeTooltip(0, GameType.SPECTATOR), List.of(
                "Game Type: Spectator"
        ));
    }

    @Test
    public void testStatsTooltip() {
        MixinMinMaxBounds.Ints ints = (MixinMinMaxBounds.Ints) mock(MinMaxBounds.Ints.class, withSettings().extraInterfaces(MixinMinMaxBounds.Ints.class));

        Map<Stat<?>, MinMaxBounds.Ints> statMap = new LinkedHashMap<>();

        statMap.put(Stats.BLOCK_MINED.get(Blocks.COBBLESTONE), (MinMaxBounds.Ints) ints);
        statMap.put(Stats.ITEM_USED.get(Items.SALMON), (MinMaxBounds.Ints) ints);
        statMap.put(Stats.ENTITY_KILLED.get(EntityType.BAT), (MinMaxBounds.Ints) ints);

        when(ints.getMin()).thenReturn(2, 3, 5);

        assertTooltip(GenericTooltipUtils.getStatsTooltip(0, Map.of()), List.of());
        assertTooltip(GenericTooltipUtils.getStatsTooltip(0, statMap), List.of(
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

        assertTooltip(GenericTooltipUtils.getRecipesTooltip(0, new Object2BooleanArrayMap<>()), List.of());
        assertTooltip(GenericTooltipUtils.getRecipesTooltip(0, recipeList), List.of(
                "Recipes:",
                "  -> minecraft:furnace_recipe: true",
                "  -> minecraft:apple_recipe: false"
        ));
    }

    @Test
    public void testAdvancementsTooltip() {
        MixinPlayerPredicate.AdvancementDonePredicate advancementDonePredicate = (MixinPlayerPredicate.AdvancementDonePredicate) mock(PlayerPredicate.AdvancementDonePredicate.class, withSettings().extraInterfaces(MixinPlayerPredicate.AdvancementDonePredicate.class));
        MixinPlayerPredicate.AdvancementCriterionsPredicate advancementCriterionsPredicate = (MixinPlayerPredicate.AdvancementCriterionsPredicate) mock(PlayerPredicate.AdvancementCriterionsPredicate.class, withSettings().extraInterfaces(MixinPlayerPredicate.AdvancementCriterionsPredicate.class));

        Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> predicateMap = new LinkedHashMap<>();
        Object2BooleanMap<String> criterions = new Object2BooleanArrayMap<>();

        predicateMap.put(new ResourceLocation("first"), (PlayerPredicate.AdvancementPredicate) advancementDonePredicate);
        predicateMap.put(new ResourceLocation("second"), (PlayerPredicate.AdvancementPredicate) advancementCriterionsPredicate);
        criterions.put("test", true);

        when(advancementDonePredicate.getState()).thenReturn(true);
        when(advancementCriterionsPredicate.getCriterions()).thenReturn(criterions);

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
