package com.yanny.awi.rei.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.compatibility.GenericUtils;
import com.yanny.awi.manager.AwiClientRegistry;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import com.yanny.awi.rei.compatibility.rei.*;
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

    private final Map<Holder<ReiGameplayDisplay, RecipeHolder>, List<RecipeHolder>> gameplayCategories = new LinkedHashMap<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        gameplayCategories.clear();

        GenericUtils.register(registry, this::registerData);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registerFiller(registry, gameplayCategories, ReiCompatibility::gameplayPredicate);
    }

    private void registerData(CategoryRegistry registry, byte[] fullCompressedData) {
        AwiClientRegistry clientRegistry = PluginManager.getInstance().clientRegistry;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to REI");

        if (level != null) {
            Map<ResourceLocation, LevelStemNode> worldgenData = GenericUtils.decompressWorldgenData(clientRegistry, fullCompressedData);

            worldgenData.forEach((key, levelNode) -> {
                WorldCategory category = new WorldCategory(key);
                Holder<ReiGameplayDisplay, RecipeHolder> holder = createCategory(category, ReiGameplayDisplay::new, ReiGameplayCategory::new);
                List<RecipeHolder> recipes = new ArrayList<>();

                for (IDataNode biomeNode : levelNode.nodes()) {
                    List<Block> blocks = GenericUtils.collectBlocks(biomeNode);
                    recipes.add(new RecipeHolder(biomeNode, ((BiomeNode) biomeNode).getBiomeId(), blocks));
                }

                gameplayCategories.put(holder, recipes);
                registry.add(holder.category);
            });
        } else {
            LOGGER.warn("REI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    private static <D extends ReiBaseDisplay, T> Holder<D, T> createCategory(WorldCategory lootCategory,
                                                                             BiFunction<T, CategoryIdentifier<D>, D> displayFactory,
                                                                             BiFunction<CategoryIdentifier<D>, Component, ReiBaseCategory<D>> categoryFactory) {
        CategoryIdentifier<D> identifier = CategoryIdentifier.of(lootCategory.id);
        Component title = Component.translatable("emi.category." + lootCategory.id.getNamespace() + "." + lootCategory.id.getPath().replace('/', '.'));
        Function<T, D> filler = (type) -> displayFactory.apply(type, identifier);
        return new Holder<>(identifier, categoryFactory.apply(identifier, title), filler);
    }

    @NotNull
    private static Predicate<Object> gameplayPredicate(List<RecipeHolder> lootTypes) {
        return (o) -> {
            if (o != null) {
                if (o instanceof RecipeHolder type) {
                    return lootTypes.contains(type);
                }
            }

            return false;
        };
    }

    private static <D extends ReiBaseDisplay, T> void registerFiller(DisplayRegistry registry, Map<Holder<D, T>, List<T>> categories, Function<List<T>, Predicate<Object>> predicate) {
        for (Map.Entry<Holder<D, T>, List<T>> entry : categories.entrySet()) {
            registry.registerFiller(predicate.apply(entry.getValue()), entry.getKey().filler());
            entry.getValue().forEach(registry::add);
        }
    }

    private record Holder<D extends ReiBaseDisplay, T>(CategoryIdentifier<D> identifier, ReiBaseCategory<D> category, Function<T, D> filler) {}

    private record WorldCategory(ResourceLocation id) {}
}