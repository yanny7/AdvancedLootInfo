package com.yanny.alicompat.compat.refinedstorage;

import com.refinedmods.refinedstorage.loottable.ControllerLootFunction;
import com.refinedmods.refinedstorage.loottable.CrafterLootFunction;
import com.refinedmods.refinedstorage.loottable.PortableGridBlockLootFunction;
import com.refinedmods.refinedstorage.loottable.StorageBlockLootFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class RefinedStorageTooltipTest {
    @Test
    public void testControllerFunction() {
        LootItemFunction function = ControllerLootFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Stored Energy:"
        ));
    }

    @Test
    public void testControllerFunctionWithPredicate() {
        LootItemFunction function = ControllerLootFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Stored Energy:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testCrafterFunction() {
        LootItemFunction function = CrafterLootFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Crafter Name:"
        ));
    }

    @Test
    public void testCrafterFunctionWithPredicate() {
        LootItemFunction function = CrafterLootFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Crafter Name:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testPortableGridBlockFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, create(PortableGridBlockLootFunction.class)).build(), List.of(
                "Copy Portable Grid Data:"
        ));
    }

    @Test
    public void testPortableGridBlockFunctionWithPredicate() {
        LootItemFunction function = create(PortableGridBlockLootFunction.class, ExplosionCondition.survivesExplosion().build());

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Portable Grid Data:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testStorageBlockFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, create(StorageBlockLootFunction.class)).build(), List.of(
                "Copy Storage Id:"
        ));
    }

    @Test
    public void testStorageBlockFunctionWithPredicate() {
        LootItemFunction function = create(StorageBlockLootFunction.class, ExplosionCondition.survivesExplosion().build());

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Copy Storage Id:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @NotNull
    private static <T extends LootItemFunction> T create(Class<T> clazz, LootItemCondition... conditions) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(LootItemCondition[].class);

            constructor.setAccessible(true);
            return constructor.newInstance((Object) conditions);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + clazz.getSimpleName(), e);
        }
    }
}
