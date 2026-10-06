package com.yanny.aci.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yanny.aci.CommonLogUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CoreConfigUtils {
    @NotNull
    public static <T extends ICoreConfig> T readConfiguration(@Nullable Path configDir, String modId, String fileName,
                                                              Class<T> type, Supplier<T> factory, Gson gson) {
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
            saveConfig(modId, configFile, factory, gson);
        }

        T loadedConfig = load(modId, configFile, type, factory, gson);
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

                saveConfig(modId, configFile, factory, gson);
                return load(modId, configFile, type, factory, gson);
            } catch (Exception e) {
                logger.warn("Failed to rotate outdated config file!", e);
                return loadedConfig;
            }
        }

        addMissingKeys(modId, configFile, loadedConfig, gson);
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
        Logger logger = CommonLogUtils.getLogger(modId);

        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            logger.info("Loading configuration file {}", configFilePath);
            T config = gson.fromJson(reader, type);

            if (config == null) {
                return factory.get();
            }

            config.normalize();
            return config;
        } catch (Exception e) {
            logger.warn("Error while reading configuration file: {}", e.getMessage(), e);
            return factory.get();
        }
    }

    private static void addMissingKeys(String modId, Path configFilePath, ICoreConfig config, Gson gson) {
        Logger logger = CommonLogUtils.getLogger(modId);

        try {
            JsonElement file;

            try (Reader reader = Files.newBufferedReader(configFilePath)) {
                file = JsonParser.parseReader(reader);
            }

            if (!(file instanceof JsonObject fileObject) || !(gson.toJsonTree(config) instanceof JsonObject defaults)) {
                return;
            }

            List<String> added = new ArrayList<>();

            addMissingKeys(fileObject, defaults, "", added);

            if (!added.isEmpty()) {
                try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
                    gson.toJson(fileObject, writer);
                }

                logger.info("Added missing keys {} to configuration file {}", added, configFilePath);
            }
        } catch (Exception e) {
            logger.warn("Failed to add missing keys to configuration file: {}", e.getMessage(), e);
        }
    }

    private static void addMissingKeys(JsonObject target, JsonObject defaults, String path, List<String> added) {
        for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
            JsonElement existing = target.get(entry.getKey());

            if (existing == null) {
                target.add(entry.getKey(), entry.getValue().deepCopy());
                added.add(path + entry.getKey());
            } else if (existing instanceof JsonObject existingObject && entry.getValue() instanceof JsonObject defaultObject) {
                addMissingKeys(existingObject, defaultObject, path + entry.getKey() + ".", added);
            }
        }
    }

    private static <T extends ICoreConfig> void saveConfig(String modId, Path configFilePath, Supplier<T> factory, Gson gson) {
        Logger logger = CommonLogUtils.getLogger(modId);

        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            T config = factory.get();

            config.setConfigVersion(config.getCurrentVersion());
            gson.toJson(config, writer);
            logger.info("Created new configuration file {}", configFilePath);
        } catch (IOException e) {
            logger.warn("Error while writing configuration file: {}", e.getMessage(), e);
        }
    }
}
