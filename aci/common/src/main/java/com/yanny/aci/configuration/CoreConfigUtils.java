package com.yanny.aci.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class CoreConfigUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    public static <T extends ICoreConfig> T readConfiguration(@Nullable Path configDir, String modId, String fileName,
                                                              Class<T> type, Supplier<T> factory, Gson gson) {
        if (configDir == null) {
            LOGGER.warn("[{}] Failed to obtain config dir path!", modId);
            return factory.get();
        }

        Path modConfigDir = configDir.resolve(modId);
        Path configFile = modConfigDir.resolve(fileName);

        if (!Files.exists(modConfigDir)) {
            try {
                Files.createDirectories(modConfigDir);
            } catch (IOException e) {
                LOGGER.warn("[{}] Failed to create path {} for configuration", modId, modConfigDir);
                return factory.get();
            }
        }

        File config = configFile.toFile();

        if (!config.exists()) {
            saveConfig(modId, configFile, factory, gson);
        }

        T loadedConfig = load(modId, configFile, type, factory, gson);
        int currentVersion = loadedConfig.getCurrentVersion();

        if (loadedConfig.getConfigVersion() < currentVersion) {
            LOGGER.info("[{}] Config version mismatch (found {}, expected {}). Re-creating...", modId, loadedConfig.getConfigVersion(), currentVersion);

            try {
                File backupFile = new File(config.getAbsolutePath() + ".bak");

                if (backupFile.exists()) {
                    if (!backupFile.delete()) {
                        LOGGER.warn("[{}] Failed to delete backup file {}", modId, backupFile);
                    }
                }

                if (!config.renameTo(backupFile)) {
                    LOGGER.warn("[{}] Failed to rename config file {} to {}", modId, config, backupFile);
                }

                saveConfig(modId, configFile, factory, gson);
                return load(modId, configFile, type, factory, gson);
            } catch (Exception e) {
                LOGGER.warn("[{}] Failed to rotate outdated config file!", modId, e);
            }
        }

        return loadedConfig;
    }

    @NotNull
    public static GsonBuilder gsonBuilder() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer());
    }

    @NotNull
    private static <T extends ICoreConfig> T load(String modId, Path configFilePath, Class<T> type, Supplier<T> factory, Gson gson) {
        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            LOGGER.info("[{}] Loading configuration file {}", modId, configFilePath);
            T config = gson.fromJson(reader, type);

            if (config == null) {
                return factory.get();
            }

            config.normalize();
            return config;
        } catch (Exception e) {
            LOGGER.warn("[{}] Error while reading configuration file: {}", modId, e.getMessage(), e);
            return factory.get();
        }
    }

    private static <T extends ICoreConfig> void saveConfig(String modId, Path configFilePath, Supplier<T> factory, Gson gson) {
        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            T config = factory.get();

            config.setConfigVersion(config.getCurrentVersion());
            gson.toJson(config, writer);
            LOGGER.info("[{}] Created new configuration file {}", modId, configFilePath);
        } catch (IOException e) {
            LOGGER.warn("[{}] Error while writing configuration file: {}", modId, e.getMessage(), e);
        }
    }
}
