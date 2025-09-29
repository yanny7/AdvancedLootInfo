package com.yanny.ali.compatibility;

import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.compatibility.common.*;
import com.yanny.ali.compatibility.jei.*;
import com.yanny.ali.manager.AliClientRegistry;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.registries.LootCategories;
import com.yanny.ali.registries.LootCategory;
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
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@JeiPlugin
public class JeiCompatibility implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<JeiBlockLoot> blockCategoryList = new LinkedList<>();
    private final List<JeiEntityLoot> entityCategoryList = new LinkedList<>();
    private final List<JeiGameplayLoot> gameplayCategoryList = new LinkedList<>();
    private final List<JeiTradeLoot> tradeCategoryList = new LinkedList<>();

    private final CompletableFuture<Pair<Map<ResourceLocation, IDataNode>, Map<ResourceLocation, IDataNode>>> futureData = new CompletableFuture<>();

    @Override
    public void onRuntimeUnavailable() {
        blockCategoryList.clear();
        entityCategoryList.clear();
        gameplayCategoryList.clear();
        tradeCategoryList.clear();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        blockCategoryList.add(createCategory(guiHelper, LootCategories.PLANT_LOOT, JeiBlockLoot::new));
        blockCategoryList.addAll(LootCategories.BLOCK_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiBlockLoot::new))
                .collect(Collectors.toSet()));
        blockCategoryList.add(createCategory(guiHelper, LootCategories.BLOCK_LOOT, JeiBlockLoot::new));

        entityCategoryList.addAll(LootCategories.ENTITY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiEntityLoot::new))
                .collect(Collectors.toSet()));
        entityCategoryList.add(createCategory(guiHelper, LootCategories.ENTITY_LOOT, JeiEntityLoot::new));

        gameplayCategoryList.addAll(LootCategories.GAMEPLAY_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiGameplayLoot::new))
                .collect(Collectors.toSet()));
        gameplayCategoryList.add(createCategory(guiHelper, LootCategories.GAMEPLAY_LOOT, JeiGameplayLoot::new));

        tradeCategoryList.addAll(LootCategories.TRADE_LOOT_CATEGORIES.entrySet().stream()
                .map((e) -> createCategory(guiHelper, e, JeiTradeLoot::new))
                .collect(Collectors.toSet()));
        tradeCategoryList.add(createCategory(guiHelper, LootCategories.TRADE_LOOT, JeiTradeLoot::new));

        blockCategoryList.forEach(registration::addRecipeCategories);
        entityCategoryList.forEach(registration::addRecipeCategories);
        gameplayCategoryList.forEach(registration::addRecipeCategories);
        tradeCategoryList.forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        PluginManager.CLIENT_REGISTRY.setOnDoneListener((lootData, tradeData) -> futureData.complete(Pair.of(lootData, tradeData)));

        futureData.thenAccept((pair) -> {
            try {
                registerData(registration, pair.getLeft(), pair.getRight());
            } catch (Throwable e) {
                e.printStackTrace();
                LOGGER.error("Failed to register data with error {}", e.getMessage());
            }
        });

        if (!futureData.isDone()) {
            LOGGER.info("Blocking this thread until all data are received!");
        }

        try {
            futureData.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            LOGGER.error("Failed to finish registering data with error {}", e.getMessage());
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

                        for (JeiBlockLoot recipeCategory : blockCategoryList) {
                            if (recipeCategory.getLootCategory().validate(block)) {
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

                        for (JeiEntityLoot recipeCategory : entityCategoryList) {
                            if (recipeCategory.getLootCategory().validate(entity)) {
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

                        for (JeiGameplayLoot recipeCategory : gameplayCategoryList) {
                            if (recipeCategory.getLootCategory().validate(location.getPath())) {
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

                        for (JeiTradeLoot recipeCategory : tradeCategoryList) {
                            if (recipeCategory.getLootCategory().validate(location.getPath())) {
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

                        for (JeiTradeLoot recipeCategory : tradeCategoryList) {
                            if (recipeCategory.getLootCategory().validate(location.getPath())) {
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
        RecipeType<RecipeHolder<V>> recipeType = (RecipeType<RecipeHolder<V>>) (Object) RecipeType.create(Utils.MOD_ID, e.getKey(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + Utils.MOD_ID + "." + e.getKey().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e, title, guiHelper.createDrawableItemStack(e.getIcon()));
    }

    private static <T, U, V extends IType> T createCategory(IGuiHelper guiHelper, Map.Entry<ResourceLocation, LootCategory<U>> e, LootConstructor<T, U, V> constructor) {
        ResourceLocation id = e.getKey();
        //noinspection unchecked
        RecipeType<RecipeHolder<V>> recipeType = (RecipeType<RecipeHolder<V>>) (Object) RecipeType.create(id.getNamespace(), id.getPath(), RecipeHolder.class);
        Component title = Component.translatable("emi.category." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        return constructor.construct(guiHelper, recipeType, e.getValue(), title, guiHelper.createDrawableItemStack(e.getValue().getIcon()));
    }

    @FunctionalInterface
    private interface LootConstructor<T, U, V extends IType> {
        T construct(IGuiHelper guiHelper, RecipeType<RecipeHolder<V>> recipeType, LootCategory<U> lootCategory, Component title, IDrawable icon);
    }
}
