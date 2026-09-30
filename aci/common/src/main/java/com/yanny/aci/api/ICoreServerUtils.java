package com.yanny.aci.api;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNodePalette;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICoreServerUtils<SELF extends ICoreServerUtils<?>> {
    @NotNull
    String getModId();

    @NotNull
    <T> TooltipBuilder getValueTooltip(SELF utils, @Nullable T value);

    @NotNull
    ServerLevel getServerLevel();

    @NotNull
    TooltipNodePalette getTooltipCache();

    @NotNull
    HolderLookup.Provider lookupProvider();

    int getTranslationKeyIndex(String key);

    @NotNull
    NumberExpr convertIntProvider(SELF utils, IntProvider provider);

    @NotNull
    NumberExpr convertFloatProvider(SELF utils, FloatProvider provider);
}
