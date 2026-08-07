package com.yanny.awi.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.yanny.awi.Utils;
import com.yanny.awi.platform.Services;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @NotNull
    public static AwiConfig readConfiguration() {
        Path configDir = Services.getPlatform().getConfiguration();

        if (configDir == null) {
            LOGGER.warn("Failed to obtain config dir path!");
            return new AwiConfig();
        }

        Path modConfigDir = configDir.resolve(Utils.MOD_ID);
        Path configFile = modConfigDir.resolve(Utils.COMMON_CONFIG_NAME);

        if (!Files.exists(modConfigDir)) {
            try {
                Files.createDirectories(modConfigDir);
            } catch (IOException e) {
                LOGGER.warn("Failed to create path {} for configuration", modConfigDir);
                return new AwiConfig();
            }
        }

        File config = configFile.toFile();

        if (!config.exists()) {
            saveConfig(configFile);
        }

        AwiConfig loadedConfig = load(configFile);

        if (loadedConfig.configVersion < AwiConfig.CURRENT_VERSION) {
            LOGGER.info("Config version mismatch (found {}, expected {}). Re-creating...", loadedConfig.configVersion, AwiConfig.CURRENT_VERSION);

            try {
                File backupFile = new File(config.getAbsolutePath() + ".bak");

                if (backupFile.exists()) {
                    if (!backupFile.delete()) {
                        LOGGER.warn("Failed to delete backup file {}", backupFile);
                    }
                }

                if (!config.renameTo(backupFile)) {
                    LOGGER.warn("Failed to rename config file {} to {}", config, backupFile);
                }

                saveConfig(configFile);
                return load(configFile);
            } catch (Exception e) {
                LOGGER.warn("Failed to rotate outdated config file!", e);
            }
        }

        return loadedConfig;
    }

    @NotNull
    private static AwiConfig load(Path configFilePath) {
        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            LOGGER.info("Loading configuration file {}", configFilePath);

            JsonElement json = JsonParser.parseReader(reader);

            return AwiConfig.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow((s) -> new RuntimeException("Config error: " + s));
        } catch (Exception e) {
            LOGGER.warn("Error while reading configuration file: {}", e.getMessage(), e);
            return new AwiConfig();
        }
    }

    private static void saveConfig(Path configFilePath) {
        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            LOGGER.info("Creating new configuration file {}", configFilePath);
            AwiConfig config = new AwiConfig();

            config.configVersion = AwiConfig.CURRENT_VERSION;

            JsonElement json = AwiConfig.CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow((s) -> new RuntimeException("Config save error: " + s));

            GSON.toJson(json, writer);
        } catch (IOException e) {
            LOGGER.warn("Error while writing configuration file: {}", e.getMessage(), e);
        }
    }
}
