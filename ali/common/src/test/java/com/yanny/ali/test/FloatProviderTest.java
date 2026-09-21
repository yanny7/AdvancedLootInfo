package com.yanny.ali.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.DispatcherProvider;
import net.minecraft.world.level.storage.loot.providers.number.StoredNumberAccess;
import net.minecraft.world.level.storage.loot.providers.number.floats.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.Floor;
import net.minecraft.world.level.storage.loot.providers.number.floats.FromInt;
import net.minecraft.world.level.storage.loot.providers.number.floats.NumberDispatcher;
import net.minecraft.world.level.storage.loot.providers.number.floats.StorageValue;
import net.minecraft.world.level.storage.loot.providers.number.floats.WeightedListValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatProviderTest {
    private static final Holder<LootItemCondition> CONDITION = Holder.direct(ExplosionCondition.survivesExplosion().build());

    @Test
    public void testConstant() {
        assertEquals("1.50", convert(ContextFloatProviders.exactly(1.5f)));
    }

    @Test
    public void testUniform() {
        assertEquals("1-3", convert(ContextFloatProviders.between(1f, 3f)));
    }

    @Test
    public void testStorage() throws CommandSyntaxException {
        StoredNumberAccess access = new StoredNumberAccess(Identifier.withDefaultNamespace("test"), new NbtPathArgument().parse(new StringReader("value")));

        assertEquals("1[+???]", convert(Holder.direct(new StorageValue(access, ContextFloatProviders.exactly(1f)))));
    }

    @Test
    public void testEnvironmentAttribute() {
        assertEquals("1[+???]", convert(ContextFloatProviders.forEnvironmentAttribute(EnvironmentAttributes.CLOUD_HEIGHT)));
    }

    @Test
    public void testEnchantmentLevel() {
        assertEquals("1[+???]", convert(ContextFloatProviders.forEnchantmentLevel(LevelBasedValue.constant(2f))));
    }

    @Test
    public void testSum() {
        assertEquals("4", convert(ContextFloatProviders.add(ContextFloatProviders.exactly(1.5f), ContextFloatProviders.exactly(2.5f))));
    }

    @Test
    public void testProduct() {
        assertEquals("5", convert(ContextFloatProviders.mul(ContextFloatProviders.exactly(2f), ContextFloatProviders.exactly(2.5f))));
    }

    @Test
    public void testAverage() {
        assertEquals("2", convert(ContextFloatProviders.avg(ContextFloatProviders.exactly(1f), ContextFloatProviders.exactly(3f))));
    }

    @Test
    public void testMinimum() {
        assertEquals("1-3", convert(ContextFloatProviders.min(ContextFloatProviders.between(1f, 5f), ContextFloatProviders.exactly(3f))));
    }

    @Test
    public void testMaximum() {
        assertEquals("3-5", convert(ContextFloatProviders.max(ContextFloatProviders.between(1f, 5f), ContextFloatProviders.exactly(3f))));
    }

    @Test
    public void testDifference() {
        assertEquals("3.50", convert(ContextFloatProviders.sub(ContextFloatProviders.exactly(5f), ContextFloatProviders.exactly(1.5f))));
    }

    @Test
    public void testNegate() {
        assertEquals("-2.50", convert(ContextFloatProviders.negate(ContextFloatProviders.exactly(2.5f))));
    }

    @Test
    public void testAbsolute() {
        assertEquals("2.50", convert(ContextFloatProviders.abs(ContextFloatProviders.negate(ContextFloatProviders.exactly(2.5f)))));
    }

    @Test
    public void testFromInt() {
        assertEquals("1-4", convert(Holder.direct(new FromInt(ContextIntProviders.between(1, 4)))));
    }

    @Test
    public void testFloor() {
        assertEquals("1-3", convert(Holder.direct(new Floor(ContextFloatProviders.between(1.5f, 3.5f)))));
    }

    @Test
    public void testCeiling() {
        assertEquals("2-4", convert(ContextFloatProviders.ceiling(ContextFloatProviders.between(1.5f, 3.5f))));
    }

    @Test
    public void testRound() {
        assertEquals("1-4", convert(ContextFloatProviders.round(ContextFloatProviders.between(1.4f, 3.6f))));
    }

    @Test
    public void testTruncate() {
        assertEquals("1-3", convert(ContextFloatProviders.trunc(ContextFloatProviders.between(1.9f, 3.9f))));
    }

    @Test
    public void testSquareRoot() {
        assertEquals("2-3", convert(ContextFloatProviders.sqrt(ContextFloatProviders.between(4f, 9f))));
    }

    @Test
    public void testSine() {
        assertEquals("-1-1", convert(ContextFloatProviders.sin(ContextFloatProviders.exactly(1f))));
    }

    @Test
    public void testCosine() {
        assertEquals("-1-1", convert(ContextFloatProviders.cos(ContextFloatProviders.exactly(1f))));
    }

    @Test
    public void testConditional() {
        assertEquals("1-9", convert(Holder.direct(new ConditionalValue(CONDITION, ContextFloatProviders.exactly(1f), ContextFloatProviders.exactly(9f)))));
    }

    @Test
    public void testDispatcher() {
        List<DispatcherProvider.Case<ContextFloatProvider>> cases = List.of(new DispatcherProvider.Case<>(CONDITION, ContextFloatProviders.exactly(7f)));

        assertEquals("1-7", convert(Holder.direct(new NumberDispatcher(cases, ContextFloatProviders.exactly(1f)))));
    }

    @Test
    public void testWeightedList() {
        WeightedList<Holder<ContextFloatProvider>> list = WeightedList.of(List.of(
                new Weighted<>(ContextFloatProviders.exactly(2f), 1),
                new Weighted<>(ContextFloatProviders.exactly(8f), 3)
        ));

        assertEquals("2-8", convert(Holder.direct(new WeightedListValue(list))));
    }

    @Test
    public void testLength() {
        assertEquals("1[+???]", convert(ContextFloatProviders.length(ContextFloatProviders.exactly(3f), ContextFloatProviders.exactly(4f))));
    }

    @Test
    public void testModulus() {
        assertEquals("1[+???]", convert(ContextFloatProviders.mod(ContextFloatProviders.exactly(7f), ContextFloatProviders.exactly(3f))));
    }

    @Test
    public void testQuotient() {
        assertEquals("1[+???]", convert(ContextFloatProviders.div(ContextFloatProviders.exactly(7f), ContextFloatProviders.exactly(3f))));
    }

    @Test
    public void testPower() {
        assertEquals("1[+???]", convert(ContextFloatProviders.pow(ContextFloatProviders.exactly(2f), ContextFloatProviders.exactly(3f))));
    }

    private static String convert(Holder<ContextFloatProvider> provider) {
        return UTILS.convertFloat(UTILS, provider).toString();
    }
}
