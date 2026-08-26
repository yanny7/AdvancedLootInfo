package com.yanny.awi.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.aci.configuration.ICoreConfig;
import com.yanny.aci.configuration.TooltipColors;

public class AwiConfig implements ICoreConfig {
    public static final int CURRENT_VERSION = 1;

    public static final Codec<AwiConfig> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                Codec.INT.fieldOf("configVersion").orElse(0).forGetter((c) -> c.configVersion),
                Codec.BOOL.fieldOf("logMoreStatistics").orElse(false).forGetter((c) -> c.logMoreStatistics),
                Codec.BOOL.fieldOf("showInGameNames").orElse(true).forGetter((c) -> c.showInGameNames),
                Codec.BOOL.fieldOf("showConfigConditionalBlocks").orElse(false).forGetter((c) -> c.showConfigConditionalBlocks),
                TooltipColors.CODEC.fieldOf("tooltipColors").orElseGet(TooltipColors::new).forGetter((c) -> c.tooltipColors)
        ).apply(instance, (version, log, show, showConfigConditional, colors) -> {
            AwiConfig config = new AwiConfig();

            config.configVersion = version;
            config.logMoreStatistics = log;
            config.showInGameNames = show;
            config.showConfigConditionalBlocks = showConfigConditional;
            config.tooltipColors = colors;
            return config;
        })
    );

    public int configVersion = 0;

    public TooltipColors tooltipColors = new TooltipColors();

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;

    /**
     * Whether to display blocks that a feature's {@code place()} bytecode only reaches through a test on the
     * configuration it was given. Off by default: for a lava lake that is {@code minecraft:ice}, which the feature only
     * places when its fluid is water, so showing it is wrong for every vanilla lake. Turn it on to see everything the
     * bytecode scan found, at the cost of those blocks being wrong for some configurations.
     */
    public boolean showConfigConditionalBlocks = false;

    @Override
    public int getConfigVersion() {
        return configVersion;
    }

    @Override
    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    @Override
    public int getCurrentVersion() {
        return CURRENT_VERSION;
    }
}
