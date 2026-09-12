package com.yanny.alicompat.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.util.loot.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class ImmersiveEngineeringTooltipTest {
    @Test
    public void testTileDropEntry() {
        LootPoolEntryContainer entry = BEDropLootEntry.builder().setWeight(3).setQuality(1).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Block Entity Drop:",
                "  -> Weight: 3",
                "  -> Quality: 1"
        ));
    }

    @Test
    public void testDropInventoryEntry() {
        LootPoolEntryContainer entry = DropInventoryLootEntry.builder().setWeight(2).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Drop Inventory:",
                "  -> Weight: 2"
        ));
    }

    @Test
    public void testMultiblockDropsEntry() {
        LootPoolEntryContainer entry = MultiblockDropsLootContainer.builder().setWeight(2).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Multiblock Drops:",
                "  -> Weight: 2"
        ));
    }

    @Test
    public void testConveyorCoverFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, ConveyorCoverLootFunction.builder().build()).build(), List.of(
                "Conveyor Cover:"
        ));
    }

    @Test
    public void testConveyorCoverFunctionWithPredicate() {
        LootItemFunction function = create(ConveyorCoverLootFunction.class, ExplosionCondition.survivesExplosion().build());

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Conveyor Cover:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testRevolverperkFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, RevolverperkLootFunction.builder().build()).build(), List.of(
                "Revolver Perks:"
        ));
    }

    @Test
    public void testRevolverperkFunctionWithPredicate() {
        LootItemFunction function = RevolverperkLootFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Revolver Perks:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testWindmillFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, WindmillLootFunction.builder().build()).build(), List.of(
                "Windmill Sails:"
        ));
    }

    @Test
    public void testWindmillFunctionWithPredicate() {
        LootItemFunction function = create(WindmillLootFunction.class, ExplosionCondition.survivesExplosion().build());

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Windmill Sails:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testSecretBluprintzFunction() {
        LootItemFunction function = BluprintzLootFunction.builder().build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Secret Bluprintz:"
        ));
    }

    @Test
    public void testSecretBluprintzFunctionWithPredicate() {
        LootItemFunction function = BluprintzLootFunction.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Secret Bluprintz:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testPropertyCountFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, propertyCount()).build(), List.of(
                "Property Count:",
                "  -> Property: count"
        ));
    }

    @Test
    public void testFluidStackIngredient() {
        IngredientFluidStack ingredient = new IngredientFluidStack(new FluidTagInput(FluidTags.WATER, 500));

        assertTooltip(UTILS.getIngredientTooltip(UTILS, ingredient).build(), List.of(
                "Tag: minecraft:water",
                "Amount: 500"
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

    @NotNull
    private static PropertyCountLootFunction propertyCount() {
        try {
            Constructor<PropertyCountLootFunction> constructor = PropertyCountLootFunction.class.getDeclaredConstructor(LootItemCondition[].class, String.class);

            constructor.setAccessible(true);
            return constructor.newInstance(new LootItemCondition[0], "count");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create PropertyCountLootFunction", e);
        }
    }
}
