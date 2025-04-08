package com.yanny.ali.test;

import com.yanny.ali.plugin.ItemSubPredicateTooltipUtils;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class ItemSubPredicateTooltipTest {
    @Test
    public void testItemDamagePredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemDamagePredicateTooltip(UTILS, 0, ItemDamagePredicate.durability(MinMaxBounds.Ints.atMost(50))), List.of(
                "Damage:",
                "  -> Durability: ≤50"
        ));
        assertTooltip(ItemSubPredicateTooltipUtils.getItemDamagePredicateTooltip(UTILS, 0, new ItemDamagePredicate(
                MinMaxBounds.Ints.atMost(50),
                MinMaxBounds.Ints.atLeast(5)
        )), List.of(
                "Damage:",
                "  -> Damage: ≥5",
                "  -> Durability: ≤50"
        ));
    }

    @Test
    public void testItemEnchantmentsPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemEnchantmentsPredicateTooltip(UTILS, 0, ItemEnchantmentsPredicate.enchantments(List.of(
                new EnchantmentPredicate(Enchantments.LOOTING, MinMaxBounds.Ints.ANY),
                new EnchantmentPredicate(Enchantments.MENDING, MinMaxBounds.Ints.between(1, 5))
        ))), List.of(
                "Enchantments:",
                "  -> Enchantment: Looting",
                "  -> Enchantment: Mending",
                "    -> Level: 1-5"
        ));
    }

    @Test
    public void testItemStoredEnchantmentsPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemStoredEnchantmentsPredicateTooltip(UTILS, 0, ItemEnchantmentsPredicate.storedEnchantments(List.of(
                new EnchantmentPredicate(Enchantments.LOOTING, MinMaxBounds.Ints.ANY),
                new EnchantmentPredicate(Enchantments.MENDING, MinMaxBounds.Ints.between(1, 5))
        ))), List.of(
                "Stored Enchantments:",
                "  -> Enchantment: Looting",
                "  -> Enchantment: Mending",
                "    -> Level: 1-5"
        ));
    }

    @Test
    public void testPotionsPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemPotionsPredicateTooltip(UTILS, 0, (ItemPotionsPredicate) ItemPotionsPredicate.potions(
                HolderSet.direct(Potions.HEALING)
        )), List.of(
                "Potions:",
                "  -> Potion:",
                "    -> Mob Effects:",
                "      -> Mob Effect: minecraft:instant_health",
                "        -> Amplifier: 0",
                "        -> Duration: 1",
                "        -> Is Ambient: false",
                "        -> Is Visible: true",
                "        -> Show Icon: true"
        ));
    }

    @Test
    public void testMobEffectInstanceTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("tst", 5);

        assertTooltip(ItemSubPredicateTooltipUtils.getItemCustomDataPredicateTooltip(UTILS, 0, ItemCustomDataPredicate.customData(new NbtPredicate(compoundTag))), List.of(
                "Custom Data:",
                "  -> Nbt: {tst:5}"
        ));
    }

    @Test
    public void testContainerPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemContainerPredicateTooltip(UTILS, 0, new ItemContainerPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(ItemPredicate.Builder.item().of(Items.ANDESITE).build())),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(ItemPredicate.Builder.item().of(ItemTags.ARROWS).build(), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Container:",
                "  -> Contains:",
                "    -> Items:",
                "      -> Item: Andesite",
                "  -> Counts:",
                "    -> Items:",
                "      -> Tag: minecraft:arrows",
                "    -> Count: 1-5",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testBundlePredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemBundlePredicateTooltip(UTILS, 0, new ItemBundlePredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(ItemPredicate.Builder.item().of(Items.ANDESITE).build())),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(ItemPredicate.Builder.item().of(ItemTags.ARROWS).build(), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Bundle:",
                "  -> Contains:",
                "    -> Items:",
                "      -> Item: Andesite",
                "  -> Counts:",
                "    -> Items:",
                "      -> Tag: minecraft:arrows",
                "    -> Count: 1-5",
                "  -> Size: ≥4"
        ));
    }

    @Test
    public void testFireworkExplosionPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemFireworkExplosionPredicateTooltip(UTILS, 0, new ItemFireworkExplosionPredicate(new ItemFireworkExplosionPredicate.FireworkPredicate(
                Optional.of(FireworkExplosion.Shape.LARGE_BALL),
                Optional.of(true),
                Optional.of(false)
        ))), List.of(
                "Firework Explosion:",
                "  -> Shape: LARGE_BALL",
                "  -> Trail: false",
                "  -> Twinkle: true"
        ));
    }

    @Test
    public void testFireworksPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemFireworksPredicateTooltip(UTILS, 0, new ItemFireworksPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new ItemFireworkExplosionPredicate.FireworkPredicate(Optional.of(FireworkExplosion.Shape.BURST), Optional.empty(), Optional.empty()))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new ItemFireworkExplosionPredicate.FireworkPredicate(Optional.of(FireworkExplosion.Shape.CREEPER), Optional.empty(), Optional.empty()), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )), MinMaxBounds.Ints.between(1, 4))), List.of(
                "Fireworks:",
                "  -> Explosions:",
                "    -> Contains:",
                "      -> Shape: BURST",
                "    -> Counts:",
                "      -> Shape: CREEPER",
                "      -> Count: 1-5",
                "    -> Size: ≥4",
                "  -> Flight Duration: 1-4"
        ));
    }

    @Test
    public void testWritableBookPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemWritableBookPredicateTooltip(UTILS, 0, new ItemWritableBookPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new ItemWritableBookPredicate.PagePredicate("Hello"))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new ItemWritableBookPredicate.PagePredicate("World"), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Writable Book:",
                "  -> Pages:",
                "    -> Contains:",
                "      -> Page: Hello",
                "    -> Counts:",
                "      -> Page: World",
                "      -> Count: 1-5",
                "    -> Size: ≥4"
        ));
    }

    @Test
    public void testWrittenBookPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemWrittenBookPredicateTooltip(UTILS, 0, new ItemWrittenBookPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new ItemWrittenBookPredicate.PagePredicate(Component.literal("Hello")))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new ItemWrittenBookPredicate.PagePredicate(Component.literal("World")), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )), Optional.of("Yanny"), Optional.of("Testing"), MinMaxBounds.Ints.between(1, 8), Optional.of(false))), List.of(
                "Written Book:",
                "  -> Pages:",
                "    -> Contains:",
                "      -> Page: Hello",
                "    -> Counts:",
                "      -> Page: World",
                "      -> Count: 1-5",
                "    -> Size: ≥4",
                "  -> Author: Yanny",
                "  -> Title: Testing",
                "  -> Generation: 1-8",
                "  -> Resolved: false"
        ));
    }

    @Test
    public void testAttributeModifiersPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemAttributeModifiersPredicateTooltip(UTILS, 0, new ItemAttributeModifiersPredicate(Optional.of(new CollectionPredicate<>(
                Optional.of(CollectionContentsPredicate.of(new ItemAttributeModifiersPredicate.EntryPredicate(
                        Optional.of(HolderSet.direct(Attributes.ARMOR)),
                        Optional.of(UUID.nameUUIDFromBytes(new byte[]{1, 2, 3, 4})),
                        Optional.of("Help"),
                        MinMaxBounds.Doubles.between(1, 4),
                        Optional.of(AttributeModifier.Operation.ADD_VALUE),
                        Optional.of(EquipmentSlotGroup.ARMOR)
                ))),
                Optional.of(CollectionCountsPredicate.of(new CollectionCountsPredicate.Entry<>(new ItemAttributeModifiersPredicate.EntryPredicate(
                        Optional.of(HolderSet.direct(Attributes.GRAVITY)),
                        Optional.empty(),
                        Optional.empty(),
                        MinMaxBounds.Doubles.ANY,
                        Optional.empty(),
                        Optional.empty()
                ), MinMaxBounds.Ints.between(1, 5)))),
                Optional.of(MinMaxBounds.Ints.atLeast(4))
        )))), List.of(
                "Attribute Modifiers:",
                "  -> Modifiers:",
                "    -> Contains:",
                "      -> Attributes:",
                "        -> Attribute: Armor",
                "      -> UUID: 08d6c05a-2151-3a79-a1df-eb9d2a8f262f",
                "      -> Name: Help",
                "      -> Amount: 1.0-4.0",
                "      -> Operation: ADD_VALUE",
                "      -> Slot: ARMOR",
                "    -> Counts:",
                "      -> Attributes:",
                "        -> Attribute: Gravity",
                "      -> Count: 1-5",
                "    -> Size: ≥4"
        ));
    }

    @Test
    public void testTrimPredicateTooltip() {
        assertTooltip(ItemSubPredicateTooltipUtils.getItemTrimPredicateTooltip(UTILS, 0, new ItemTrimPredicate(
                Optional.of(HolderSet.direct(Holder.direct(TooltipTestSuite.LOOKUP.lookup(Registries.TRIM_MATERIAL).orElseThrow().get(TrimMaterials.GOLD).orElseThrow().value()))),
                Optional.of(HolderSet.direct(Holder.direct(TooltipTestSuite.LOOKUP.lookup(Registries.TRIM_PATTERN).orElseThrow().get(TrimPatterns.EYE).orElseThrow().value()))
        ))), List.of(
                "Trim:",
                "  -> Materials:",
                "    -> Asset Name: gold",
                "    -> Item: Gold Ingot",
                "    -> Model Index: 0.6",
                "    -> Description: Gold Material",
                "  -> Patterns:",
                "    -> Asset Id: minecraft:eye",
                "    -> Item: Smithing Template",
                "    -> Description: Eye Armor Trim",
                "    -> Decal: false"
        ));
    }
}
