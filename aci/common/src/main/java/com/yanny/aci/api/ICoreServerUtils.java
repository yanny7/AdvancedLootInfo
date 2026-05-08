package com.yanny.aci.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICoreServerUtils<SELF extends ICoreServerUtils<?>> {
    @NotNull
    <T> TooltipBuilder getValueTooltip(SELF utils, @Nullable T value);

    @Nullable
    ServerLevel getServerLevel();

    @Nullable
    HolderLookup.Provider lookupProvider();
}
