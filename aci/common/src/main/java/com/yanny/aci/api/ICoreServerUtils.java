package com.yanny.aci.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface ICoreServerUtils {
    @Nullable
    ServerLevel getServerLevel();

    @Nullable
    HolderLookup.Provider lookupProvider();
}
