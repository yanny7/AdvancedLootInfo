package com.yanny.alicompat.compat.ironsspellbooks;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import io.redspace.ironsspellbooks.loot.FurledMapLootFunction;
import io.redspace.ironsspellbooks.loot.RandomizeRingEnhancementFunction;
import io.redspace.ironsspellbooks.loot.RandomizeSpellFunction;
import io.redspace.ironsspellbooks.loot.SpellFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class IronsSpellbooksTooltipTest {
    @Test
    public void testFurledMapFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, furledMap()).build(), List.of(
                "Set Furled Map:",
                "  -> Destination: ironsspellbooks:citadel",
                "  -> Name: item.irons_spellbooks.map_citadel",
                "  -> Dimension: minecraft:overworld"
        ));
    }

    @Test
    public void testRandomizeSpellFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, randomizeSpell()).build(), List.of(
                "Randomize Spell:",
                "  -> Quality: 1-4",
                "  -> Applicable Spells:",
                "    -> Force: false"
        ));
    }

    @Test
    public void testRandomizeRingEnhancementFunction() {
        assertTooltip(UTILS.getFunctionTooltip(UTILS, randomizeRingEnhancement()).build(), List.of(
                "Randomize Ring Enhancement:",
                "  -> Spell Filter:",
                "    -> Force: false"
        ));
    }

    @Test
    public void testWizardTradeListing() {
        WizardTrade trade = WizardTrade.of(new ItemStack(Items.EMERALD), new RangeValue(4), new ItemStack(Items.PAPER), new RangeValue(1), 8, 3, 0.05F);

        assertTooltip(UTILS.getItemListing(UTILS, trade, TooltipNode.empty()).getTooltip(), List.of(
                "Uses: 8",
                "XP: 3",
                "Price Multiplier: 0.05"
        ));
    }

    @NotNull
    private static LootItemFunction furledMap() {
        return create(FurledMapLootFunction.class,
                new Class[]{LootItemCondition[].class, String.class, String.class, Optional.class},
                new LootItemCondition[0], "ironsspellbooks:citadel", "item.irons_spellbooks.map_citadel", Optional.of("minecraft:overworld"));
    }

    @NotNull
    private static LootItemFunction randomizeSpell() {
        return create(RandomizeSpellFunction.class,
                new Class[]{LootItemCondition[].class, NumberProvider.class, SpellFilter.class},
                new LootItemCondition[0], UniformGenerator.between(1.0F, 4.0F), new SpellFilter());
    }

    @NotNull
    private static LootItemFunction randomizeRingEnhancement() {
        return create(RandomizeRingEnhancementFunction.class,
                new Class[]{LootItemCondition[].class, SpellFilter.class},
                new LootItemCondition[0], new SpellFilter());
    }

    private static <T extends LootItemFunction> T create(Class<T> clazz, Class<?>[] parameters, Object... arguments) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(parameters);

            constructor.setAccessible(true);
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + clazz.getSimpleName(), e);
        }
    }
}
