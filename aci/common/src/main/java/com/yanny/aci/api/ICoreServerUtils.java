package com.yanny.aci.api;

import com.yanny.aci.tooltip.TooltipBuilder;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICoreServerUtils<SELF extends ICoreServerUtils<?>> {
    @NotNull
    <T> TooltipBuilder getValueTooltip(SELF utils, @Nullable T value);

    @NotNull
    ServerLevel getServerLevel();

    int getTranslationKeyIndex(String key);
}
