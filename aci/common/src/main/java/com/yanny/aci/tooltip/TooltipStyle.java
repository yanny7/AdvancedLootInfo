package com.yanny.aci.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.List;

public record TooltipStyle(Style text, Style value, Style error, Style branch, Style secondary, List<Integer> levels) {
    public static final List<Integer> DEFAULT_LEVELS = List.of(0x55FF55, 0xFFFF55, 0xFF55FF, 0x5555FF, 0xFF5555, 0xFFAA00);
    public static final TooltipStyle DEFAULT = new TooltipStyle(
            Style.EMPTY.withColor(ChatFormatting.GOLD),
            Style.EMPTY.withColor(ChatFormatting.AQUA),
            Style.EMPTY.withColor(ChatFormatting.RED),
            Style.EMPTY.withColor(ChatFormatting.DARK_GRAY),
            Style.EMPTY.withColor(ChatFormatting.GRAY),
            DEFAULT_LEVELS
    );

    public TooltipStyle {
        levels = levels.isEmpty() ? DEFAULT_LEVELS : List.copyOf(levels);
    }

    public int level(int index) {
        return 0xFF000000 | levels.get(index % levels.size());
    }
}
