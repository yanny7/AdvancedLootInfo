package com.yanny.advanced_loot_info.configuration;

import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

public class Config {
    private final ForgeConfigSpec.BooleanValue debug;

    public Config(@NotNull ForgeConfigSpec.Builder builder) {
        debug = builder.comment("Show debug values").define("debug", false);
    }

    public boolean isDebug() {
        return debug.get();
    }
}
