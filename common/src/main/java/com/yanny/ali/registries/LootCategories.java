package com.yanny.ali.registries;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

public class LootCategories {
    public static final Map<ResourceLocation, LootCategory<Block>> BLOCK_LOOT_CATEGORIES = new HashMap<>();
    public static final Map<ResourceLocation, LootCategory<EntityType<?>>> ENTITY_LOOT_CATEGORIES = new HashMap<>();
    public static final Map<ResourceLocation, LootCategory<String>> GAMEPLAY_LOOT_CATEGORIES = new HashMap<>();
    public static final Map<ResourceLocation, LootCategory<String>> TRADE_LOOT_CATEGORIES = new HashMap<>();

    public static final LootCategory<Block> BLOCK_LOOT = getBlockCategory(Utils.modLoc("block_loot"), Items.DIAMOND_PICKAXE, false, Collections.singletonList(Block.class));
    public static final LootCategory<EntityType<?>> ENTITY_LOOT = getEntityCategory(Utils.modLoc("entity_loot"), Items.SKELETON_SKULL, false, Collections.singletonList(Entity.class));
    public static final LootCategory<String> TRADE_LOOT = getTradeCategory(Utils.modLoc("trade_loot"), Items.EMERALD_BLOCK, false, Collections.singletonList(Pattern.compile(".*")));
    public static final LootCategory<String> GAMEPLAY_LOOT = getGameplayCategory(Utils.modLoc("gameplay_loot"), Items.COMPASS, false, Collections.singletonList(Pattern.compile(".*")));

    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    private static LootCategory<Block> getBlockCategory(ResourceLocation key, Item icon, boolean hide, List<Class<?>> classes) {
        return new BlockLootCategory(key, new ItemStack(icon), hide, classes);
    }

    @NotNull
    private static LootCategory<EntityType<?>> getEntityCategory(ResourceLocation key, Item icon, boolean hide, List<Class<?>> classes) {
        return new EntityLootCategory(key, new ItemStack(icon), hide, classes);
    }

    @NotNull
    private static LootCategory<String> getTradeCategory(ResourceLocation key, Item icon, boolean hide, List<Pattern> validator) {
        return new TradeLootCategory(key, new ItemStack(icon), hide, validator);
    }

    @NotNull
    private static LootCategory<String> getGameplayCategory(ResourceLocation key, Item icon, boolean hide, List<Pattern> prefix) {
        return new GameplayLootCategory(key, new ItemStack(icon), hide, prefix);
    }

    @NotNull
    public static SimpleJsonResourceReloadListener getReloadListener(Gson gson, String id) {
        return new SimpleJsonResourceReloadListener(gson, id) {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
                BLOCK_LOOT_CATEGORIES.clear();
                ENTITY_LOOT_CATEGORIES.clear();
                GAMEPLAY_LOOT_CATEGORIES.clear();
                TRADE_LOOT_CATEGORIES.clear();

                map.forEach((location, value) -> {
                    try {
                        JsonObject jsonObject = GsonHelper.convertToJsonObject(value, location.toString());
                        LootCategory.Type type = LootCategory.Type.valueOf(GsonHelper.getAsString(jsonObject, "type"));
                        Item icon = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "icon")));
                        boolean hide = GsonHelper.getAsBoolean(jsonObject, "hide");

                        switch (type) {
                            case BLOCK -> {
                                //noinspection unchecked
                                List<Class<?>> classes = (List<Class<?>>) (Object) GsonHelper.getAsJsonArray(jsonObject, "classes").asList().stream().map(JsonElement::getAsString).map((String className) -> {
                                    try {
                                        return Class.forName(className);
                                    } catch (ClassNotFoundException e) {
                                        LOGGER.warn("Failed to resolve class {} for loot category {} with error {}", className, location, e.getException());
                                        return null;
                                    }
                                }).filter(Objects::nonNull).toList();
                                BLOCK_LOOT_CATEGORIES.put(location, getBlockCategory(location, icon, hide, classes));
                            }
                            case ENTITY -> {
                                //noinspection unchecked
                                List<Class<?>> classes = (List<Class<?>>) (Object) GsonHelper.getAsJsonArray(jsonObject, "classes").asList().stream().map(JsonElement::getAsString).map((String className) -> {
                                    try {
                                        return Class.forName(className);
                                    } catch (ClassNotFoundException e) {
                                        LOGGER.warn("Failed to resolve class {} for loot category {} with error {}", className, location, e.getException());
                                        return null;
                                    }
                                }).filter(Objects::nonNull).toList();
                                ENTITY_LOOT_CATEGORIES.put(location, getEntityCategory(location, icon, hide, classes));
                            }
                            case TRADE -> TRADE_LOOT_CATEGORIES.put(location, getTradeCategory(location, icon, hide,
                                    GsonHelper.getAsJsonArray(jsonObject, "pattern").asList().stream().map(JsonElement::getAsString).map(Pattern::compile).toList()));
                            case GAMEPLAY -> GAMEPLAY_LOOT_CATEGORIES.put(location, getGameplayCategory(location, icon, hide,
                                    GsonHelper.getAsJsonArray(jsonObject, "pattern").asList().stream().map(JsonElement::getAsString).map(Pattern::compile).toList()));
                        }

                        LOGGER.info("Loaded LootCategory resource: {} [Hide:{}]", location, hide);
                    } catch (Exception e) {
                        LOGGER.error("Failed to load LootCategory resource: {}", location, e);
                    }
                });
            }
        };
    }
}
