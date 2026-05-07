package com.yanny.aci.manager;

import com.yanny.aci.api.ICoreCommonUtils;
import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CoreServerRegistry<
        TConfig,
        TCommonUtils extends ICoreCommonUtils<TConfig>,
        TServerUtils extends ICoreServerUtils<?>
        >
        extends BaseRegistry
        implements ICoreServerUtils<TServerUtils>, ICoreCommonUtils<TConfig> {
    private ServerLevel serverLevel;
    protected final TCommonUtils commonUtils;

    public CoreServerRegistry(TCommonUtils registry) {
        commonUtils = registry;
    }

    public void setServerLevel(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    @Nullable
    @Override
    public ServerLevel getServerLevel() {
        return serverLevel;
    }

    @NotNull
    @Override
    public TConfig getConfiguration() {
        return commonUtils.getConfiguration();
    }
}
