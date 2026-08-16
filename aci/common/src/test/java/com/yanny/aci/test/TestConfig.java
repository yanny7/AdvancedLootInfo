package com.yanny.aci.test;

import com.yanny.aci.configuration.ICoreConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TestConfig implements ICoreConfig {
    public static final int CURRENT_VERSION = 3;

    public int configVersion = 0;

    public boolean flag = true;
    public ResourceLocation location;
    public List<String> values;

    public transient boolean normalized = false;

    public TestConfig() {
        location = new ResourceLocation("minecraft", "stone");
        values = new ArrayList<>(List.of("first", "second"));
    }

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

    @Override
    public void normalize() {
        normalized = true;

        if (values == null) {
            values = new TestConfig().values;
        }
    }
}
