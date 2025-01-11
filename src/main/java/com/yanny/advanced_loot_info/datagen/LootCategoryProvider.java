package com.yanny.advanced_loot_info.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yanny.advanced_loot_info.AdvancedLootInfoMod;
import com.yanny.advanced_loot_info.registries.GameplayLootCategory;
import com.yanny.advanced_loot_info.registries.LootCategory;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LootCategoryProvider implements DataProvider {
    private final PackOutput generator;
    private final Set<LootCategory<?>> categories = new HashSet<>();

    public LootCategoryProvider(PackOutput generator) {
        this.generator = generator;
    }

    public void generate() {
        addGameplayCategory("chest_loot", Items.CHEST, "chest");
        addGameplayCategory("fishing_loot", Items.FISHING_ROD, "fishing");
        addGameplayCategory("archaeology_loot", Items.DECORATED_POT, "archaeology");
        addGameplayCategory("hero_loot", Items.EMERALD, "gameplay/hero_of_the_village");
    }

    protected void addGameplayCategory(String key, Item icon, String prefix) {
        categories.add(new GameplayLootCategory(key, new ItemStack(icon), LootCategory.Type.GAMEPLAY, prefix));
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        generate();
        JsonObject exampleResource = new JsonObject();
        exampleResource.addProperty("value", "Hello, Custom Resource!");
        exampleResource.addProperty("number", 123);

        return CompletableFuture.allOf(
                categories.stream()
                        .map((category) -> {
                            Path output = generator.getOutputFolder().resolve("data/" + AdvancedLootInfoMod.MOD_ID + "/loot_categories/" + category.getKey() + ".json");
                            return DataProvider.saveStable(cachedOutput, toJson(category), output);
                        })
                        .toArray(CompletableFuture[]::new)
        );
    }

    @NotNull
    @Override
    public String getName() {
        return "loot_categories";
    }

    @NotNull
    private static JsonElement toJson(LootCategory<?> category) {
        JsonObject object = new JsonObject();

        object.addProperty("type", category.getType().name());
        object.addProperty("icon", ForgeRegistries.ITEMS.getKey(category.getIcon().getItem()).toString());
        object.addProperty("key", category.getKey());

        if (Objects.requireNonNull(category.getType()) == LootCategory.Type.GAMEPLAY) {
            if (category instanceof GameplayLootCategory lootCategory && lootCategory.getPrefix() != null) {
                object.addProperty("prefix", lootCategory.getPrefix());
            }
        }

        return object;
    }
}
