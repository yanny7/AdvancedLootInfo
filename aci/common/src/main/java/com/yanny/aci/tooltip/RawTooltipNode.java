package com.yanny.aci.tooltip;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RawTooltipNode(@Nullable String key, String @Nullable[] values, @Nullable Component componentValue, @Nullable TooltipNumber number, short flags, List<Integer> children) {
}
