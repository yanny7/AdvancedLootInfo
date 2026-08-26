package com.yanny.aci.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public record TooltipStyle(Style text, Style value, Style error, Style branch) {
    public static final TooltipStyle DEFAULT = new TooltipStyle(
            Style.EMPTY.withColor(ChatFormatting.GOLD),
            Style.EMPTY.withColor(ChatFormatting.AQUA),
            Style.EMPTY.withColor(ChatFormatting.RED),
            Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)
    );
}
