package com.yanny.aci.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.yanny.aci.CommonLogUtils;
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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @NotNull
    public static <T extends ICoreConfig> T readConfiguration(@Nullable Path configDir, String modId, String fileName,
                                                              Codec<T> codec, DynamicOps<JsonElement> ops, Supplier<T> factory) {
        Logger logger = CommonLogUtils.getLogger(modId);

        if (configDir == null) {
            logger.warn("Failed to obtain config dir path!");
            return factory.get();
        }

        Path modConfigDir = configDir.resolve(modId);
        Path configFile = modConfigDir.resolve(fileName);

        if (!Files.exists(modConfigDir)) {
            try {
                Files.createDirectories(modConfigDir);
            } catch (IOException e) {
                logger.warn("Failed to create path {} for configuration", modConfigDir);
                return factory.get();
            }
        }

        File config = configFile.toFile();

        if (!config.exists()) {
            saveConfig(modId, configFile, codec, ops, factory);
        }

        T loadedConfig = load(modId, configFile, codec, ops, factory);
        int currentVersion = loadedConfig.getCurrentVersion();

        if (loadedConfig.getConfigVersion() < currentVersion) {
            logger.info("Config version mismatch (found {}, expected {}). Re-creating...", loadedConfig.getConfigVersion(), currentVersion);

            try {
                File backupFile = new File(config.getAbsolutePath() + ".bak");

                if (backupFile.exists()) {
                    if (!backupFile.delete()) {
                        logger.warn("Failed to delete backup file {}", backupFile);
                    }
                }

                if (!config.renameTo(backupFile)) {
                    logger.warn("Failed to rename config file {} to {}", config, backupFile);
                }

                saveConfig(modId, configFile, codec, ops, factory);
                return load(modId, configFile, codec, ops, factory);
            } catch (Exception e) {
                logger.warn("Failed to rotate outdated config file!", e);
            }
        }

        return loadedConfig;
    }

    @NotNull
    private static <T extends ICoreConfig> T load(String modId, Path configFilePath, Codec<T> codec, DynamicOps<JsonElement> ops, Supplier<T> factory) {
        Logger logger = CommonLogUtils.getLogger(modId);

        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            logger.info("Loading configuration file {}", configFilePath);

            JsonElement json = JsonParser.parseReader(reader);

            return codec.parse(ops, json).getOrThrow((s) -> new RuntimeException("Config error: " + s));
        } catch (Exception e) {
            logger.warn("Error while reading configuration file: {}", e.getMessage(), e);
            return factory.get();
        }
    }

    private static <T extends ICoreConfig> void saveConfig(String modId, Path configFilePath, Codec<T> codec, DynamicOps<JsonElement> ops, Supplier<T> factory) {
        Logger logger = CommonLogUtils.getLogger(modId);

        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            T config = factory.get();

            config.setConfigVersion(config.getCurrentVersion());

            JsonElement json = codec.encodeStart(ops, config).getOrThrow((s) -> new RuntimeException("Config save error: " + s));

            GSON.toJson(json, writer);
            logger.info("Created new configuration file {}", configFilePath);
        } catch (Exception e) {
            logger.warn("Error while writing configuration file: {}", e.getMessage(), e);
        }
    }
}
