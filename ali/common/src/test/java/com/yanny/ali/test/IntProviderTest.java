package com.yanny.ali.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.TooltipUtils;
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

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class IntProviderTest {
    private static final Holder<LootItemCondition> CONDITION = Holder.direct(ExplosionCondition.survivesExplosion().build());

    @Test
    public void testConstant() {
        assertValue(ContextIntProviders.exactly(5), "Value: 5");
    }

    @Test
    public void testUniform() {
        assertValue(ContextIntProviders.between(2, 6), "Value: 2 to 6");
    }

    @Test
    public void testBinomial() {
        assertValue(ContextIntProviders.binomial(5, 0.5f), "Value: 0 to 5  ~2 to 3 (31%)");
    }

    @Test
    public void testScore() {
        assertValue(ContextIntProviders.fromScoreboard(LootContext.EntityTarget.THIS, "objective"), "Value: ", "  -> any (score \"objective\" (this))", "    -> Score Exists", "  -> otherwise 0");
    }

    @Test
    public void testStorage() throws CommandSyntaxException {
        StoredNumberAccess access = new StoredNumberAccess(Identifier.withDefaultNamespace("test"), new NbtPathArgument().parse(new StringReader("value")));

        assertValue(Holder.direct(new StorageValue(access, ContextIntProviders.exactly(1))), "Value: ", "  -> any (storage \"value\" (minecraft:test))", "    -> Storage Value Exists", "  -> otherwise 1");
    }

    @Test
    public void testEnvironmentAttribute() {
        assertValue(Holder.direct(new EnvironmentAttributeValue(EnvironmentAttributes.CLOUD_HEIGHT)), "Value: any (environment attribute \"minecraft:visual/cloud_height\")");
    }

    @Test
    public void testSum() {
        assertValue(ContextIntProviders.add(ContextIntProviders.exactly(2), ContextIntProviders.exactly(3)), "Value: 5");
    }

    @Test
    public void testProduct() {
        assertValue(ContextIntProviders.mul(ContextIntProviders.exactly(2), ContextIntProviders.exactly(3)), "Value: 6");
    }

    @Test
    public void testAverage() {
        assertValue(ContextIntProviders.avg(ContextIntProviders.exactly(2), ContextIntProviders.exactly(4)), "Value: 3");
    }

    @Test
    public void testMinimum() {
        assertValue(ContextIntProviders.min(ContextIntProviders.between(1, 5), ContextIntProviders.exactly(3)), "Value: 1 to 3  ~3 (60%)");
    }

    @Test
    public void testMaximum() {
        assertValue(ContextIntProviders.max(ContextIntProviders.between(1, 5), ContextIntProviders.exactly(3)), "Value: 3 to 5  ~3 (60%)");
    }

    @Test
    public void testDifference() {
        assertValue(ContextIntProviders.sub(ContextIntProviders.exactly(5), ContextIntProviders.exactly(2)), "Value: 3");
    }

    @Test
    public void testNegate() {
        assertValue(ContextIntProviders.negate(ContextIntProviders.exactly(4)), "Value: −4");
    }

    @Test
    public void testAbsolute() {
        assertValue(Holder.direct(new Absolute(ContextIntProviders.negate(ContextIntProviders.exactly(4)))), "Value: 4");
        assertValue(Holder.direct(new Absolute(ContextIntProviders.between(-5, 3))), "Value: 0 to 5  ~1 to 3 (22%)");
    }

    @Test
    public void testFromFloat() {
        assertValue(ContextIntProviders.fromFloat(ContextFloatProviders.exactly(2.7f)), "Value: 2");
    }

    @Test
    public void testConditional() {
        assertValue(Holder.direct(new ConditionalValue(CONDITION, ContextIntProviders.exactly(1), ContextIntProviders.exactly(9))), "Value: ", "  -> 1", "    -> Survives Explosion", "  -> otherwise 9");
    }

    @Test
    public void testDispatcher() {
        List<DispatcherProvider.Case<ContextIntProvider>> cases = List.of(new DispatcherProvider.Case<>(CONDITION, ContextIntProviders.exactly(7)));

        assertValue(Holder.direct(new NumberDispatcher(cases, ContextIntProviders.exactly(1))), "Value: ", "  -> 7", "    -> Survives Explosion", "  -> otherwise 1");
    }

    @Test
    public void testWeightedList() {
        WeightedList<Holder<ContextIntProvider>> list = WeightedList.of(List.of(
                new Weighted<>(ContextIntProviders.exactly(2), 1),
                new Weighted<>(ContextIntProviders.exactly(8), 3)
        ));

        assertValue(ContextIntProviders.weighted(list), "Value: 2 to 8  ~8 (75%)", "  -> 2 (25%)", "  -> 8 (75%)");
    }

    @Test
    public void testModulus() {
        assertValue(ContextIntProviders.mod(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3)), "Value: 1");
    }

    @Test
    public void testFloorModulus() {
        assertValue(ContextIntProviders.floorMod(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3)), "Value: 1");
    }

    @Test
    public void testQuotient() {
        assertValue(ContextIntProviders.div(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3)), "Value: 2");
    }

    @Test
    public void testFloorQuotient() {
        assertValue(ContextIntProviders.floorDiv(ContextIntProviders.exactly(7), ContextIntProviders.exactly(3)), "Value: 2");
    }

    @Test
    public void testPower() {
        assertValue(Holder.direct(new Power(ContextIntProviders.exactly(2), ContextIntProviders.exactly(3))), "Value: 8");
    }

    private static void assertValue(Holder<ContextIntProvider> provider, String... expected) {
        assertTooltip(TooltipUtils.getNumberTooltip((c) -> UTILS.convertContextInt(UTILS, provider, c)).build(Lang.Value.VALUE), true, List.of(expected));
    }
}
