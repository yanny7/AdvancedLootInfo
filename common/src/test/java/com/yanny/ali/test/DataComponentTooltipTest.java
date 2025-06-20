package com.yanny.ali.test;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.yanny.ali.plugin.client.DataComponentTooltipUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.utils.TestUtils.assertUnorderedTooltip;

public class DataComponentTooltipTest {
    @Test
    public void testCustomDataTooltip() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("test", 5);

        assertTooltip(DataComponentTooltipUtils.getCustomDataTooltip(UTILS, 0, CustomData.of(tag)), List.of("Tag: {test:5}"));
    }

    @Test
    public void testIntTooltip() {
        assertTooltip(DataComponentTooltipUtils.getIntTooltip(UTILS, 0, 13), List.of("Value: 13"));
    }

    @Test
    public void testCustomNameTooltip() {
        assertTooltip(DataComponentTooltipUtils.getCustomNameTooltip(UTILS, 0, Component.literal("Hello")), List.of("Custom Name: Hello"));
    }

    @Test
    public void testItemNameTooltip() {
        assertTooltip(DataComponentTooltipUtils.getItemNameTooltip(UTILS, 0, Component.literal("Hello")), List.of("Item Name: Hello"));
    }

    @Test
    public void testItemLoreTooltip() {
        assertTooltip(DataComponentTooltipUtils.getItemLoreTooltip(UTILS, 0, new ItemLore(
                List.of(Component.literal("Hello"), Component.literal("World")),
                List.of(Component.literal("Lorem"), Component.literal("Ipsum"))
        )), List.of(
                "Lines:",
                "  -> Hello",
                "  -> World",
                "Styled Lines:",
                "  -> Lorem",
                "  -> Ipsum"
        ));
    }

    @Test
    public void testRarityTooltip() {
        assertTooltip(DataComponentTooltipUtils.getRarityTooltip(UTILS, 0, Rarity.EPIC), List.of("Rarity: EPIC"));
    }

    @Test
    public void testItemEnchantmentsTooltip() {
        Object2IntOpenHashMap<Holder<Enchantment>> map = new Object2IntOpenHashMap<>();

        map.put(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), 2);
        map.put(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING), 1);

        assertUnorderedTooltip(DataComponentTooltipUtils.getItemEnchantmentsTooltip(UTILS, 0, new ItemEnchantments(map)), List.of(
                "Enchantments:",
                List.of(
                        "  -> minecraft:mending",
                        "    -> Level: 1",
                        "  -> minecraft:fortune",
                        "    -> Level: 2"
                )
        ));
    }

    @Test
    public void testAdventureModePredicateTooltip() {
        assertTooltip(DataComponentTooltipUtils.getAdventureModePredicateTooltip(UTILS, 0, new AdventureModePredicate(
                List.of(
                        BlockPredicate.Builder.block()
                                .of(LOOKUP.lookupOrThrow(Registries.BLOCK), BlockTags.BEDS)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.BED_PART, BedPart.FOOT)).build(),
                        BlockPredicate.Builder.block()
                                .of(LOOKUP.lookupOrThrow(Registries.BLOCK), Blocks.BELL).build()
                )
        )), List.of(
                "Blocks:",
                "  -> Predicate:",
                "    -> Blocks:",
                "      -> Tag: minecraft:beds",
                "    -> Properties:",
                "      -> part: foot",
                "  -> Predicate:",
                "    -> Blocks:",
                "      -> minecraft:bell"
        ));
    }

    @Test
    public void testAttributeModifiersTooltip() {
        assertTooltip(DataComponentTooltipUtils.getAttributeModifiersTooltip(UTILS, 0, new ItemAttributeModifiers(
                List.of(
                        new ItemAttributeModifiers.Entry(
                                Attributes.ARMOR,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("hello"),
                                        0.5,
                                        AttributeModifier.Operation.ADD_VALUE
                                ),
                                EquipmentSlotGroup.HEAD
                        ),
                        new ItemAttributeModifiers.Entry(
                                Attributes.ARMOR_TOUGHNESS,
                                new AttributeModifier(
                                        ResourceLocation.withDefaultNamespace("world"),
                                        1.25,
                                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                                ),
                                EquipmentSlotGroup.HAND
                        )
                )
        )), List.of(
                "Modifiers:",
                "  -> Modifier:",
                "    -> Attribute: minecraft:armor",
                "    -> Modifier:",
                "      -> Id: minecraft:hello",
                "      -> Amount: 0.5",
                "      -> Operation: ADD_VALUE",
                "    -> Slot: HEAD",
                "  -> Modifier:",
                "    -> Attribute: minecraft:armor_toughness",
                "    -> Modifier:",
                "      -> Id: minecraft:world",
                "      -> Amount: 1.25",
                "      -> Operation: ADD_MULTIPLIED_TOTAL",
                "    -> Slot: HAND"
        ));
    }

    @Test
    public void testCustomModelDataTooltip() {
        assertTooltip(DataComponentTooltipUtils.getCustomModelDataTooltip(UTILS, 0, new CustomModelData(
                List.of(0.1f, 0.2f),
                List.of(true, false),
                List.of("hello", "world"),
                List.of(1, 2)
        )), List.of(
                "Floats: [0.1, 0.2]",
                "Flags: [true, false]",
                "Strings: [hello, world]",
                "Colors: [1, 2]"
        ));
    }

    @Test
    public void testTooltipDisplayTooltip() {
        assertTooltip(DataComponentTooltipUtils.getTooltipDisplayTooltip(UTILS, 0, new TooltipDisplay(
                true,
                new ReferenceLinkedOpenHashSet<>(List.of(
                        DataComponents.DAMAGE,
                        DataComponents.MAX_STACK_SIZE
                ))
        )), List.of(
                "Hide Tooltip: true",
                "Hidden Components:",
                "  -> minecraft:damage",
                "  -> minecraft:max_stack_size"
        ));
    }

    @Test
    public void testEmptyTooltip() {
        assertTooltip(DataComponentTooltipUtils.getEmptyTooltip(UTILS, 0, Unit.INSTANCE), List.of());
    }

    @Test
    public void testBoolTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBoolTooltip(UTILS, 0, true), List.of("Value: true"));
    }

    @Test
    public void testFoodTooltip() {
        assertTooltip(DataComponentTooltipUtils.getFoodTooltip(UTILS, 0, new FoodProperties(
                5,
                2.5f,
                false
        )), List.of(
                "Nutrition: 5",
                "Saturation: 2.5",
                "Can Always Eat: false"
        ));
    }

    @Test
    public void testConsumableTooltip() {
        assertTooltip(DataComponentTooltipUtils.getConsumableTooltip(UTILS, 0, new Consumable(
                5,
                ItemUseAnimation.DRINK,
                Holder.direct(SoundEvents.ALLAY_HURT),
                true,
                List.of(
                        new TeleportRandomlyConsumeEffect(20),
                        new ClearAllStatusEffectsConsumeEffect()
                )
        )), List.of(
                "Consume Seconds: 5.0",
                "Animation: DRINK",
                "Sound: minecraft:entity.allay.hurt",
                "Has Custom Particles: true",
                "On Consume Effects:",
                "  -> Teleport Randomly:",
                "    -> Diameter: 20.0",
                "  -> Clear All Effects:"
        ));
    }

    @Test
    public void testUseRemainderTooltip() {
        assertUnorderedTooltip(DataComponentTooltipUtils.getUseRemainderTooltip(UTILS, 0, new UseRemainder(
                new ItemStack(Holder.direct(Items.ANDESITE), 10)
        )), List.of(
                "Convert Into:",
                "  -> Item: minecraft:andesite",
                "  -> Count: 10",
                "  -> Components:",
                List.of(
                        "    -> minecraft:attribute_modifiers",
                        "    -> minecraft:repair_cost",
                        "      -> Value: 0",
                        "    -> minecraft:item_name",
                        "      -> Item Name: Andesite",
                        "    -> minecraft:rarity",
                        "      -> Rarity: COMMON",
                        "    -> minecraft:lore",
                        "    -> minecraft:max_stack_size",
                        "      -> Value: 64",
                        "    -> minecraft:enchantments",
                        "    -> minecraft:item_model",
                        "      -> Value: minecraft:andesite",
                        "    -> minecraft:break_sound",
                        "      -> Sound: minecraft:entity.item.break",
                        "    -> minecraft:tooltip_display",
                        "      -> Hide Tooltip: false"
                )
        ));
    }

    @Test
    public void testUseCooldownTooltip() {
        assertTooltip(DataComponentTooltipUtils.getUseCooldownTooltip(UTILS, 0, new UseCooldown(
                3.5f,
                Optional.of(ResourceLocation.withDefaultNamespace("test"))
        )), List.of(
                "Seconds: 3.5",
                "Cooldown Group: minecraft:test"
        ));
    }

    @Test
    public void testDamageResistantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getDamageResistantTooltip(UTILS, 0, new DamageResistant(DamageTypeTags.DAMAGES_HELMET)),
                List.of("Type: minecraft:damages_helmet"));
    }

    @Test
    public void testToolTooltip() {
        assertTooltip(DataComponentTooltipUtils.getToolTooltip(UTILS, 0, new Tool(
                List.of(
                        new Tool.Rule(
                                HolderSet.direct(Holder.direct(Blocks.DIRT), Holder.direct(Blocks.STONE)),
                                Optional.of(2.5F),
                                Optional.of(true)
                        ),
                        new Tool.Rule(
                                HolderSet.direct(Holder.direct(Blocks.FURNACE)),
                                Optional.empty(),
                                Optional.empty()
                        )
                ),
                0.5f,
                10,
                true
        )), List.of(
                "Rules:",
                "  -> Rule:",
                "    -> Blocks:",
                "      -> minecraft:dirt",
                "      -> minecraft:stone",
                "    -> Correct For Drops: true",
                "    -> Speed: 2.5",
                "  -> Rule:",
                "    -> Blocks:",
                "      -> minecraft:furnace",
                "Default Mining Speed: 0.5",
                "Damage Per Block: 10",
                "Can Destroy Blocks In Creative: true"
        ));
    }

    @Test
    public void testWeaponTooltip() {
        assertTooltip(DataComponentTooltipUtils.getWeaponTooltip(UTILS, 0, new Weapon(
                5,
                3.5f
        )), List.of(
                "Item Damage Per Attack: 5",
                "Disable Blocking For Seconds: 3.5"
        ));
    }

    @Test
    public void testEnchantableTooltip() {
        assertTooltip(DataComponentTooltipUtils.getEnchantableTooltip(UTILS, 0, new Enchantable(16)), List.of("Value: 16"));
    }

    @Test
    public void testEquipableTooltip() {
        assertTooltip(DataComponentTooltipUtils.getEquipableTooltip(UTILS, 0, new Equippable(
                EquipmentSlot.LEGS,
                Holder.direct(SoundEvents.ANVIL_FALL),
                Optional.of(ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.withDefaultNamespace("anvil"))),
                Optional.of(ResourceLocation.withDefaultNamespace("camera")),
                Optional.of(HolderSet.direct(Holder.direct(EntityType.ALLAY), Holder.direct(EntityType.ARMADILLO))),
                true,
                true,
                false,
                true
        )), List.of(
                "Equipment Slot: LEGS",
                "Equip Sound: minecraft:block.anvil.fall",
                "Asset Id: minecraft:anvil",
                "Camera Overlay: minecraft:camera",
                "Allowed Entities:",
                "  -> minecraft:allay",
                "  -> minecraft:armadillo",
                "Dispensable: true",
                "Swappable: true",
                "Damage On Hurt: false",
                "Equip On Interact: true"
        ));
    }

    @Test
    public void testRepairableTooltip() {
        assertTooltip(DataComponentTooltipUtils.getRepairableTooltip(UTILS, 0, new Repairable(
                HolderSet.direct(Holder.direct(Items.ANDESITE), Holder.direct(Items.DIORITE))
        )), List.of(
                "Items:",
                "  -> minecraft:andesite",
                "  -> minecraft:diorite"
        ));
    }

    @Test
    public void testDeathProtectionTooltip() {
        assertTooltip(DataComponentTooltipUtils.getDeathProtectionTooltip(UTILS, 0, new DeathProtection(List.of(
                new TeleportRandomlyConsumeEffect(30),
                new ClearAllStatusEffectsConsumeEffect()
        ))), List.of(
                "Death Effects:",
                "  -> Teleport Randomly:",
                "    -> Diameter: 30.0",
                "  -> Clear All Effects:"
        ));
    }

    @Test
    public void testBlockAttacksTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBlockAttacksTooltip(UTILS, 0, new BlocksAttacks(
                3.5f,
                2.5f,
                List.of(
                        new BlocksAttacks.DamageReduction(1.2f, Optional.empty(), 1.8f, 3.12f),
                        new BlocksAttacks.DamageReduction(1.3f, Optional.empty(), 1.2f, 3.14f)
                ),
                new BlocksAttacks.ItemDamageFunction(1.24f, 3.15f, 5.24f),
                Optional.of(DamageTypeTags.IS_EXPLOSION),
                Optional.of(SoundEvents.AMBIENT_CAVE),
                Optional.of(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD)
        )), List.of(
                "Block Delay Seconds: 3.5",
                "Disable Cooldown Scale: 2.5",
                "Damage Reductions:",
                "  -> Damage Reduction:",
                "    -> Horizontal Blocking Angle: 1.2",
                "    -> Base: 1.8",
                "    -> Factor: 3.12",
                "  -> Damage Reduction:",
                "    -> Horizontal Blocking Angle: 1.3",
                "    -> Base: 1.2",
                "    -> Factor: 3.14",
                "Item Damage:",
                "  -> Threshold: 1.24",
                "  -> Base: 3.15",
                "  -> Factor: 5.24",
                "Bypassed By: minecraft:is_explosion",
                "Block Sound: minecraft:ambient.cave",
                "Disable Sound: minecraft:ambient.crimson_forest.mood"
        ));
    }

    @Test
    public void testDyedColorTooltip() {
        assertTooltip(DataComponentTooltipUtils.getDyedColorTooltip(UTILS, 0, new DyedItemColor(12345)), List.of("RGB: 12345"));
    }

    @Test
    public void testMapColorTooltip() {
        assertTooltip(DataComponentTooltipUtils.getMapColorTooltip(UTILS, 0, new MapItemColor(54321)), List.of("RGB: 54321"));
    }

    @Test
    public void testMapIdTooltip() {
        assertTooltip(DataComponentTooltipUtils.getMapIdTooltip(UTILS, 0, new MapId(654)), List.of("Value: 654"));
    }

    @Test
    public void testMapDecorationsTooltip() {
        Map<String, MapDecorations.Entry> map = new LinkedHashMap<>();

        map.put("Village", new MapDecorations.Entry(MapDecorationTypes.DESERT_VILLAGE, 100, 200, 25.5f));
        map.put("Player", new MapDecorations.Entry(MapDecorationTypes.PLAYER, 10, 20, 90));

        assertTooltip(DataComponentTooltipUtils.getMapDecorationsTooltip(UTILS, 0, new MapDecorations(map)), List.of(
                "Decorations:",
                "  -> Decoration: Village",
                "    -> minecraft:village_desert",
                "      -> X: 100.0",
                "      -> Z: 200.0",
                "      -> Rotation: 25.5",
                "  -> Decoration: Player",
                "    -> minecraft:player",
                "      -> X: 10.0",
                "      -> Z: 20.0",
                "      -> Rotation: 90.0"
        ));
    }

    @Test
    public void testMapPostProcessingTooltip() {
        assertTooltip(DataComponentTooltipUtils.getMapPostProcessingTooltip(UTILS, 0, MapPostProcessing.SCALE), List.of("Value: SCALE"));
    }

    @Test
    public void testChargedProjectilesTooltip() {
        assertUnorderedTooltip(DataComponentTooltipUtils.getChargedProjectilesTooltip(UTILS, 0, ChargedProjectiles.of(List.of(
                new ItemStack(Holder.direct(Items.ARROW), 25),
                new ItemStack(Holder.direct(Items.SNOWBALL), 2)
        ))), List.of(
                "Items:",
                "  -> Item:",
                "    -> Item: minecraft:arrow",
                "    -> Count: 25",
                "    -> Components:",
                List.of(
                        "      -> minecraft:attribute_modifiers",
                        "      -> minecraft:repair_cost",
                        "        -> Value: 0",
                        "      -> minecraft:item_name",
                        "        -> Item Name: Arrow",
                        "      -> minecraft:rarity",
                        "        -> Rarity: COMMON",
                        "      -> minecraft:lore",
                        "      -> minecraft:max_stack_size",
                        "        -> Value: 64",
                        "      -> minecraft:enchantments",
                        "      -> minecraft:item_model",
                        "        -> Value: minecraft:arrow",
                        "      -> minecraft:break_sound",
                        "        -> Sound: minecraft:entity.item.break",
                        "      -> minecraft:tooltip_display",
                        "        -> Hide Tooltip: false"
                ),
                "  -> Item:",
                "    -> Item: minecraft:snowball",
                "    -> Count: 2",
                "    -> Components:",
                List.of(
                        "      -> minecraft:attribute_modifiers",
                        "      -> minecraft:repair_cost",
                        "        -> Value: 0",
                        "      -> minecraft:item_name",
                        "        -> Item Name: Snowball",
                        "      -> minecraft:rarity",
                        "        -> Rarity: COMMON",
                        "      -> minecraft:lore",
                        "      -> minecraft:max_stack_size",
                        "        -> Value: 16",
                        "      -> minecraft:enchantments",
                        "      -> minecraft:item_model",
                        "        -> Value: minecraft:snowball",
                        "      -> minecraft:break_sound",
                        "        -> Sound: minecraft:entity.item.break",
                        "      -> minecraft:tooltip_display",
                        "        -> Hide Tooltip: false"
                )
        ));
    }

    @Test
    public void testBundleContentsTooltip() {
        assertUnorderedTooltip(DataComponentTooltipUtils.getBundleContentsTooltip(UTILS, 0, new BundleContents(List.of(
                new ItemStack(Holder.direct(Items.COAL_BLOCK)),
                new ItemStack(Holder.direct(Items.DIORITE))
        ))), List.of(
                "Items:",
                "  -> Item:",
                "    -> Item: minecraft:coal_block",
                "    -> Count: 1",
                "    -> Components:",
                List.of(
                        "      -> minecraft:attribute_modifiers",
                        "      -> minecraft:repair_cost",
                        "        -> Value: 0",
                        "      -> minecraft:item_name",
                        "        -> Item Name: Block of Coal",
                        "      -> minecraft:rarity",
                        "        -> Rarity: COMMON",
                        "      -> minecraft:lore",
                        "      -> minecraft:max_stack_size",
                        "        -> Value: 64",
                        "      -> minecraft:enchantments",
                        "      -> minecraft:item_model",
                        "        -> Value: minecraft:coal_block",
                        "      -> minecraft:break_sound",
                        "        -> Sound: minecraft:entity.item.break",
                        "      -> minecraft:tooltip_display",
                        "        -> Hide Tooltip: false"
                ),
                "  -> Item:",
                "    -> Item: minecraft:diorite",
                "    -> Count: 1",
                "    -> Components:",
                List.of(
                        "      -> minecraft:attribute_modifiers",
                        "      -> minecraft:repair_cost",
                        "        -> Value: 0",
                        "      -> minecraft:item_name",
                        "        -> Item Name: Diorite",
                        "      -> minecraft:rarity",
                        "        -> Rarity: COMMON",
                        "      -> minecraft:lore",
                        "      -> minecraft:max_stack_size",
                        "        -> Value: 64",
                        "      -> minecraft:enchantments",
                        "      -> minecraft:item_model",
                        "        -> Value: minecraft:diorite",
                        "      -> minecraft:break_sound",
                        "        -> Sound: minecraft:entity.item.break",
                        "      -> minecraft:tooltip_display",
                        "        -> Hide Tooltip: false"
                ),
                "Fraction: 1/32"
        ));
    }

    @Test
    public void testPotionContentsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getPotionContentsTooltip(UTILS, 0, new PotionContents(
                Optional.of(Potions.HARMING),
                Optional.of(5),
                List.of(
                        new MobEffectInstance(MobEffects.BLINDNESS, 5, 2),
                        new MobEffectInstance(MobEffects.ABSORPTION)
                ),
                Optional.of("Hello")
        )), List.of(
                "Potion: minecraft:harming",
                "Custom Color: 5",
                "Custom Effects:",
                "  -> minecraft:blindness",
                "    -> Duration: 5",
                "    -> Amplifier: 2",
                "    -> Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true",
                "  -> minecraft:absorption",
                "    -> Duration: 0",
                "    -> Amplifier: 0",
                "    -> Ambient: false",
                "    -> Is Visible: true",
                "    -> Show Icon: true",
                "Custom Name: Hello"
        ));
    }

    @Test
    public void testFloatValueTooltip() {
        assertTooltip(DataComponentTooltipUtils.getFloatValueTooltip(UTILS, 0, 3.2f), List.of("Value: 3.2"));
    }

    @Test
    public void testSuspiciousStewEffectsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getSuspiciousStewEffectsTooltip(UTILS, 0, new SuspiciousStewEffects(List.of(
                new SuspiciousStewEffects.Entry(MobEffects.ABSORPTION, 2),
                new SuspiciousStewEffects.Entry(MobEffects.LUCK, 3)
        ))), List.of(
                "Effects:",
                "  -> minecraft:absorption",
                "    -> Duration: 2",
                "  -> minecraft:luck",
                "    -> Duration: 3"
        ));
    }

    @Test
    public void testWritableBookContentTooltip() {
        assertTooltip(DataComponentTooltipUtils.getWritableBookContentTooltip(UTILS, 0, new WritableBookContent(List.of(
                new Filterable<>("Hello", Optional.of("World")),
                new Filterable<>("Lorem", Optional.of("Ipsum"))
        ))), List.of(
                "Pages:",
                "  -> Page:",
                "    -> Raw: Hello",
                "    -> Filtered: World",
                "  -> Page:",
                "    -> Raw: Lorem",
                "    -> Filtered: Ipsum"
        ));
    }

    @Test
    public void testWrittenBookContentTooltip() {
        assertTooltip(DataComponentTooltipUtils.getWrittenBookContentTooltip(UTILS, 0, new WrittenBookContent(
                new Filterable<>("Hello", Optional.of("World")),
                "Yanny",
                3,
                List.of(
                        new Filterable<>(Component.literal("Lorem"), Optional.of(Component.literal("Ipsum"))),
                        new Filterable<>(Component.literal("Sum"), Optional.of(Component.literal("Rum")))
                ),
                true
        )), List.of(
                "Title:",
                "  -> Raw: Hello",
                "  -> Filtered: World",
                "Author: Yanny",
                "Generation: 3",
                "Pages:",
                "  -> Page:",
                "    -> Raw: Lorem",
                "    -> Filtered: Ipsum",
                "  -> Page:",
                "    -> Raw: Sum",
                "    -> Filtered: Rum",
                "Resolved: true"
        ));
    }

    @Test
    public void testTrimTooltip() {
        assertTooltip(DataComponentTooltipUtils.getTrimTooltip(UTILS, 0, new ArmorTrim(
                LOOKUP.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.NETHERITE),
                LOOKUP.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.SILENCE)
        )), List.of(
                "Material: minecraft:netherite",
                "Pattern: minecraft:silence"
        ));
    }

    @Test
    public void testDebugStickStateTooltip() {
        Map<Holder<Block>, Property<?>> map = new LinkedHashMap<>();

        map.put(Holder.direct(Blocks.STONE), BlockStateProperties.VERTICAL_DIRECTION);
        map.put(Holder.direct(Blocks.FURNACE), BlockStateProperties.LIT);

        assertTooltip(DataComponentTooltipUtils.getDebugStickStateTooltip(UTILS, 0, new DebugStickState(map)), List.of(
                "Properties:",
                "  -> Block: minecraft:stone",
                "    -> Property: vertical_direction",
                "  -> Block: minecraft:furnace",
                "    -> Property: lit"
        ));
    }

    @Test
    public void testInstrumentTooltip() {
        assertTooltip(DataComponentTooltipUtils.getInstrumentTooltip(UTILS, 0, new InstrumentComponent(LOOKUP.lookupOrThrow(Registries.INSTRUMENT)
                .getOrThrow(Instruments.SING_GOAT_HORN))), List.of("Value: minecraft:sing_goat_horn"));
    }

    @Test
    public void testProvidesTrimMaterialTooltip() {
        assertTooltip(DataComponentTooltipUtils.getProvidesTrimMaterialTooltip(UTILS, 0, new ProvidesTrimMaterial(
                LOOKUP.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.GOLD)
        )), List.of("Material: minecraft:gold"));
    }

    @Test
    public void testOminousBottleAmplifierTooltip() {
        assertTooltip(DataComponentTooltipUtils.getOminousBottleAmplifierTooltip(UTILS, 0, new OminousBottleAmplifier(5)), List.of("Value: 5"));
    }

    @Test
    public void testJukeboxPlayableTooltip() {
        assertTooltip(DataComponentTooltipUtils.getJukeboxPlayableTooltip(UTILS, 0, new JukeboxPlayable(
                new EitherHolder<>(Either.right(JukeboxSongs.PIGSTEP))
        )), List.of("Song: minecraft:pigstep"));
        assertTooltip(DataComponentTooltipUtils.getJukeboxPlayableTooltip(UTILS, 0, new JukeboxPlayable(
                new EitherHolder<>(Either.left(LOOKUP.lookupOrThrow(Registries.JUKEBOX_SONG).getOrThrow(JukeboxSongs.PIGSTEP)))
        )), List.of("Song: minecraft:pigstep"));
    }

    @Test
    public void testProvidesBannerPatternsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getProvidesBannerPatternsTooltip(UTILS, 0, BannerPatternTags.PATTERN_ITEM_FLOWER), List.of("Banner Pattern: minecraft:pattern_item/flower"));
    }

    @Test
    public void testRecipesTooltip() {
        assertTooltip(DataComponentTooltipUtils.getRecipesTooltip(UTILS, 0, List.of(
                ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("recipe1")),
                ResourceKey.create(Registries.RECIPE, ResourceLocation.withDefaultNamespace("recipe2"))
        )), List.of(
                "Recipes:",
                "  -> minecraft:recipe1",
                "  -> minecraft:recipe2"
        ));
    }

    @Test
    public void testLodestoneTrackerTooltip() {
        assertTooltip(DataComponentTooltipUtils.getLodestoneTrackerTooltip(UTILS, 0, new LodestoneTracker(
                Optional.of(GlobalPos.of(Level.END, new BlockPos(1, 2, 3))),
                true
        )), List.of(
                "Global Position:",
                "  -> Dimension: minecraft:the_end",
                "  -> Position: [X: 1, Y: 2, Z: 3]",
                "Tracked: true"
        ));
    }

    @Test
    public void testFireworkExplosionTooltip() {
        assertTooltip(DataComponentTooltipUtils.getFireworkExplosionTooltip(UTILS, 0, new FireworkExplosion(
                FireworkExplosion.Shape.LARGE_BALL,
                IntList.of(1, 2, 3),
                IntList.of(),
                true,
                false
        )), List.of(
                "Shape: LARGE_BALL",
                "Colors: [1, 2, 3]",
                "Fade Colors: []",
                "Has Trail: true",
                "Has Twinkle: false"
        ));
    }

    @Test
    public void testFireworksTooltip() {
        assertTooltip(DataComponentTooltipUtils.getFireworksTooltip(UTILS, 0, new Fireworks(
                10,
                List.of(
                        new FireworkExplosion(FireworkExplosion.Shape.STAR, IntList.of(), IntList.of(), true, true),
                        new FireworkExplosion(FireworkExplosion.Shape.CREEPER, IntList.of(), IntList.of(), true, true)
                )
        )), List.of(
                "Flight Duration: 10",
                "Explosions:",
                "  -> Explosion:",
                "    -> Shape: STAR",
                "    -> Colors: []",
                "    -> Fade Colors: []",
                "    -> Has Trail: true",
                "    -> Has Twinkle: true",
                "  -> Explosion:",
                "    -> Shape: CREEPER",
                "    -> Colors: []",
                "    -> Fade Colors: []",
                "    -> Has Trail: true",
                "    -> Has Twinkle: true"
        ));
    }

    @Test
    public void testProfileTooltip() {
        PropertyMap map = new PropertyMap();

        map.put("Hello", new com.mojang.authlib.properties.Property("asdf", "jklo", "sign"));
        map.put("World", new com.mojang.authlib.properties.Property("qwer", "uiop", "help"));

        assertTooltip(DataComponentTooltipUtils.getProfileTooltip(UTILS, 0, new ResolvableProfile(
                Optional.of("Hello"),
                Optional.of(UUID.nameUUIDFromBytes(new byte[]{1, 1, 1, 1})),
                map
        )), List.of(
                "Name: Hello",
                "UUID: 3b5b9852-567e-3761-8aac-7f5f2d74ef74",
                "Properties:",
                "  -> Hello",
                "    -> Name: asdf",
                "    -> Value: jklo",
                "    -> Signature: sign",
                "  -> World",
                "    -> Name: qwer",
                "    -> Value: uiop",
                "    -> Signature: help"
        ));
    }

    @Test
    public void testResourceLocationTooltip() {
        assertTooltip(DataComponentTooltipUtils.getResourceLocationTooltip(UTILS, 0, ResourceLocation.withDefaultNamespace("test")), List.of("Value: minecraft:test"));
    }

    @Test
    public void testBannerPatternsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBannerPatternsTooltip(UTILS, 0, new BannerPatternLayers(List.of(
                new BannerPatternLayers.Layer(LOOKUP.lookupOrThrow(Registries.BANNER_PATTERN).getOrThrow(BannerPatterns.BASE), DyeColor.BLUE),
                new BannerPatternLayers.Layer(LOOKUP.lookupOrThrow(Registries.BANNER_PATTERN).getOrThrow(BannerPatterns.BORDER), DyeColor.RED)
        ))), List.of(
                "Banner Patterns:",
                "  -> minecraft:base",
                "    -> Color: BLUE",
                "  -> minecraft:border",
                "    -> Color: RED"
        ));
    }

    @Test
    public void testDyeColorTooltip() {
        assertTooltip(DataComponentTooltipUtils.getDyeColorTooltip(UTILS, 0, DyeColor.CYAN), List.of("Color: CYAN"));
    }

    @Test
    public void testPotDecorationsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getPotDecorationsTooltip(UTILS, 0, new PotDecorations(
                Items.SHELTER_POTTERY_SHERD,
                Items.SHEAF_POTTERY_SHERD,
                Items.ARMS_UP_POTTERY_SHERD,
                Items.BLADE_POTTERY_SHERD
        )), List.of(
                "Back: minecraft:shelter_pottery_sherd",
                "Left: minecraft:sheaf_pottery_sherd",
                "Right: minecraft:arms_up_pottery_sherd",
                "Front: minecraft:blade_pottery_sherd"
        ));
    }

    @Test
    public void testContainerTooltip() {
        assertUnorderedTooltip(DataComponentTooltipUtils.getContainerTooltip(UTILS, 0, ItemContainerContents.fromItems(List.of(
                new ItemStack(Holder.direct(Items.ANDESITE), 10),
                new ItemStack(Holder.direct(Items.DIORITE), 1)
        ))), List.of(
                "Items:",
                "  -> Item:",
                "    -> Item: minecraft:andesite",
                "    -> Count: 10",
                "    -> Components:",
                List.of(
                        "      -> minecraft:attribute_modifiers",
                        "      -> minecraft:repair_cost",
                        "        -> Value: 0",
                        "      -> minecraft:item_name",
                        "        -> Item Name: Andesite",
                        "      -> minecraft:rarity",
                        "        -> Rarity: COMMON",
                        "      -> minecraft:lore",
                        "      -> minecraft:max_stack_size",
                        "        -> Value: 64",
                        "      -> minecraft:enchantments",
                        "      -> minecraft:item_model",
                        "        -> Value: minecraft:andesite",
                        "      -> minecraft:break_sound",
                        "        -> Sound: minecraft:entity.item.break",
                        "      -> minecraft:tooltip_display",
                        "        -> Hide Tooltip: false"
                ),
                "  -> Item:",
                "    -> Item: minecraft:diorite",
                "    -> Count: 1",
                "    -> Components:",
                List.of(
                        "      -> minecraft:attribute_modifiers",
                        "      -> minecraft:repair_cost",
                        "        -> Value: 0",
                        "      -> minecraft:item_name",
                        "        -> Item Name: Diorite",
                        "      -> minecraft:rarity",
                        "        -> Rarity: COMMON",
                        "      -> minecraft:lore",
                        "      -> minecraft:max_stack_size",
                        "        -> Value: 64",
                        "      -> minecraft:enchantments",
                        "      -> minecraft:item_model",
                        "        -> Value: minecraft:diorite",
                        "      -> minecraft:break_sound",
                        "        -> Sound: minecraft:entity.item.break",
                        "      -> minecraft:tooltip_display",
                        "        -> Hide Tooltip: false"
                )
        ));
    }

    @Test
    public void testBlockStateTooltip() {
        Map<String, String> map = new LinkedHashMap<>();

        map.put("lit", "true");
        map.put("level", "5");

        assertTooltip(DataComponentTooltipUtils.getBlockStateTooltip(UTILS, 0, new BlockItemStateProperties(map)), List.of(
                "Properties:",
                "  -> lit: true",
                "  -> level: 5"
        ));
    }

    @Test
    public void testBeesTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBeesTooltip(UTILS, 0, new Bees(List.of(
                new BeehiveBlockEntity.Occupant(CustomData.of(new CompoundTag()), 100, 20),
                new BeehiveBlockEntity.Occupant(CustomData.of(new CompoundTag()), 1000, 30)
        ))), List.of(
                "Bees:",
                "  -> Occupant:",
                "    -> Entity Data: {}",
                "    -> Ticks In Hive: 100",
                "    -> Min Ticks In Hive: 20",
                "  -> Occupant:",
                "    -> Entity Data: {}",
                "    -> Ticks In Hive: 1000",
                "    -> Min Ticks In Hive: 30"
        ));
    }

    @Test
    public void testLockTooltip() {
        assertTooltip(DataComponentTooltipUtils.getLockTooltip(UTILS, 0, new LockCode(new ItemPredicate(
                Optional.empty(),
                MinMaxBounds.Ints.atLeast(5),
                DataComponentMatchers.Builder.components()
                        .exact(DataComponentExactPredicate.builder()
                                .expect(DataComponents.DAMAGE, 3)
                                .expect(DataComponents.MAX_STACK_SIZE, 16).build()).build()
        ))), List.of(
                "Predicate:",
                "  -> Count: ≥5",
                "  -> Components:",
                "    -> Expected Components:",
                "      -> minecraft:damage",
                "        -> Value: 3",
                "      -> minecraft:max_stack_size",
                "        -> Value: 16"
        ));
    }

    @Test
    public void testContainerLootTooltip() {
        assertTooltip(DataComponentTooltipUtils.getContainerLootTooltip(UTILS, 0, new SeededContainerLoot(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("loot")),
                12345L
        )), List.of(
                "Loot Table: minecraft:loot",
                "Seed: 12345"
        ));
    }

    @Test
    public void testBreakSoundTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBreakSoundTooltip(UTILS, 0, Holder.direct(SoundEvents.BASALT_BREAK)), List.of("Sound: minecraft:block.basalt.break"));
    }

    @Test
    public void testVillagerVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getVillagerVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.VILLAGER_TYPE).getOrThrow(VillagerType.JUNGLE)),
                List.of("Type: minecraft:jungle"));
    }

    @Test
    public void testWolfVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getWolfVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.PALE)),
                List.of("Type: minecraft:pale"));
    }

    @Test
    public void testWolfSoundVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getWolfSoundVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.WOLF_SOUND_VARIANT).getOrThrow(WolfSoundVariants.CUTE)),
                List.of("Type: minecraft:cute"));
    }

    @Test
    public void testEnumTypeTooltip() {
        assertTooltip(DataComponentTooltipUtils.getEnumTypeTooltip(UTILS, 0, Rabbit.Variant.EVIL),
                List.of("Type: EVIL"));
    }

    @Test
    public void testPigVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getPigVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.PIG_VARIANT).getOrThrow(PigVariants.COLD)),
                List.of("Type: minecraft:cold"));
    }

    @Test
    public void testCowVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getCowVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.COW_VARIANT).getOrThrow(CowVariants.COLD)),
                List.of("Type: minecraft:cold"));
    }

    @Test
    public void testChickenVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getChickenVariantTooltip(UTILS, 0, new EitherHolder<>(ChickenVariants.COLD)),
                List.of("Type: minecraft:cold"));
    }

    @Test
    public void testFrogVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getFrogVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.FROG_VARIANT).getOrThrow(FrogVariants.COLD)),
                List.of("Type: minecraft:cold"));
    }

    @Test
    public void testPaintingVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getPaintingVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(PaintingVariants.BUST)),
                List.of("Type: minecraft:bust"));
    }

    @Test
    public void testCatVariantTooltip() {
        assertTooltip(DataComponentTooltipUtils.getCatVariantTooltip(UTILS, 0, LOOKUP.lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariants.JELLIE)),
                List.of("Type: minecraft:jellie"));
    }
}
