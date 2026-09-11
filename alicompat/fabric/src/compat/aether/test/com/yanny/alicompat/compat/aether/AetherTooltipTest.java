package com.yanny.alicompat.compat.aether;

import com.aetherteam.aether.loot.conditions.ConfigEnabled;
import com.aetherteam.aether.loot.functions.DoubleDrops;
import com.aetherteam.aether.loot.functions.SpawnTNT;
import com.aetherteam.aether.loot.functions.SpawnXP;
import com.aetherteam.aether.loot.functions.WhirlwindSpawnEntity;
import io.github.fabricators_of_create.porting_lib.config.ModConfigSpec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class AetherTooltipTest {
    @Test
    public void testDoubleDropsFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, DoubleDrops.builder().build()).build(), List.of(
                "Double Drops:"
        ));
    }

    @Test
    public void testDoubleDropsFunctionWithPredicate() {
        LootItemFunction function = DoubleDrops.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Double Drops:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testSpawnTntFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, SpawnTNT.builder().build()).build(), List.of(
                "Spawn TNT:"
        ));
    }

    @Test
    public void testSpawnTntFunctionWithPredicate() {
        LootItemFunction function = SpawnTNT.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Spawn TNT:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testSpawnXpFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, SpawnXP.builder().build()).build(), List.of(
                "Spawn XP:"
        ));
    }

    @Test
    public void testSpawnXpFunctionWithPredicate() {
        LootItemFunction function = SpawnXP.builder().when(ExplosionCondition.survivesExplosion()).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Spawn XP:",
                "  -> Predicates:",
                "    -> Survives Explosion"
        ));
    }

    @Test
    public void testWhirlwindSpawnEntityFunction() {
        LootItemFunction function = WhirlwindSpawnEntity.builder(EntityType.PIG, 3).build();

        assertTooltip(UTILS.getFunctionTooltip(UTILS, function).build(), List.of(
                "Whirlwind Spawn Entity:",
                "  -> Entity Type: minecraft:pig",
                "  -> Count: 3"
        ));
    }

    @Test
    public void testConfigEnabledCondition() {
        ModConfigSpec.ConfigValue<Boolean> value = new ModConfigSpec.Builder().define("gameplay.double_drops", true);

        assertTooltip(UTILS.getConditionTooltip(UTILS, new ConfigEnabled(value)).build(), List.of(
                "Config Enabled:",
                "  -> Config: [gameplay, double_drops]"
        ));
    }
}
