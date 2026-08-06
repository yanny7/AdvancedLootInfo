package com.yanny.awi.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AwiConfig {
    public static final int CURRENT_VERSION = 1;

    public static final Codec<AwiConfig> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                Codec.INT.fieldOf("configVersion").orElse(0).forGetter((c) -> c.configVersion),
                Codec.BOOL.fieldOf("logMoreStatistics").orElse(false).forGetter((c) -> c.logMoreStatistics),
                Codec.BOOL.fieldOf("showInGameNames").orElse(true).forGetter((c) -> c.showInGameNames)
        ).apply(instance, (version, log, show) -> {
            AwiConfig config = new AwiConfig();

            config.configVersion = version;
            config.logMoreStatistics = log;
            config.showInGameNames = show;
            return config;
        })
    );

    public int configVersion = 0;

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;
}