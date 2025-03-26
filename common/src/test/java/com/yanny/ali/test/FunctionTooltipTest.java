package com.yanny.ali.test;

import com.yanny.ali.api.ICommonUtils;
import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import com.yanny.ali.plugin.function.ExplosionDecayAliFunction;
import com.yanny.ali.plugin.function.FurnaceSmeltAliFunction;
import com.yanny.ali.plugin.function.SetAttributesAliFunction;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.test.utils.TestUtils.assertTooltip;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FunctionTooltipTest {
    @Test
    public void testApplyBonusCountTooltip() {
        assertTooltip(FunctionTooltipUtils.getApplyBonusTooltip(0, Holder.direct(Enchantments.LOOTING), new ApplyBonusCount.OreDrops()), List.of(
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
        //FIXME pridaj testy
        assertTooltip(FunctionTooltipUtils.getCopyCustomData(0, ContextNbtProvider.forContextEntity(LootContext.EntityTarget.KILLER), List.of()), List.of("Copy Nbt"));
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
                MapDecorationTypes.OCEAN_MONUMENT, 2, 50, true), List.of(
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
        assertTooltip(FunctionTooltipUtils.getReferenceTooltip(0, ResourceKey.create(Registries.LOOT_TABLE, new ResourceLocation("gameplay/fishing"))), List.of(
                "Reference:",
                "  -> Name: minecraft:gameplay/fishing"
        ));
    }

    @Test
    public void testSequenceTooltip() {
        IContext context = mock(IContext.class);
        ICommonUtils utils = mock(ICommonUtils.class);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        when(context.utils()).thenReturn(utils);
        when(utils.decodeFunctions(any(), any())).thenReturn(List.of());
        List<ILootFunction> functions = List.of(new FurnaceSmeltAliFunction(context, buf), new ExplosionDecayAliFunction(context, buf));

        assertTooltip(FunctionTooltipUtils.getSequenceTooltip(0, functions), List.of(
                "Sequence:",
                "  -> Use Smelting Recipe On Item",
                "  -> Explosion Decay"
        ));
    }

    @Test
    public void testSetAttributesTooltip() {
        List<EquipmentSlotGroup> equipmentSlots = new LinkedList<>();

        equipmentSlots.add(EquipmentSlotGroup.HEAD);
        equipmentSlots.add(EquipmentSlotGroup.CHEST);
        equipmentSlots.add(EquipmentSlotGroup.LEGS);
        equipmentSlots.add(EquipmentSlotGroup.FEET);

        assertTooltip(FunctionTooltipUtils.getSetAttributesTooltip(0, List.of(
                new SetAttributesAliFunction.Modifier(
                        "armor",
                        Attributes.ARMOR,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
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
        /*BannerPatternLayers bannerPatternLayers = new BannerPatternLayers.Builder() FIXME
                .add(BannerPatterns.BASE, DyeColor.WHITE)
                .add(BannerPatterns.CREEPER, DyeColor.WHITE)
                .build();
        assertTooltip(FunctionTooltipUtils.getSetBannerPatternTooltip(0, true, bannerPatternLayers), List.of(
                "Set Banner Pattern:",
                "  -> Append: true",
                "  -> Banner Patterns:",
                "    -> Banner Pattern: minecraft:base",
                "      -> Color: WHITE",
                "    -> Banner Pattern: minecraft:creeper",
                "      -> Color: GREEN"
        ));*/
    }

    @Test
    public void testSetContentsTooltip() {
        /* FIXME
        assertTooltip(FunctionTooltipUtils.getSetContentsTooltip(0, BlockEntityType.BREWING_STAND), List.of(
                "Set Contents:",
                "  -> Block Entity Type: minecraft:brewing_stand"
        ));*/
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
        enchantmentRangeValueMap.put(Holder.direct(Enchantments.LOOTING), new RangeValue(1, 3));

        assertTooltip(FunctionTooltipUtils.getSetEnchantmentsTooltip(0, Map.of(), true), List.of(
                "Set Enchantments:",
                "  -> Add: true"
        ));
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
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(0, ListOperation.ReplaceAll.INSTANCE, List.of(Component.literal("Hello"), Component.literal("World")), Optional.empty()), List.of(
                "Set Lore:",
                "  -> Replace: REPLACE_ALL",
                "  -> Lore:",
                "    -> Hello",
                "    -> World"
        ));
        assertTooltip(FunctionTooltipUtils.getSetLoreTooltip(0, new ListOperation.Insert(1), List.of(Component.translatable("emi.category.ali.block_loot")), Optional.of(LootContext.EntityTarget.KILLER)), List.of(
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
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putBoolean("antlers", true);

        assertTooltip(FunctionTooltipUtils.getSetCustomDataTooltip(0, compoundTag), List.of(
                "Set Nbt:",
                "  -> Tag: {antlers:true}"
        ));
    }

    @Test
    public void testSetPotionTooltip() {
        assertTooltip(FunctionTooltipUtils.getSetPotionTooltip(0, Potions.TURTLE_MASTER), List.of(
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

        effectRangeValueMap.put(MobEffects.LUCK, new RangeValue(1, 5));
        effectRangeValueMap.put(MobEffects.UNLUCK, new RangeValue(3, 4));

        assertTooltip(FunctionTooltipUtils.getSetStewEffectTooltip(0, Map.of()), List.of(
                "Set Stew Effect:"
        ));
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
