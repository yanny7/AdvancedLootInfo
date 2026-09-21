package com.yanny.ali.test;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.functions.FunctionReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
    private static final ResourceLocation PREDICATE = new ResourceLocation("test", "predicate");
    private static final ResourceLocation CYCLIC_PREDICATE = new ResourceLocation("test", "cyclic_predicate");
    private static final ResourceLocation MODIFIER = new ResourceLocation("test", "modifier");
    private static final ResourceLocation MISSING = new ResourceLocation("test", "missing");

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
    public void testCompositePredicateChance() {
        LootItemCondition composite = LootDataManager.createComposite(new LootItemCondition[]{LootItemRandomChanceCondition.randomChance(0.25f).build()});

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
        assertTooltip(getChanceTooltip(getEnchantedChance(utils, List.of(ConditionReference.conditionReference(MISSING).build()), 1)).build(), List.of());
        assertTooltip(getChanceTooltip(getEnchantedChance(utils, List.of(ConditionReference.conditionReference(CYCLIC_PREDICATE).build()), 1)).build(), List.of());
    }

    @Test
    public void testFunctionSequenceCount() {
        LootItemFunction sequence = LootDataManager.createComposite(new LootItemFunction[]{SetItemCountFunction.setCount(ConstantValue.exactly(10)).build()});

        assertTooltip(getCountTooltip(getEnchantedCount(UTILS, List.of(sequence))).build(), List.of("Count: 10"));
    }

    @Test
    public void testFunctionReferenceCount() {
        IServerUtils utils = resolvingUtils();

        assertTooltip(getCountTooltip(getEnchantedCount(utils, List.of(FunctionReference.functionReference(MODIFIER).build()))).build(), List.of("Count: 10"));
        assertTooltip(getCountTooltip(getEnchantedCount(utils, List.of(
                FunctionReference.functionReference(MODIFIER).when(LootItemRandomChanceCondition.randomChance(0.5f)).build()
        ))).build(), List.of("Count: 1"));
        assertTooltip(getCountTooltip(getEnchantedCount(utils, List.of(FunctionReference.functionReference(MISSING).build()))).build(), List.of("Count: 1"));
    }

    @Test
    public void testFunctionSequenceItemStack() {
        LootItemFunction sequence = LootDataManager.createComposite(new LootItemFunction[]{SetNameFunction.setName(Component.literal("Unwrapped")).build()});
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
        Map<ResourceLocation, Object> elements = Map.of(
                PREDICATE, LootItemRandomChanceCondition.randomChance(0.25f).build(),
                CYCLIC_PREDICATE, ConditionReference.conditionReference(CYCLIC_PREDICATE).build(),
                MODIFIER, SetItemCountFunction.setCount(ConstantValue.exactly(10)).build()
        );
        LootDataManager lootData = mock(LootDataManager.class);
        MinecraftServer server = mock(MinecraftServer.class);
        ServerLevel level = mock(ServerLevel.class);
        IServerUtils utils = spy(UTILS);

        doAnswer((i) -> elements.get(i.<LootDataId<?>>getArgument(0).location())).when(lootData).getElement(any());
        doReturn(lootData).when(server).getLootData();
        doReturn(server).when(level).getServer();
        doReturn(level).when(utils).getServerLevel();
        return utils;
    }
}
