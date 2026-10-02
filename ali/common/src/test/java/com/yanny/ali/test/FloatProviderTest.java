package com.yanny.ali.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.enchantment.Enchantment;
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

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.mockito.Mockito.*;

public class FloatProviderTest {
    private static final Holder<LootItemCondition> CONDITION = Holder.direct(ExplosionCondition.survivesExplosion().build());

    @Test
    public void testConstant() {
        assertValue(ContextFloatProviders.exactly(1.5f), "Value: 1.5");
    }

    @Test
    public void testUniform() {
        assertValue(ContextFloatProviders.between(1f, 3f), "Value: 1 to 3");
    }

    @Test
    public void testStorage() throws CommandSyntaxException {
        StoredNumberAccess access = new StoredNumberAccess(Identifier.withDefaultNamespace("test"), new NbtPathArgument().parse(new StringReader("value")));

        assertValue(Holder.direct(new StorageValue(access, ContextFloatProviders.exactly(1f))), "Value: any (storage \"value\" (minecraft:test))");
    }

    @Test
    public void testEnvironmentAttribute() {
        assertValue(ContextFloatProviders.forEnvironmentAttribute(EnvironmentAttributes.CLOUD_HEIGHT), "Value: any (environment attribute \"minecraft:visual/cloud_height\")");
    }

    @Test
    public void testEnchantmentLevel() {
        RegistryAccess registryAccess = mock(RegistryAccess.class);
        ServerLevel level = mock(ServerLevel.class);
        IServerUtils utils = spy(UTILS);
        Registry<Enchantment> enchantments = mock();
        Holder<ContextFloatProvider> provider = ContextFloatProviders.forEnchantmentLevel(LevelBasedValue.constant(2f));

        doAnswer((i) -> LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).listElements()).when(enchantments).listElements();
        doReturn(enchantments).when(registryAccess).lookupOrThrow(Registries.ENCHANTMENT);
        doReturn(registryAccess).when(level).registryAccess();
        doReturn(level).when(utils).getServerLevel();
        assertTooltip(TooltipUtils.getNumberTooltip(utils, () -> utils.convertContextFloat(utils, provider)).build(Lang.Value.VALUE), true, List.of("Value: 2"));
    }

    @Test
    public void testSum() {
        assertValue(ContextFloatProviders.add(ContextFloatProviders.exactly(1.5f), ContextFloatProviders.exactly(2.5f)), "Value: 4");
    }

    @Test
    public void testProduct() {
        assertValue(ContextFloatProviders.mul(ContextFloatProviders.exactly(2f), ContextFloatProviders.exactly(2.5f)), "Value: 5");
    }

    @Test
    public void testAverage() {
        assertValue(ContextFloatProviders.avg(ContextFloatProviders.exactly(1f), ContextFloatProviders.exactly(3f)), "Value: 2");
    }

    @Test
    public void testMinimum() {
        assertValue(ContextFloatProviders.min(ContextFloatProviders.between(1f, 5f), ContextFloatProviders.exactly(3f)), "Value: 1 to 3  ~3 (50%)");
    }

    @Test
    public void testMaximum() {
        assertValue(ContextFloatProviders.max(ContextFloatProviders.between(1f, 5f), ContextFloatProviders.exactly(3f)), "Value: 3 to 5  ~3 (50%)");
    }

    @Test
    public void testDifference() {
        assertValue(ContextFloatProviders.sub(ContextFloatProviders.exactly(5f), ContextFloatProviders.exactly(1.5f)), "Value: 3.5");
    }

    @Test
    public void testNegate() {
        assertValue(ContextFloatProviders.negate(ContextFloatProviders.exactly(2.5f)), "Value: −2.5");
    }

    @Test
    public void testAbsolute() {
        assertValue(ContextFloatProviders.abs(ContextFloatProviders.negate(ContextFloatProviders.exactly(2.5f))), "Value: 2.5");
    }

    @Test
    public void testFromInt() {
        assertValue(Holder.direct(new FromInt(ContextIntProviders.between(1, 4))), "Value: 1 to 4");
    }

    @Test
    public void testFloor() {
        assertValue(Holder.direct(new Floor(ContextFloatProviders.between(1.5f, 3.5f))), "Value: 1 to 3  ~2 (50%)");
    }

    @Test
    public void testCeiling() {
        assertValue(ContextFloatProviders.ceiling(ContextFloatProviders.between(1.5f, 3.5f)), "Value: 2 to 4  ~3 (50%)");
    }

    @Test
    public void testRound() {
        assertValue(ContextFloatProviders.round(ContextFloatProviders.between(1.4f, 3.6f)), "Value: 1 to 4  ~2 to 3 (45%)");
    }

    @Test
    public void testTruncate() {
        assertValue(ContextFloatProviders.trunc(ContextFloatProviders.between(1.9f, 3.9f)), "Value: 1 to 3  ~2 (50%)");
    }

    @Test
    public void testSquareRoot() {
        assertValue(ContextFloatProviders.sqrt(ContextFloatProviders.between(4f, 9f)), "Value: 2 to 3");
    }

    @Test
    public void testSine() {
        assertValue(ContextFloatProviders.sin(ContextFloatProviders.exactly(1f)), "Value: 0.84");
    }

    @Test
    public void testCosine() {
        assertValue(ContextFloatProviders.cos(ContextFloatProviders.exactly(1f)), "Value: 0.54");
    }

    @Test
    public void testConditional() {
        assertValue(Holder.direct(new ConditionalValue(CONDITION, ContextFloatProviders.exactly(1f), ContextFloatProviders.exactly(9f))), "Value: ", "  -> 1", "    -> Survives Explosion", "  -> otherwise 9");
    }

    @Test
    public void testDispatcher() {
        List<DispatcherProvider.Case<ContextFloatProvider>> cases = List.of(new DispatcherProvider.Case<>(CONDITION, ContextFloatProviders.exactly(7f)));

        assertValue(Holder.direct(new NumberDispatcher(cases, ContextFloatProviders.exactly(1f))), "Value: ", "  -> 7", "    -> Survives Explosion", "  -> otherwise 1");
    }

    @Test
    public void testWeightedList() {
        WeightedList<Holder<ContextFloatProvider>> list = WeightedList.of(List.of(
                new Weighted<>(ContextFloatProviders.exactly(2f), 1),
                new Weighted<>(ContextFloatProviders.exactly(8f), 3)
        ));

        assertValue(Holder.direct(new WeightedListValue(list)), "Value: 2 to 8  ~8 (75%)", "  -> 2 (25%)", "  -> 8 (75%)");
    }

    @Test
    public void testLength() {
        assertValue(ContextFloatProviders.length(ContextFloatProviders.exactly(3f), ContextFloatProviders.exactly(4f)), "Value: 5");
    }

    @Test
    public void testModulus() {
        assertValue(ContextFloatProviders.mod(ContextFloatProviders.exactly(7f), ContextFloatProviders.exactly(3f)), "Value: 1");
    }

    @Test
    public void testQuotient() {
        assertValue(ContextFloatProviders.div(ContextFloatProviders.exactly(7f), ContextFloatProviders.exactly(3f)), "Value: 2.33");
    }

    @Test
    public void testPower() {
        assertValue(ContextFloatProviders.pow(ContextFloatProviders.exactly(2f), ContextFloatProviders.exactly(3f)), "Value: 8");
    }

    private static void assertValue(Holder<ContextFloatProvider> provider, String... expected) {
        assertTooltip(TooltipUtils.getNumberTooltip(UTILS, () -> UTILS.convertContextFloat(UTILS, provider)).build(Lang.Value.VALUE), true, List.of(expected));
    }
}
