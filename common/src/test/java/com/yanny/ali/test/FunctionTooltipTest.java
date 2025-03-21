package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import com.yanny.ali.plugin.function.SetAttributesAliFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class FunctionTooltipTest {
    @Test
    public void testApplyBonusCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getApplyBonusTooltip(0, Holder.direct(Enchantments.MOB_LOOTING), new ApplyBonusCount.OreDrops()), List.of(
                "Apply Bonus:",
                "  -> Enchantment: Looting",
                "  -> Formula: minecraft:ore_drops"
        ));
    }

    @Test
    public void testCopyNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyNameTooltip(0, CopyNameFunction.NameSource.THIS), List.of(
                "Copy Name:",
                "  -> Source: This Entity"
        ));
    }

    @Test
    public void testCopyNbtTooltip() {
        assertTooltip(FunctionTooltipUtils.getCopyNbtTooltip(0), List.of("Copy Nbt"));
    }

    @Test
    public void testCopyStateTooltip() {
        Set<Property<?>> properties = new LinkedHashSet<>();

        properties.add(FurnaceBlock.LIT);
        properties.add(FurnaceBlock.FACING);

        assertTooltip(FunctionTooltipUtils.getCopyStateTooltip(0, Holder.direct(Blocks.FURNACE), properties), List.of(
                "Copy State:",
                "  -> Block: Furnace",
                "  -> Properties:",
                "    -> lit [true, false]",
                "    -> facing [north, south, west, east]"
        ));
    }

    @Test
    public void testEnchantRandomlyTooltip() {
        Optional<HolderSet< Enchantment>> enchantments = Optional.of(HolderSet.direct(Holder.direct(Enchantments.CHANNELING), Holder.direct(Enchantments.IMPALING)));

        assertTooltip(FunctionTooltipUtils.getEnchantRandomlyTooltip(0, enchantments), List.of(
                "Enchant Randomly:",
                "  -> Enchantments:",
                "    -> Enchantment: Channeling",
                "    -> Enchantment: Impaling"
        ));
    }

    @Test
    public void testEnchantWithLevelsTooltip() {
        assertTooltip(FunctionTooltipUtils.getEnchantWithLevelsTooltip(0, new RangeValue(1, 3), true), List.of(
                "Enchant With Levels:",
                "  -> Levels: 1-3",
                "  -> Treasure: true"
        ));
    }
    
    @Test
    public void testExplorationMapTooltip() {
        assertTooltip(FunctionTooltipUtils.getExplorationMapTooltip(0, StructureTags.RUINED_PORTAL,
                MapDecoration.Type.MONUMENT, 2, 50, true), List.of(
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
        assertTooltip(FunctionTooltipUtils.getExplosionDecayTooltip(0), List.of("Explosion Decay"));
    }

    @Test
    public void testFillPlayerHeadTooltip() {
        assertTooltip(FunctionTooltipUtils.getFillPlayerHeadTooltip(0, LootContext.EntityTarget.KILLER), List.of(
                "Fill Player Head:",
                "  -> Target: Killer Entity"
        ));
    }

    @Test
    public void testFurnaceSmeltTooltip() {
        assertTooltip(FunctionTooltipUtils.getFurnaceSmeltTooltip(0), List.of("Use Smelting Recipe On Item"));
    }

    @Test
    public void testLimitCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getLimitCountTooltip(0, new RangeValue(0), new RangeValue(5, 10)), List.of(
                "Limit Count:",
                "  -> Min: 0",
                "  -> Max: 5-10"
        ));
    }

    @Test
    public void testLootingEnchantTooltip() {
        assertTooltip(FunctionTooltipUtils.getLootingEnchantTooltip(0, new RangeValue(0, 4), 3), List.of(
                "Looting Enchant:",
                "  -> Value: 0-4",
                "  -> Limit: 3"
        ));
    }

    @Test
    public void testReferenceTooltip() {
        assertTooltip(FunctionTooltipUtils.getReferenceTooltip(0, new ResourceLocation("gameplay/fishing")), List.of(
                "Reference:",
                "  -> Name: minecraft:gameplay/fishing"
        ));
    }

    @Test
    public void testSetAttributesTooltip() {
        List<EquipmentSlot> equipmentSlots = new LinkedList<>();

        equipmentSlots.add(EquipmentSlot.HEAD);
        equipmentSlots.add(EquipmentSlot.CHEST);
        equipmentSlots.add(EquipmentSlot.LEGS);
        equipmentSlots.add(EquipmentSlot.FEET);

        assertTooltip(FunctionTooltipUtils.getSetAttributesTooltip(0, List.of(
                new SetAttributesAliFunction.Modifier(
                        "armor",
                        Holder.direct(Attributes.ARMOR),
                        AttributeModifier.Operation.MULTIPLY_TOTAL,
                        new RangeValue(1, 5),
                        Optional.empty(),
                        equipmentSlots
                )
        )), List.of(
                "Set Attributes:",
                "  -> Modifiers:",
                "    -> Modifier:",
                "      -> Name: armor",
                "      -> Attribute: Armor",
                "      -> Operation: MULTIPLY_TOTAL",
                "      -> Amount: 1-5",
                "      -> Equipment Slots:",
                "        -> HEAD",
                "        -> CHEST",
                "        -> LEGS",
                "        -> FEET"
        ));
    }

    @Test
    public void testSetBannerPatternTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetBannerPatternTooltip(0, true, List.of(
                Pair.of(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.BASE))), DyeColor.WHITE),
                Pair.of(Holder.direct(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.get(BannerPatterns.CREEPER))), DyeColor.GREEN)
        )), List.of(
                "Set Banner Pattern:",
                "  -> Append: true",
                "  -> Banner Patterns:",
                "    -> Banner Pattern: minecraft:base",
                "      -> Color: WHITE",
                "    -> Banner Pattern: minecraft:creeper",
                "      -> Color: GREEN"
        ));
    }

    @Test
    public void testSetContentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetContentsTooltip(0, Holder.direct(BlockEntityType.BREWING_STAND)), List.of(
                "Set Contents:",
                "  -> Block Entity Type: minecraft:brewing_stand"
        ));
    }

    @Test
    public void testSetCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetCountTooltip(0, new RangeValue(12, 24), true), List.of(
                "Set Count:",
                "  -> Count: 12-24",
                "  -> Add: true"
        ));
    }

    @Test
    public void testSetDamageTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetDamageTooltip(0, new RangeValue(0.12345F, 3.1412F), false), List.of(
                "Set Damage:",
                "  -> Damage: 0.12-3.14",
                "  -> Add: false"
        ));
    }

    @Test
    public void testSetEnchantmentsTooltip() {
        Map<Holder<Enchantment>, RangeValue> enchantmentRangeValueMap = new LinkedHashMap<>();

        enchantmentRangeValueMap.put(Holder.direct(Enchantments.CHANNELING), new RangeValue(1));
        enchantmentRangeValueMap.put(Holder.direct(Enchantments.BINDING_CURSE), new RangeValue(1));
        enchantmentRangeValueMap.put(Holder.direct(Enchantments.MOB_LOOTING), new RangeValue(1, 3));

        assertTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(0, enchantmentRangeValueMap, false), List.of(
                "Set Enchantments:",
                "  -> Enchantments:",
                "    -> Enchantment: Channeling",
                "      -> Levels: 1",
                "    -> Enchantment: Curse of Binding",
                "      -> Levels: 1",
                "    -> Enchantment: Looting",
                "      -> Levels: 1-3",
                "  -> Add: false"
        ));
    }

    @Test
    public void testSetInstrumentsTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetInstrumentTooltip(0, InstrumentTags.SCREAMING_GOAT_HORNS), List.of(
                "Set Instrument:",
                "  -> Options: minecraft:screaming_goat_horns"
        ));
    }

    @Test
    public void testSetLootTableTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetLootTableTooltip(0, new ResourceLocation("gameplay/mesh"), 42L, Holder.direct(BlockEntityType.BELL)), List.of(
                "Set Loot Table:",
                "  -> Name: minecraft:gameplay/mesh",
                "  -> Seed: 42",
                "  -> Block Entity Type: minecraft:bell"
        ));
    }

    @Test
    public void testSetLoreTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(0, true, List.of(Component.literal("Hello"), Component.literal("World")), Optional.empty()), List.of(
                "Set Lore:",
                "  -> Replace: true",
                "  -> Lore:",
                "    -> Hello",
                "    -> World"
        ));
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(0, true, List.of(Component.translatable("emi.category.ali.block_loot")), Optional.of(LootContext.EntityTarget.KILLER)), List.of(
                "Set Lore:",
                "  -> Replace: true",
                "  -> Lore:",
                "    -> Block Drops",
                "  -> Resolution Context: Killer Entity"
        ));
    }

    @Test
    public void testSetNameTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(0, Optional.of(Component.literal("Epic Item")), Optional.empty()), List.of(
                "Set Name:",
                "  -> Name: Epic Item"
        ));
        assertTooltip(FunctionTooltipUtils.getSetNameTooltip(0, Optional.of(Component.translatable("emi.category.ali.block_loot")), Optional.of(LootContext.EntityTarget.KILLER)), List.of(
                "Set Name:",
                "  -> Name: Block Drops",
                "  -> Resolution Context: Killer Entity"
        ));
    }

    @Test
    public void testSetNbtTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetNbtTooltip(0, "{antlers:true}"), List.of(
                "Set Nbt:",
                "  -> Tag: {antlers:true}"
        ));
    }

    @Test
    public void testSetPotionTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetPotionTooltip(0, Holder.direct(Potions.TURTLE_MASTER)), List.of(
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
        Map<Holder<MobEffect>, RangeValue> effectRangeValueMap = new LinkedHashMap<>();

        effectRangeValueMap.put(Holder.direct(MobEffects.LUCK), new RangeValue(1, 5));
        effectRangeValueMap.put(Holder.direct(MobEffects.UNLUCK), new RangeValue(3, 4));

        assertTooltip(FunctionTooltipUtils.getSetStewEffectTooltip(0, effectRangeValueMap), List.of(
                "Set Stew Effect:",
                "  -> Mob Effects:",
                "    -> Mob Effect: minecraft:luck",
                "      -> Duration: 1-5",
                "    -> Mob Effect: minecraft:unluck",
                "      -> Duration: 3-4"
        ));
    }
}
