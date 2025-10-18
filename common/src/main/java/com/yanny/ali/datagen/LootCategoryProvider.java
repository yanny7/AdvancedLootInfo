package com.yanny.ali.datagen;

import com.yanny.ali.Utils;
import com.yanny.ali.registries.BlockLootCategory;
import com.yanny.ali.registries.GameplayLootCategory;
import com.yanny.ali.registries.LootCategory;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BushBlock;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
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
        addBlockCategory("plant_loot", Items.DIAMOND_HOE, List.of(BushBlock.class));

        addGameplayCategory("chest_loot", Items.CHEST, List.of(Pattern.compile("^chests/.*$")));
        addGameplayCategory("fishing_loot", Items.FISHING_ROD, List.of(Pattern.compile("^gameplay/fishing.*$")));
        addGameplayCategory("archaeology_loot", Items.DECORATED_POT, List.of(Pattern.compile("^archaeology/.*$")));
        addGameplayCategory("hero_loot", Items.EMERALD, List.of(Pattern.compile("^gameplay/hero_of_the_village/.*$")));
    }

    protected void addGameplayCategory(String key, Item icon, List<Pattern> patterns) {
        categories.add(new GameplayLootCategory(Utils.modLoc(key), new ItemStack(icon), patterns));
    }

    protected void addBlockCategory(String key, Item icon, List<Class<?>> classes) {
        categories.add(new BlockLootCategory(Utils.modLoc(key), new ItemStack(icon), classes));
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        generate();

        return CompletableFuture.allOf(
                categories.stream()
                        .map((category) -> {
                            Path output = generator.getOutputFolder().resolve(String.format("assets/%s/loot_categories/%s.json", category.getKey().getNamespace(), category.getKey().getPath()));
                            return DataProvider.saveStable(cachedOutput, category.toJson(), output);
                        })
                        .toArray(CompletableFuture[]::new)
        );
    }

    @NotNull
    @Override
    public String getName() {
        return "loot_categories";
    }
}
