package com.yanny.alicompat.compat.moonlight;

import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPoolEntry;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.yanny.aci.test.utils.TestUtils.assertTooltip;
import static com.yanny.alicompat.test.CompatTooltipSuite.UTILS;

public class MoonlightTooltipTest {
    @Test
    public void testOptionalItemPoolEntry() {
        LootPoolEntryContainer entry = OptionalItemPoolEntry.lootTableOptionalItem("minecraft:apple").setWeight(3).setQuality(2).build();

        assertTooltip(UTILS.getEntryTooltip(UTILS, entry).build(), List.of(
                "Optional Item:",
                "  -> Item: minecraft:apple",
                "  -> Name: minecraft:apple",
                "  -> Weight: 3",
                "  -> Quality: 2"
        ));
    }

    @Test
    public void testOptionalPropertyCondition() {
        Optional<StatePropertiesPredicate> properties = StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.LIT, true).build();

        assertTooltip(UTILS.getConditionTooltip(UTILS, condition(ResourceLocation.parse("minecraft:furnace"), properties)).build(), List.of(
                "Optional Block State Property:",
                "  -> Block: minecraft:furnace",
                "  -> Properties:",
                "    -> lit: true"
        ));
    }

    @NotNull
    private static OptionalPropertyCondition condition(ResourceLocation blockId, Optional<StatePropertiesPredicate> properties) {
        try {
            var constructor = OptionalPropertyCondition.class.getDeclaredConstructor(ResourceLocation.class, Optional.class);

            constructor.setAccessible(true);
            return constructor.newInstance(blockId, properties);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create OptionalPropertyCondition", e);
        }
    }
}
