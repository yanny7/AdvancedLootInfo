package com.yanny.awi.configuration;

import com.yanny.aci.configuration.CoreConfigUtils;
import com.yanny.awi.Utils;
import com.yanny.awi.platform.Services;
import org.jetbrains.annotations.NotNull;

public class ConfigUtils {
    @NotNull
    public static AwiConfig readConfiguration() {
        return CoreConfigUtils.readConfiguration(Services.getPlatform().getConfiguration(), Utils.MOD_ID, Utils.COMMON_CONFIG_NAME,
                AwiConfig.class, AwiConfig::new, CoreConfigUtils.gsonBuilder().create());
    }
}
