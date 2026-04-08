package com.yanny.ali.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.Utils;
import com.yanny.ali.platform.Services;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ConfigUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @NotNull
    public static AliConfig readConfiguration() {
        Path configDir = Services.getPlatform().getConfiguration();

        if (configDir == null) {
            LOGGER.warn("Failed to obtain config dir path!");
            return new AliConfig();
        }

        Path modConfigDir = configDir.resolve(Utils.MOD_ID);
        Path configFile = modConfigDir.resolve(Utils.COMMON_CONFIG_NAME);

        if (!modConfigDir.toFile().exists()) {
            if (!modConfigDir.toFile().mkdirs()) {
                LOGGER.warn("Failed to create path {} for configuration", modConfigDir);
                return new AliConfig();
            }
        }

        File config = configFile.toFile();

        if (!config.exists()) {
            saveConfig(configFile);
        }

        return load(configFile);
    }

    private static AliConfig load(Path configFilePath) {
        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            LOGGER.info("Loading configuration file {}", configFilePath);

            JsonElement json = JsonParser.parseReader(reader);
            HolderLookup.Provider lookup = HolderLookup.Provider.create((Stream<HolderLookup.RegistryLookup<?>>)(Object) BuiltInRegistries.REGISTRY.stream());
            DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);

            return AliConfig.CODEC.parse(ops, json).getOrThrow((s) -> new RuntimeException("Config error: " + s));
        } catch (Exception e) {
            LOGGER.warn("Error while reading configuration file: {}", e.getMessage(), e);
            return new AliConfig();
        }
    }

    private static void saveConfig(Path configFilePath) {
        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            LOGGER.info("Creating new configuration file {}", configFilePath);

            HolderLookup.Provider lookup = HolderLookup.Provider.create((Stream<HolderLookup.RegistryLookup<?>>)(Object) BuiltInRegistries.REGISTRY.stream());
            DynamicOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
            JsonElement json = AliConfig.CODEC.encodeStart(ops, new AliConfig()).getOrThrow((s) -> new RuntimeException("Config save error: " + s));

            GSON.toJson(json, writer);
        } catch (IOException e) {
            LOGGER.warn("Error while writing configuration file: {}", e.getMessage(), e);
        }
    }
}
