package com.yanny.aci.tooltip;

import com.yanny.aci.api.NumberInterval;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CacheKey(@Nullable String key, @Nullable List<String> values, @Nullable Component componentValue, @Nullable TooltipNumber number, @Nullable List<NumberInterval> intervals, short flags, List<TooltipNode> children) {}
