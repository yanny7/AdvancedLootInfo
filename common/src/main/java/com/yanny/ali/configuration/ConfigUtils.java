package com.yanny.ali.configuration;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        Gson gson = createGson();

        if (!config.exists()) {
            saveConfig(configFile, gson);
        }

        return load(configFile, gson);
    }

    private static AliConfig load(Path configFilePath, Gson gson) {
        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            LOGGER.info("Loading configuration file {}", configFilePath);
            return gson.fromJson(reader, AliConfig.class);
        } catch (Exception e) {
            LOGGER.warn("Error while reading configuration file: {}", e.getMessage(), e);
            return new AliConfig();
        }
    }

    private static void saveConfig(Path configFilePath, Gson gson) {
        try (FileWriter writer = new FileWriter(configFilePath.toFile())) {
            gson.toJson(new AliConfig(), writer);
            LOGGER.info("Created new configuration file {}", configFilePath);
        } catch (IOException e) {
            LOGGER.warn("Error while writing configuration file: {}", e.getMessage(), e);
        }
    }

    private static class LootCategoryAdapter implements JsonSerializer<LootCategory<?>>, JsonDeserializer<LootCategory<?>> {
        @NotNull
        @Override
        public LootCategory<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            LootCategory.Type type = LootCategory.Type.valueOf(GsonHelper.getAsString(jsonObject, "type"));

            return switch (type) {
                case BLOCK -> new BlockLootCategory(jsonObject);
                case ENTITY -> new EntityLootCategory(jsonObject);
                case TRADE -> new TradeLootCategory(jsonObject);
                case GAMEPLAY -> new GameplayLootCategory(jsonObject);
            };
        }

        @NotNull
        @Override
        public JsonElement serialize(LootCategory<?> lootCategory, Type typeOfT, JsonSerializationContext context) {
            return lootCategory.toJson();
        }
    }

    private static class ResourceLocationAdapter implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
        @NotNull
        @Override
        public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return ResourceLocation.parse(json.getAsString());
        }

        @NotNull
        @Override
        public JsonElement serialize(ResourceLocation location, Type typeOfT, JsonSerializationContext context) {
            return new JsonPrimitive(location.toString());
        }
    }

    @NotNull
    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter())
                .registerTypeAdapter(LootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(BlockLootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(EntityLootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(GameplayLootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(TradeLootCategory.class, new LootCategoryAdapter())
                .create();
    }
}
