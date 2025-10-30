package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.compatibility.jei.*;
import com.yanny.ali.configuration.LootCategory;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.AliCommonRegistry;
import com.yanny.ali.manager.PluginManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<LootCategory<Block>, JeiBlockLoot> blockCategories = new HashMap<>();
    private final Map<LootCategory<EntityType<?>>, JeiEntityLoot> entityCategories = new HashMap<>();
    private final Map<LootCategory<ResourceLocation>, JeiGameplayLoot> gameplayCategories = new HashMap<>();
    private final Map<LootCategory<ResourceLocation>, JeiTradeLoot> tradeCategories = new HashMap<>();

    @Override
    public void onRuntimeUnavailable() {
        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        AliCommonRegistry commonRegistry = PluginManager.COMMON_REGISTRY;
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        blockCategories.clear();
        entityCategories.clear();
        gameplayCategories.clear();
        tradeCategories.clear();

        blockCategories.putAll(commonRegistry.getConfiguration().blockCategories.stream().collect(getCollector(guiHelper, JeiBlockLoot::new)));
        entityCategories.putAll(commonRegistry.getConfiguration().entityCategories.stream().collect(getCollector(guiHelper, JeiEntityLoot::new)));
        gameplayCategories.putAll(commonRegistry.getConfiguration().gameplayCategories.stream().collect(getCollector(guiHelper, JeiGameplayLoot::new)));
        tradeCategories.putAll(commonRegistry.getConfiguration().tradeCategories.stream().collect(getCollector(guiHelper, JeiTradeLoot::new)));

        blockCategories.values().forEach(registration::addRecipeCategories);
        entityCategories.values().forEach(registration::addRecipeCategories);
        gameplayCategories.values().forEach(registration::addRecipeCategories);
        tradeCategories.values().forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = PluginManager.CLIENT_REGISTRY.getCurrentDataFuture();

        if (futureData.isDone()) {
            LOGGER.info("Data already received, processing instantly.");
        } else {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>> pair = futureData.get();

            registerData(registration, pair.getLeft(), pair.getRight());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            if (cause instanceof TimeoutException) {
                futureData.cancel(true);
                PluginManager.CLIENT_REGISTRY.clearLootData();
                LOGGER.error("Failed to received data: Inactivity timeout occurred. Registration aborted!");
            } else {
                LOGGER.error("Failed to finish registering data with error {}", cause.getMessage());
                cause.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with unexpected error {}", e.getMessage());
        }
    }

    private void registerData(IRecipeRegistration registration, Map<ResourceLocation, IDataNode> lootData, Map<ResourceLocation, IDataNode> tradeData) {
        AliClientRegistry clientRegistry = PluginManager.CLIENT_REGISTRY;
        ClientLevel level = Minecraft.getInstance().level;

        LOGGER.info("Adding loot information to JEI");

        if (level != null) {
            Map<RecipeType<RecipeHolder<BlockLootType>>, List<BlockLootType>> blockRecipeTypes = new HashMap<>();
            Map<RecipeType<RecipeHolder<EntityLootType>>, List<EntityLootType>> entityRecipeTypes = new HashMap<>();
            Map<RecipeType<RecipeHolder<GameplayLootType>>, List<GameplayLootType>> gameplayRecipeTypes = new HashMap<>();
            Map<RecipeType<RecipeHolder<TradeLootType>>, List<TradeLootType>> tradeRecipeTypes = new HashMap<>();

            GenericUtils.processData(
                    level,
                    clientRegistry,
                    lootData,
                    tradeData,
                    (node, location, block, outputs) -> {
                        RecipeType<RecipeHolder<BlockLootType>> recipeType = null;

                        for (JeiBlockLoot recipeCategory : blockCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(block)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                        blockRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new BlockLootType(block, node, Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, entity, outputs) -> {
                        RecipeType<RecipeHolder<EntityLootType>> recipeType = null;

                        for (JeiEntityLoot recipeCategory : entityCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(entity)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            entityRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new EntityLootType(entity, location, node, Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, outputs) -> {
                        RecipeType<RecipeHolder<GameplayLootType>> recipeType = null;

                        for (JeiGameplayLoot recipeCategory : gameplayCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            gameplayRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new GameplayLootType(node, location.getPath(), Collections.emptyList(), outputs));
                        }
                    },
                    (node, location, inputs, outputs) -> {
                        RecipeType<RecipeHolder<TradeLootType>> recipeType = null;

                        for (JeiTradeLoot recipeCategory : tradeCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(node, location.getPath(), inputs, outputs));
                        }
                    },
                    (node, location, inputs, outputs) -> {
                        RecipeType<RecipeHolder<TradeLootType>> recipeType = null;

                        for (JeiTradeLoot recipeCategory : tradeCategories.values()) {
                            if (recipeCategory.getLootCategory().validate(location)) {
                                if (recipeCategory.getLootCategory().isHidden()) {
                                    return;
                                }

                                recipeType = recipeCategory.getRecipeType();
                                break;
                            }
                        }

                        if (recipeType != null) {
                            tradeRecipeTypes.computeIfAbsent(recipeType, (p) -> new LinkedList<>()).add(new TradeLootType(node, location.getPath(), inputs, outputs));
                        }
                    }
            );

            for (Map.Entry<RecipeType<RecipeHolder<BlockLootType>>, List<BlockLootType>> entry : blockRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }

            for (Map.Entry<RecipeType<RecipeHolder<EntityLootType>>, List<EntityLootType>> entry : entityRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }

            for (Map.Entry<RecipeType<RecipeHolder<GameplayLootType>>, List<GameplayLootType>> entry : gameplayRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }

            for (Map.Entry<RecipeType<RecipeHolder<TradeLootType>>, List<TradeLootType>> entry : tradeRecipeTypes.entrySet()) {
                registration.addRecipes(entry.getKey(), entry.getValue().stream().map(RecipeHolder::new).toList());
            }
        } else {
            LOGGER.warn("JEI integration was not loaded! Level is null!");
        }
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return Utils.modLoc("jei_plugin");
    }

    private static <T, U, V extends IType> T createCategory(IGuiHelper guiHelper, LootCategory<U> e, LootConstructor<T, U, V> constructor) {
        //noinspection unchecked
        RecipeType<RecipeHolder<V>> recipeType = (RecipeType<RecipeHolder<V>>) (Object) RecipeType.create(e.getKey().getNamespace(), e.getKey().getPath(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + e.getKey().getNamespace() + "." + e.getKey().getPath().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e, title, guiHelper.createDrawableItemStack(e.getIcon().getDefaultInstance()));
    }

    @NotNull
    private static  <T, U, V extends IType> Collector<LootCategory<U>, ?, Map<LootCategory<U>, T>> getCollector(IGuiHelper guiHelper, LootConstructor<T, U, V> supplier) {
        return Collectors.toMap(
                (e) -> e,
                (e) -> createCategory(guiHelper, e, supplier),
                (a, b) -> a,
                LinkedHashMap::new
        );
    }

    @FunctionalInterface
    private interface LootConstructor<T, U, V extends IType> {
        T construct(IGuiHelper guiHelper, RecipeType<RecipeHolder<V>> recipeType, LootCategory<U> lootCategory, Component title, IDrawable icon);
    }
}
