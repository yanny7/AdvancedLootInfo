package com.yanny.ali.test;

import com.yanny.ali.plugin.server.SlotSourceTooltipUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.slot.*;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContextArg;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;
import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class SlotSourceTooltipTest {
    @Test
    public void testEmptySlotTooltip() {
        assertTooltip(SlotSourceTooltipUtils.getEmptyTooltip(UTILS, new EmptySlotSource()), List.of("Empty Slot"));
    }

    @Test
    public void testContentsTooltip() {
        assertTooltip(SlotSourceTooltipUtils.getContentsTooltip(UTILS, new ContentsSlotSource(new EmptySlotSource(), ContainerComponentManipulators.CONTAINER)), List.of(
                "Contents:",
                "  -> Component: minecraft:container",
                "  -> Slot Source:",
                "    -> Empty Slot"
        ));
    }

    @Test
    public void testSlotRangeTooltip() {
        LootContextArg<Object> lootContextArg = new LootContextArg.SimpleGetter<>() {
            @NotNull
            @Override
            public ContextKey<?> contextParam() {
                return new ContextKey<>(Identifier.withDefaultNamespace("this"));
            }
        };
        assertTooltip(SlotSourceTooltipUtils.getSlotRangeTooltip(UTILS, new RangeSlotSource(lootContextArg, SlotRange.of("armor.*", IntList.of(1, 2)))), List.of(
                "Slot Range:",
                "  -> Source: minecraft:this",
                "  -> Slot Range: armor.*"
        ));
    }

    @Test
    public void testLimitSlotsTooltip() {
        assertTooltip(SlotSourceTooltipUtils.getLimitSlotsTooltip(UTILS, new LimitSlotSource(new EmptySlotSource(), 1)), List.of(
                "Limit Slots:",
                "  -> Limit: 1",
                "  -> Slot Source:",
                "    -> Empty Slot"
        ));
    }

    @Test
    public void testFilteredTooltip() {
        assertTooltip(SlotSourceTooltipUtils.getFilteredTooltip(UTILS, new FilteredSlotSource(new EmptySlotSource(), ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(3)).build())), List.of(
                "Filtered:",
                "  -> Filter:",
                "    -> Count: ≥3",
                "  -> Slot Source:",
                "    -> Empty Slot"
        ));
    }

    @Test
    public void testGroupTooltip() {
        assertTooltip(SlotSourceTooltipUtils.getGroupTooltip(UTILS, new GroupSlotSource(List.of((SlotSource) new EmptySlotSource(), new EmptySlotSource()))), List.of(
                "Group:",
                "  -> Slots:",
                "    -> Empty Slot",
                "    -> Empty Slot"
        ));
    }
}
