package com.yanny.ali.test;

import com.yanny.ali.plugin.server.LootConditionTypes;
import com.yanny.ali.plugin.server.LootFunctionTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.utils.TestUtils.assertUnorderedTooltip;

public class ServerUtilsTest {
    @Test
    public void testGetFunctionTooltip() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(5, 0.5f)).build()), List.of(
                "Set Count:",
                "  -> Count: 0-5",
                "  -> Add: false"
        ));
        assertTooltip(UTILS.getFunctionTooltip(UTILS, new UnknownFunction(Items.ANDESITE, BinomialDistributionGenerator.binomial(5, 0.3f))), List.of(
                "Auto-detected: minecraft:unknown",
                "  -> item: minecraft:andesite",
                "  -> value: 0-5"
        ));
    }

    @Test
    public void testGetConditionTooltip() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, LootItemRandomChanceCondition.randomChance(0.5f).build()), List.of(
                "Random Chance:",
                "  -> Chance: 0.50"
        ));
        assertTooltip(UTILS.getConditionTooltip(UTILS, new UnknownCondition(true)), List.of(
                "Auto-detected: minecraft:unknown",
                "  -> valid: true"
        ));
    }

    @Test
    public void testGetValueTooltip() {
        assertUnorderedTooltip(UTILS.getValueTooltip(UTILS, Items.EMERALD.getDefaultInstance()).build("ali.property.branch.item"), List.of(
                "Item:",
                "  -> Item: minecraft:emerald",
                "  -> Count: 1",
                "  -> Components:",
                List.of(
                        "    -> minecraft:max_stack_size",
                        "      -> Value: 64",
                        "    -> minecraft:lore",
                        "    -> minecraft:enchantments",
                        "    -> minecraft:repair_cost",
                        "      -> Value: 0",
                        "    -> minecraft:attribute_modifiers",
                        "    -> minecraft:rarity",
                        "      -> Rarity: COMMON",
                        "    -> minecraft:item_name",
                        "      -> Item Name: Emerald",
                        "    -> minecraft:item_model",
                        "      -> Value: minecraft:emerald",
                        "    -> minecraft:provides_trim_material",
                        "      -> Material: minecraft:emerald",
                        "    -> minecraft:break_sound",
                        "      -> Sound: minecraft:entity.item.break",
                        "    -> minecraft:tooltip_display",
                        "      -> Hide Tooltip: false",
                        "    -> minecraft:use_effects",
                        "      -> Not implemented: SimpleType",
                        "      -> Not implemented: SimpleType",
                        "    -> minecraft:swing_animation"
                )
        ));
        assertTooltip(UTILS.getValueTooltip(UTILS, new StringBuilder()).build("ali.property.value.value"), List.of(
                "Not implemented: [java.lang.StringBuilder]"
        ));
    }

    @Test
    public void testAutoDetection() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new TestCondition(
                new StringBuilder("hello"),
                new boolean[]{true, false},
                new Boolean[]{false, true},
                new LootItemFunction[]{new UnknownFunction(Items.ITEM_FRAME, UniformGenerator.between(1, 4)), SetItemDamageFunction.setDamage(ConstantValue.exactly(0.5f)).build()},
                new UnknownCondition[0],
                new StringBuilder[]{new StringBuilder("a"), new StringBuilder("b")},
                new int[0],
                BlockStateProperties.ATTACHED,
                true,
                false,
                SetStewEffectFunction.stewEffect().withEffect(MobEffects.ABSORPTION, ConstantValue.exactly(2)).build(),
                LootItemRandomChanceCondition.randomChance(0.3f).build()
        )), List.of(
            "Auto-detected: minecraft:unknown",
                "  -> builder: [java.lang.StringBuilder]",
                "  -> primitiveArray:",
                "    -> true",
                "    -> false",
                "  -> array:",
                "    -> false",
                "    -> true",
                "  -> functions:",
                "    -> Auto-detected: minecraft:unknown",
                "      -> item: minecraft:item_frame",
                "      -> value: 1-4",
                "    -> Set Damage:",
                "      -> Damage: 0.50",
                "      -> Add: false",
                "  -> conditions: []",
                "  -> builders:",
                "    -> Not implemented: [java.lang.StringBuilder]",
                "    -> Not implemented: [java.lang.StringBuilder]",
                "  -> empty: []",
                "  -> enumValue: attached",
                "  -> primitive: true",
                "  -> state: false",
                "  -> function:",
                "    -> Set Stew Effect:",
                "      -> minecraft:absorption",
                "        -> Duration: 2",
                "  -> condition:",
                "    -> Random Chance:",
                "      -> Chance: 0.30"
        ));
    }

    private record UnknownFunction(Item item, NumberProvider value) implements LootItemFunction {
        @NotNull
        @Override
        public LootItemFunctionType<?> getType() {
            return LootFunctionTypes.UNUSED;
        }

        @Override
        public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
            return itemStack;
        }
    }

    private record UnknownCondition(boolean valid) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return true;
        }
    }

    private record TestCondition(
            StringBuilder builder,
            boolean[] primitiveArray,
            Boolean[] array,
            LootItemFunction[] functions,
            UnknownCondition[] conditions,
            StringBuilder[] builders,
            int[] empty,
            BooleanProperty enumValue,
            boolean primitive,
            Boolean state,
            LootItemFunction function,
            LootItemCondition condition
    ) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return true;
        }
    }
}
