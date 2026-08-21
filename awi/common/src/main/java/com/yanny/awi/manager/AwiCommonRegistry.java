package com.yanny.awi.manager;

import com.yanny.aci.manager.CoreCommonRegistry;
import com.yanny.awi.Utils;
import com.yanny.awi.api.ICommonRegistry;
import com.yanny.awi.api.ICommonUtils;
import com.yanny.awi.configuration.AwiConfig;
import com.yanny.awi.configuration.ConfigUtils;
import org.jetbrains.annotations.NotNull;

public class AwiCommonRegistry extends CoreCommonRegistry<AwiConfig> implements ICommonRegistry, ICommonUtils {
    public AwiCommonRegistry() {
        super(Utils.MOD_ID);
    }

    @NotNull
    @Override
    protected AwiConfig loadConfiguration() {
        return ConfigUtils.readConfiguration();
    }
}
