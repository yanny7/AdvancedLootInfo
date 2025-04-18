package com.yanny.ali.test;

import com.yanny.ali.plugin.client.FunctionTooltipUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class FunctionTooltipTest {
    @Test
    public void testApplyBonusCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getApplyBonusTooltip(UTILS, 0, (ApplyBonusCount) ApplyBonusCount.addOreBonusCount(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING).orElseThrow()).build()), List.of(
                "Apply Bonus:",
                "  -> Enchantment: Looting",
                "  -> Formula: minecraft:ore_drops"
        ));
    }

    @Test
    public void testCopyNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyNameTooltip(UTILS, 0, (CopyNameFunction) CopyNameFunction.copyName(CopyNameFunction.NameSource.THIS).build()), List.of(
                "Copy Name:",
                "  -> Source: THIS"
        ));
    }

    @Test
    public void testCopyNbtTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyCustomDataTooltip(UTILS, 0, (CopyCustomDataFunction) CopyCustomDataFunction.copyData(LootContext.EntityTarget.ATTACKER).build()), List.of(
                "Copy Custom Data:",
                "  -> Source: minecraft:context"
        ));
        assertTooltip(FunctionTooltipUtils.getCopyCustomDataTooltip(UTILS, 0, (CopyCustomDataFunction) CopyCustomDataFunction.copyData(LootContext.EntityTarget.ATTACKER)
                .copy("asdf", "jklo", CopyCustomDataFunction.MergeStrategy.MERGE)
                .build()
        ), List.of(
                "Copy Custom Data:",
                "  -> Source: minecraft:context",
                "  -> Copy Operations:",
                "    -> Source Path: asdf",
                "    -> Target Path: jklo",
                "    -> Merge Strategy: MERGE"
        ));
    }

    @Test
    public void testCopyStateTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyStateTooltip(UTILS, 0, (CopyBlockState) CopyBlockState.copyState(Blocks.FURNACE)
                .copy(BlockStateProperties.LIT)
                .copy(BlockStateProperties.HORIZONTAL_FACING)
                .build()
        ), List.of(
                "Copy State:",
                "  -> Block: Furnace",
                "  -> Properties:",
                "    -> lit [true, false]",
                "    -> facing [north, south, west, east]"
        ));
    }

    @Test
    public void testEnchantRandomlyTooltip() {
        assertTooltip(FunctionTooltipUtils.getEnchantRandomlyTooltip(UTILS, 0, (EnchantRandomlyFunction) EnchantRandomlyFunction.randomEnchantment()
                .withEnchantment(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.CHANNELING).orElseThrow())
                .build()
        ), List.of(
                "Enchant Randomly:",
                "  -> Enchantments:",
                "    -> Enchantment: Channeling",
                "  -> Only Compatible: true"
        ));
    }

    @Test
    public void testEnchantWithLevelsTooltip() {
        assertTooltip(FunctionTooltipUtils.getEnchantWithLevelsTooltip(UTILS, 0, (EnchantWithLevelsFunction) EnchantWithLevelsFunction.enchantWithLevels(LOOKUP, UniformGenerator.between(1, 3))
                .fromOptions(HolderSet.direct(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING).orElseThrow())).build()
        ), List.of(
                "Enchant With Levels:",
                "  -> Levels: 1-3",
                "  -> Options:",
                "    -> Enchantment: Looting"
        ));
    }

    @Test
    public void testExplorationMapTooltip() {
        assertTooltip(FunctionTooltipUtils.getExplorationMapTooltip(UTILS, 0, (ExplorationMapFunction) ExplorationMapFunction.makeExplorationMap()
                .setDestination(StructureTags.RUINED_PORTAL)
                .setMapDecoration(MapDecorationTypes.OCEAN_MONUMENT)
                .setZoom((byte) 2)
                .setSearchRadius(50)
                .setSkipKnownStructures(true)
                .build()
        ), List.of(
                "Exploration Map:",
                "  -> Destination: minecraft:ruined_portal",
                "  -> Map Decoration:",
                "    -> Asset Id: minecraft:ocean_monument",
                "    -> Color: 3830373",
                "    -> Show On Item Frame: true",
                "    -> Exploration Map Element: true",
                "    -> Track Count: false",
                "  -> Zoom: 2",
                "  -> Search Radius: 50",
                "  -> Skip Known Structures: true"
        ));
    }

    @Test
    public void testExplosionDecayTooltip() {
        assertTooltip(FunctionTooltipUtils.getExplosionDecayTooltip(UTILS, 0, (ApplyExplosionDecay) ApplyExplosionDecay.explosionDecay().build()), List.of("Explosion Decay"));
    }

    @Test
    public void testFillPlayerHeadTooltip() {
        assertTooltip(FunctionTooltipUtils.getFillPlayerHeadTooltip(UTILS, 0, (FillPlayerHead) FillPlayerHead.fillPlayerHead(LootContext.EntityTarget.ATTACKER).build()), List.of(
                "Fill Player Head:",
                "  -> Target: ATTACKER"
        ));
    }

    @Test
    public void testFurnaceSmeltTooltip() {
        assertTooltip(FunctionTooltipUtils.getFurnaceSmeltTooltip(UTILS, 0, (SmeltItemFunction) SmeltItemFunction.smelted().build()), List.of("Use Smelting Recipe On Item"));
    }

    @Test
    public void testLimitCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getLimitCountTooltip(UTILS, 0, (LimitCount) LimitCount.limitCount(IntRange.range(0, 10)).build()), List.of(
                "Limit Count:",
                "  -> Limit: 0 - 10"
        ));
    }

    @Test
    public void testLootingEnchantTooltip() {
        assertTooltip(FunctionTooltipUtils.getEnchantedCountIncreaseTooltip(UTILS, 0, (EnchantedCountIncreaseFunction) EnchantedCountIncreaseFunction.lootingMultiplier(LOOKUP, UniformGenerator.between(0, 4))
                .setLimit(3)
                .build()
        ), List.of(
                "Enchanted Count Increase:",
                "  -> Enchantment: Looting",
                "  -> Value: 0-4",
                "  -> Limit: 3"
        ));
    }

    @Test
    public void testReferenceTooltip() {
        assertTooltip(FunctionTooltipUtils.getReferenceTooltip(UTILS, 0, (FunctionReference) FunctionReference.functionReference(ResourceKey.create(
                Registries.ITEM_MODIFIER,
                ResourceLocation.withDefaultNamespace("gameplay/fishing"))
        ).build()), List.of(
                "Reference:",
                "  -> Name: minecraft:gameplay/fishing"
        ));
    }

    @Test
    public void testSequenceTooltip() {
        assertTooltip(FunctionTooltipUtils.getSequenceTooltip(UTILS, 0, SequenceFunction.of(List.of(
                ApplyExplosionDecay.explosionDecay().build(),
                SmeltItemFunction.smelted().build()
        ))), List.of(
                "Sequence:",
                "  -> Explosion Decay",
                "  -> Use Smelting Recipe On Item"
        ));
    }

    @Test
    public void testSetAttributesTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetAttributesTooltip(UTILS, 0, (SetAttributesFunction) SetAttributesFunction.setAttributes()
                .withModifier(new SetAttributesFunction.ModifierBuilder(ResourceLocation.withDefaultNamespace("armor"), Attributes.ARMOR, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, UniformGenerator.between(1, 5))
                        .forSlot(EquipmentSlotGroup.HEAD)
                        .forSlot(EquipmentSlotGroup.CHEST)
                        .forSlot(EquipmentSlotGroup.LEGS)
                        .forSlot(EquipmentSlotGroup.FEET))
                .build()), List.of(
                "Set Attributes:",
                "  -> Modifiers:",
                "    -> Modifier:",
                "      -> Attribute: Armor",
                "      -> Operation: ADD_MULTIPLIED_TOTAL",
                "      -> Amount: 1-5",
                "      -> Id: minecraft:armor",
                "      -> Equipment Slots:",
                "        -> FEET",
                "        -> LEGS",
                "        -> CHEST",
                "        -> HEAD"
        ));
    }

    @Test
    public void testSetBannerPatternTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetBannerPatternTooltip(UTILS, 0, (SetBannerPatternFunction) SetBannerPatternFunction.setBannerPattern(true)
                .addPattern(TooltipTestSuite.LOOKUP.lookup(Registries.BANNER_PATTERN).orElseThrow().get(BannerPatterns.BASE).orElseThrow(), DyeColor.WHITE)
                .addPattern(TooltipTestSuite.LOOKUP.lookup(Registries.BANNER_PATTERN).orElseThrow().get(BannerPatterns.CREEPER).orElseThrow(), DyeColor.GREEN)
                .build()), List.of(
                "Set Banner Pattern:",
                "  -> Append: true",
                "  -> Banner Patterns:",
                "    -> Banner Pattern: Fully White Field",
                "    -> Banner Pattern: Green Creeper Charge"
        ));
    }

    @Test
    public void testSetContentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetContentsTooltip(UTILS, 0, (SetContainerContents) SetContainerContents.setContents(ContainerComponentManipulators.CONTAINER).build()), List.of(
                "Set Contents:",
                "  -> Type: minecraft:container"
        ));
    }

    @Test
    public void testSetCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetCountTooltip(UTILS, 0, (SetItemCountFunction) SetItemCountFunction.setCount(UniformGenerator.between(12, 24), true).build()), List.of(
                "Set Count:",
                "  -> Count: 12-24",
                "  -> Add: true"
        ));
    }

    @Test
    public void testSetDamageTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetDamageTooltip(UTILS, 0, (SetItemDamageFunction) SetItemDamageFunction.setDamage(UniformGenerator.between(0.12345F, 3.1412F), false).build()), List.of(
                "Set Damage:",
                "  -> Damage: 0.12-3.14",
                "  -> Add: false"
        ));
    }

    @Test
    public void testSetEnchantmentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(UTILS, 0, (SetEnchantmentsFunction) new SetEnchantmentsFunction.Builder(true).build()), List.of(
                "Set Enchantments:",
                "  -> Add: true"
        ));
        assertTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(UTILS, 0, (SetEnchantmentsFunction) new SetEnchantmentsFunction.Builder(false)
                .withEnchantment(LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.CHANNELING).orElseThrow(), ConstantValue.exactly(1))
                .build()), List.of(
                "Set Enchantments:",
                "  -> Enchantments:",
                "    -> Enchantment: Channeling",
                "      -> Levels: 1",
                "  -> Add: false"
        ));
    }

    @Test
    public void testSetInstrumentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetInstrumentTooltip(UTILS, 0, (SetInstrumentFunction) SetInstrumentFunction.setInstrumentOptions(InstrumentTags.SCREAMING_GOAT_HORNS).build()), List.of(
                "Set Instrument:",
                "  -> Options: minecraft:screaming_goat_horns"
        ));
    }

    @Test
    public void testSetLootTableTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetLootTableTooltip(UTILS, 0, (SetContainerLootTable) SetContainerLootTable.withLootTable(
                BlockEntityType.BELL,
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("gameplay/mesh")),
                42L
        ).build()), List.of(
                "Set Loot Table:",
                "  -> Name: minecraft:gameplay/mesh",
                "  -> Seed: 42",
                "  -> Block Entity Type: minecraft:bell"
        ));
    }

    @Test
    public void testSetLoreTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(UTILS, 0, (SetLoreFunction) SetLoreFunction.setLore()
                .addLine(Component.literal("Hello"))
                .addLine(Component.literal("World"))
                .setMode(new ListOperation.ReplaceSection(1, Optional.of(2)))
                .build()
        ), List.of(
                "Set Lore:",
                "  -> List Operation: REPLACE_SECTION",
                "    -> Offset: 1",
                "    -> Size: 2",
                "  -> Lore:",
                "    -> Hello",
                "    -> World"
        ));
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(UTILS, 0, (SetLoreFunction) SetLoreFunction.setLore()
                .setMode(new ListOperation.Insert(1))
                .addLine(Component.translatable("emi.category.ali.block_loot"))
                .setResolutionContext(LootContext.EntityTarget.ATTACKER)
                .build()
        ), List.of(
                "Set Lore:",
                "  -> List Operation: INSERT",
                "    -> Offset: 1",
                "  -> Lore:",
                "    -> Block Drops",
                "  -> Resolution Context: ATTACKER"
        ));
    }

    @Test
    public void testSetNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(UTILS, 0, (SetNameFunction) SetNameFunction.setName(Component.literal("Epic Item"), SetNameFunction.Target.ITEM_NAME).build()), List.of(
                "Set Name:",
                "  -> Name: Epic Item",
                "  -> Target: ITEM_NAME"
        ));
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(UTILS, 0, (SetNameFunction) SetNameFunction.setName(
                Component.translatable("emi.category.ali.block_loot"),
                SetNameFunction.Target.CUSTOM_NAME,
                LootContext.EntityTarget.ATTACKER).build()
        ), List.of(
                "Set Name:",
                "  -> Name: Block Drops",
                "  -> Resolution Context: ATTACKER",
                "  -> Target: CUSTOM_NAME"
        ));
    }

    @Test
    public void testSetNbtTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putBoolean("antlers", true);

        //noinspection deprecation
        assertTooltip(FunctionTooltipUtils.getSetCustomDataTooltip(UTILS, 0, (SetCustomDataFunction) SetCustomDataFunction.setCustomData(compoundTag).build()), List.of(
                "Set Custom Data:",
                "  -> Tag: {antlers:1b}"
        ));
    }

    @Test
    public void testSetPotionTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetPotionTooltip(UTILS, 0, (SetPotionFunction) SetPotionFunction.setPotion(Potions.TURTLE_MASTER).build()), List.of(
                "Set Potion:",
                "  -> Potion: minecraft:turtle_master"
        ));
    }

    @Test
    public void testSetStewEffectTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetStewEffectTooltip(UTILS, 0, (SetStewEffectFunction) SetStewEffectFunction.stewEffect().build()), List.of(
                "Set Stew Effect:"
        ));
        assertTooltip(FunctionTooltipUtils.getSetStewEffectTooltip(UTILS, 0, (SetStewEffectFunction) SetStewEffectFunction.stewEffect()
                .withEffect(MobEffects.LUCK, UniformGenerator.between(1, 5))
                .withEffect(MobEffects.UNLUCK, UniformGenerator.between(3, 4))
                .build()
        ), List.of(
                "Set Stew Effect:",
                "  -> Mob Effects:",
                "    -> Mob Effect: minecraft:luck",
                "      -> Duration: 1-5",
                "    -> Mob Effect: minecraft:unluck",
                "      -> Duration: 3-4"
        ));
    }

    @Test
    public void testSetItemTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetItemTooltip(UTILS, 0, new SetItemFunction(List.of(), Holder.direct(Items.MUSIC_DISC_MALL))), List.of(
                "Set Item:",
                "  -> Item: Music Disc"
        ));
    }

    @Test
    public void testSetComponentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetComponentsTooltip(UTILS, 0, (SetComponentsFunction) SetComponentsFunction
                .setComponent(DataComponents.DAMAGE, 5)
                .build()
        ), List.of(
                "Set Components:",
                "  -> Components:",
                "    -> Type: minecraft:damage"
        ));
    }

    @Test
    public void testModifyContentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getModifyContentsTooltip(UTILS, 0, new ModifyContainerContents(
                List.of(),
                ContainerComponentManipulators.CONTAINER,
                ApplyExplosionDecay.explosionDecay().build()
        )), List.of(
                "Modify Contents:",
                "  -> Type: minecraft:container",
                "  -> Modifier:",
                "    -> Explosion Decay"
        ));
    }

    @Test
    public void testFilteredTooltip() {
        assertTooltip(FunctionTooltipUtils.getFilteredTooltip(UTILS, 0, new FilteredFunction(
                List.of(),
                ItemPredicate.Builder.item().of(ItemTags.COALS).build(),
                ApplyExplosionDecay.explosionDecay().build()
        )), List.of(
                "Filtered:",
                "  -> Filter:",
                "    -> Items:",
                "      -> Tag: minecraft:coals",
                "  -> Modifier:",
                "    -> Explosion Decay"
        ));
    }

    @Test
    public void testCopyComponentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyComponentsTooltip(UTILS, 0, (CopyComponentsFunction) CopyComponentsFunction
                .copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                .include(DataComponents.DAMAGE)
                .include(DataComponents.FOOD)
                .exclude(DataComponents.BEES)
                .exclude(DataComponents.DYED_COLOR)
                .build()
        ), List.of(
                "Copy Components:",
                "  -> Source: BLOCK_ENTITY",
                "  -> Include:",
                "    -> Type: minecraft:damage",
                "    -> Type: minecraft:food",
                "  -> Exclude:",
                "    -> Type: minecraft:bees",
                "    -> Type: minecraft:dyed_color"
        ));
    }

    @Test
    public void testSetFireworks() {
        assertTooltip(FunctionTooltipUtils.getSetFireworksTooltip(UTILS, 0, new SetFireworksFunction(
                List.of(),
                Optional.of(new ListOperation.StandAlone<>(
                        List.of(
                                FireworkExplosion.DEFAULT,
                                new FireworkExplosion(FireworkExplosion.Shape.STAR, IntList.of(1), IntList.of(2), true, false)
                        ),
                        new ListOperation.Insert(0)
                )),
                Optional.of(10)
        )), List.of(
                "Set Fireworks:",
                "  -> Explosions:",
                "    -> Explosion:",
                "      -> Shape: SMALL_BALL",
                "      -> Colors: []",
                "      -> Fade Colors: []",
                "      -> Has Trail: false",
                "      -> Has Twinkle: false",
                "    -> Explosion:",
                "      -> Shape: STAR",
                "      -> Colors: [1]",
                "      -> Fade Colors: [2]",
                "      -> Has Trail: true",
                "      -> Has Twinkle: false",
                "  -> Flight Duration: 10"
        ));
    }

    @Test
    public void testSetFireworkExplosionTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetFireworkExplosionTooltip(UTILS, 0, new SetFireworkExplosionFunction(
                List.of(),
                Optional.of(FireworkExplosion.Shape.CREEPER),
                Optional.of(IntList.of(1, 2)),
                Optional.of(IntList.of(3, 4)),
                Optional.of(false),
                Optional.of(true)
        )), List.of(
                "Set Firework Explosion:",
                "  -> Shape: CREEPER",
                "  -> Colors: [1, 2]",
                "  -> Fade Colors: [3, 4]",
                "  -> Trail: false",
                "  -> Twinkle: true"
        ));
    }

    @Test
    public void testSetBookCoverTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetBookCoverTooltip(UTILS, 0, new SetBookCoverFunction(
                List.of(),
                Optional.of(new Filterable<>("Hello", Optional.of("World"))),
                Optional.of("Yanny"),
                Optional.of(3)
        )), List.of(
                "Set Book Cover:",
                "  -> Author: Yanny",
                "  -> Title:",
                "    -> Raw: Hello",
                "    -> Filtered: World",
                "  -> Generation: 3"
        ));
    }

    @Test
    public void testSetWrittenBookPagesTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetWrittenBookPagesTooltip(UTILS, 0, new SetWrittenBookPagesFunction(
                List.of(),
                List.of(new Filterable<>(Component.literal("Hello"), Optional.of(Component.literal("World")))),
                new ListOperation.Insert(0)
        )), List.of(
                "Set Written Book Pages:",
                "  -> Pages:",
                "    -> Page:",
                "      -> Raw: Hello",
                "      -> Filtered: World",
                "  -> List Operation: INSERT",
                "    -> Offset: 0"
        ));
    }

    @Test
    public void testSetWritableBookPagesTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetWritableBookPagesTooltip(UTILS, 0, new SetWritableBookPagesFunction(
                List.of(),
                List.of(new Filterable<>("Hello", Optional.of("World")), new Filterable<>("Keep", Optional.of("Calm"))),
                new ListOperation.ReplaceSection(1, Optional.of(4))
        )), List.of(
                "Set Writable Book Pages:",
                "  -> Pages:",
                "    -> Page:",
                "      -> Raw: Hello",
                "      -> Filtered: World",
                "    -> Page:",
                "      -> Raw: Keep",
                "      -> Filtered: Calm",
                "  -> List Operation: REPLACE_SECTION",
                "    -> Offset: 1",
                "    -> Size: 4"
        ));
    }

    @Test
    public void testToggleTooltipsTooltip() {
        assertTooltip(FunctionTooltipUtils.getToggleTooltipsTooltip(UTILS, 0, new ToggleTooltips(
                List.of(),
                Map.of(new ToggleTooltips.ComponentToggle<>(DataComponents.BASE_COLOR, (dyeColor, b) -> DyeColor.BLACK), true)
        )), List.of(
                "Toggle Tooltips:",
                "  -> Values:",
                "    -> Type: minecraft:base_color",
                "      -> Value: true"
        ));
    }

    @Test
    public void testSetOminousBottleAmplifierTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetOminousBottleAmplifierTooltip(UTILS, 0, new SetOminousBottleAmplifierFunction(
                List.of(),
                UniformGenerator.between(0.5F, 4.99F)
        )), List.of(
                "Set Ominous Bottle Amplifier:",
                "  -> Amplifier: 0.50-4.99"
        ));
    }

    @Test
    public void testSetCustomModelDataTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetCustomModelDataTooltip(UTILS, 0, new SetCustomModelDataFunction(
                List.of(),
                ConstantValue.exactly(3.14F)
        )), List.of(
                "Set Custom Model Data:",
                "  -> Value: 3.14"
        ));
    }
}
