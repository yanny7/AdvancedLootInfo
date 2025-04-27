package com.yanny.ali.registries;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class LootCategories {
    public static final Map<ResourceLocation, LootCategory<Block>> BLOCK_LOOT_CATEGORIES = new HashMap<>();
    public static final Map<ResourceLocation, LootCategory<Entity>> ENTITY_LOOT_CATEGORIES = new HashMap<>();
    public static final Map<ResourceLocation, LootCategory<String>> GAMEPLAY_LOOT_CATEGORIES = new HashMap<>();

    public static final LootCategory<Block> PLANT_LOOT = getBlockCategory("plant_loot", Items.DIAMOND_HOE, (block) -> block instanceof BushBlock);
    public static final LootCategory<Block> BLOCK_LOOT = getBlockCategory("block_loot", Items.DIAMOND_PICKAXE, (block) -> true);
    public static final LootCategory<Entity> ENTITY_LOOT = getEntityCategory("entity_loot", Items.SKELETON_SKULL, (entity) -> true);
    public static final LootCategory<String> GAMEPLAY_LOOT = getGameplayCategory((path) -> true);

    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    private static LootCategory<Block> getBlockCategory(String key, Item icon, Predicate<Block> validator) {
        return new LootCategory<>(key, new ItemStack(icon), LootCategory.Type.BLOCK, validator);
    }

    @NotNull
    private static LootCategory<Entity> getEntityCategory(String key, Item icon, Predicate<Entity> validator) {
        return new LootCategory<>(key, new ItemStack(icon), LootCategory.Type.ENTITY, validator);
    }

    @NotNull
    private static LootCategory<String> getGameplayCategory(Predicate<String> validator) {
        return new GameplayLootCategory("gameplay_loot", new ItemStack(Items.COMPASS), LootCategory.Type.GAMEPLAY, validator);
    }

    @NotNull
    private static LootCategory<String> getGameplayCategory(String key, Item icon, List<Pattern> prefix) {
        return new GameplayLootCategory(key, new ItemStack(icon), LootCategory.Type.GAMEPLAY, prefix);
    }

    @NotNull
    public static SimpleJsonResourceReloadListener<JsonElement> getReloadListener(Gson gson, String id) {
        return new SimpleJsonResourceReloadListener<>(ExtraCodecs.JSON, id) {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
                BLOCK_LOOT_CATEGORIES.clear();
                ENTITY_LOOT_CATEGORIES.clear();
                GAMEPLAY_LOOT_CATEGORIES.clear();

                map.forEach((location, value) -> {
                    try {
                        JsonObject jsonObject = GsonHelper.convertToJsonObject(value, location.toString());
                        LootCategory.Type type = LootCategory.Type.valueOf(GsonHelper.getAsString(jsonObject, "type"));
                        String key = GsonHelper.getAsString(jsonObject, "key");
                        Item icon = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "icon")));

                        switch (type) {
                            case BLOCK -> BLOCK_LOOT_CATEGORIES.put(location, getBlockCategory(key, icon, (block) -> true));
                            case ENTITY -> ENTITY_LOOT_CATEGORIES.put(location, getEntityCategory(key, icon, (entity) -> true));
                            case GAMEPLAY -> GAMEPLAY_LOOT_CATEGORIES.put(location, getGameplayCategory(key, icon,
                                    GsonHelper.getAsJsonArray(jsonObject, "prefix").asList().stream().map(JsonElement::getAsString).map(Pattern::compile).toList()));
                        }

                        LOGGER.info("Loaded LootCategory resource: {}", location);
                    } catch (Exception e) {
                        LOGGER.error("Failed to load LootCategory resource: {}", location, e);
                    }
                });
            }
        };
    }
}
