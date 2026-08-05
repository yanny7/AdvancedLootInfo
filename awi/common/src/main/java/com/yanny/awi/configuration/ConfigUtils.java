package com.yanny.awi.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.yanny.awi.Utils;
import com.yanny.awi.platform.Services;
import net.minecraft.resources.ResourceLocation;
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
        Gson gson = createGson();

        if (!config.exists()) {
            saveConfig(configFile, gson);
        }

        AwiConfig loadedConfig = load(configFile, gson);

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

                saveConfig(configFile, gson);
                return load(configFile, gson);
            } catch (Exception e) {
                LOGGER.warn("Failed to rotate outdated config file!", e);
            }
        }

        return loadedConfig;
    }

    @NotNull
    private static AwiConfig load(Path configFilePath, Gson gson) {
        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            LOGGER.info("Loading configuration file {}", configFilePath);
            return gson.fromJson(reader, AwiConfig.class);
        } catch (Exception e) {
            LOGGER.warn("Error while reading configuration file: {}", e.getMessage(), e);
            return new AwiConfig();
        }
    }

    private static void saveConfig(Path configFilePath, Gson gson) {
        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            AwiConfig config = new AwiConfig();

            config.configVersion = AwiConfig.CURRENT_VERSION;
            gson.toJson(config, writer);
            LOGGER.info("Created new configuration file {}", configFilePath);
        } catch (IOException e) {
            LOGGER.warn("Error while writing configuration file: {}", e.getMessage(), e);
        }
    }

    @NotNull
    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
                .create();
    }
}
