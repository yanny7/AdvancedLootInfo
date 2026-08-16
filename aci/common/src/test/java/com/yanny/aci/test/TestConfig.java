package com.yanny.aci.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.aci.configuration.ICoreConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TestConfig implements ICoreConfig {
    public static final int CURRENT_VERSION = 3;

    public static final Codec<TestConfig> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                Codec.INT.fieldOf("configVersion").orElse(0).forGetter((c) -> c.configVersion),
                Codec.BOOL.fieldOf("flag").orElse(true).forGetter((c) -> c.flag),
                ResourceLocation.CODEC.fieldOf("location").orElseGet(() -> new TestConfig().location).forGetter((c) -> c.location),
                Codec.STRING.listOf().fieldOf("values").orElseGet(() -> new TestConfig().values).forGetter((c) -> c.values)
        ).apply(instance, (version, flag, location, values) -> {
            TestConfig config = new TestConfig();

            config.configVersion = version;
            config.flag = flag;
            config.location = location;
            config.values = new ArrayList<>(values);
            return config;
        })
    );

    public int configVersion = 0;

    public boolean flag = true;
    public ResourceLocation location;
    public List<String> values;

    public TestConfig() {
        location = ResourceLocation.fromNamespaceAndPath("minecraft", "stone");
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
}
