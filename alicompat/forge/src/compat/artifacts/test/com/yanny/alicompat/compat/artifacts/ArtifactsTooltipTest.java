package com.yanny.alicompat.compat.artifacts;

import artifacts.loot.ArtifactRarityAdjustedChance;
import artifacts.loot.ConfigValueChance;
import artifacts.loot.IsAprilFools;
import artifacts.loot.ReplaceWithLootTableFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class ArtifactsTooltipTest {
    @Test
    public void testReplaceWithLootTableFunction() {
        ReplaceWithLootTableFunction function = new ReplaceWithLootTableFunction(new LootItemCondition[0], new ResourceLocation("artifacts", "chests/cave"));

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Replace With Loot Table:",
                "  -> Loot Table: artifacts:chests/cave"
        ));
    }

    @Test
    public void testRarityAdjustedChanceCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new ArtifactRarityAdjustedChance(0.25F)).build(), List.of(
                "Artifact Rarity Adjusted Chance:",
                "  -> Default Probability: 0.25"
        ));
    }

    @Test
    public void testAprilFoolsCondition() {
        assertTooltip(UTILS.getConditionTooltip(UTILS, new IsAprilFools()).build(), List.of(
                "Is April Fools"
        ));
    }

    @Test
    public void testConfigValueChanceCondition() {
        LootItemCondition condition = ConfigValueChance.archaeologyChance().build();

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition).build(), List.of(
                "Config Value Chance:",
                "  -> Config: archaeology"
        ));
    }
}
