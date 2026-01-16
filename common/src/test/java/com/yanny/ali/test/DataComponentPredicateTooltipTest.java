package com.yanny.ali.test;

import com.yanny.ali.plugin.server.DataComponentPredicateTooltipUtils;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.predicates.*;
import net.minecraft.core.component.predicates.DamagePredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSongs;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class DataComponentPredicateTooltipTest {
    @Test
    public void testItemDamagePredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getDamagePredicateTooltip(UTILS, DamagePredicate.durability(MinMaxBounds.Ints.atMost(50))), List.of("Durability: ≤50"));
        assertTooltip(DataComponentPredicateTooltipUtils.getDamagePredicateTooltip(UTILS, new DamagePredicate(
                MinMaxBounds.Ints.atMost(50),
                MinMaxBounds.Ints.atLeast(5)
        )), List.of(
                "Damage: ≥5",
                "Durability: ≤50"
        ));
    }

    @Test
    public void testItemEnchantmentsPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getEnchantmentsPredicateTooltip(UTILS, EnchantmentsPredicate.enchantments(List.of(
                new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.LOOTING).orElseThrow(), MinMaxBounds.Ints.ANY),
                new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.MENDING).orElseThrow(), MinMaxBounds.Ints.between(1, 5))
        ))), List.of(
                "Enchantment Predicate:",
                "  -> Enchantments:",
                "    -> minecraft:looting",
                "  -> Enchantments:",
                "    -> minecraft:mending",
                "    -> Level: 1-5"
        ));
    }

    @Test
    public void testItemStoredEnchantmentsPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getStoredEnchantmentsPredicateTooltip(UTILS, EnchantmentsPredicate.storedEnchantments(List.of(
                new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.LOOTING).orElseThrow(), MinMaxBounds.Ints.ANY),
                new EnchantmentPredicate(LOOKUP.lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.MENDING).orElseThrow(), MinMaxBounds.Ints.between(1, 5))
        ))), List.of(
                "Enchantment Predicate:",
                "  -> Enchantments:",
                "    -> minecraft:looting",
                "  -> Enchantments:",
                "    -> minecraft:mending",
                "    -> Level: 1-5"
        ));
    }

    @Test
    public void testPotionsPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getPotionsPredicateTooltip(UTILS, (PotionsPredicate) PotionsPredicate.potions(
                HolderSet.direct(Potions.HEALING, Potions.INFESTED)
        )), List.of(
                "Potions:",
                "  -> minecraft:healing",
                "  -> minecraft:infested"
        ));
    }

    @Test
    public void testCustomDataPredicateTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("tst", 5);

        assertTooltip(DataComponentPredicateTooltipUtils.getCustomDataPredicateTooltip(UTILS, CustomDataPredicate.customData(new NbtPredicate(compoundTag))), List.of("Nbt: {tst:5}"));
    }

    @Test
    public void testContainerPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getContainerPredicateTooltip(UTILS, new ContainerPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.ANDESITE).build())),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), ItemTags.ARROWS).build(), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Predicate:",
                "  -> Contains:",
                "    -> Predicate:",
                "      -> Items:",
                "        -> minecraft:andesite",
                "  -> Counts:",
                "    -> Predicate:",
                "      -> Items:",
                "        -> Tag: minecraft:arrows",
                "      -> Count: 1-5",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testBundlePredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getBundlePredicateTooltip(UTILS, new BundlePredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.ANDESITE).build())),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), ItemTags.ARROWS).build(), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Predicate:",
                "  -> Contains:",
                "    -> Predicate:",
                "      -> Items:",
                "        -> minecraft:andesite",
                "  -> Counts:",
                "    -> Predicate:",
                "      -> Items:",
                "        -> Tag: minecraft:arrows",
                "      -> Count: 1-5",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testFireworkExplosionPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getFireworkExplosionPredicateTooltip(UTILS, new FireworkExplosionPredicate(new FireworkExplosionPredicate.FireworkPredicate(
                Optional.of(FireworkExplosion.Shape.LARGE_BALL),
                Optional.of(true),
                Optional.of(false)
        ))), List.of(
                "Predicate:",
                "  -> Shape: LARGE_BALL",
                "  -> Trail: false",
                "  -> Twinkle: true"
        ));
    }

    @Test
    public void testFireworksPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getFireworksPredicateTooltip(UTILS, new FireworksPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new FireworkExplosionPredicate.FireworkPredicate(Optional.of(FireworkExplosion.Shape.BURST), Optional.empty(), Optional.empty()))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new FireworkExplosionPredicate.FireworkPredicate(Optional.of(FireworkExplosion.Shape.CREEPER), Optional.empty(), Optional.empty()), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )), MinMaxBounds.Ints.between(1, 4))), List.of(
                "Explosions:",
                "  -> Contains:",
                "    -> Predicate:",
                "      -> Shape: BURST",
                "  -> Counts:",
                "    -> Predicate:",
                "      -> Shape: CREEPER",
                "      -> Count: 1-5",
                "  -> Size: ≥4",
                "Flight Duration: 1-4"
        ));
    }

    @Test
    public void testWritableBookPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getWritableBookPredicateTooltip(UTILS, new WritableBookPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new WritableBookPredicate.PagePredicate("Hello"))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new WritableBookPredicate.PagePredicate("World"), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Predicate:",
                "  -> Contains:",
                "    -> Page: Hello",
                "  -> Counts:",
                "    -> Page: World",
                "      -> Count: 1-5",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testWrittenBookPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getWrittenBookPredicateTooltip(UTILS, new WrittenBookPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new WrittenBookPredicate.PagePredicate(Component.literal("Hello")))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new WrittenBookPredicate.PagePredicate(Component.literal("World")), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )), Optional.of("Yanny"), Optional.of("Testing"), MinMaxBounds.Ints.between(1, 8), Optional.of(false))), List.of(
                "Pages:",
                "  -> Contains:",
                "    -> Page: Hello",
                "  -> Counts:",
                "    -> Page: World",
                "      -> Count: 1-5",
                "  -> Size: ≥4",
                "Author: Yanny",
                "Title: Testing",
                "Generation: 1-8",
                "Resolved: false"
        ));
    }

    @Test
    public void testAttributeModifiersPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getAttributeModifiersPredicateTooltip(UTILS, new AttributeModifiersPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new AttributeModifiersPredicate.EntryPredicate(
                        Optional.of(HolderSet.direct(Attributes.ARMOR)),
                        Optional.of(Identifier.withDefaultNamespace("help")),
                        MinMaxBounds.Doubles.between(1, 4),
                        Optional.of(AttributeModifier.Operation.ADD_VALUE),
                        Optional.of(EquipmentSlotGroup.ARMOR)
                ))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new AttributeModifiersPredicate.EntryPredicate(
                        Optional.of(HolderSet.direct(Attributes.GRAVITY)),
                        Optional.empty(),
                        MinMaxBounds.Doubles.ANY,
                        Optional.empty(),
                        Optional.empty()
                ), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Predicate:",
                "  -> Contains:",
                "    -> Modifier:",
                "      -> Attributes:",
                "        -> minecraft:armor",
                "      -> Id: minecraft:help",
                "      -> Amount: 1.0-4.0",
                "      -> Operation: ADD_VALUE",
                "      -> Slot: ARMOR",
                "  -> Counts:",
                "    -> Modifier:",
                "      -> Attributes:",
                "        -> minecraft:gravity",
                "      -> Count: 1-5",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testTrimPredicateTooltip() {
        assertTooltip(DataComponentPredicateTooltipUtils.getTrimPredicateTooltip(UTILS, new TrimPredicate(
                Optional.of(HolderSet.direct(Holder.direct(TooltipTestSuite.LOOKUP.lookup(Registries.TRIM_MATERIAL).orElseThrow().get(TrimMaterials.GOLD).orElseThrow().value()))),
                Optional.of(HolderSet.direct(Holder.direct(TooltipTestSuite.LOOKUP.lookup(Registries.TRIM_PATTERN).orElseThrow().get(TrimPatterns.EYE).orElseThrow().value()))
        ))), List.of(
                "Materials:",
                "  -> minecraft:gold",
                "Patterns:",
                "  -> minecraft:eye"
        ));
    }

    @Test
    public void testJukebox() {
        assertTooltip(DataComponentPredicateTooltipUtils.getJukeboxPlayableTooltip(UTILS, new JukeboxPlayablePredicate(
                Optional.of(HolderSet.direct(LOOKUP.lookup(Registries.JUKEBOX_SONG).orElseThrow().get(JukeboxSongs.PIGSTEP).orElseThrow()))
        )), List.of(
                "Songs:",
                "  -> minecraft:pigstep"
        ));
    }
}
