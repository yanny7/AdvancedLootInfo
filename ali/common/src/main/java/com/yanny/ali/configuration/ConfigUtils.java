package com.yanny.ali.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.yanny.aci.configuration.CoreConfigUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.platform.Services;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class ConfigUtils {
    @NotNull
    public static AliConfig readConfiguration() {
        return CoreConfigUtils.readConfiguration(Services.getPlatform().getConfiguration(), Utils.MOD_ID, Utils.COMMON_CONFIG_NAME,
                AliConfig.class, AliConfig::new, createGson());
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

    @NotNull
    private static Gson createGson() {
        return CoreConfigUtils.gsonBuilder()
                .registerTypeAdapter(LootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(BlockLootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(EntityLootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(GameplayLootCategory.class, new LootCategoryAdapter())
                .registerTypeAdapter(TradeLootCategory.class, new LootCategoryAdapter())
                .create();
    }
}
