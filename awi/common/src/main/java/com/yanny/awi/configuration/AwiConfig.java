package com.yanny.awi.configuration;

import com.yanny.aci.configuration.ICoreConfig;

public class AwiConfig implements ICoreConfig {
    public static final int CURRENT_VERSION = 1;

    public int configVersion = 0;

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
