package com.yanny.aci.tooltip;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface TooltipLine {
    int LINE_HEIGHT = 10;

    record Text(Component component) implements TooltipLine {
    }

    record Series(List<Double> heights, List<Boolean> modes, int color, boolean base, int first, int last, Component min, Component max) {
        public Series {
            heights = List.copyOf(heights);
            modes = List.copyOf(modes);
        }
    }

    record Chart(int indent, int columnWidth, List<Series> series) implements TooltipLine {
        public static final int BAR_LINES = 3;
        public static final int PANEL_LINES = BAR_LINES + 1;

        public Chart {
            series = List.copyOf(series);
        }
    }

    @NotNull
    static TooltipLine text(Component component) {
        return new Text(component);
    }

    @NotNull
    static List<TooltipLine> texts(List<Component> components) {
        return components.stream().map(TooltipLine::text).toList();
    }
}
