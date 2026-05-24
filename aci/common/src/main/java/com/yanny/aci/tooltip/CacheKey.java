package com.yanny.aci.tooltip;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CacheKey(@Nullable String key, @Nullable List<String> values, @Nullable Component componentValue, short flags, List<TooltipNode> children) {}
