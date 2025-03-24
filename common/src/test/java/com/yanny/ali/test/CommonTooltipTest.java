package com.yanny.ali.test;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.yanny.ali.test.utils.TestUtils.assertTooltip;

public class CommonTooltipTest {
    @Test
    public void testRangeValue() {
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(123))), "123");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(1, 5))), "1-5");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(-1, 3))), "-1-3");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(456, 789))), "456-789");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(2.5F))), "2.50");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(2.5F, 3.6F))), "2.50-3.60");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(true, false))), "1[+Score]");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(false, true))), "1[+???]");
        assertTooltip(Component.translatable(String.valueOf(new RangeValue(true, true))), "1[+Score][+???]");
    }

    @Test
    public void testCount() {
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.getCount(new RangeValue(7, 12)));
        components.addAll(TooltipUtils.getBonusCount(new Pair<>(Holder.direct(Enchantments.BLOCK_FORTUNE), Map.of(
                1, new RangeValue(0.25F),
                2, new RangeValue(0.5F),
                3, new RangeValue(0.75F)
        ))));
        components.addAll(TooltipUtils.getBonusCount(null));
        assertTooltip(components, List.of(
                "Count: 7-12",
                "  -> 0.25 (Fortune I)",
                "  -> 0.50 (Fortune II)",
                "  -> 0.75 (Fortune III)"
        ));
    }

    @Test
    public void testChance() {
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.getChance(new RangeValue(5.2F, 9.3F)));
        components.addAll(TooltipUtils.getBonusChance(Pair.of(Holder.direct(Enchantments.MOB_LOOTING), Map.of(
                1, new RangeValue(0.25F),
                2, new RangeValue(0.5F),
                3, new RangeValue(0.75F)
        ))));
        components.addAll(TooltipUtils.getBonusChance(null));
        assertTooltip(components, List.of(
                "Chance: 5.20-9.30%",
                "  -> 0.25% (Looting I)",
                "  -> 0.50% (Looting II)",
                "  -> 0.75% (Looting III)"
        ));
    }

    @Test
    public void testPad() {
        assertTooltip(TooltipUtils.pad(0, 123), "123");
        assertTooltip(TooltipUtils.pad(1, 123), "  -> 123");
        assertTooltip(TooltipUtils.pad(2, 123), "    -> 123");
        assertTooltip(TooltipUtils.pad(3, 123), "      -> 123");

        assertTooltip(TooltipUtils.pad(1, Component.literal("Hello")), "  -> Hello");
    }
}
