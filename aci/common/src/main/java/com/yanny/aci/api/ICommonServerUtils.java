package com.yanny.aci.api;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface ICommonServerUtils {
    @Nullable
    ServerLevel getServerLevel();
}
