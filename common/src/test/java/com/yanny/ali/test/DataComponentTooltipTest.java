package com.yanny.ali.test;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.yanny.ali.plugin.server.DataComponentTooltipUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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

        assertTooltip(DataComponentTooltipUtils.getCustomDataTooltip(UTILS, CustomData.of(tag)), List.of("Tag: {test:5}"));
    }

    @Test
    public void testIntTooltip() {
        assertTooltip(DataComponentTooltipUtils.getIntTooltip(UTILS, 13), List.of("Value: 13"));
    }

    @Test
    public void testUnbreakableTooltip() {
        assertTooltip(DataComponentTooltipUtils.getUnbreakableTooltip(UTILS, new Unbreakable(true)), List.of("Show In Tooltip: true"));
    }

    @Test
    public void testCustomNameTooltip() {
        assertTooltip(DataComponentTooltipUtils.getCustomNameTooltip(UTILS, Component.literal("Hello")), List.of("Custom Name: Hello"));
    }

    @Test
    public void testItemNameTooltip() {
        assertTooltip(DataComponentTooltipUtils.getItemNameTooltip(UTILS, Component.literal("Hello")), List.of("Item Name: Hello"));
    }

    @Test
    public void testItemLoreTooltip() {
        assertTooltip(DataComponentTooltipUtils.getItemLoreTooltip(UTILS, new ItemLore(
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
        assertTooltip(DataComponentTooltipUtils.getRarityTooltip(UTILS, Rarity.EPIC), List.of("Rarity: EPIC"));
    }

    @Test
    public void testItemEnchantmentsTooltip() {
        Object2IntOpenHashMap<Holder<Enchantment>> map = new Object2IntOpenHashMap<>();

        map.put(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), 2);
        map.put(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING), 1);

        assertUnorderedTooltip(DataComponentTooltipUtils.getItemEnchantmentsTooltip(UTILS, new ItemEnchantments(map, true)), List.of(
                "Enchantments:",
                List.of(
                        "  -> minecraft:mending",
                        "    -> Level: 1",
                        "  -> minecraft:fortune",
                        "    -> Level: 2"
                ),
                "Show In Tooltip: true"
        ));
    }

    @Test
    public void testAdventureModePredicateTooltip() {
        assertTooltip(DataComponentTooltipUtils.getAdventureModePredicateTooltip(UTILS, new AdventureModePredicate(
                List.of(
                        BlockPredicate.Builder.block()
                                .of(BlockTags.BEDS)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.BED_PART, BedPart.FOOT)).build(),
                        BlockPredicate.Builder.block()
                                .of(Blocks.BELL).build()
                ),
                true
        )), List.of(
                "Blocks:",
                "  -> Predicate:",
                "    -> Blocks:",
                "      -> Tag: minecraft:beds",
                "    -> Properties:",
                "      -> part: foot",
                "  -> Predicate:",
                "    -> Blocks:",
                "      -> minecraft:bell",
                "Tooltip:",
                "  -> Bell",
                "Show In Tooltip: true"
        ));
    }

    @Test
    public void testAttributeModifiersTooltip() {
        assertTooltip(DataComponentTooltipUtils.getAttributeModifiersTooltip(UTILS, new ItemAttributeModifiers(
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
                ),
                false
        )), List.of(
                "Modifiers:",
                "  -> Modifier:",
                "    -> Attribute: minecraft:generic.armor",
                "    -> Modifier:",
                "      -> Id: minecraft:hello",
                "      -> Amount: 0.5",
                "      -> Operation: ADD_VALUE",
                "    -> Slot: HEAD",
                "  -> Modifier:",
                "    -> Attribute: minecraft:generic.armor_toughness",
                "    -> Modifier:",
                "      -> Id: minecraft:world",
                "      -> Amount: 1.25",
                "      -> Operation: ADD_MULTIPLIED_TOTAL",
                "    -> Slot: HAND",
                "Show In Tooltip: false"
        ));
    }

    @Test
    public void testCustomModelDataTooltip() {
        assertTooltip(DataComponentTooltipUtils.getCustomModelDataTooltip(UTILS, new CustomModelData(5)), List.of("Value: 5"));
    }

    @Test
    public void testEmptyTooltip() {
        assertTooltip(DataComponentTooltipUtils.getEmptyTooltip(UTILS, Unit.INSTANCE), List.of());
    }

    @Test
    public void testBoolTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBoolTooltip(UTILS, true), List.of("Value: true"));
    }

    @Test
    public void testFoodTooltip() {
        assertTooltip(DataComponentTooltipUtils.getFoodTooltip(UTILS, new FoodProperties(
                5,
                2.5f,
                false,
                3.5f,
                Optional.of(new ItemStack(Holder.direct(Items.COAL), 5)),
                List.of(
                        new FoodProperties.PossibleEffect(
                                new MobEffectInstance(MobEffects.LUCK),
                                0.5f
                        ),
                        new FoodProperties.PossibleEffect(
                                new MobEffectInstance(MobEffects.BLINDNESS, 5),
                                0.25f
                        )
                )
        )), List.of(
                "Nutrition: 5",
                "Saturation: 2.5",
                "Can Always Eat: false",
                "Eat Seconds: 3.5",
                "Using Converts To:",
                "  -> Item: minecraft:coal",
                "  -> Count: 5",
                "  -> Components:",
                "    -> minecraft:max_stack_size",
                "      -> Value: 64",
                "    -> minecraft:lore",
                "    -> minecraft:enchantments",
                "    -> minecraft:repair_cost",
                "      -> Value: 0",
                "    -> minecraft:attribute_modifiers",
                "      -> Show In Tooltip: true",
                "    -> minecraft:rarity",
                "      -> Rarity: COMMON",
                "Effects:",
                "  -> Effect:",
                "    -> Effect: minecraft:luck",
                "      -> Duration: 0",
                "      -> Amplifier: 0",
                "      -> Ambient: false",
                "      -> Is Visible: true",
                "      -> Show Icon: true",
                "    -> Probability: 0.5",
                "  -> Effect:",
                "    -> Effect: minecraft:blindness",
                "      -> Duration: 5",
                "      -> Amplifier: 0",
                "      -> Ambient: false",
                "      -> Is Visible: true",
                "      -> Show Icon: true",
                "    -> Probability: 0.25"
        ));
    }

    @Test
    public void testToolTooltip() {
        assertTooltip(DataComponentTooltipUtils.getToolTooltip(UTILS, new Tool(
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
                10
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
                "Damage Per Block: 10"
        ));
    }

    @Test
    public void testDyedColorTooltip() {
        assertTooltip(DataComponentTooltipUtils.getDyedColorTooltip(UTILS, new DyedItemColor(
                12345,
                true
        )), List.of(
                "RGB: 12345",
                "Show In Tooltip: true"
        ));
    }

    @Test
    public void testMapColorTooltip() {
        assertTooltip(DataComponentTooltipUtils.getMapColorTooltip(UTILS, new MapItemColor(54321)), List.of("RGB: 54321"));
    }

    @Test
    public void testMapIdTooltip() {
        assertTooltip(DataComponentTooltipUtils.getMapIdTooltip(UTILS, new MapId(654)), List.of("Value: 654"));
    }

    @Test
    public void testMapDecorationsTooltip() {
        Map<String, MapDecorations.Entry> map = new LinkedHashMap<>();

        map.put("Village", new MapDecorations.Entry(MapDecorationTypes.DESERT_VILLAGE, 100, 200, 25.5f));
        map.put("Player", new MapDecorations.Entry(MapDecorationTypes.PLAYER, 10, 20, 90));

        assertTooltip(DataComponentTooltipUtils.getMapDecorationsTooltip(UTILS, new MapDecorations(map)), List.of(
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
        assertTooltip(DataComponentTooltipUtils.getMapPostProcessingTooltip(UTILS, MapPostProcessing.SCALE), List.of("Value: SCALE"));
    }

    @Test
    public void testChargedProjectilesTooltip() {
        assertTooltip(DataComponentTooltipUtils.getChargedProjectilesTooltip(UTILS, ChargedProjectiles.of(List.of(
                new ItemStack(Holder.direct(Items.ARROW), 25),
                new ItemStack(Holder.direct(Items.SNOWBALL), 2)
        ))), List.of(
                "Items:",
                "  -> Item:",
                "    -> Item: minecraft:arrow",
                "    -> Count: 25",
                "    -> Components:",
                "      -> minecraft:max_stack_size",
                "        -> Value: 64",
                "      -> minecraft:lore",
                "      -> minecraft:enchantments",
                "      -> minecraft:repair_cost",
                "        -> Value: 0",
                "      -> minecraft:attribute_modifiers",
                "        -> Show In Tooltip: true",
                "      -> minecraft:rarity",
                "        -> Rarity: COMMON",
                "  -> Item:",
                "    -> Item: minecraft:snowball",
                "    -> Count: 2",
                "    -> Components:",
                "      -> minecraft:max_stack_size",
                "        -> Value: 16",
                "      -> minecraft:lore",
                "      -> minecraft:enchantments",
                "      -> minecraft:repair_cost",
                "        -> Value: 0",
                "      -> minecraft:attribute_modifiers",
                "        -> Show In Tooltip: true",
                "      -> minecraft:rarity",
                "        -> Rarity: COMMON"
        ));
    }

    @Test
    public void testBundleContentsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBundleContentsTooltip(UTILS, new BundleContents(List.of(
                new ItemStack(Holder.direct(Items.COAL_BLOCK)),
                new ItemStack(Holder.direct(Items.DIORITE))
        ))), List.of(
                "Items:",
                "  -> Item:",
                "    -> Item: minecraft:coal_block",
                "    -> Count: 1",
                "    -> Components:",
                "      -> minecraft:max_stack_size",
                "        -> Value: 64",
                "      -> minecraft:lore",
                "      -> minecraft:enchantments",
                "      -> minecraft:repair_cost",
                "        -> Value: 0",
                "      -> minecraft:attribute_modifiers",
                "        -> Show In Tooltip: true",
                "      -> minecraft:rarity",
                "        -> Rarity: COMMON",
                "  -> Item:",
                "    -> Item: minecraft:diorite",
                "    -> Count: 1",
                "    -> Components:",
                "      -> minecraft:max_stack_size",
                "        -> Value: 64",
                "      -> minecraft:lore",
                "      -> minecraft:enchantments",
                "      -> minecraft:repair_cost",
                "        -> Value: 0",
                "      -> minecraft:attribute_modifiers",
                "        -> Show In Tooltip: true",
                "      -> minecraft:rarity",
                "        -> Rarity: COMMON",
                "Fraction: 1/32"
        ));
    }

    @Test
    public void testPotionContentsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getPotionContentsTooltip(UTILS, new PotionContents(
                Optional.of(Potions.HARMING),
                Optional.of(5),
                List.of(
                        new MobEffectInstance(MobEffects.BLINDNESS, 5, 2),
                        new MobEffectInstance(MobEffects.ABSORPTION)
                )
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
                "    -> Show Icon: true"
        ));
    }

    @Test
    public void testSuspiciousStewEffectsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getSuspiciousStewEffectsTooltip(UTILS, new SuspiciousStewEffects(List.of(
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
        assertTooltip(DataComponentTooltipUtils.getWritableBookContentTooltip(UTILS, new WritableBookContent(List.of(
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
        assertTooltip(DataComponentTooltipUtils.getWrittenBookContentTooltip(UTILS, new WrittenBookContent(
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
        assertTooltip(DataComponentTooltipUtils.getTrimTooltip(UTILS, new ArmorTrim(
                LOOKUP.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.NETHERITE),
                LOOKUP.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.SILENCE),
                true
        )), List.of(
                "Material: minecraft:netherite",
                "Pattern: minecraft:silence",
                "Show In Tooltip: true"
        ));
    }

    @Test
    public void testDebugStickStateTooltip() {
        Map<Holder<Block>, Property<?>> map = new LinkedHashMap<>();

        map.put(Holder.direct(Blocks.STONE), BlockStateProperties.VERTICAL_DIRECTION);
        map.put(Holder.direct(Blocks.FURNACE), BlockStateProperties.LIT);

        assertTooltip(DataComponentTooltipUtils.getDebugStickStateTooltip(UTILS, new DebugStickState(map)), List.of(
                "Properties:",
                "  -> Block: minecraft:stone",
                "    -> Property: vertical_direction",
                "  -> Block: minecraft:furnace",
                "    -> Property: lit"
        ));
    }

    @Test
    public void testInstrumentTooltip() {
        assertTooltip(DataComponentTooltipUtils.getInstrumentTooltip(UTILS, LOOKUP.lookupOrThrow(Registries.INSTRUMENT)
                .getOrThrow(Instruments.SING_GOAT_HORN)), List.of("Value: minecraft:sing_goat_horn"));
    }

    @Test
    public void testJukeboxPlayableTooltip() {
        assertTooltip(DataComponentTooltipUtils.getJukeboxPlayableTooltip(UTILS, new JukeboxPlayable(
                EitherHolder.fromEither(Either.right(JukeboxSongs.PIGSTEP)),
                true
        )), List.of(
                "Song: minecraft:pigstep",
                "Show In Tooltip: true"
        ));
        assertTooltip(DataComponentTooltipUtils.getJukeboxPlayableTooltip(UTILS, new JukeboxPlayable(
                EitherHolder.fromEither(Either.left(LOOKUP.lookupOrThrow(Registries.JUKEBOX_SONG).getOrThrow(JukeboxSongs.PIGSTEP))),
                true
        )), List.of(
                "Song: minecraft:pigstep",
                "Show In Tooltip: true"
        ));
    }

    @Test
    public void testRecipesTooltip() {
        assertTooltip(DataComponentTooltipUtils.getRecipesTooltip(UTILS, List.of(
                ResourceLocation.withDefaultNamespace("recipe1"),
                ResourceLocation.withDefaultNamespace("recipe2")
        )), List.of(
                "Recipes:",
                "  -> minecraft:recipe1",
                "  -> minecraft:recipe2"
        ));
    }

    @Test
    public void testLodestoneTrackerTooltip() {
        assertTooltip(DataComponentTooltipUtils.getLodestoneTrackerTooltip(UTILS, new LodestoneTracker(
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
        assertTooltip(DataComponentTooltipUtils.getFireworkExplosionTooltip(UTILS, new FireworkExplosion(
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
        assertTooltip(DataComponentTooltipUtils.getFireworksTooltip(UTILS, new Fireworks(
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

        assertTooltip(DataComponentTooltipUtils.getProfileTooltip(UTILS, new ResolvableProfile(
                Optional.of("Hello"),
                Optional.of(UUID.nameUUIDFromBytes(new byte[]{1, 1, 1, 1})),
                map
        )), List.of(
                "Name: Hello",
                "UUID: 3b5b9852-567e-3761-8aac-7f5f2d74ef74",
                "Properties:",
                "  -> Hello",
                "    -> Properties:",
                "      -> Property:",
                "        -> Name: asdf",
                "        -> Value: jklo",
                "        -> Signature: sign",
                "  -> World",
                "    -> Properties:",
                "      -> Property:",
                "        -> Name: qwer",
                "        -> Value: uiop",
                "        -> Signature: help"
        ));
    }

    @Test
    public void testNoteBlockSoundTooltip() {
        assertTooltip(DataComponentTooltipUtils.getNoteBlockSoundTooltip(UTILS, ResourceLocation.withDefaultNamespace("test")), List.of("Value: minecraft:test"));
    }

    @Test
    public void testBannerPatternsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBannerPatternsTooltip(UTILS, new BannerPatternLayers(List.of(
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
    public void testBaseColorTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBaseColorTooltip(UTILS, DyeColor.CYAN), List.of("Color: CYAN"));
    }

    @Test
    public void testPotDecorationsTooltip() {
        assertTooltip(DataComponentTooltipUtils.getPotDecorationsTooltip(UTILS, new PotDecorations(
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
        assertTooltip(DataComponentTooltipUtils.getContainerTooltip(UTILS, ItemContainerContents.fromItems(List.of(
                new ItemStack(Holder.direct(Items.ANDESITE), 10),
                new ItemStack(Holder.direct(Items.DIORITE), 1)
        ))), List.of(
                "Items:",
                "  -> Item:",
                "    -> Item: minecraft:andesite",
                "    -> Count: 10",
                "    -> Components:",
                "      -> minecraft:max_stack_size",
                "        -> Value: 64",
                "      -> minecraft:lore",
                "      -> minecraft:enchantments",
                "      -> minecraft:repair_cost",
                "        -> Value: 0",
                "      -> minecraft:attribute_modifiers",
                "        -> Show In Tooltip: true",
                "      -> minecraft:rarity",
                "        -> Rarity: COMMON",
                "  -> Item:",
                "    -> Item: minecraft:diorite",
                "    -> Count: 1",
                "    -> Components:",
                "      -> minecraft:max_stack_size",
                "        -> Value: 64",
                "      -> minecraft:lore",
                "      -> minecraft:enchantments",
                "      -> minecraft:repair_cost",
                "        -> Value: 0",
                "      -> minecraft:attribute_modifiers",
                "        -> Show In Tooltip: true",
                "      -> minecraft:rarity",
                "        -> Rarity: COMMON"
        ));
    }

    @Test
    public void testBlockStateTooltip() {
        Map<String, String> map = new LinkedHashMap<>();

        map.put("lit", "true");
        map.put("level", "5");

        assertTooltip(DataComponentTooltipUtils.getBlockStateTooltip(UTILS, new BlockItemStateProperties(map)), List.of(
                "Properties:",
                "  -> lit: true",
                "  -> level: 5"
        ));
    }

    @Test
    public void testBeesTooltip() {
        assertTooltip(DataComponentTooltipUtils.getBeesTooltip(UTILS, List.of(
                new BeehiveBlockEntity.Occupant(CustomData.of(new CompoundTag()), 100, 20),
                new BeehiveBlockEntity.Occupant(CustomData.of(new CompoundTag()), 1000, 30)
        )), List.of(
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
        assertTooltip(DataComponentTooltipUtils.getLockTooltip(UTILS, new LockCode("SECRET")), List.of("Value: SECRET"));
    }

    @Test
    public void testContainerLootTooltip() {
        assertTooltip(DataComponentTooltipUtils.getContainerLootTooltip(UTILS, new SeededContainerLoot(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("loot")),
                12345L
        )), List.of(
                "Loot Table: minecraft:loot",
                "Seed: 12345"
        ));
    }
}
