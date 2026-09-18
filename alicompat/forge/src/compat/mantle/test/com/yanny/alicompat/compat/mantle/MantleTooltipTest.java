package com.yanny.alicompat.compat.mantle;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.RetexturedLootFunction;
import slimeknights.mantle.loot.function.SetFluidLootFunction;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MantleTooltipTest {
    @Test
    public void testTagPreferenceEntry() {
        LootPoolEntryContainer entry = TagPreferenceLootEntry.tagPreference(ItemTags.PLANKS).setWeight(3).setQuality(1).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Tag Preference:",
                "  -> Tag: minecraft:planks",
                "  -> Weight: 3",
                "  -> Quality: 1"
        ));
    }

    @Test
    public void testBlockTagCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new BlockTagLootCondition(BlockTags.LOGS)).build(), List.of(
                "Block Tag:",
                "  -> Tag: minecraft:logs"
        ));
    }

    @Test
    public void testHasLootContextSetCondition() {
        LootItemCondition condition = new HasLootContextSetCondition(LootContextParamSets.BLOCK);

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Has Loot Context Set:",
                "  -> Id: minecraft:block"
        ));
    }

    @Test
    public void testRetexturedFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, new RetexturedLootFunction()).build(), List.of(
                "Retextured:"
        ));
    }

    @Test
    public void testRetexturedFunctionWithPredicate() {
        LootItemCondition[] conditions = {ExplosionCondition.survivesExplosion().build()};
        LootItemFunction function = new RetexturedLootFunction(conditions);

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Retextured:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testSetFluidFunction() {
        LootItemFunction function = SetFluidLootFunction.builder(new FluidStack(Fluids.WATER, 1000)).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Set Fluid:",
                "  -> Fluid:",
                "    -> Fluid: minecraft:water",
                "    -> Amount: 1000"
        ));
    }
}
