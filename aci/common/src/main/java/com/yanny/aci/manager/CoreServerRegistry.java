package com.yanny.aci.manager;

import com.yanny.aci.api.ICoreCommonUtils;
import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CoreServerRegistry
        <
                CN,
                BU extends ICoreCommonUtils<CN>
        >
        extends BaseRegistry
        implements ICoreServerUtils, ICoreCommonUtils<CN> {
    private ServerLevel serverLevel;
    protected final BU commonUtils;

    public CoreServerRegistry(BU registry) {
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
    public CN getConfiguration() {
        return commonUtils.getConfiguration();
    }
}
