package com.yanny.ali.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.DispatcherProvider;
import net.minecraft.world.level.storage.loot.providers.number.StoredNumberAccess;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.Absolute;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.EnvironmentAttributeValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.NumberDispatcher;
import net.minecraft.world.level.storage.loot.providers.number.ints.Power;
import net.minecraft.world.level.storage.loot.providers.number.ints.StorageValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntProviderTest {
    private static final Holder<LootItemCondition> CONDITION = Holder.direct(ExplosionCondition.survivesExplosion().build());

    @Test
    public void testConstant() {
        assertEquals("5", convert(ContextIntProviders.exactly(5)));
    }

    @Test
    public void testUniform() {
        assertEquals("2-6", convert(ContextIntProviders.between(2, 6)));
    }

    @Test
    public void testBinomial() {
        assertEquals("0-5", convert(ContextIntProviders.binomial(5, 0.5f)));
    }

    @Test
    public void testScore() {
        assertEquals("1[+Score]", convert(ContextIntProviders.fromScoreboard(LootContext.EntityTarget.THIS, "objective")));
    }

    @Test
    public void testStorage() throws CommandSyntaxException {
        StoredNumberAccess access = new StoredNumberAccess(Identifier.withDefaultNamespace("test"), new NbtPathArgument().parse(new StringReader("value")));

        assertEquals("1[+???]", convert(Holder.direct(new StorageValue(access, ContextIntProviders.exactly(1)))));
    }

    @Test
    public void testEnvironmentAttribute() {
        assertEquals("1[+???]", convert(Holder.direct(new EnvironmentAttributeValue(EnvironmentAttributes.CLOUD_HEIGHT))));
    }

    @Test
    public void testSum() {
        assertEquals("5", convert(ContextIntProviders.add(ContextIntProviders.exactly(2), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testProduct() {
        assertEquals("6", convert(ContextIntProviders.mul(ContextIntProviders.exactly(2), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testAverage() {
        assertEquals("3", convert(ContextIntProviders.avg(ContextIntProviders.exactly(2), ContextIntProviders.exactly(4))));
    }

    @Test
    public void testMinimum() {
        assertEquals("1-3", convert(ContextIntProviders.min(ContextIntProviders.between(1, 5), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testMaximum() {
        assertEquals("3-5", convert(ContextIntProviders.max(ContextIntProviders.between(1, 5), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testDifference() {
        assertEquals("3", convert(ContextIntProviders.sub(ContextIntProviders.exactly(5), ContextIntProviders.exactly(2))));
    }

    @Test
    public void testNegate() {
        assertEquals("-4", convert(ContextIntProviders.negate(ContextIntProviders.exactly(4))));
    }

    @Test
    public void testAbsolute() {
        assertEquals("4", convert(Holder.direct(new Absolute(ContextIntProviders.negate(ContextIntProviders.exactly(4))))));
        assertEquals("0-5", convert(Holder.direct(new Absolute(ContextIntProviders.between(-5, 3)))));
    }

    @Test
    public void testFromFloat() {
        assertEquals("2", convert(ContextIntProviders.fromFloat(ContextFloatProviders.exactly(2.7f))));
    }

    @Test
    public void testConditional() {
        assertEquals("1-9", convert(Holder.direct(new ConditionalValue(CONDITION, ContextIntProviders.exactly(1), ContextIntProviders.exactly(9)))));
    }

    @Test
    public void testDispatcher() {
        List<DispatcherProvider.Case<ContextIntProvider>> cases = List.of(new DispatcherProvider.Case<>(CONDITION, ContextIntProviders.exactly(7)));

        assertEquals("1-7", convert(Holder.direct(new NumberDispatcher(cases, ContextIntProviders.exactly(1)))));
    }

    @Test
    public void testWeightedList() {
        WeightedList<Holder<ContextIntProvider>> list = WeightedList.of(List.of(
                new Weighted<>(ContextIntProviders.exactly(2), 1),
                new Weighted<>(ContextIntProviders.exactly(8), 3)
        ));

        assertEquals("2-8", convert(ContextIntProviders.weighted(list)));
    }

    @Test
    public void testModulus() {
        assertEquals("1[+???]", convert(ContextIntProviders.mod(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testFloorModulus() {
        assertEquals("1[+???]", convert(ContextIntProviders.floorMod(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testQuotient() {
        assertEquals("1[+???]", convert(ContextIntProviders.div(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testFloorQuotient() {
        assertEquals("1[+???]", convert(ContextIntProviders.floorDiv(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3))));
    }

    @Test
    public void testPower() {
        assertEquals("1[+???]", convert(Holder.direct(new Power(ContextIntProviders.exactly(2), ContextIntProviders.exactly(3)))));
    }

    private static String convert(Holder<ContextIntProvider> provider) {
        return UTILS.convertInt(UTILS, provider).toString();
    }
}
