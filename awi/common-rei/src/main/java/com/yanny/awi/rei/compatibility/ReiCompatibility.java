package com.yanny.awi.rei.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.manager.AwiClientRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import com.yanny.awi.rei.compatibility.rei.RecipeHolder;
import com.yanny.awi.rei.compatibility.rei.ReiBaseCategory;
import com.yanny.awi.rei.compatibility.rei.ReiBiomeCategory;
import com.yanny.awi.rei.compatibility.rei.ReiBiomeDisplay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ReiCompatibility implements REIClientPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<Holder, List<RecipeHolder>> dimensions = new LinkedHashMap<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        dimensions.clear();

        GenericUtils.register(registry, this::registerData);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registerFiller(registry, dimensions, ReiCompatibility::biomePredicate);
    }

    private void registerData(CategoryRegistry registry, byte[] fullCompressedData) {
        AwiClientRegistry clientRegistry = PluginManager.getInstance().clientRegistry;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to REI");

        if (level != null) {
            Map<ResourceLocation, LevelStemNode> worldgenData = GenericUtils.decompressWorldgenData(clientRegistry, fullCompressedData);

            worldgenData.forEach((key, levelNode) -> {
                WorldCategory category = new WorldCategory(key);
                Holder holder = createCategory(category, ReiBiomeDisplay::new, ReiBiomeCategory::new);
                List<RecipeHolder> recipes = new ArrayList<>();

                for (IDataNode biomeNode : levelNode.nodes()) {
                    List<Block> blocks = GenericUtils.collectBlocks(biomeNode);
                    recipes.add(new RecipeHolder(biomeNode, ((BiomeNode) biomeNode).getBiomeId(), blocks));
                }

                dimensions.put(holder, recipes);
                registry.add(holder.category);
            });
        } else {
            LOGGER.warn("REI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    private static Holder createCategory(WorldCategory lootCategory,
                                         BiFunction<RecipeHolder, CategoryIdentifier<ReiBiomeDisplay>, ReiBiomeDisplay> displayFactory,
                                         BiFunction<CategoryIdentifier<ReiBiomeDisplay>, Component, ReiBaseCategory<ReiBiomeDisplay>> categoryFactory) {
        CategoryIdentifier<ReiBiomeDisplay> identifier = CategoryIdentifier.of(lootCategory.id);
        Component title = GenericUtils.getFormattedCategoryTitle(lootCategory.id);
        Function<RecipeHolder, ReiBiomeDisplay> filler = (type) -> displayFactory.apply(type, identifier);
        return new Holder(identifier, categoryFactory.apply(identifier, title), filler);
    }

    @NotNull
    private static Predicate<Object> biomePredicate(List<RecipeHolder> lootTypes) {
        return (o) -> {
            if (o != null) {
                if (o instanceof RecipeHolder type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    private static void registerFiller(DisplayRegistry registry, Map<Holder, List<RecipeHolder>> categories, Function<List<RecipeHolder>, Predicate<Object>> predicate) {
        for (Map.Entry<Holder, List<RecipeHolder>> entry : categories.entrySet()) {
            registry.registerFiller(predicate.apply(entry.getValue()), entry.getKey().filler());
            entry.getValue().forEach(registry::add);
        }
    }

    private record Holder(CategoryIdentifier<ReiBiomeDisplay> identifier, ReiBaseCategory<ReiBiomeDisplay> category, Function<RecipeHolder, ReiBiomeDisplay> filler) {}

    private record WorldCategory(ResourceLocation id) {}
}