package com.yanny.ali.test;

import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.alchemy.Potions;
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
import java.util.Optional;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class FunctionTooltipTest {
    @Test
    public void testApplyBonusCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getApplyBonusTooltip(UTILS, 0, ApplyBonusCount.addOreBonusCount(Enchantments.LOOTING).build()), List.of(
                "Apply Bonus:",
                "  -> Enchantment: Looting",
                "  -> Formula: minecraft:ore_drops"
        ));
    }

    @Test
    public void testCopyNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyNameTooltip(UTILS, 0, CopyNameFunction.copyName(CopyNameFunction.NameSource.THIS).build()), List.of(
                "Copy Name:",
                "  -> Source: This Entity"
        ));
    }

    @Test
    public void testCopyNbtTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyCustomDataTooltip(UTILS, 0, CopyCustomDataFunction.copyData(LootContext.EntityTarget.KILLER).build()), List.of(
                "Copy Custom Data:",
                "  -> Source: minecraft:context"
        ));
        assertTooltip(FunctionTooltipUtils.getCopyCustomDataTooltip(UTILS, 0, CopyCustomDataFunction.copyData(LootContext.EntityTarget.KILLER)
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
        assertTooltip(FunctionTooltipUtils.getCopyStateTooltip(UTILS, 0, CopyBlockState.copyState(Blocks.FURNACE)
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
        assertTooltip(FunctionTooltipUtils.getEnchantRandomlyTooltip(UTILS, 0, EnchantRandomlyFunction.randomEnchantment()
                .withEnchantment(Enchantments.CHANNELING)
                .build()
        ), List.of(
                "Enchant Randomly:",
                "  -> Enchantments:",
                "    -> Enchantment: Channeling"
        ));
    }

    @Test
    public void testEnchantWithLevelsTooltip() {
        assertTooltip(FunctionTooltipUtils.getEnchantWithLevelsTooltip(UTILS, 0, EnchantWithLevelsFunction.enchantWithLevels(UniformGenerator.between(1, 3)).allowTreasure().build()), List.of(
                "Enchant With Levels:",
                "  -> Levels: 1-3",
                "  -> Treasure: true"
        ));
    }

    @Test
    public void testExplorationMapTooltip() {
        assertTooltip(FunctionTooltipUtils.getExplorationMapTooltip(UTILS, 0, ExplorationMapFunction.makeExplorationMap()
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
        assertTooltip(FunctionTooltipUtils.getExplosionDecayTooltip(UTILS, 0, ApplyExplosionDecay.explosionDecay().build()), List.of("Explosion Decay"));
    }

    @Test
    public void testFillPlayerHeadTooltip() {
        assertTooltip(FunctionTooltipUtils.getFillPlayerHeadTooltip(UTILS, 0, FillPlayerHead.fillPlayerHead(LootContext.EntityTarget.KILLER).build()), List.of(
                "Fill Player Head:",
                "  -> Target: Killer Entity"
        ));
    }

    @Test
    public void testFurnaceSmeltTooltip() {
        assertTooltip(FunctionTooltipUtils.getFurnaceSmeltTooltip(UTILS, 0, SmeltItemFunction.smelted().build()), List.of("Use Smelting Recipe On Item"));
    }

    @Test
    public void testLimitCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getLimitCountTooltip(UTILS, 0, LimitCount.limitCount(IntRange.range(0, 10)).build()), List.of(
                "Limit Count:",
                "  -> Min: 0",
                "  -> Max: 10"
        ));
    }

    @Test
    public void testLootingEnchantTooltip() {
        assertTooltip(FunctionTooltipUtils.getLootingEnchantTooltip(UTILS, 0, LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 4)).setLimit(3).build()), List.of(
                "Looting Enchant:",
                "  -> Value: 0-4",
                "  -> Limit: 3"
        ));
    }

    @Test
    public void testReferenceTooltip() {
        assertTooltip(FunctionTooltipUtils.getReferenceTooltip(UTILS, 0, FunctionReference.functionReference(ResourceKey.create(
                Registries.ITEM_MODIFIER,
                new ResourceLocation("gameplay/fishing"))
        ).build()), List.of(
                "Reference:",
                "  -> Name: minecraft:gameplay/fishing"
        ));
    }

    @Test
    public void testSetAttributesTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetAttributesTooltip(UTILS, 0, SetAttributesFunction.setAttributes()
                .withModifier(new SetAttributesFunction.ModifierBuilder("armor", Attributes.ARMOR, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, UniformGenerator.between(1, 5))
                        .forSlot(EquipmentSlotGroup.HEAD)
                        .forSlot(EquipmentSlotGroup.CHEST)
                        .forSlot(EquipmentSlotGroup.LEGS)
                        .forSlot(EquipmentSlotGroup.FEET))
                .build()), List.of(
                "Set Attributes:",
                "  -> Modifiers:",
                "    -> Modifier:",
                "      -> Name: armor",
                "      -> Attribute: Armor",
                "      -> Operation: ADD_MULTIPLIED_TOTAL",
                "      -> Amount: 1-5",
                "      -> Equipment Slots:",
                "        -> FEET",
                "        -> LEGS",
                "        -> CHEST",
                "        -> HEAD"
        ));
    }

    @Test
    public void testSetBannerPatternTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetBannerPatternTooltip(UTILS, 0, SetBannerPatternFunction.setBannerPattern(true)
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
        assertTooltip(FunctionTooltipUtils.getSetContentsTooltip(UTILS, 0, SetContainerContents.setContents(ContainerComponentManipulators.CONTAINER).build()), List.of(
                "Set Contents:",
                "  -> Component: minecraft:container"
        ));
    }

    @Test
    public void testSetCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetCountTooltip(UTILS, 0, SetItemCountFunction.setCount(UniformGenerator.between(12, 24), true).build()), List.of(
                "Set Count:",
                "  -> Count: 12-24",
                "  -> Add: true"
        ));
    }

    @Test
    public void testSetDamageTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetDamageTooltip(UTILS, 0, SetItemDamageFunction.setDamage(UniformGenerator.between(0.12345F, 3.1412F), false).build()), List.of(
                "Set Damage:",
                "  -> Damage: 0.12-3.14",
                "  -> Add: false"
        ));
    }

    @Test
    public void testSetEnchantmentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(UTILS, 0, new SetEnchantmentsFunction.Builder(true).build()), List.of(
                "Set Enchantments:",
                "  -> Add: true"
        ));
        assertTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(UTILS, 0, new SetEnchantmentsFunction.Builder(false)
                .withEnchantment(Enchantments.CHANNELING, ConstantValue.exactly(1))
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
        assertTooltip(FunctionTooltipUtils.getSetInstrumentTooltip(UTILS, 0, SetInstrumentFunction.setInstrumentOptions(InstrumentTags.SCREAMING_GOAT_HORNS).build()), List.of(
                "Set Instrument:",
                "  -> Options: minecraft:screaming_goat_horns"
        ));
    }

    @Test
    public void testSetLootTableTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetLootTableTooltip(UTILS, 0, SetContainerLootTable.withLootTable(
                BlockEntityType.BELL,
                ResourceKey.create(Registries.LOOT_TABLE, new ResourceLocation("gameplay/mesh")),
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
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(UTILS, 0, SetLoreFunction.setLore()
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
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(UTILS, 0, SetLoreFunction.setLore()
                .setMode(new ListOperation.Insert(1))
                .addLine(Component.translatable("emi.category.ali.block_loot"))
                .setResolutionContext(LootContext.EntityTarget.KILLER)
                .build()
        ), List.of(
                "Set Lore:",
                "  -> List Operation: INSERT",
                "    -> Offset: 1",
                "  -> Lore:",
                "    -> Block Drops",
                "  -> Resolution Context: Killer Entity"
        ));
    }

    @Test
    public void testSetNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(UTILS, 0, SetNameFunction.setName(Component.literal("Epic Item"), SetNameFunction.Target.ITEM_NAME).build()), List.of(
                "Set Name:",
                "  -> Name: Epic Item",
                "  -> Target: ITEM_NAME"
        ));
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(UTILS, 0, SetNameFunction.setName(
                Component.translatable("emi.category.ali.block_loot"),
                SetNameFunction.Target.CUSTOM_NAME,
                LootContext.EntityTarget.KILLER).build()
        ), List.of(
                "Set Name:",
                "  -> Name: Block Drops",
                "  -> Resolution Context: Killer Entity",
                "  -> Target: CUSTOM_NAME"
        ));
    }

    @Test
    public void testSetNbtTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putBoolean("antlers", true);

        assertTooltip(FunctionTooltipUtils.getSetCustomDataTooltip(UTILS, 0, SetCustomDataFunction.setCustomData(compoundTag).build()), List.of(
                "Set Custom Data:",
                "  -> Tag: {antlers:1b}"
        ));
    }

    @Test
    public void testSetPotionTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetPotionTooltip(UTILS, 0, SetPotionFunction.setPotion(Potions.TURTLE_MASTER).build()), List.of(
                "Set Potion:",
                "  -> Potion:",
                "    -> Mob Effects:",
                "      -> Mob Effect: minecraft:slowness",
                "        -> Amplifier: 3",
                "        -> Duration: 400",
                "        -> Is Ambient: false",
                "        -> Is Visible: true",
                "        -> Show Icon: true",
                "      -> Mob Effect: minecraft:resistance",
                "        -> Amplifier: 2",
                "        -> Duration: 400",
                "        -> Is Ambient: false",
                "        -> Is Visible: true",
                "        -> Show Icon: true"
        ));
    }

    @Test
    public void testSetStewEffectTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetStewEffectTooltip(UTILS, 0, SetStewEffectFunction.stewEffect().build()), List.of(
                "Set Stew Effect:"
        ));
        assertTooltip(FunctionTooltipUtils.getSetStewEffectTooltip(UTILS, 0, SetStewEffectFunction.stewEffect()
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
}
