package com.yanny.ali.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yanny.ali.Utils;
import com.yanny.ali.registries.GameplayLootCategory;
import com.yanny.ali.registries.LootCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class LootCategoryProvider implements DataProvider {
    private final PackOutput generator;
    private final Set<LootCategory<?>> categories = new HashSet<>();

    public LootCategoryProvider(PackOutput generator) {
        this.generator = generator;
    }

    public void generate() {
        addGameplayCategory("chest_loot", Items.CHEST, List.of(
                Pattern.compile("^chests/[a-z_]*$"),
                Pattern.compile("^chests/village/[a-z_]*$")
        ));
        addGameplayCategory("trial_chambers", Items.SPAWNER, List.of(
                Pattern.compile("^chests/trial_chambers/.*$"),
                Pattern.compile("^pots/trial_chambers/.*$"),
                Pattern.compile("^dispensers/trial_chambers/.*$"),
                Pattern.compile("^spawners/ominous/trial_chamber/.*$"),
                Pattern.compile("^spawners/trial_chamber/.*$"),
                Pattern.compile("^equipment/.*$")
        ));
        addGameplayCategory("fishing_loot", Items.FISHING_ROD, List.of(Pattern.compile("^gameplay/fishing.*$")));
        addGameplayCategory("archaeology_loot", Items.DECORATED_POT, List.of(Pattern.compile("^archaeology/.*$")));
        addGameplayCategory("hero_loot", Items.EMERALD, List.of(Pattern.compile("^gameplay/hero_of_the_village/.*$")));
    }

    protected void addGameplayCategory(String key, Item icon, List<Pattern> prefix) {
        categories.add(new GameplayLootCategory(key, new ItemStack(icon), LootCategory.Type.GAMEPLAY, prefix));
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        generate();

        return CompletableFuture.allOf(
                categories.stream()
                        .map((category) -> {
                            Path output = generator.getOutputFolder().resolve("data/" + Utils.MOD_ID + "/loot_categories/" + category.getKey() + ".json");
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
        object.addProperty("icon", BuiltInRegistries.ITEM.getKey(category.getIcon().getItem()).toString());
        object.addProperty("key", category.getKey());

        if (Objects.requireNonNull(category.getType()) == LootCategory.Type.GAMEPLAY) {
            if (category instanceof GameplayLootCategory lootCategory && lootCategory.getPrefix() != null) {
                JsonArray array = new JsonArray();

                lootCategory.getPrefix().forEach((p) -> array.add(p.pattern()));
                object.add("prefix", array);
            }
        }

        return object;
    }
}
