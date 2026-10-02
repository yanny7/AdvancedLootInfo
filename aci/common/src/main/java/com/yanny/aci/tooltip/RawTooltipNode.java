package com.yanny.aci.tooltip;

import com.yanny.aci.api.NumberInterval;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RawTooltipNode(@Nullable String key, String @Nullable[] values, @Nullable Component componentValue, @Nullable TooltipNumber number, @Nullable List<NumberInterval> intervals, short flags, List<Integer> children) {
}
