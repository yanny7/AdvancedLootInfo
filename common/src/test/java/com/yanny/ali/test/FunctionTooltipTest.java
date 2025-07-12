package com.yanny.ali.test;

import com.yanny.ali.plugin.server.FunctionTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.utils.TestUtils.assertUnorderedTooltip;

public class FunctionTooltipTest {
    @Test
    public void testApplyBonusCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getApplyBonusTooltip(UTILS, (ApplyBonusCount) ApplyBonusCount.addOreBonusCount(Enchantments.MOB_LOOTING).build()), List.of(
                "Apply Bonus:",
                "  -> Enchantment: minecraft:looting",
                "  -> Formula: minecraft:ore_drops"
        ));
    }

    @Test
    public void testCopyNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyNameTooltip(UTILS, (CopyNameFunction) CopyNameFunction.copyName(CopyNameFunction.NameSource.THIS).build()), List.of(
                "Copy Name:",
                "  -> Source: THIS"
        ));
    }

    @Test
    public void testCopyNbtTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyNbtTooltip(UTILS, (CopyNbtFunction) CopyNbtFunction.copyData(LootContext.EntityTarget.KILLER).build()), List.of(
                "Copy Nbt:",
                "  -> Nbt Provider: minecraft:context"
        ));
        assertTooltip(FunctionTooltipUtils.getCopyNbtTooltip(UTILS, (CopyNbtFunction) CopyNbtFunction.copyData(LootContext.EntityTarget.KILLER)
                .copy("asdf", "jklo", CopyNbtFunction.MergeStrategy.MERGE)
                .copy("qwer", "uiop", CopyNbtFunction.MergeStrategy.APPEND)
                .build()
        ), List.of(
                "Copy Nbt:",
                "  -> Nbt Provider: minecraft:context",
                "  -> Operations:",
                "    -> Operation:",
                "      -> Source: asdf",
                "      -> Target: jklo",
                "      -> Merge Strategy: MERGE",
                "    -> Operation:",
                "      -> Source: qwer",
                "      -> Target: uiop",
                "      -> Merge Strategy: APPEND"
        ));
    }

    @Test
    public void testCopyStateTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyStateTooltip(UTILS, (CopyBlockState) CopyBlockState.copyState(Blocks.FURNACE)
                .copy(BlockStateProperties.LIT)
                .copy(BlockStateProperties.HORIZONTAL_FACING)
                .build()
        ), List.of(
                "Copy State:",
                "  -> Block: minecraft:furnace",
                "  -> Properties:",
                "    -> lit",
                "    -> facing"
        ));
    }

    @Test
    public void testEnchantRandomlyTooltip() {
        assertTooltip(FunctionTooltipUtils.getEnchantRandomlyTooltip(UTILS, (EnchantRandomlyFunction) EnchantRandomlyFunction.randomApplicableEnchantment().build()), List.of("Enchant Randomly:"));
        assertTooltip(FunctionTooltipUtils.getEnchantRandomlyTooltip(UTILS, (EnchantRandomlyFunction) EnchantRandomlyFunction.randomEnchantment()
                .withEnchantment(Enchantments.CHANNELING)
                .build()
        ), List.of(
                "Enchant Randomly:",
                "  -> minecraft:channeling"
        ));
    }

    @Test
    public void testEnchantWithLevelsTooltip() {
        assertTooltip(FunctionTooltipUtils.getEnchantWithLevelsTooltip(UTILS, (EnchantWithLevelsFunction) EnchantWithLevelsFunction.enchantWithLevels(UniformGenerator.between(1, 3)).allowTreasure().build()), List.of(
                "Enchant With Levels:",
                "  -> Levels: 1-3",
                "  -> Treasure: true"
        ));
    }

    @Test
    public void testExplorationMapTooltip() {
        assertTooltip(FunctionTooltipUtils.getExplorationMapTooltip(UTILS, (ExplorationMapFunction) ExplorationMapFunction.makeExplorationMap()
                .setDestination(StructureTags.RUINED_PORTAL)
                .setMapDecoration(MapDecoration.Type.MONUMENT)
                .setZoom((byte) 2)
                .setSearchRadius(50)
                .setSkipKnownStructures(true)
                .build()
        ), List.of(
                "Exploration Map:",
                "  -> Destination: minecraft:ruined_portal",
                "  -> Map Decoration: MONUMENT",
                "  -> Zoom: 2",
                "  -> Search Radius: 50",
                "  -> Skip Known Structures: true"
        ));
    }

    @Test
    public void testExplosionDecayTooltip() {
        assertTooltip(FunctionTooltipUtils.getExplosionDecayTooltip(UTILS, (ApplyExplosionDecay) ApplyExplosionDecay.explosionDecay().build()), List.of("Explosion Decay"));
    }

    @Test
    public void testFillPlayerHeadTooltip() {
        assertTooltip(FunctionTooltipUtils.getFillPlayerHeadTooltip(UTILS, (FillPlayerHead) FillPlayerHead.fillPlayerHead(LootContext.EntityTarget.KILLER).build()), List.of(
                "Fill Player Head:",
                "  -> Target: KILLER"
        ));
    }

    @Test
    public void testFurnaceSmeltTooltip() {
        assertTooltip(FunctionTooltipUtils.getFurnaceSmeltTooltip(UTILS, (SmeltItemFunction) SmeltItemFunction.smelted().build()), List.of("Use Smelting Recipe On Item"));
    }

    @Test
    public void testLimitCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getLimitCountTooltip(UTILS, (LimitCount) LimitCount.limitCount(IntRange.range(0, 10)).build()), List.of(
                "Limit Count:",
                "  -> Limit: 0 - 10"
        ));
    }

    @Test
    public void testLootingEnchantTooltip() {
        assertTooltip(FunctionTooltipUtils.getLootingEnchantTooltip(UTILS, (LootingEnchantFunction) LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 4)).setLimit(3).build()), List.of(
                "Looting Enchant:",
                "  -> Value: 0-4",
                "  -> Limit: 3"
        ));
    }

    @Test
    public void testReferenceTooltip() {
        assertTooltip(FunctionTooltipUtils.getReferenceTooltip(UTILS, (FunctionReference) FunctionReference.functionReference(new ResourceLocation("gameplay/fishing")).build()), List.of(
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
        assertTooltip(FunctionTooltipUtils.getSetAttributesTooltip(UTILS, (SetAttributesFunction) SetAttributesFunction.setAttributes()
                .withModifier(new SetAttributesFunction.ModifierBuilder("armor", Holder.direct(Attributes.ARMOR), AttributeModifier.Operation.MULTIPLY_TOTAL, UniformGenerator.between(1, 5))
                        .forSlot(EquipmentSlot.HEAD)
                        .forSlot(EquipmentSlot.CHEST)
                        .forSlot(EquipmentSlot.LEGS)
                        .forSlot(EquipmentSlot.FEET))
                .withModifier(new SetAttributesFunction.ModifierBuilder("chest", Holder.direct(Attributes.ARMOR_TOUGHNESS), AttributeModifier.Operation.MULTIPLY_BASE, ConstantValue.exactly(3))
                        .forSlot(EquipmentSlot.MAINHAND))
                .build()), List.of(
                "Set Attributes:",
                "  -> Modifier:",
                "    -> Name: armor",
                "    -> Attribute: minecraft:generic.armor",
                "    -> Operation: MULTIPLY_TOTAL",
                "    -> Amount: 1-5",
                "    -> Equipment Slots:",
                "      -> FEET",
                "      -> LEGS",
                "      -> CHEST",
                "      -> HEAD",
                "  -> Modifier:",
                "    -> Name: chest",
                "    -> Attribute: minecraft:generic.armor_toughness",
                "    -> Operation: MULTIPLY_BASE",
                "    -> Amount: 3",
                "    -> Equipment Slots:",
                "      -> MAINHAND"
        ));
    }

    @Test
    public void testSetBannerPatternTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetBannerPatternTooltip(UTILS, (SetBannerPatternFunction) SetBannerPatternFunction.setBannerPattern(true)
                .addPattern(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.BASE))), DyeColor.WHITE)
                .addPattern(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.CREEPER))), DyeColor.GREEN)
                .build()), List.of(
                "Set Banner Pattern:",
                "  -> Append: true",
                "  -> Banner Patterns:",
                "    -> minecraft:base",
                "      -> Color: WHITE",
                "    -> minecraft:creeper",
                "      -> Color: GREEN"
        ));
    }

    @Test
    public void testSetContentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetContentsTooltip(UTILS, (SetContainerContents) SetContainerContents.setContents(BlockEntityType.BREWING_STAND)
                .withEntry(LootItem.lootTableItem(Items.BOOK))
                .withEntry(LootItem.lootTableItem(Items.ENCHANTED_BOOK))
                .build()), List.of(
                "Set Contents:",
                "  -> Block Entity Type: minecraft:brewing_stand"
        ));
    }

    @Test
    public void testSetCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetCountTooltip(UTILS, (SetItemCountFunction) SetItemCountFunction.setCount(UniformGenerator.between(12, 24), true).build()), List.of(
                "Set Count:",
                "  -> Count: 12-24",
                "  -> Add: true"
        ));
    }

    @Test
    public void testSetDamageTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetDamageTooltip(UTILS, (SetItemDamageFunction) SetItemDamageFunction.setDamage(UniformGenerator.between(0.12345F, 3.1412F), false).build()), List.of(
                "Set Damage:",
                "  -> Damage: 0.12-3.14",
                "  -> Add: false"
        ));
    }

    @Test
    public void testSetEnchantmentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(UTILS, (SetEnchantmentsFunction) new SetEnchantmentsFunction.Builder(true).build()), List.of(
                "Set Enchantments:",
                "  -> Add: true"
        ));
        assertUnorderedTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(UTILS, (SetEnchantmentsFunction) new SetEnchantmentsFunction.Builder(false)
                .withEnchantment(Enchantments.CHANNELING, ConstantValue.exactly(1))
                .withEnchantment(Enchantments.MENDING, ConstantValue.exactly(2))
                .build()), List.of(
                "Set Enchantments:",
                "  -> Enchantments:",
                List.of(
                        "    -> minecraft:channeling",
                        "      -> Levels: 1",
                        "    -> minecraft:mending",
                        "      -> Levels: 2"
                ),
                "  -> Add: false"
        ));
    }

    @Test
    public void testSetInstrumentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetInstrumentTooltip(UTILS, (SetInstrumentFunction) SetInstrumentFunction.setInstrumentOptions(InstrumentTags.SCREAMING_GOAT_HORNS).build()), List.of(
                "Set Instrument:",
                "  -> Options: minecraft:screaming_goat_horns"
        ));
    }

    @Test
    public void testSetLootTableTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetLootTableTooltip(UTILS, (SetContainerLootTable) SetContainerLootTable.withLootTable(
                BlockEntityType.BELL,
                new ResourceLocation("gameplay/mesh"),
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
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(UTILS, (SetLoreFunction) SetLoreFunction.setLore()
                .addLine(Component.literal("Hello"))
                .addLine(Component.literal("World"))
                .setReplace(true)
                .build()
        ), List.of(
                "Set Lore:",
                "  -> Replace: true",
                "  -> Lore:",
                "    -> Hello",
                "    -> World"
        ));
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(UTILS, (SetLoreFunction) SetLoreFunction.setLore()
                .setReplace(true)
                .addLine(Component.translatable("emi.category.ali.block_loot"))
                .setResolutionContext(LootContext.EntityTarget.KILLER)
                .build()
        ), List.of(
                "Set Lore:",
                "  -> Replace: true",
                "  -> Lore:",
                "    -> Block Drops",
                "  -> Resolution Context: KILLER"
        ));
    }

    @Test
    public void testSetNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(UTILS, (SetNameFunction) SetNameFunction.setName(Component.literal("Epic Item")).build()), List.of(
                "Set Name:",
                "  -> Name: Epic Item"
        ));
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(UTILS, (SetNameFunction) SetNameFunction.setName(
                Component.translatable("emi.category.ali.block_loot"),
                LootContext.EntityTarget.KILLER).build()
        ), List.of(
                "Set Name:",
                "  -> Name: Block Drops",
                "  -> Resolution Context: KILLER"
        ));
    }

    @Test
    public void testSetNbtTooltip() {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putBoolean("antlers", true);

        //noinspection deprecation
        assertTooltip(FunctionTooltipUtils.getSetNbtTooltip(UTILS, (SetNbtFunction) SetNbtFunction.setTag(compoundTag).build()), List.of(
                "Set Nbt:",
                "  -> Tag: {antlers:1b}"
        ));
    }

    @Test
    public void testSetPotionTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetPotionTooltip(UTILS, (SetPotionFunction) SetPotionFunction.setPotion(Potions.TURTLE_MASTER).build()), List.of(
                "Set Potion:",
                "  -> Potion: minecraft:turtle_master"
        ));
    }

    @Test
    public void testSetStewEffectTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetStewEffectTooltip(UTILS, (SetStewEffectFunction) SetStewEffectFunction.stewEffect()
                .withEffect(MobEffects.LUCK, UniformGenerator.between(1, 5))
                .withEffect(MobEffects.UNLUCK, UniformGenerator.between(3, 4))
                .build()
        ), List.of(
                "Set Stew Effect:",
                "  -> minecraft:luck",
                "    -> Duration: 1-5",
                "  -> minecraft:unluck",
                "    -> Duration: 3-4"
        ));
    }
}
