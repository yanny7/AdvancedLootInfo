package com.yanny.ali.test;

import com.mojang.serialization.MapCodec;
import com.yanny.aci.language.CoreLang;
import com.yanny.aci.api.NumberExpr;
import com.yanny.ali.language.Lang;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.server.LootConditionTypes;
import com.yanny.ali.plugin.server.LootFunctionTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ServerUtilsTest {
    @Test
    public void testGetFunctionTooltip() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(5, 0.5f)).build()).build(), List.of(
                "Set Count:",
                "  -> Count: 0 to 5  ~2 to 3 (31%)",
                "  -> Add: False"
        ));
        assertTooltip(UTILS.getFunctionTooltip(UTILS, new UnknownFunction(Items.ANDESITE, BinomialDistributionGenerator.binomial(5, 0.3f))).build(), List.of(
                "Auto-detected: minecraft:unknown"
        ));
    }

    @Test
    public void testGetConditionTooltip() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, LootItemRandomChanceCondition.randomChance(0.5f).build()).build(), List.of(
                "Random Chance:",
                "  -> Chance: 0.5"
        ));
        assertTooltip(UTILS.getConditionTooltip(UTILS, new UnknownCondition(true)).build(), List.of(
                "Auto-detected: minecraft:unknown"
        ));
    }

    @Test
    public void testGetValueTooltip() {
        assertTooltip(UTILS.getValueTooltip(UTILS, Items.EMERALD.getDefaultInstance()).build(Lang.Branch.ITEM), List.of(
                "Item:",
                "  -> Item: minecraft:emerald",
                "  -> Count: 1",
                "  -> Components:",
                "    -> minecraft:item_model",
                "      -> Value: minecraft:emerald",
                "    -> minecraft:item_name",
                "      -> Item Name: Emerald",
                "    -> minecraft:provides_trim_material",
                "      -> Value: minecraft:emerald"
        ));
        assertTooltip(UTILS.getValueTooltip(UTILS, new StringBuilder()).build(), List.of(
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
        )).build(), List.of(
            "Auto-detected: minecraft:unknown"
        ));
    }

    @Test
    public void testFailingItemStackModifierKeepsStack() {
        PluginManager.getInstance().serverRegistry.registerItemStackModifier(BrokenItemFunction.class, (u, f, i) -> {
            throw new IllegalStateException("broken modifier");
        });

        ItemStack stack = new ItemStack(Items.STONE);

        assertSame(stack, UTILS.applyItemStackModifier(UTILS, new BrokenItemFunction(), stack));
    }

    @Test
    public void testFailingUnwrapperKeepsWrapper() {
        BrokenWrapperCondition condition = new BrokenWrapperCondition();

        PluginManager.getInstance().serverRegistry.registerConditionUnwrapper(BrokenWrapperCondition.class, (u, c) -> {
            throw new IllegalStateException("broken unwrapper");
        });

        assertEquals(List.of(condition), UTILS.unwrapCondition(UTILS, condition));
    }

    @Test
    public void testFailingCountModifierGivesOpaque() {
        PluginManager.getInstance().serverRegistry.registerCountModifier(BrokenFunction.class, (u, f, c, l) -> {
            throw new IllegalStateException("broken modifier");
        });

        assertEquals(NumberExpr.opaque("minecraft:unknown"), UTILS.applyCountModifier(UTILS, new BrokenFunction(), NumberExpr.constant(1), new ArrayList<>()));
    }

    @Test
    public void testFailingChanceModifierGivesOpaque() {
        PluginManager.getInstance().serverRegistry.registerChanceModifier(BrokenCondition.class, (u, c, v, l) -> {
            throw new IllegalStateException("broken modifier");
        });

        assertEquals(NumberExpr.opaque("minecraft:unknown"), UTILS.applyChanceModifier(UTILS, new BrokenCondition(), NumberExpr.constant(1), new ArrayList<>()));
    }

    private record BrokenItemFunction() implements LootItemFunction {
        @Override
        public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
            return itemStack;
        }

        @NotNull
        @Override
        public MapCodec<? extends LootItemFunction> codec() {
            return LootFunctionTypes.UNUSED;
        }
    }

    private record BrokenWrapperCondition() implements LootItemCondition {
        @Override
        public boolean test(LootContext lootContext) {
            return true;
        }

        @NotNull
        @Override
        public MapCodec<? extends LootItemCondition> codec() {
            return LootConditionTypes.UNUSED;
        }
    }

    private record BrokenFunction() implements LootItemFunction {
        @Override
        public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
            return itemStack;
        }

        @NotNull
        @Override
        public MapCodec<? extends LootItemFunction> codec() {
            return LootFunctionTypes.UNUSED;
        }
    }

    private record BrokenCondition() implements LootItemCondition {
        @Override
        public boolean test(LootContext lootContext) {
            return true;
        }

        @NotNull
        @Override
        public MapCodec<? extends LootItemCondition> codec() {
            return LootConditionTypes.UNUSED;
        }
    }

    private record UnknownFunction(Item item, NumberProvider value) implements LootItemFunction {
        @Override
        public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
            return itemStack;
        }

        @NotNull
        @Override
        public MapCodec<? extends LootItemFunction> codec() {
            return LootFunctionTypes.UNUSED;
        }
    }

    private record UnknownCondition(boolean valid) implements LootItemCondition {
        @Override
        public boolean test(LootContext lootContext) {
            return true;
        }

        @NotNull
        @Override
        public MapCodec<? extends LootItemCondition> codec() {
            return LootConditionTypes.UNUSED;
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
        @Override
        public boolean test(LootContext lootContext) {
            return true;
        }

        @NotNull
        @Override
        public MapCodec<? extends LootItemCondition> codec() {
            return LootConditionTypes.UNUSED;
        }
    }
}
