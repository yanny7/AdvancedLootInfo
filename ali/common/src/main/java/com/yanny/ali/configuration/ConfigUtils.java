package com.yanny.ali.configuration;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.yanny.aci.configuration.CoreConfigUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.platform.Services;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ConfigUtils {
    @NotNull
    public static AliConfig readConfiguration() {
        HolderLookup.Provider lookup = HolderLookup.Provider.create((Stream<HolderLookup.RegistryLookup<?>>)(Object) BuiltInRegistries.REGISTRY.stream());
        DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);

        return CoreConfigUtils.readConfiguration(Services.getPlatform().getConfiguration(), Utils.MOD_ID, Utils.COMMON_CONFIG_NAME,
                AliConfig.CODEC, ops, AliConfig::new);
    }
}
