package com.yanny.awi.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.aci.configuration.ConfigCodecs;
import com.yanny.aci.configuration.ICoreConfig;
import com.yanny.aci.configuration.SpawnInfoFilter;
import com.yanny.aci.configuration.TooltipColors;
import com.yanny.awi.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwiConfig implements ICoreConfig {
    public static final int CURRENT_VERSION = 1;

    public static final Codec<AwiConfig> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                ConfigCodecs.field(Utils.MOD_ID, Codec.INT, "configVersion", () -> 0).forGetter((c) -> c.configVersion),
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "logMoreStatistics", () -> false).forGetter((c) -> c.logMoreStatistics),
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "showInGameNames", () -> true).forGetter((c) -> c.showInGameNames),
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "showCharts", () -> true).forGetter((c) -> c.showCharts),
                ConfigCodecs.field(Utils.MOD_ID, Codec.BOOL, "showConfigConditionalBlocks", () -> false).forGetter((c) -> c.showConfigConditionalBlocks),
                ConfigCodecs.field(Utils.MOD_ID, TooltipColors.codec(Utils.MOD_ID), "tooltipColors", TooltipColors::new).forGetter((c) -> c.tooltipColors),
                ConfigCodecs.field(Utils.MOD_ID, SpawnInfoFilter.codec(Utils.MOD_ID), "spawnInfo", SpawnInfoFilter::new).forGetter((c) -> c.spawnInfo),
                ConfigCodecs.field(Utils.MOD_ID, ConfigCodecs.lenientList(Utils.MOD_ID, Codec.STRING, "dimensions"), "dimensions", ArrayList::new).forGetter((c) -> c.dimensions),
                ConfigCodecs.field(Utils.MOD_ID, Codec.unboundedMap(Codec.STRING, Codec.STRING), "dimensionIcons", HashMap::new).forGetter((c) -> c.dimensionIcons)
        ).apply(instance, (version, log, show, showCharts, showConfigConditional, colors, spawnInfo, dimensions, dimensionIcons) -> {
            AwiConfig config = new AwiConfig();

            config.configVersion = version;
            config.logMoreStatistics = log;
            config.showInGameNames = show;
            config.showCharts = showCharts;
            config.showConfigConditionalBlocks = showConfigConditional;
            config.tooltipColors = colors;
            config.spawnInfo = spawnInfo;
            config.dimensions = dimensions;
            config.dimensionIcons = dimensionIcons;
            return config;
        })
    );

    public int configVersion = 0;

    public TooltipColors tooltipColors = new TooltipColors();
    public SpawnInfoFilter spawnInfo = new SpawnInfoFilter();

    public boolean logMoreStatistics = false;
    public boolean showInGameNames = true;
    public boolean showCharts = true;

    /**
     * Whether to display blocks that a feature's {@code place()} bytecode only reaches through a test on the
     * configuration it was given. Off by default: for a lava lake that is {@code minecraft:ice}, which the feature only
     * places when its fluid is water, so showing it is wrong for every vanilla lake. Turn it on to see everything the
     * bytecode scan found, at the cost of those blocks being wrong for some configurations.
     */
    public boolean showConfigConditionalBlocks = false;

    public List<String> dimensions = new ArrayList<>();
    public Map<String, String> dimensionIcons = new HashMap<>();

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
