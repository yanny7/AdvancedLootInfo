package com.yanny.ali.test;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedChance;
import static com.yanny.ali.plugin.common.NodeUtils.getEnchantedCount;
import static com.yanny.ali.plugin.server.TooltipUtils.getChanceTooltip;
import static com.yanny.ali.plugin.server.TooltipUtils.getCountTooltip;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnwrapperTest {
    private static final ResourceKey<LootItemCondition> PREDICATE = ResourceKey.create(Registries.PREDICATE, Identifier.fromNamespaceAndPath("test", "predicate"));
    private static final ResourceKey<LootItemCondition> CYCLIC_PREDICATE = ResourceKey.create(Registries.PREDICATE, Identifier.fromNamespaceAndPath("test", "cyclic_predicate"));
    private static final ResourceKey<LootItemCondition> MISSING_PREDICATE = ResourceKey.create(Registries.PREDICATE, Identifier.fromNamespaceAndPath("test", "missing"));
    private static final ResourceKey<LootItemFunction> MODIFIER = ResourceKey.create(Registries.ITEM_MODIFIER, Identifier.fromNamespaceAndPath("test", "modifier"));
    private static final ResourceKey<LootItemFunction> MISSING_MODIFIER = ResourceKey.create(Registries.ITEM_MODIFIER, Identifier.fromNamespaceAndPath("test", "missing"));

    @Test
    public void testAllOfChance() {
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.25f)).build()
        ), 1)).build(), List.of("Chance: 25%"));
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                AllOfCondition.allOf(
                        AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.5f)),
                        LootItemRandomChanceCondition.randomChance(0.5f)
                ).build()
        ), 1)).build(), List.of("Chance: 25%"));
    }

    @Test
    public void testInlinePredicateListChance() {
        LootItemCondition composite = AllOfCondition.allOf(List.of(LootItemRandomChanceCondition.randomChance(0.25f).build()));

        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(composite), 1)).build(), List.of("Chance: 25%"));
    }

    @Test
    public void testInexactConditionsKept() {
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                AnyOfCondition.anyOf(LootItemRandomChanceCondition.randomChance(0.25f)).build()
        ), 1)).build(), List.of());
        assertTooltip(getChanceTooltip(getEnchantedChance(UTILS, List.of(
                LootItemRandomChanceCondition.randomChance(0.25f).invert().build()
        ), 1)).build(), List.of());
    }

    @Test
    public void testConditionReferenceChance() {
        IServerUtils utils = resolvingUtils();

        assertTooltip(getChanceTooltip(getEnchantedChance(utils, List.of(ConditionReference.conditionReference(PREDICATE).build()), 1)).build(), List.of("Chance: 25%"));
        assertTooltip(getChanceTooltip(getEnchantedChance(utils, List.of(ConditionReference.conditionReference(MISSING_PREDICATE).build()), 1)).build(), List.of());
        assertTooltip(getChanceTooltip(getEnchantedChance(utils, List.of(ConditionReference.conditionReference(CYCLIC_PREDICATE).build()), 1)).build(), List.of());
    }

    @Test
    public void testFunctionSequenceCount() {
        LootItemFunction sequence = SequenceFunction.of(List.of(SetItemCountFunction.setCount(ConstantValue.exactly(10)).build()));

        assertTooltip(getCountTooltip(getEnchantedCount(UTILS, List.of(sequence))).build(), List.of("Count: 10"));
    }

    @Test
    public void testFunctionReferenceCount() {
        IServerUtils utils = resolvingUtils();

        assertTooltip(getCountTooltip(getEnchantedCount(utils, List.of(FunctionReference.functionReference(MODIFIER).build()))).build(), List.of("Count: 10"));
        assertTooltip(getCountTooltip(getEnchantedCount(utils, List.of(
                FunctionReference.functionReference(MODIFIER).when(LootItemRandomChanceCondition.randomChance(0.5f)).build()
        ))).build(), List.of("Count: 1"));
        assertTooltip(getCountTooltip(getEnchantedCount(utils, List.of(FunctionReference.functionReference(MISSING_MODIFIER).build()))).build(), List.of("Count: 1"));
    }

    @Test
    public void testFunctionSequenceItemStack() {
        LootItemFunction sequence = SequenceFunction.of(List.of(SetNameFunction.setName(Component.literal("Unwrapped"), SetNameFunction.Target.ITEM_NAME).build()));
        ItemStack stack = TooltipUtils.getItemStack(UTILS, Items.DIAMOND.getDefaultInstance(), List.of(sequence));

        assertEquals("Unwrapped", stack.getHoverName().getString());
    }

    @Test
    public void testHasPredicates() {
        assertFalse(NodeUtils.hasPredicates(UTILS, List.of(
                AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.5f), ExplosionCondition.survivesExplosion()).build()
        )));
        assertTrue(NodeUtils.hasPredicates(UTILS, List.of(
                AllOfCondition.allOf(LootItemRandomChanceCondition.randomChance(0.5f), WeatherCheck.weather().setRaining(true)).build()
        )));
    }

    @NotNull
    private static IServerUtils resolvingUtils() {
        Map<ResourceKey<?>, Object> elements = Map.of(
                PREDICATE, LootItemRandomChanceCondition.randomChance(0.25f).build(),
                CYCLIC_PREDICATE, ConditionReference.conditionReference(CYCLIC_PREDICATE).build(),
                MODIFIER, SetItemCountFunction.setCount(ConstantValue.exactly(10)).build()
        );
        HolderLookup.Provider lookup = mock(HolderLookup.Provider.class);
        ReloadableServerRegistries.Holder registries = mock(ReloadableServerRegistries.Holder.class);
        MinecraftServer server = mock(MinecraftServer.class);
        ServerLevel level = mock(ServerLevel.class);
        IServerUtils utils = spy(UTILS);

        doAnswer((i) -> Optional.ofNullable(elements.get(i.<ResourceKey<?>>getArgument(0))).map(UnwrapperTest::reference)).when(lookup).get(any());
        doReturn(lookup).when(registries).lookup();
        doReturn(registries).when(server).reloadableRegistries();
        doReturn(server).when(level).getServer();
        doReturn(level).when(utils).getServerLevel();
        return utils;
    }

    @NotNull
    private static Holder.Reference<?> reference(Object value) {
        Holder.Reference<?> reference = mock(Holder.Reference.class);

        doReturn(value).when(reference).value();
        return reference;
    }
}
